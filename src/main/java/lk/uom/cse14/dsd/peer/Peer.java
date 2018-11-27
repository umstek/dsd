package lk.uom.cse14.dsd.peer;

import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.fileio.DummyFile;
import lk.uom.cse14.dsd.fileio.FileGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Peer class represents an actual peer. It contains UdpSender and UdpReceiver objects to manage
 * message passing between peer nodes.
 * todo: implement message scheduler and manage high level message passing
 * todo: dynamic scheduling of messages, peer discovery and queries
 * */
public class Peer {
    private DatagramSocket socket;
    private UdpSender udpSender;
    private UdpReceiver udpReceiver;
    private ExecutorService taskExecutor;
    private ArrayList<String> hostedFileNames;
    private HashMap<String, DummyFile> hostedFiles;
    private String ownHost;
    private int ownPort;

    public Peer(String host,int port) {
        try {
            this.socket = new DatagramSocket(port);
            this.taskExecutor = Executors.newFixedThreadPool(5);
            this.udpSender = new UdpSender(1000, 100, socket);
            this.udpReceiver = new UdpReceiver(socket);
            this.ownHost = host;
            this.ownPort = port;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startPeer() {
        this.generateFiles();
        taskExecutor.submit(this.udpReceiver);
        taskExecutor.submit(this.udpSender);
        System.out.println("DisFish Peer Started at: " + new Date().toString());
        System.out.println("Local Address: " + socket.getLocalSocketAddress());
    }

    private void generateFiles() {
        BufferedReader br = null;
        ArrayList<String> filenames;

        filenames = new ArrayList<>();
        try {
            Path filepath = Paths.get(Paths.get("").toAbsolutePath() + "/src/main/java/lk/uom/cse14/dsd/fileio/File Names.txt");
            br = new BufferedReader(new FileReader(filepath.toString()));
            String line = br.readLine();
            System.out.println("\n****** LIST OF FILES TO BE HOSTED *******\n");
            while (line != null){
                System.out.println(line + "\n");
                filenames.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Initialize the \"File Names.txt\" with the list of files to be hosted in this node.");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        this.hostedFileNames = filenames;
        try {
            this.hostedFiles = FileGenerator.generateAllHostedFiles(filenames);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("Files have been successfully generated");
    }

    public UdpSender getUdpSender() {
        return udpSender;
    }

    public String getOwnHost() {
        return ownHost;
    }

    public int getOwnPort() {
        return ownPort;
    }
}
