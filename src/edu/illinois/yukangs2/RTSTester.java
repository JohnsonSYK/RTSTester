package edu.illinois.yukangs2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static edu.illinois.yukangs2.RTSUtils.*;

public class RTSTester{
    private final static int evaluationNum = 15;
    private final static String repoURL = "https://github.com/JodaOrg/joda-time.git";
    private final static String domain = "https://github.com/";
    //https://github.com/kevinsawicki/http-request.git
    //https://github.com/JodaOrg/joda-time.git

    /*
         <plugin>
        <groupId>org.ekstazi</groupId>
        <artifactId>ekstazi-maven-plugin</artifactId>
        <version>5.2.0</version>
        <executions>
          <execution>
            <id>ekstazi</id>
            <goals>
              <goal>select</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

     */

    /*
    public static void RTSTester() throws IOException, InterruptedException, XmlPullParserException {

        //Set up repository
        initialize_Repo(repoURL);

        //Get hashes for past evaluations
        List<String> hashes = getHashes(evaluationNum);

        //Add RTS dependency
        patch_RTS(true);
        for (int i = hashes.size()-1; i >= 0; i--) {
            System.out.println("Testing commit " + i + " :" +hashes.get(i));

            // Make modification to the code
            update_Repo(hashes.get(i));

            // Run test and collect dependencies
            run_Test();
        }
    }
    */

    public static void main(String[] args) throws IOException, InterruptedException, XmlPullParserException, TimeoutException {
/*
        String name = "temp";
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./temp";

        runComm("git clone " + repoURL + " " + dir);
        patchAndTest(dir, true);
        patchEvosuite(dir);
        testAndLogResult(dir);
*/
/*
        initialize_Repo(repoURL);
        List<String> hashes = getHashes(evaluationNum);
        patch_RTS(true);

        for (int i = hashes.size()-1; i >= 0; i--) {
            System.out.println("Testing commit " + i + " :" +hashes.get(i));
            update_Repo(hashes.get(i));
            run_Test();
        }

        generate_Test(true);
        run_Test();
*/
        List<BuildInfo> allBuilds = readCSV("filtered_info.csv");

        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));

        for (BuildInfo build : allBuilds){

            Timer timer = new Timer(true);
            /*
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
            */
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
/*
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
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            }
            theTread.interrupt();
        }

    }

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
    //------------------
    private static TestResult run_Test() throws IOException, XmlPullParserException, InterruptedException {
        String dir = "./temp";
        File parent = new File("temp");
        if (isMultiModule(dir))
            addParent(dir);
        patchEkstazi(dir);
        TestResult res = testAndLogResult("./temp");
        runComm("git checkout .", parent);
        return res;
    }

    private static void patch_RTS(boolean isEkstazi) throws IOException, XmlPullParserException, InterruptedException {

    }

    private static void generate_Test(boolean isEvosuite) throws IOException, XmlPullParserException, InterruptedException {
        patchEvosuite("./temp");
    }

    private static void update_Repo(String input) throws IOException, InterruptedException {
        File dir = new File("temp");
        runComm("git checkout " + input, dir);
    }

    private static void initialize_Repo(String url) throws IOException, InterruptedException {
        runComm("rm -rf ./temp");
        runComm("git clone " + url + " ./temp");
    }
    //------------------
    */

    private static void runRTS() throws IOException, InterruptedException, XmlPullParserException {
        /*
            1. create temp dir, move into temp *
            2. clone repo *
            3. move into main dir -
            4. Add Ekstazi to pom.xml *
            5. run RTS tool *
            6. save result *
            7. clear temp dir *
        */

        String name = "temp";
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./"+name;
        String subDir = "./"+name+"/lib";

        runComm("git clone " + repoURL + " " + dir);


        List hashes = getHashes(evaluationNum, dir);
        double sumTests = 0;
        double maxDuration = -1;
        double totalTime = 0;

        for (int i = hashes.size()-1; i >= 0; i--){
            //runComm("git --git-dir=./temp/.git checkout " + hashes.get(i));
            runComm("git checkout " + hashes.get(i), parent);

            //long startTime = System.nanoTime();
            TestResult res = patchAndTest(dir, true); // true: Eksiazi, false: STARTS
            sumTests += res.testRun;
            totalTime += res.duration;

            //long endTime   = System.nanoTime();
            //Double duration = (double)(endTime - startTime)/1000000000;

            //totalTime += duration;
            if (res.duration > maxDuration)
                maxDuration = res.duration;

            System.out.println("Testing commit " + i + " :" +hashes.get(i) + ", time:" + res.duration);

            runComm("git checkout .", parent);
        }

        System.out.println("Total Tests run: " + sumTests +", Average Tests over "+ evaluationNum +" commits: " + sumTests/evaluationNum);
        System.out.println("Total Time: " + totalTime +"s, Max single duration: " + maxDuration +"s");
        runComm("rm -rf " + name);

    }

    private static List<String> getHashes(int num, String dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("git", "--git-dir="+dir+"/.git", "log", "-" + num, "--format=\"%H\"");
        Process p = pb.start();
        //p.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        List<String> hashes = new ArrayList<>();
        while((line = br.readLine()) != null)
            hashes.add(line.substring(1, 41));
        br.close();

        return hashes;
    }

    private static TestResult patchAndTest(String dir, boolean isEkstazi) throws IOException, XmlPullParserException, InterruptedException {
        if (isMultiModule(dir))
            addParent(dir);
        if (isEkstazi)
            patchEkstazi(dir);
        else
            patchSTARTS(dir);

        return testAndLogResult(dir);
    }

    private static void runComm(String com) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(com);
        pr.waitFor();
    }

    private static void runComm(String com, File dir) throws InterruptedException, IOException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(com, null, dir);
        pr.waitFor();
    }

    private static File createDir(String name){
        File dir = new File(name);
        if (!dir.mkdir()) {
            System.out.print("Temp directory already exist!");
            System.exit(0);
        }
        return dir;
    }

    private static File createDir(File parent, String name){
        File dir = new File(parent, name);
        if (!dir.mkdir()) {
            System.out.print("Temp directory already exist!");
            System.exit(0);
        }
        return dir;
    }

    //https://stackoverflow.com/questions/2811536/how-to-edit-a-maven-pom-at-runtime
    private static void patchEkstazi(String dir) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(new File(dir, "/pom.xml")));

        PluginExecution execution = new PluginExecution();
        execution.addGoal("select");
        execution.setId("ekstazi");
        List<PluginExecution> executionList = new ArrayList<>();
        executionList.add(execution);

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.ekstazi");
        plugin.setArtifactId("ekstazi-maven-plugin");
        plugin.setVersion("5.2.0");
        plugin.setExecutions(executionList);

        if (model.getBuild() == null)
            model.setBuild(new Build());
        model.getBuild().addPlugin(plugin);

        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(new File(dir, "/pom.xml")), model);
    }

    private static void patchSTARTS(String dir) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(new File(dir, "/pom.xml")));

        Plugin plugin = new Plugin();
        plugin.setGroupId("edu.illinois");
        plugin.setArtifactId("starts-maven-plugin");
        plugin.setVersion("1.3");

        if (model.getBuild() == null)
            model.setBuild(new Build());
        model.getBuild().addPlugin(plugin);

        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(new File(dir, "/pom.xml")), model);
    }

    private static void patchEvosuite(String dir) throws IOException, XmlPullParserException, InterruptedException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(new File(dir, "/pom.xml")));

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.evosuite.plugins");
        plugin.setArtifactId("evosuite-maven-plugin");
        plugin.setVersion("1.0.6");

        Plugin plugin2 = new Plugin();
        plugin2.setGroupId("org.apache.maven.plugins");
        plugin2.setArtifactId("maven-compiler-plugin");
        plugin2.setVersion("3.8.0");

        Xpp3Dom configDom = Xpp3DomBuilder.build(new StringReader("<configuration><source>1.8</source><target>1.8</target></configuration>"));
        plugin2.setConfiguration(configDom);

        if (model.getBuild() == null)
            model.setBuild(new Build());
        model.getBuild().addPlugin(plugin);
        model.getBuild().addPlugin(plugin2);

        Dependency dependency = new Dependency();
        dependency.setGroupId("org.evosuite");
        dependency.setArtifactId("evosuite-standalone-runtime");
        dependency.setVersion("1.0.6");
        dependency.setScope("test");
        model.addDependency(dependency);
        /*
        Repository rep = new Repository();
        rep.setId("EvoSuite");
        rep.setName("EvoSuite Repository");
        rep.setUrl("http://www.evosuite.org/m2");
        model.addRepository(rep);
        */

        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(new File(dir, "/pom.xml")), model);

        Model model2 = reader.read(new FileInputStream(new File(dir+"/lib", "/pom.xml")));
        model2.getBuild().addPlugin(plugin2);
        writer.write(new FileOutputStream(new File(dir+"/lib", "/pom.xml")), model2);

        File parent = new File(dir);
        runComm("mvn evosuite:generate", parent);
        runComm("mvn evosuite:export", parent);
    }

    private static void addParent(String dir) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(new File(dir, "/pom.xml")));
        String parentGID = model.getGroupId();
        if (parentGID == null && model.getParent() != null)
            parentGID = model.getParent().getGroupId();
        String parentAID = model.getArtifactId();
        if (parentAID == null && model.getParent() != null)
            parentAID = model.getParent().getArtifactId();
        String parentVer = model.getVersion();
        if (parentVer == null && model.getParent() != null)
            parentVer = model.getParent().getVersion();

        List<String> allSubModule = model.getModules();
        for (String sub : allSubModule){
            if (isMultiModule(dir+"/"+sub))
                addParent(dir+"/"+sub);
            Model subModel = reader.read(new FileInputStream(new File(dir+"/"+sub, "/pom.xml")));
            if (subModel.getParent() == null)
                subModel.setParent(new Parent());
            subModel.getParent().setGroupId(parentGID);
            subModel.getParent().setArtifactId(parentAID);
            subModel.getParent().setVersion(parentVer);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileOutputStream(new File(dir+"/"+sub, "/pom.xml")), subModel);
        }
    }

    private static TestResult testAndLogResult(String dir) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        ProcessBuilder pb = new ProcessBuilder("mvn", "-f", dir + "/pom.xml", "test");
        Process p = pb.start();
        //p.waitFor();
        int testRun = 0;

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        boolean isResult = false;
        File resultFile = new File("result.txt");
        File logFile = new File("log.txt");

        while((line = br.readLine()) != null){
            PrintWriter logData = new PrintWriter(new FileWriter(logFile, true));
            logData.append(line).append("\n");
            logData.close();
            if (line.contains("Results"))
                isResult = true;
            if (isResult && line.length() > 45 && line.contains("Tests run: ")){
                if (!resultFile.exists())
                    resultFile.createNewFile();
                PrintWriter resultData = new PrintWriter(new FileWriter(resultFile, true));
                System.out.println(line);
                testRun += getTestRun(line);
                resultData.append(line).append("\n");
                resultData.close();
                isResult = false;
                //break;
            }
        }
        br.close();

        long endTime   = System.nanoTime();
        double duration = (double)(endTime - startTime)/1000000000;
        return new TestResult(testRun, duration);
    }

    private static int getTestRun(String line) {
        int num = 0;
        for (int i = 0; i < line.length(); i++){
            char c = line.charAt(i);
            if (Character.isDigit(c)){
                num *= 10;
                num += (c - '0');
            }
            if (c == ',')
                break;
        }
        return num;
    }

    private static boolean isMultiModule(String dir) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(new File(dir, "/pom.xml")));

        return model.getModules() != null;
    }

    static class TestResult{
        int testRun;
        double duration;
        TestResult(int testRun, double duration){
            this.testRun = testRun;
            this.duration = duration;
        }
    }
}
