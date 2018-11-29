package lk.uom.cse14.dsd.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TextFileUtils {

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
}
