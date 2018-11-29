package lk.uom.cse14.dsd.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/*
 * In dev mode, localhost must be given priority. UDP listener should be on random port.
 * Otherwise, 192.168.0.0 - 192.168.255.255, 172.16.0.0 - 172.31.255.255, 10.0.0.0 - 10.255.255.255 IP ranges
 * should get priority in that order. Assign a fixed port number and if that is in use, suggest the next.
 */

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
