package lk.uom.cse14.dsd.ui;

import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;

import java.net.SocketException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class GUI {
    private static String readQuery(Scanner scanner) {
        System.out.print("Search: ");
        return scanner.next();
    }

    private static boolean readUseCacheResponse(Scanner scanner) {
        String useCacheStr;
        do {
            System.out.print("Use cached query responses? [Y/n]: ");
            useCacheStr = scanner.next();
            if (useCacheStr.isEmpty()) {
                useCacheStr = "y";
            }
        } while (!useCacheStr.toLowerCase().equals("n") && !useCacheStr.toLowerCase().equals("y"));

        return useCacheStr.toLowerCase().equals("y");
    }

    public static void main(String[] args) {
        List<String> argsList = Arrays.asList(args);
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\n");

        boolean dev = argsList.size() > 0 && argsList.contains("--dev");

        int port = 3000;
        if (argsList.size() > 0 && argsList.contains("-p")) {
            int r = argsList.indexOf("-p") + 1;
            if (r < argsList.size()) {
                try {
                    port = Integer.parseInt(argsList.get(r));
                } catch (NumberFormatException e) {
                    // Do not change the port
                }
            }
        }

        String address = "";
        try {
            List<String> ownHosts = NetworkInterfaceUtils.findOwnHosts(dev);
            if (ownHosts.size() == 0) {
                System.out.println("No network interfaces found. ");
                System.exit(40);
            } else if (ownHosts.size() == 1) {
                address = ownHosts.get(0);
            } else {
                System.out.println("Multiple network interfaces found. Enter index to select. ");
                for (int i = 0; i < ownHosts.size(); i++) {
                    System.out.println((i + 1) + ".\t" + ownHosts.get(i));
                }

                int selectedAddrIndex = 0;
                while (selectedAddrIndex <= 0 || selectedAddrIndex > ownHosts.size()) {
                    try {
                        System.out.print("Select: ");
                        selectedAddrIndex = scanner.nextInt();
                    } catch (InputMismatchException e) {
                        // Do not change the address
                    }
                }
                address = ownHosts.get(selectedAddrIndex - 1);
            }
        } catch (SocketException e) {
            // TODO Handle error
        }

        if (dev) {
            System.out.println("Running in dev mode...");
        }
        System.out.println("Starting on address " + address + " and port " + port);
    }
}
