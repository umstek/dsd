package lk.uom.cse14.dsd.util;


import lk.uom.cse14.dsd.fileio.DummyFile;
import lk.uom.cse14.dsd.fileio.FileGenerator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class FileTransferUtils {

    /**
     * This method downloads the given file from the given host over TCP and updates the local file index
     *
     * @param hostIP   host IP
     * @param hostPort host Port
     * @param filename name of the file
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchAlgorithmException
     */
    public static void downloadFile(String hostIP, int hostPort, String filename) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        boolean validated = false;
        while (!validated) {
            Socket clientSock = new Socket(hostIP, hostPort);
            ArrayList<File> files = FileTransferUtils.receive(clientSock);
            validated = FileTransferUtils.validateDownload(files);
        }
        TextFileUtils.updateFileContent(filename, QueryUtils.FILE_LIST);
        System.out.println("File Downloaded successfully!");
    }


    /**
     * This method serves the given file in the given port over TCP
     * @param serverPort
     * @param filename
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void serveFile(int serverPort, String filename) throws IOException, NoSuchAlgorithmException {

        File file = new File(Paths.get("").toAbsolutePath() + "/Hosted_Files/" + filename);
        File hash = new File(Paths.get("").toAbsolutePath() + "/Hosted_Files/SHA-256-checksum-" + filename);

        boolean fileExists = file.exists();
        boolean hashExists = hash.exists();

        if (!fileExists || !hashExists) {
            FileGenerator.generateFile(filename);
        }

        //construct the payload
        ArrayList<File> filesToSend = new ArrayList<>();
        filesToSend.add(hash);
        filesToSend.add(file);

        ServerSocket serverSocket = new ServerSocket(serverPort);
        Socket sock = serverSocket.accept();

        FileTransferUtils.send(filesToSend, sock);
    }

    public static ArrayList<File> receive(Socket socket) {

        ArrayList<String> fileNames = null;
        ArrayList<File> files = null;

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            int number = dis.readInt();
            files = new ArrayList<>(number);
            fileNames = new ArrayList<>(number);

            System.out.println("Number of Files to be received: " + number);

            for (int i = 0; i < number; i++) {
                String filename = dis.readUTF();
                fileNames.add(filename);
                File file = new File(filename);
                files.add(file);
            }

            int n = 0;
            byte[] buf = new byte[4092];

            for (int i = 0; i < files.size(); i++) {

                System.out.println("Receiving file: " + files.get(i).getName());
                File file = new File(Paths.get("").toAbsolutePath() + "/Downloads/" + files.get(i).getName());
                file.getParentFile().mkdirs();
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);

                while ((n = dis.read(buf)) != -1) {
                    fos.write(buf, 0, n);
                    fos.flush();
                }
                fos.close();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return files;
        }
    }

    public static void send(ArrayList<File> files, Socket socket) {

        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            System.out.println(files.size());

            dos.writeInt(files.size());
            dos.flush();


            for (int i = 0; i < files.size(); i++) {
                dos.writeUTF(files.get(i).getName());
                dos.flush();
            }


            int n = 0;
            byte[] buf = new byte[4092];

            for (int i = 0; i < files.size(); i++) {
                System.out.println(files.get(i).getName());
                FileInputStream fis = new FileInputStream(files.get(i));

                while ((n = fis.read(buf)) != -1) {
                    dos.write(buf, 0, n);
                    dos.flush();
                }

            }
            dos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean validateDownload(ArrayList<File> files) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        File file;
        File hash;

        File file1 = files.get(0);

        if (file1.getName().contains("SHA-256-checksum")) {
            hash = file1;
            file = files.get(1);
        } else {
            file = file1;
            hash = files.get(1);
        }

        byte[] fileBytes;
        DummyFile dummyFile = (DummyFile) new ObjectInputStream(new FileInputStream(file)).readObject();
        fileBytes = dummyFile.toByteArray();

        //calculating the hash of the dummy file
        byte[] calculatedHash = FileGenerator.generateHash(fileBytes);

        //reading the original hash
        FileInputStream fin = new FileInputStream(hash);
        byte[] originalHash = new byte[(int) hash.length()];
        fin.read(originalHash);

        return Arrays.equals(calculatedHash, originalHash);
    }


}
