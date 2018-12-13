package edu.illinois.yukangs2;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static edu.illinois.yukangs2.RTSUtils.*;
import static edu.illinois.yukangs2.RTSTester.*;

/**
 * For side project: to test hundreds of projects automatically
 */
public class GroupTester {
    private final static String domain = "https://github.com/";

    public static void main(String[] args) throws IOException, XmlPullParserException {
        timeoutTesterI();
    }

    /**
     * Test group of projects with timeout when project does not run properly
     * @throws IOException .
     * @throws XmlPullParserException .
     */
    private static void timeoutTesterI() throws IOException, XmlPullParserException {
        List<RTSUtils.BuildInfo> allBuilds = readCSV("filtered_info.csv");

        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));

        for (RTSUtils.BuildInfo build : allBuilds){

            Timer timer = new Timer(true);
            InterruptTimerTaskAddDel interruptTimerTask = new InterruptTimerTaskAddDel(
                    Thread.currentThread(),180000);
            timer.schedule(interruptTimerTask, 0);
            try {
                runEval(build, pw);
            } catch (InterruptedException e) {
                pw.println(build.repoName+","+build.preSHA+","+build.curSHA+",NA,NA");
                pw.flush();
            } finally {
                timer.cancel();
            }
        }
        pw.close();
    }

    /**
     * Second Implementation: Test group of projects with timeout when project does not run properly
     * @throws IOException .
     * @throws XmlPullParserException .
     */
    public static void timeoutTesterII() throws IOException, XmlPullParserException {
        List<RTSUtils.BuildInfo> allBuilds = readCSV("filtered_info.csv");

        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));

        for (RTSUtils.BuildInfo build : allBuilds){

            Timer timer = new Timer(true);

            InterruptTimerTask interruptTimerTask =
                    new InterruptTimerTask(Thread.currentThread());
            timer.schedule(interruptTimerTask, 180000); //3 min
            try {
                runEval(build, pw);
            } catch (InterruptedException e) {
                pw.println(build.repoName+","+build.preSHA+","+build.curSHA+",NA,NA");
                pw.flush();
            } finally {
                timer.cancel();
            }
        }
        pw.close();
    }

    /**
     * Threads interrupter for timeout operation
     */
    protected static class InterruptTimerTask extends TimerTask {

        private Thread theTread;

        public InterruptTimerTask(Thread theTread) {
            this.theTread = theTread;
        }

        @Override
        public void run() {
            theTread.interrupt();
        }

    }

    /**
     * Second Implementation: Threads interrupter for timeout operation
     */
    static class InterruptTimerTaskAddDel extends TimerTask {

        private Thread theTread;
        private long timeout;

        public InterruptTimerTaskAddDel(Thread theTread,long i_timeout) {
            this.theTread = theTread;
            timeout=i_timeout;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().sleep(timeout);
            } catch (InterruptedException e) {
                // TODO Handle exception
                e.printStackTrace(System.err);
            }
            theTread.interrupt();
        }

    }

        /*
        List<BuildInfo> allBuilds = readCSV("info.csv");

        HashSet<String> bannedBuild = new HashSet<>();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));
        int count = 0;

        for (BuildInfo build : allBuilds){
            if (bannedBuild.contains(build.repoName)) {
                pw.println("NA,NA");
                pw.flush();
                continue;
            }

            Timer timer = new Timer(true);
            InterruptTimerTask interruptTimerTask =
                    new InterruptTimerTask(Thread.currentThread());
            timer.schedule(interruptTimerTask, 180000); //3 min
            try {
                runEval(build, pw);
            } catch (InterruptedException e) {
                bannedBuild.add(build.repoName);
            } finally {
                timer.cancel();
            }

        }
        pw.close();
     */

    /**
     * Test pair of SHAs/Hashes, store test result, and store ./ekstazi and log files
     * @param build: BuildInfo Object with the project want to run
     * @param pw: for output result
     * @throws IOException
     * @throws InterruptedException
     * @throws XmlPullParserException
     */
    private static void runEval(BuildInfo build, PrintWriter pw) throws IOException, InterruptedException, XmlPullParserException {
        System.out.println("On repo: " + build.repoName + ", time: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        //String name = "temp";
        String name = build.repoName.split("/")[1];
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./"+name;
        runComm("git clone " + domain+build.repoName+".git" + " " + dir);
        runComm("git checkout " + build.preSHA, parent);
        TestResult pre = patchAndTest(dir, true);
        runComm("git checkout .", parent);
        runComm("git checkout " + build.curSHA, parent);
        TestResult cur = patchAndTest(dir, true);
        System.out.println("Testing Build " + build.buildID + " and select " +cur.testRun +" tests out of " + pre.testRun + " tests.");
        pw.println(build.repoName+","+build.preSHA+","+build.curSHA+","+pre.testRun+","+cur.testRun);
        pw.flush();
        //Moving files
        try {
            FileUtils.copyDirectory(new File("./"+name+"/.ekstazi"), new File("./saved/"+name+"/.ekstazi"));
            FileUtils.copyFile(new File("./log.txt"), new File("./saved/"+name+"/log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        runComm("rm -rf " + name);
        runComm("rm log.txt");
    }
        /*
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));

        System.out.println("On repo: SonarSource/sonarqube");
        String name = "temp";
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./temp";
        runComm("git clone " + domain+"SonarSource/sonarqube.git" + " " + dir);
        runComm("git checkout " + "10d02b48362808b6f109d41f5a29380778f919b8", parent);
        TestResult pre = patchAndTest(dir, true);
        runComm("git checkout .", parent);
        runComm("git checkout " + "ff3a016e7896ead2a5387c88c152032097b7b475", parent);
        TestResult res = patchAndTest(dir, true);
        System.out.println("Testing Build " + 73345909 + " and select " +res.testRun +" tests out of " + pre.testRun + " tests.");
        pw.println(pre.testRun+","+res.testRun);
        //runComm("rm -rf " + name);
        pw.flush();
        */

}
