package edu.illinois.yukangs2;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class RTSUtils {
    public static void main(String[] args) throws IOException {
        List<String[]> hash = parseIn("./final/in.csv");


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

    private static Set<String> parse164Out(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        Set<String> res = new HashSet<>();
        while (scanner.hasNextLine()){
            String s = scanner.nextLine();
            String[] parts = s.split(",");
            res.add(parts[2]);
        }
        scanner.close();
        return res;
    }

    private static List<String[]> parseIn(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        List<String[]> res = new ArrayList<>();
        while (scanner.hasNextLine()){
            String s = scanner.nextLine();
            String[] parts = s.split(",");
            res.add(new String[]{parts[1], parts[2]});
        }
        scanner.close();
        return res;
    }

    private static void removeQuotationMark(String path) throws FileNotFoundException {
        File file = new File(path);  // create File object to read from
        Scanner scanner = new Scanner(file);       // create scanner to read
        PrintWriter writer = new PrintWriter("out RemoveQuotationMark.csv"); // create file to write to

        while(scanner.hasNextLine()){  // while there is a next line
            String line = scanner.nextLine();  // line = that next line

            // do something with that line
            StringBuilder newLine = new StringBuilder();

            // replace a character
            for (int i = 0; i < line.length(); i++){
                if (line.charAt(i) != '\"') {  // or anything other character you chose
                    newLine.append(line.charAt(i));
                }
            }

            // print to another file.
            writer.println(newLine.toString());
            writer.flush();

        }
        writer.close();
    }

    private static void downloadZip(String hash, String name){
        URL url;
        URLConnection con;
        DataInputStream dis;
        FileOutputStream fos;
        byte[] fileData;
        try {
            url = new URL("http://168.61.190.96:8080/external/prevfiledumps/"+hash+"/dump.zip"); //File Location goes here
            con = url.openConnection(); // open the url connection.
            dis = new DataInputStream(con.getInputStream());
            fileData = new byte[con.getContentLength()];
            for (int q = 0; q < fileData.length; q++) {
                fileData[q] = dis.readByte();
            }
            dis.close(); // close the data input stream
            fos = new FileOutputStream(new File("./final/"+name+".zip")); //FILE Save Location goes here
            fos.write(fileData);  // write out the file we want to save.
            fos.close(); // close the output stream writer
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    private static void mergeOut() throws IOException {
        List<BuildInfo> allBuilds = readCSV("verboseInfo.csv");
        System.out.println(allBuilds.size());
        Set<String> seen = parse164Out("out.csv");
        System.out.println(seen.size());

        Map<String, String> nameFromHash = new HashMap<>();
        for (BuildInfo b : allBuilds){
            nameFromHash.put(b.curSHA, b.repoName);
        }

        PrintWriter printTo = new PrintWriter(new BufferedWriter(new FileWriter("out merge.csv", true)));
        Scanner infoFrom = new Scanner(new File("./Achieved Files/out 12.6.csv"));

        while (infoFrom.hasNextLine()){
            String s = infoFrom.nextLine();
            String[] parts = s.split(",");
            if (seen.contains(parts[1])) {
                System.out.println("seen: " + parts[1]);
                continue;
            }
            printTo.append(nameFromHash.get(parts[1])).append(",").append(s).append(",old").append("\n");
            printTo.flush();
        }
        printTo.close();
        infoFrom.close();
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
