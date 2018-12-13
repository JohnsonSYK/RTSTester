package edu.illinois.yukangs2;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.List;

import static edu.illinois.yukangs2.RTSUtils.*;
import static edu.illinois.yukangs2.RTSTester.*;

public class Demo {
    private final static int evaluationNum = 15;
    private final static String repoURL = "https://github.com/JodaOrg/joda-time.git";
    public static void main(String[] args) throws InterruptedException, XmlPullParserException, IOException {
        RTSTester();
    }

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

    public static void testGeneration() throws IOException, InterruptedException, XmlPullParserException {
        String name = "temp";
        runComm("rm -rf " + name);
        File parent = createDir(name);
        String dir = "./temp";

        runComm("git clone " + repoURL + " " + dir);
        patchAndTest(dir, true);
        patchEvosuite(dir);
        testAndLogResult(dir);
    }

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
        String dir = "./temp";
        patchEkstazi(dir);
    }

    private static List<String> getHashes(int num) throws IOException {
        String dir = "./temp";
        return RTSTester.getHashes(num, dir);
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
}
