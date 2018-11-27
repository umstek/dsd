package lk.uom.cse14.dsd.fileio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to handle generating random files to serve upo  request.
 * todo: retrieve files upon search request
 */

public class FileGenerator {

    private final static int MAXSIZE = 10;
    private final static int MINSIZE = 2;

    public static HashMap generateAllHostedFiles(ArrayList<String> filenames) {
        HashMap<String, DummyFile> files = new HashMap<>();
        for (String filename : filenames) {
             DummyFile file = generateFile(filename);
             files.put(filename, file);
        }
        return files;
    }

    private static byte[] generateLargeNumber() {
        int randomNum = ThreadLocalRandom.current().nextInt(MINSIZE + 1, MAXSIZE + 1);
        byte[] number = new byte[randomNum * 1000000];
        Random r = new Random();
        r.nextBytes(number);
        return number;
    }

    public static DummyFile generateFile(String filename){
        byte[] bigint = FileGenerator.generateLargeNumber();
        int size = bigint.length;
        int sizeMB = size / (1024*1024);

        StringBuilder sb = new StringBuilder();
        for (int i=0; i<size; i++){
            sb.append('a');
        }
        String data = sb.toString();
        byte[] hash = generateHash(data.getBytes(StandardCharsets.UTF_8));
        System.out.println("Filename : " + filename + "\nFile size : " + sizeMB + " MB\nHash : " + hash + "\n" );

        DummyFile df = new DummyFile();
        df.setSize(sizeMB);
        df.setData(data);
        df.setHash(hash);
        return df;
    }

    public static byte[] generateHash(byte[] file) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest.digest(file);
    }

}
