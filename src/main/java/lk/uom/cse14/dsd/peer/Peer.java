package lk.uom.cse14.dsd.peer;

import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.fileio.DummyFile;
import lk.uom.cse14.dsd.fileio.FileGenerator;
import lk.uom.cse14.dsd.fileio.TextFileHandler;
import lk.uom.cse14.dsd.msghandler.HeartbeatHandler;
import lk.uom.cse14.dsd.util.SearchUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
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
 * todo: handle exceptions properly
 * */
public class Peer {
    private DatagramSocket socket;
    private UdpSender udpSender;
    private UdpReceiver udpReceiver;
    private ExecutorService taskExecutor;

    private HeartbeatHandler heartbeatHandler;

    /*
    This value is hardcoded
     */
    private final String FILE_LIST = "/config/File Names.txt";

    public ArrayList<String> getHostedFileNames() {
        return hostedFileNames;
    }

    public HashMap<String, DummyFile> getHostedFiles() {
        return hostedFiles;
    }

    private ArrayList<String> hostedFileNames;
    private HashMap<String, DummyFile> hostedFiles;
    private ArrayList<RoutingEntry> routingTable;
    private String ownHost;
    private int ownPort;

    public Peer(String host, int port) {
        try {
            this.socket = new DatagramSocket(port);
            this.taskExecutor = Executors.newFixedThreadPool(5);
            this.udpSender = new UdpSender(1000, 100, socket);
            this.udpReceiver = new UdpReceiver(socket);
            this.ownHost = host;
            this.ownPort = port;
//            this.heartbeatHandler = new HeartbeatHandler(ownHost, ownPort, scheduler, routingTable);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startPeer() {

        /*
        initializes the searching "index" in SearchUtils class
         */
        SearchUtils.initialize(this.hostedFileNames);

        taskExecutor.submit(this.udpReceiver);
        taskExecutor.submit(this.udpSender);
        System.out.println("DisFish Peer Started at: " + new Date().toString());
        System.out.println("Local Address: " + socket.getLocalSocketAddress());
        this.generateFiles();
        System.out.println("\n************** List of hosted files **************\n");
        for (String filename : this.hostedFileNames
        ) {
            System.out.println(filename);
        }
    }

    private void generateFiles() {
        ArrayList<String> filenames = null;
        try {
            filenames = TextFileHandler.readFileContent(FILE_LIST);
            this.hostedFileNames = filenames;
            this.hostedFiles = FileGenerator.generateAllHostedFiles(filenames);
            System.out.println("Files have been successfully generated");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(FILE_LIST + " is not initialized. Initialize it with the filenames to be hosted");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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

    public UdpReceiver getUdpReceiver() {
        return udpReceiver;
    }
}
