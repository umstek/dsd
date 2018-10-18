package lk.uom.cse14.dsd.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkInterfaceUtils {
    public static List<String> findOwnHosts(boolean allowLoopback) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        ArrayList<String> validHosts = new ArrayList<>();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (!networkInterface.isUp()) {
                continue;
            }
            /* We want to connect to somewhere external unless we're in dev mode. */
            if (!allowLoopback) {
                if (networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
            }

            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();

                /* Filter-out IPV6 Addresses. */
                if (inetAddress instanceof Inet6Address) {
                    continue;
                }

                String hostAddress = inetAddress.getHostAddress();
                validHosts.add(hostAddress);
            }
        }

        return validHosts;
    }
}
