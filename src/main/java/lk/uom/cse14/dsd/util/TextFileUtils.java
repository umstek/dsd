package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.scheduler.Scheduler;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TextFileUtils {

    private final static Logger log = Logger.getLogger(TextFileUtils.class);

    /**
     * @param filename name of the file to read
     * @return array list of the lines in the text file
     * @throws IOException if the file is not found
     */
    public static ArrayList<String> readFileContent(String filename) throws IOException {
        BufferedReader br = null;
        ArrayList<String> content = new ArrayList<>();
        Path filepath = Paths.get(Paths.get("").toAbsolutePath() + filename);
        try {
            br = new BufferedReader(new FileReader(filepath.toString()));
            String line = br.readLine();
            while (line != null) {
                content.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return content;
    }

    /**
     * this method will update the hosting file index after a download
     *
     * @param newFile  newly downloaded file
     * @param filename name of the configuration file that contains the list of files to be hosted
     * @throws IOException if the configuration file is not found this will throw
     */
    public static void updateFileContent(String newFile, String filename) throws IOException {
        ArrayList<String> f = TextFileUtils.readFileContent(filename);
        String[] existingFiles = f.toArray(new String[f.size()]);
        boolean contains = TextFileUtils.contains(existingFiles, newFile);
        if (!contains) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Paths.get("").toAbsolutePath() + filename, true));
            bw.newLine();
            bw.write(newFile);
            bw.close();
            System.out.println("Now we are hosting files");
            log.info("Now we are hosting files");
        }
    }

    /**
     * Generic helper method implementation for searching for an object in an array
     * @param array the array of objects
     * @param v     the object
     * @param <T>   type of the above objects
     * @return true if the given array contains the given object
     */
    public static <T> boolean contains(final T[] array, final T v) {
        if (v == null) {
            for (final T e : array)
                if (e == null)
                    return true;
        } else {
            for (final T e : array)
                if (e == v || v.equals(e))
                    return true;
        }

        return false;
    }
}
