package edu.illinois.yukangs2;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class RTSTester{
    private static int evaluationNum;
    private static String repoURL;
    //https://github.com/kevinsawicki/http-request.git
    //https://github.com/JodaOrg/joda-time.git

    public static void main(String[] args) throws IOException, InterruptedException, XmlPullParserException {
        if (args.length != 2){
            System.out.println("Usage: RTSTester.java <num of evaluation> <repo url>");
            System.exit(0);
        }

        evaluationNum = Integer.parseInt(args[0]);
        repoURL = args[1];

        runRTS();

    }

    /**
     * Default test flow for RTS Tester
     * @throws IOException, InterruptedException, XmlPullParserException
     */
    private static void runRTS() throws IOException, InterruptedException, XmlPullParserException {
        /*
            1. create temp dir, move into temp
            2. clone repo
            3. move into main dir
            4. Add Ekstazi to pom.xml
            5. run RTS tool
            6. save result
            7. clear temp dir
        */

        String name = "temp";
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./"+name;
        String subDir = "./"+name+"/lib";

        runComm("git clone " + repoURL + " " + dir);

        /*
        List<String> testingHashes = new ArrayList<>();
        hashes.add("2d62a3e9da726942a93cf16b6e91c0187e6c0136"); // latest
        hashes.add("405c3fe77795bb426f97bf9f63aa7573ce6f64fb"); // 6.0
        hashes.add("4ddfba9a04c9f6ff381288b73ed1c05a1e5b7638"); // 5.0
        hashes.add("717665ff188292f13ce4e7d216fe258e80b0e05c"); // 4.0
        hashes.add("777832d46a9639df85648aebcbfa2d970cc67742"); // 3.0
        hashes.add("568196a8d1e993eb33c0d784acc83f93fdea71e2"); // 2.0
        hashes.add("64c8ebfda9d49265730f30f56eec346ed14bf74d"); // 1.0
        */
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

    /**
     * Parse the list of past SHAs/hashes for the repo
     * @param num: Number of evaluation tested
     * @param dir: main directory of maven configuration
     * @return List of String
     * @throws IOException .
     */
    static List<String> getHashes(int num, String dir) throws IOException {
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


    /**
     * Group functions together for patching and testing
     * @param dir: main directory of maven configuration
     * @param isEkstazi: True use Ekstazi, False use STARTS
     * @return TestResult Object for storing test number, and runtime
     * @throws IOException, XmlPullParserException, InterruptedException
     */
    static TestResult patchAndTest(String dir, boolean isEkstazi) throws IOException, XmlPullParserException, InterruptedException {
        if (isMultiModule(dir))
            addParent(dir);
        if (isEkstazi)
            patchEkstazi(dir);
        else
            patchSTARTS(dir);

        return testAndLogResult(dir);
    }

    /**
     * Run bash script commands in current directory
     * @param com: String of bash command
     * @throws IOException, InterruptedException
     */
    static void runComm(String com) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(com);
        pr.waitFor();
    }

    /**
     * Run bash script commands in specified directory
     * @param com: String of bash command
     * @throws IOException, InterruptedException
     */
    static void runComm(String com, File dir) throws InterruptedException, IOException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(com, null, dir);
        pr.waitFor();
    }

    /**
     * Create empty directory/folder in current directory with name
     * @param name: folder name
     * @return File of the new folder
     */
    static File createDir(String name){
        File dir = new File(name);
        if (!dir.mkdir()) {
            System.out.print("Temp directory already exist!");
            System.exit(0);
        }
        return dir;
    }

    /**
     * Create empty directory/folder in specified directory with name
     * @param name: folder name
     * @return File of the new folder
     */
    private static File createDir(File parent, String name){
        File dir = new File(parent, name);
        if (!dir.mkdir()) {
            System.out.print("Temp directory already exist!");
            System.exit(0);
        }
        return dir;
    }

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
    /**
     * Patch Ekstazi dependencies in configuration file(pom.xml)
     * @param dir: main directory of maven configuration file
     * @throws IOException, XmlPullParserException
     */
    static void patchEkstazi(String dir) throws IOException, XmlPullParserException {
        //Ref: https://stackoverflow.com/questions/2811536/how-to-edit-a-maven-pom-at-runtime
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

    /**
     * Patch STARTS dependencies in configuration file(pom.xml)
     * @param dir: main directory of maven configuration file
     * @throws IOException, XmlPullParserException
     */
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

    /**
     * Patch Evosuite dependencies in configuration file(pom.xml)
     * @param dir: main directory of maven configuration file
     * @throws IOException, XmlPullParserException
     */
    static void patchEvosuite(String dir) throws IOException, XmlPullParserException, InterruptedException {
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

        Repository rep = new Repository();
        rep.setId("EvoSuite");
        rep.setName("EvoSuite Repository");
        rep.setUrl("http://www.evosuite.org/m2");
        model.addRepository(rep);

        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(new File(dir, "/pom.xml")), model);

        /*
        Model model2 = reader.read(new FileInputStream(new File(dir+"/lib", "/pom.xml")));
        model2.getBuild().addPlugin(plugin2);
        writer.write(new FileOutputStream(new File(dir+"/lib", "/pom.xml")), model2);

        File parent = new File(dir);
        runComm("mvn evosuite:generate", parent);
        runComm("mvn evosuite:export", parent);
        */
    }

    /**
     * Add dependencies to pom.xmls in multi-module project to ensure run order of modules.
     * @param dir: main directory of maven configuration file
     * @throws IOException, XmlPullParserException
     */
    static void addParent(String dir) throws IOException, XmlPullParserException {
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

    /**
     * Run test for the project and catch output, parse tests information.
     * @param dir: main directory of maven configuration file
     * @return TestResult Object to storing tests run and time used
     * @throws IOException, InterruptedException
     */
    static TestResult testAndLogResult(String dir) throws IOException, InterruptedException {
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

    /**
     * Parse test output for number of 'Tests run'
     * @param line: String contains the information
     * @return number of tests
     */
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

    /**
     * Test if the project is multi-module maven project.
     * @param dir: the path of module want to tested
     * @return boolean if the project is multi-module
     * @throws IOException, XmlPullParserException
     */
    static boolean isMultiModule(String dir) throws IOException, XmlPullParserException {
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
