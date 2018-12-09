package lk.uom.cse14.dsd.fileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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


    /**
     * @param filenames the filenames that should be generated
     * @return a hashmap which has filenames as the set of keys
     * @throws NoSuchAlgorithmException
     */
    public static HashMap<String, DummyFile> generateAllHostedFiles(ArrayList<String> filenames) throws NoSuchAlgorithmException, IOException {
        HashMap<String, DummyFile> files = new HashMap<>();
        for (String filename : filenames) {
//            DummyFile file = generateFile(filename);
//            files.put(filename, file);
        }
        return files;
    }

    /**
     * generates a random integer between 2MB and 10MB
     *
     * @return randomly generated integer byte array
     */
    private static byte[] generateLargeNumber() {
        int randomNum = ThreadLocalRandom.current().nextInt(MINSIZE + 1, MAXSIZE + 1);
        byte[] number = new byte[randomNum * 1000000];
        Random r = new Random();
        r.nextBytes(number);
        return number;
    }

    /**
     * this method generates a dummy file based on a large random integer
     *
     * @return a DummyFile object
     */
    public static DummyFile generateDummyFile() {
        byte[] bigint = FileGenerator.generateLargeNumber();
        int size = bigint.length;
        int sizeMB = size / (1024 * 1024);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append('a');
        }
        String data = sb.toString();

        DummyFile df = new DummyFile();
        df.setSize(sizeMB);
        df.setData(data);
        return df;
    }

    /**
     * This method will generate a file with size between 2-10 MB and its hash
     *
     * @param filename the name of the file
     * @return hash of the randomly generated file
     * @throws IOException              if the output file is missing
     * @throws NoSuchAlgorithmException if hashing algorithm is not available
     */

    public static byte[] generateFile(String filename) throws IOException, NoSuchAlgorithmException {

        File file = new File(Paths.get("").toAbsolutePath() + "/Hosted_Files/" + filename);
        file.getParentFile().mkdirs();
        file.createNewFile();

        ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(file));

        DummyFile dummyFile = FileGenerator.generateDummyFile();

        //writing object to file
        fileOut.writeObject(dummyFile);
        fileOut.close();

        byte[] bytes;
        bytes = dummyFile.toByteArray();

        //calculating the hash of the dummy file and writing it
        byte[] fileHash = FileGenerator.generateHash(bytes);
        return fileHash;
    }

    /**
     * @param file the byetearray of the the file that should be hashed
     * @return SHA-256 hash of the file
     * @throws NoSuchAlgorithmException in absence of the hashing algorithm
     */
    public static byte[] generateHash(byte[] file) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(file);
    }

    /**
     * Helper methods for hashing
     */

    public static byte[] getHashByteArray(String data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String getHash(String data) {
        return bytesToHex(getHashByteArray(data));
    }

}
