package edu.illinois.yukangs2;

import java.io.*;
import java.util.*;

public class RTSUtils {
    public static void main(String[] args) throws IOException {
        String inputFile = "filtered_info.csv";
        List<BuildInfo> allBuilds = readCSV(inputFile);

    }

    /**
     * read .csv file to get build information
     * @param fileName path to input .csv file
     * @return List of BuildInfo
     * @throws FileNotFoundException .
     */
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

    /**
     * Filter input data to get most recent SHA from List of BuildInfo read from .csv
     * @param allBuilds: List of BuildInfo
     * @throws IOException
     */
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

}
