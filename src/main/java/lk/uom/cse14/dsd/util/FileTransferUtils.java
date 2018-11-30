package lk.uom.cse14.dsd.util;


import lk.uom.cse14.dsd.fileio.FileGenerator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class FileTransferUtils {

//    public static void downloadAll(String hostIP, int hostPort, String filename) throws IOException {
//        FileTransferUtils.downloadHash(hostIP, hostPort, filename);
//        FileTransferUtils.downloadFile(hostIP, hostPort, filename);
//    }
//
//    public static void downloadFile(String hostIP, int hostPort, String filename) throws IOException {
//        FileTransferUtils.download(hostIP, hostPort, filename);
//    }
//
//    public static void downloadHash(String hostIP, int hostPort, String filename) throws IOException {
//        String hashFilename = "SHA-256-checksum-" + filename;
//        FileTransferUtils.download(hostIP, hostPort,hashFilename);
//    }

//    public static void download(String hostIP, int hostPort, String filename) throws IOException {
//        Socket clientSock = new Socket(hostIP, hostPort);
//        InputStream is = clientSock.getInputStream();
//
//        FileOutputStream fos = new FileOutputStream(new File(filename));
//        BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//        int bytesRead;
//        byte[] buffer = new byte[1024];
//        while ((bytesRead = is.read(buffer)) != -1) {
//            bos.write(buffer, 0, bytesRead);
//        }
//
//        bos.flush();
//        bos.close();
//        clientSock.close();
//    }

    /**
     * This method listens on the given port on the given host and download the files
     * @param hostIP IP of the host
     * @param hostPort Port the host is listening on
     * @throws IOException
     */
    public static void downloadFile(String hostIP, int hostPort, String filename) throws IOException {
        Socket clientSock = new Socket(hostIP, hostPort);
        FileTransferUtils.receive(clientSock);

    }

    /**
     * This method serves the given file on the given port
     *
     * @param serverPort the TCP port client is listening on
     * @param filename   Name of the desired file
     * @throws IOException if the
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

    public static void receive(Socket socket) {

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            int number = dis.readInt();
            ArrayList<File> files = new ArrayList<>(number);

            System.out.println("Number of Files to be received: " + number);

            for (int i = 0; i < number; i++) {
                File file = new File(dis.readUTF());
                files.add(file);
            }

            int n = 0;
            byte[] buf = new byte[4092];

            //outer loop, executes one for each file
            for (int i = 0; i < files.size(); i++) {

                System.out.println("Receiving file: " + files.get(i).getName());
                FileOutputStream fos = new FileOutputStream(Paths.get("").toAbsolutePath() + "/Downloads/" + files.get(i).getName());

                while ((n = dis.read(buf)) != -1) {
                    fos.write(buf, 0, n);
                    fos.flush();
                }
                fos.close();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

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


}
