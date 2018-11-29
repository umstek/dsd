package lk.uom.cse14.dsd.util;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferUtils {

    /**
     * This method downloads the given file from the given host
     *
     * @param hostIP   Ip of the node hosting the file
     * @param hostPort TCP port of the host
     * @param filename Name of the desired file
     * @throws IOException if the node fails to open the socket
     */

    public static void downloadFile(String hostIP, int hostPort, String filename) throws IOException {
        Socket clientSock = new Socket(hostIP, hostPort);
        InputStream is = clientSock.getInputStream();

        FileOutputStream fos = new FileOutputStream(new File(filename));
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        bos.flush();
        bos.close();
        clientSock.close();
    }

    /**
     * This method serves the given file on the given port
     *
     * @param serverPort the TCP port client is listening on
     * @param filename   Name of the desired file
     * @throws IOException if the
     */

    public static void serveFile(int serverPort, String filename) throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        File file = new File(filename);

        Socket sock = serverSocket.accept();
        byte[] fileByteArray = new byte[(int) file.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        bis.read(fileByteArray, 0, fileByteArray.length);
        OutputStream os = sock.getOutputStream();
        os.write(fileByteArray, 0, fileByteArray.length);
        os.flush();
        sock.close();
    }


//    public static boolean validateFile(File file) throws IOException, ClassNotFoundException {
//        DummyFile dummyFileObject;
//        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
//
//        dummyFileObject = (DummyFile) in.readObject();
//        byte[] calculatedHash = FileGenerator.getHashByteArray(dummyFileObject.getData());
//
//        in.close();
//        file.close();
//
//    }

}
