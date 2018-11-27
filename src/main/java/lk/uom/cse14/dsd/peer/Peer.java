package lk.uom.cse14.dsd.peer;

import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
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
        taskExecutor.submit(this.udpReceiver);
        taskExecutor.submit(this.udpSender);
        System.out.println("DisFish Peer Started at: " + new Date().toString());
        System.out.println("Local Address: " + socket.getLocalSocketAddress());
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
