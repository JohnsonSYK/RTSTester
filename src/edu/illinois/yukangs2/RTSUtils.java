package edu.illinois.yukangs2;

import java.io.*;
import java.util.*;

public class RTSUtils {
    public static void main(String[] args) throws IOException {
        List<BuildInfo> allBuilds = readCSV("filtered_info.csv");

    }

    static List<BuildInfo> readCSV(String fileName) throws FileNotFoundException {
        List<BuildInfo> allBuilds = new ArrayList<>();

        Scanner scanner = new Scanner(new File(fileName));
        Scanner dataScanner;
        int index = 0;
        scanner.nextLine();
        while (scanner.hasNextLine()){
            dataScanner = new Scanner(scanner.nextLine());
            dataScanner.useDelimiter(",");
            String repoName = null, eventType = null, preSHA = null, curSHA = null;
            long buildID = 0;
            int num_m = 0, num_t = 0, num_f = 0;
            boolean add = false;
            while (dataScanner.hasNext()){
                String data = dataScanner.next();
                if (index == 0)
                    repoName = data;
                else if (index == 1)
                    buildID = Long.parseLong(data);
                else if (index == 2)
                    eventType = data;
                else if (index == 3)
                    preSHA = data;
                else if (index == 4) {
                    curSHA = data;
                }
                else if (index == 5) {
                    num_m = Integer.parseInt(data);
                }
                else if (index == 6) {
                    num_t = Integer.parseInt(data);
                }
                else if (index == 7) {
                    num_f = Integer.parseInt(data);
                    add = true;
                }
                else{
                    System.out.println("invalid data: " + data);
                    System.exit(0);
                }
                index ++;
            }
            index = 0;
            if (add)
                allBuilds.add(new BuildInfo(repoName, buildID, eventType, preSHA, curSHA, num_m, num_t, num_f));
            else{
                System.out.println("invalid build format!");
                System.exit(0);
            }
        }

        scanner.close();

        return allBuilds;
    }

    static void filter_data(List<BuildInfo> allBuilds) throws IOException {
        HashMap<String, BuildInfo> selected = new HashMap<>();

        for (BuildInfo b : allBuilds){
            if (!selected.containsKey(b.repoName)){
                selected.put(b.repoName, b);
            }
            else if (b.buildID > selected.get(b.repoName).buildID && b.num_tests != 0 && b.num_modified != 0 && b.num_failed != 0) {
                selected.put(b.repoName, b);
            }
        }

        File outFile = new File("filtered_info.csv");
        for (String name : selected.keySet()) {
            if (!outFile.exists())
                outFile.createNewFile();
            PrintWriter out = new PrintWriter(new FileWriter(outFile, true));
            out.append(selected.get(name).repoName).append(',')
                    .append(String.valueOf(selected.get(name).buildID)).append(",")
                    .append(selected.get(name).eventType).append(',')
                    .append(selected.get(name).preSHA).append(',')
                    .append(selected.get(name).curSHA).append(',')
                    .append(String.valueOf(selected.get(name).num_modified)).append(',')
                    .append(String.valueOf(selected.get(name).num_tests)).append(',')
                    .append(String.valueOf(selected.get(name).num_failed))
                    .append("\n");
            out.close();
        }
    }

    static class BuildInfo {
        final String repoName, preSHA, curSHA, eventType;
        final long buildID;
        final int num_modified, num_tests, num_failed;
        BuildInfo(String repoName, long buildID, String eventType, String preSHA, String curSHA, int num_modified, int num_tests, int num_failed){
            this.buildID = buildID;
            this.repoName = repoName;
            this.eventType = eventType;
            this.preSHA = preSHA;
            this.curSHA = curSHA;
            this.num_modified = num_modified;
            this.num_tests = num_tests;
            this.num_failed = num_failed;
        }
    }

           /*
        List<BuildInfo> allBuilds = readCSV("info.csv");

        HashSet<String> bannedBuild = new HashSet<>();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("out.csv", true)));
        int count = 0;

        for (BuildInfo build : allBuilds){
            if (count ++ < 1190)
                continue;
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

        /*
        HashSet<String> bannedBuild = new HashSet<>(Arrays.asList(
                "undera/jmeter-plugins",
                "exteso/alf.io",
                "OpenGrok/OpenGrok",
                "springside/springside4",
                "jamesagnew/hapi-fhir",
                "cloudfoundry/uaa",
                "jprante/elasticsearch-jdbc",
                "torakiki/pdfsam",
                "rackerlabs/blueflood",
                "Findwise/Hydra",
                "Graylog2/graylog2-server"
        ));
        */

    /* Looking:
     * springside/springside4
     * Findwise/Hydra
     * SoftInstigate/restheart @
     * SonarSource/sonarqube
     */


        /*
        List<String> hashes = new ArrayList<>();
        hashes.add("2d62a3e9da726942a93cf16b6e91c0187e6c0136"); // latest
        hashes.add("405c3fe77795bb426f97bf9f63aa7573ce6f64fb"); // 6.0
        hashes.add("4ddfba9a04c9f6ff381288b73ed1c05a1e5b7638"); // 5.0
        hashes.add("717665ff188292f13ce4e7d216fe258e80b0e05c"); // 4.0
        hashes.add("777832d46a9639df85648aebcbfa2d970cc67742"); // 3.0
        hashes.add("568196a8d1e993eb33c0d784acc83f93fdea71e2"); // 2.0
        hashes.add("64c8ebfda9d49265730f30f56eec346ed14bf74d"); // 1.0
        */
        /*
        if (args.length != 2){
            System.out.println("Usage: RTSTester.java <num of evaluation> <repo url>");
            System.exit(0);
        }
         */
}
