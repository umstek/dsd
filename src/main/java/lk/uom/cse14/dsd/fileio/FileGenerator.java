package lk.uom.cse14.dsd.fileio;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to handle generating random files to serve upo  request.
 * todo: retrieve files upon search request
 */

public class FileGenerator {

    private final static int MAXSIZE = 10;
    private final static int MINSIZE = 2;

    public static ArrayList<Object> getFiles(ArrayList<String> filenames) {
        ArrayList<Object> files = new ArrayList<>();
        for (String filename : filenames) {
            int randomNum = ThreadLocalRandom.current().nextInt(MINSIZE+1, MAXSIZE+1);
            byte[] file = new byte[randomNum*1000000];
            Random r = new Random();
            r.nextBytes(file);
            BigInteger i = new BigInteger(file);
            System.out.println("Size of file " + filename + " is " + i.toByteArray().length/(1024*1024) + " MB");
            files.add(i);
        }
        return files;
    }
}
