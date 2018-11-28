package lk.uom.cse14.dsd.main;

import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.message.HeartbeatRequest;
import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;

import java.net.SocketException;
import java.util.List;

public class SchedulerTest {
    public static void main(String[] args) {
        sheculeMessage();

    }



    public static void sheculeMessage() {
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
        Peer peer = new Peer(ownHostFinal,3005);
        peer.startPeer();
        System.out.println("here");
//        peer.getUdpSender().sendMessage(HeartbeatRequest.newHeartbeatMessage(MessageType.HEARTBEAT, ownHostFinal, "127.0.0.1", 3006));


    }
}
