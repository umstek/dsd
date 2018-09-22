package lk.uom.cse14.dsd.main;

import lk.uom.cse14.dsd.bscom.PeerInfo;
import lk.uom.cse14.dsd.bscom.RegisterException;
import lk.uom.cse14.dsd.bscom.TcpRegistryCommunicator;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        TcpRegistryCommunicator x = new TcpRegistryCommunicator("127.0.0.1", 5000);
        try {
            List<PeerInfo> peerInfos = x.register("127.0.0.1", 4990, "user1");
            for (PeerInfo pi : peerInfos) {
                System.out.println(pi);
            }
        } catch (IOException e) {
            System.out.println("IO");
            e.printStackTrace();
        } catch (RegisterException e) {
            System.out.println("Register");
            e.printStackTrace();
        }

    }
}
