package lk.uom.cse14.dsd.ui;

import lk.uom.cse14.dsd.bscom.RegisterException;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.query.QueryTask;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class GUI {
    private static String readQuery(Scanner scanner) {
        System.out.print("> ");
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

    private static String readUsername(Scanner scanner) {
        System.out.print("Enter a unique username for the peer: ");
        return scanner.next();
    }

    private static boolean argExists(List<String> argsList, String arg) {
        return argsList.size() > 0 && argsList.contains(arg);
    }

    private static String argValue(List<String> argsList, String arg) {
        String result = null;
        if (argsList.size() > 0 && argsList.contains(arg)) {
            int r = argsList.indexOf(arg) + 1;
            if (r < argsList.size()) {
                result = argsList.get(r);
            }
        }

        return result;
    }

    private static volatile boolean searching = false;

    public static void main(String[] args) {
        List<String> argsList = Arrays.asList(args);
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\n");

        // Print help information
        boolean help = argExists(argsList, "--help") || argExists(argsList, "-h");
        if (help) {
            System.out.println("Arguments: \n"
                    + "--help, -h: Display this help info. \n"
                    + "--dev: Enable loopback NI. \n"
                    + "-p: Specify the port to run the peer. \n"
                    + "-bs: Specify the bootstrap server address in `ip.address:port` format. ");
            System.exit(0);
        }

        // Capture dev flag to allow loopback address
        boolean dev = argExists(argsList, "--dev");

        // Set-up port to run peer
        int port = 3000;
        try {
            port = Integer.parseInt(argValue(argsList, "-p"));
        } catch (NumberFormatException e) {
            // Do not change the port
        }

        // Configure bootstrap server address
        String bsAddr = null;
        int bsPort = 0;
        String bs = argValue(argsList, "-bs");
        if (bs == null || !bs.contains(":")) {
            System.out.println("Please specify bootstrap server address in the correct format. ");
            System.out.println("e.g.: `-bs 192.168.1.2:5000`");
            System.exit(65);
        } else {
            bsAddr = bs.split(":")[0];
            try {
                bsPort = Integer.parseInt(bs.split(":")[1]);
            } catch (NumberFormatException e) {
                System.out.println("Please specify bootstrap server address in the correct format. ");
                System.out.println("e.g.: `-bs 192.168.1.2:5000`");
                System.exit(66);
            }
        }

        // Find a unique username to be used as the peer name
        String username = readUsername(scanner);

        // Find current peer address using open network interfaces
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

        // Create a peer
        Peer peer = null;
        try {
            peer = new Peer(bsAddr, bsPort, address, port, username);
            peer.startPeer();
        } catch (IOException | RegisterException e) {
            e.printStackTrace();
        }

        // MAIN_LOOP
        String query;
        main_loop:
        while (true) {
            query = readQuery(scanner);
            if (query.isEmpty()) {
                System.out.println();
            } else if (query.startsWith(":")) {
                switch (query) {
                    case ":routing-table":
                        // Fallthrough
                    case ":routing":
                        // Fallthrough
                    case ":rt":
                        if (peer != null) {
                            peer.printRoutingTable();
                        }
                        break;
                    case ":own-files":
                        // Fallthrough
                    case ":files":
                        if (peer != null && peer.getHostedFileNames() != null) {
                            for (int i = 0; i < peer.getHostedFileNames().size(); i++) {
                                System.out.println(peer.getHostedFileNames().get(i));
                            }
                        }
                        break;
                    case ":exit":
                        System.exit(1);
                        break main_loop;
                    default:
                        System.out.println("Unknown command `" + query + "`");
                        break;
                }
            } else {
                boolean useCache = readUseCacheResponse(scanner);
                if (peer != null) {
                    GUI.searching = true;

                    peer.query(new QueryTaskListener() {
                        @Override
                        public void notifyQueryComplete(QueryTask queryTask) {
                            GUI.searching = false;
                            System.out.println();
                            System.out.println("Results [" + queryTask.getQueryResult().getFileNames().size() + "]: ");
                            List<String> fileNames = queryTask.getQueryResult().getFileNames();
                            for (int i = 0; i < fileNames.size(); i++) {
                                System.out.println((i + 1) + ".\t" + fileNames.get(i));
                            }
                        }
                    }, query, !useCache);

                    System.out.println("Searching...");
                    while (searching) {
                        System.out.print(".");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
