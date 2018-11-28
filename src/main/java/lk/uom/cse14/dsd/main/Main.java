package lk.uom.cse14.dsd.main;

import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;

import java.net.SocketException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify a port. ");
            System.exit(20);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid port number. ");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please specify a positive integer less than 65535 as the port number. ");
            System.exit(20);
        }

        String ownHostFinal = null;
        try {
            List<String> ownHosts = NetworkInterfaceUtils.findOwnHosts(true);
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

        Peer peer = new Peer(ownHostFinal, port);
        peer.startPeer();
    }
}
