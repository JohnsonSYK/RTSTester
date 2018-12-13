package edu.illinois.yukangs2;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static edu.illinois.yukangs2.RTSUtils.*;
import static edu.illinois.yukangs2.RTSTester.*;

public class GroupTester {

    public static void main(String[] args) throws IOException, XmlPullParserException {
        timeoutTesterI();
    }

    public static void timeoutTesterI() throws IOException, XmlPullParserException {
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
                // TODO Auto-generated catch block
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
            //-------------------------------
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

            //---------------------------
        }
        pw.close();
     */

}
