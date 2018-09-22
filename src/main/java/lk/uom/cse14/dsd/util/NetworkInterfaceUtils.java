package lk.uom.cse14.dsd.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkInterfaceUtils {
    public static String findOwnHosts() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            /* We want to connect to somewhere external. */
            // TODO Add networkInterface.isLoopback() || networkInterface.isVirtual() || in production.
            if (!networkInterface.isUp()) {
                continue;
            }

            // TODO Find where we can successfully start a node.
        }

        // TODO Return
        return null;
    }
}
