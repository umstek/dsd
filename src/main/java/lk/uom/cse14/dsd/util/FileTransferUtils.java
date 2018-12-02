package lk.uom.cse14.dsd.util;


import lk.uom.cse14.dsd.fileio.DummyFile;
import lk.uom.cse14.dsd.fileio.FileGenerator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class FileTransferUtils {

    /**
     * This method downloads the given file from the given host over TCP and updates the local file index
     * And also validates the file
     * @param hostIP   host IP
     * @param hostPort host Port
     * @param filename name of the file
     * @throws IOException              if the file is not downloaded this will throw in validation phase
     * @throws ClassNotFoundException   this will be thrown by the validateFile method
     * @throws NoSuchAlgorithmException this will be thrown by the validateFile method
     */
    public static void downloadFile(String hostIP, int hostPort, String filename) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        boolean validated = false;
        while (!validated) {
            Socket clientSock = new Socket(hostIP, hostPort);
            String hash = FileTransferUtils.receive(clientSock);
            clientSock.close();
            validated = FileTransferUtils.validateDownload(new File(filename), hash);
        }
        TextFileUtils.updateFileContent(filename, QueryUtils.FILE_LIST);
        System.out.println("File \"" + filename + "\" Downloaded successfully!\n");

    }


    /**
     * This method serves the given file in the given port over TCP
     * @param serverPort    TCP to listen for a client connection
     * @param filename      Name of the file to serve
     * @throws IOException              if the file to serve is not found
     * @throws NoSuchAlgorithmException if hashing algorithm is not known
     */
    public static void serveFile(int serverPort, String filename) throws IOException, NoSuchAlgorithmException {

        File file = new File(Paths.get("").toAbsolutePath() + "/Hosted_Files/" + filename);

        byte[] hashBytes = FileGenerator.generateFile(filename);
        String hash = FileGenerator.bytesToHex(hashBytes);

        ServerSocket serverSocket = new ServerSocket(serverPort);
        Socket sock = serverSocket.accept();

        FileTransferUtils.send(file, hash, sock);
        sock.close();
        serverSocket.close();
    }

    /**
     * This method connects to a host TCP port and downloads the file
     *
     * @param socket TCP port the server is listening on
     * @return the hash of the received file
     */

    public static String receive(Socket socket) {

        String hash = null;
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String filename = dis.readUTF();

            hash = dis.readUTF();
            System.out.println("SHA-256-checksum of the file " + filename + "\n" + hash);

            int n = 0;
            byte[] buf = new byte[8192];

            System.out.println("Receiving file: " + filename);
            File file = new File(Paths.get("").toAbsolutePath() + "/Downloads/" + filename);
            file.getParentFile().mkdirs();
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);

            while ((n = dis.read(buf)) != -1) {
                fos.write(buf, 0, n);
                fos.flush();
            }
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return hash;
        }
    }

    /**
     * This method transfers a file and its SHA-256 hash over a TCP connection
     * @param file      the file that should be transferred
     * @param hash      SHA-256 hash of the file
     * @param socket    TCP socket the client will connect to
     */
    public static void send(File file, String hash, Socket socket) {

        try {
            String filename = file.getName();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            //sending filename
            dos.writeUTF(filename);
            dos.flush();

            //sending hash
            dos.writeUTF(hash);

            int n = 0;
            byte[] buf = new byte[4092];

            FileInputStream fis = new FileInputStream(file);
            System.out.println("Sending file: " + filename);
            while ((n = fis.read(buf)) != -1) {
                dos.write(buf, 0, n);
                dos.flush();
            }
            dos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * this method validates a download by comparing the received SHA-256 hash with the downloaded files hash
     * @param file the file to validate
     * @param hash the SHA-256 hash of the file
     * @return true if the file is validated
     * @throws IOException              if the file is not found we throw this
     * @throws NoSuchAlgorithmException if the hashing algorithms is not known this will throw
     * @throws ClassNotFoundException   in case the received object fails to be casted in to a DummyFile object
     */
    public static boolean validateDownload(File file, String hash) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {

        byte[] fileBytes;
        DummyFile dummyFile = (DummyFile) new ObjectInputStream(
                new FileInputStream(Paths.get("").toAbsolutePath() + "/Downloads/" + file)).readObject();
        fileBytes = dummyFile.toByteArray();

        //calculating the hash of the dummy file
        byte[] calculatedHashBytes = FileGenerator.generateHash(fileBytes);
        String calculatedHash = FileGenerator.bytesToHex(calculatedHashBytes);

        return hash.equalsIgnoreCase(calculatedHash);
    }


}
