package lk.uom.cse14.dsd.peer;

import lk.uom.cse14.dsd.bscom.PeerInfo;
import lk.uom.cse14.dsd.bscom.RegisterException;
import lk.uom.cse14.dsd.bscom.TcpRegistryCommunicator;
import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.fileio.DummyFile;
import lk.uom.cse14.dsd.fileio.FileGenerator;
import lk.uom.cse14.dsd.fileio.TextFileHandler;
import lk.uom.cse14.dsd.msghandler.*;
import lk.uom.cse14.dsd.query.DummyCacheQueryProcessor;
import lk.uom.cse14.dsd.query.DummyFileQueryProcessor;
import lk.uom.cse14.dsd.query.ICacheQuery;
import lk.uom.cse14.dsd.query.IFileQuery;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import lk.uom.cse14.dsd.util.SearchUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Peer class represents an actual peer. It contains UdpSender and UdpReceiver objects to manage
 * request passing between peer nodes.
 * todo: implement request scheduler and manage high level request passing
 * todo: dynamic scheduling of messages, peer discovery and queries
 * todo: handle exceptions properly
 * */
public class Peer {
    /*
    This value is hardcoded
     */
    private final String FILE_LIST = "/config/File Names.txt";
    private DatagramSocket socket;
    private UdpSender udpSender;
    private UdpReceiver udpReceiver;
    private ExecutorService taskExecutor;
    private Scheduler scheduler;
    private IHandler queryHandler;
    private IHandler heartbeatHandler;
    private IHandler peerDiscoveryHandler;
    private IFileQuery fileQueryProcessor;
    private ICacheQuery cacheQueryProcessor;
    private ArrayList<String> hostedFileNames;
    private HashMap<String, DummyFile> hostedFiles;
    private ArrayList<RoutingEntry> routingTable;
    private String ownHost;
    private int ownPort;
    public Peer(String BSHost, int BSPort, String ownHost, int ownPort, String userName) throws IOException, RegisterException {
        TcpRegistryCommunicator tcpRegistryCommunicator = new TcpRegistryCommunicator(BSHost, BSPort);
        try {
            this.ownHost = ownHost;
            this.ownPort = ownPort;
            this.socket = new DatagramSocket(ownPort);
            this.taskExecutor = Executors.newFixedThreadPool(10);
            this.udpSender = new UdpSender(1000, 100, socket);
            this.udpReceiver = new UdpReceiver(socket);
            this.routingTable = new ArrayList<>();
            this.scheduler = new Scheduler(udpReceiver, udpSender);
            List<PeerInfo> peers = tcpRegistryCommunicator.register(ownHost, ownPort, userName);
            this.peerDiscoveryHandler = new PeerDiscoveryHandler(routingTable, ownHost, ownPort, scheduler, peers);
            this.fileQueryProcessor = new DummyFileQueryProcessor();
            this.cacheQueryProcessor = new DummyCacheQueryProcessor();
            this.queryHandler = new QueryHandler(routingTable, scheduler, cacheQueryProcessor, fileQueryProcessor, ownHost, ownPort);
            this.heartbeatHandler = new HeartbeatHandler(ownHost, ownPort, scheduler, routingTable);
            this.scheduler.setHeartbeatHandler(heartbeatHandler);
            this.scheduler.setQueryHandler(queryHandler);
            this.scheduler.setPeerDiscoveryHandler(peerDiscoveryHandler);

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getHostedFileNames() {
        return hostedFileNames;
    }

    public HashMap<String, DummyFile> getHostedFiles() {
        return hostedFiles;
    }

    public void startPeer() {

        /*
        initializes the searching "index" in SearchUtils class
         */
        SearchUtils.initialize(this.hostedFileNames);

        taskExecutor.submit(this.udpReceiver);
        taskExecutor.submit(this.udpSender);
        taskExecutor.submit((Runnable) this.peerDiscoveryHandler);
        taskExecutor.submit((Runnable) this.heartbeatHandler);
        taskExecutor.submit(scheduler);
        System.out.println("DisFish Peer Started at: " + new Date().toString());
        System.out.println("Local Address: " + ownHost + ":" + ownPort);
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
