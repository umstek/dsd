package lk.uom.cse14.dsd.main;

import lk.uom.cse14.dsd.bscom.RegisterException;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

//        TcpRegistryCommunicator x = new TcpRegistryCommunicator("127.0.0.1", 5000);
//        try {
//            List<PeerInfo> peerInfos = x.register("127.0.0.1", 4990, "user1");
//            for (PeerInfo pi : peerInfos) {
//                System.out.println(pi);
//            }
//        } catch (IOException e) {
//            System.out.println("IO error");
//            e.printStackTrace();
//        } catch (RegisterException e) {
//            System.out.println("Registering error");
//            e.printStackTrace();
//        }
        String ownHostFinal = null;
        try {
            List<String> ownHosts = NetworkInterfaceUtils.findOwnHosts(false);
            for (String ownHost : ownHosts) {
                if (!"".equals(ownHost)) {
                    ownHostFinal = ownHost;
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (ownHostFinal == null) {
            System.exit(78);
        }
        Peer peer = null;
        try {
            peer = new Peer("10.10.10.35", 5000, ownHostFinal, 3000, "001");
            peer.startPeer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RegisterException e) {
            e.printStackTrace();
        }

//        peer.getUdpSender().sendMessage(HeartbeatRequest.newHeartbeatMessage(Request.MessageType.HEARTBEAT, ownHostFinal, "127.0.0.1", 3006));
//        peer.getUdpSender().sendMessage(HeartbeatRequest.newHeartbeatMessage(Request.MessageType.HEARTBEAT,
//                ownHostFinal, "127.0.0.1", 3006));


    }
}
