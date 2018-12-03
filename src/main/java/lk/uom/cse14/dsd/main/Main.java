package lk.uom.cse14.dsd.main;

import lk.uom.cse14.dsd.bscom.RegisterException;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.query.QueryTask;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
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

    private static int readPort(Scanner scanner) {
        int port = 0;
        do {
            System.out.print("Enter the port number of bootstrap server: ");
            try {
                port = scanner.nextInt();
            } catch (InputMismatchException e) {
                // Don't do anything
            }
        } while (port <= 0 || port > 65535);

        return port;
    }

    private static String readAddress(Scanner scanner) {
        System.out.print("Enter the address of bootstrap server: ");
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

    private static int readFileIndex(Scanner scanner) {
        int file = -1;
        do {
            System.out.print("Select a file [0 to cancel]: ");
            try {
                file = scanner.nextInt();
            } catch (InputMismatchException e) {
                // Don't do anything
            }
        } while (file <= -1 || file > Main.queryTask.getQueryResult().getFileNames().size());

        return file;
    }

    private static int readFileHost(Scanner scanner) {
        int file = -1;
        do {
            System.out.print("Select a host [0 to cancel]: ");
            try {
                file = scanner.nextInt();
            } catch (InputMismatchException e) {
                // Don't do anything
            }
        } while (file <= -1 || file > 65535);

        return file;
    }

    private static volatile boolean searching = false;
    private static volatile QueryTask queryTask = null;

    private static void printResults() {
//        System.out.println(Main);
        System.out.println("Results [" + Main.queryTask.getQueryResult().getFileNames().size() + "]: ");
        List<String> fileNames = Main.queryTask.getQueryResult().getFileNames();
        for (int i = 0; i < fileNames.size(); i++) {
            String filename = fileNames.get(i);
            System.out.println((i + 1) + ".\t" + filename);
            for (RoutingEntry re : Main.queryTask.getQueryResult().getRoutingEntries(filename)) {
                System.out.println("\t\u2517\u2501\u2501\u2501"
                        + re.getPeerIP() + "\t"
                        + (re.getPeerPort() + 1) + "\t"
                        + re.getStatus());
            }
        }
    }

    private static void printHosts(String filename) {
        for (RoutingEntry re : Main.queryTask.getQueryResult().getRoutingEntries(filename)) {
            System.out.println("\t\u2517\u2501\u2501\u2501"
                    + re.getPeerIP() + "\t"
                    + (re.getPeerPort() + 1) + "\t"
                    + re.getStatus());
        }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

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
        int port = 6050;
        try {
            port = Integer.parseInt(argValue(argsList, "-p"));
        } catch (NumberFormatException e) {
            // Do not change the port
        }

        // Configure bootstrap server address
        String bsAddr;
        int bsPort;
        String bs = argValue(argsList, "-bs");
        if (bs == null || !bs.contains(":")) {
            System.out.println("Bootstrap server address is not specified in arguments. ");
            System.out.println("e.g.: `-bs 192.168.1.2:5000`");
            bsAddr = readAddress(scanner);
            bsPort = readPort(scanner);
        } else {
            bsAddr = bs.split(":")[0];
            try {
                bsPort = Integer.parseInt(bs.split(":")[1]);
            } catch (NumberFormatException e) {
                System.out.println("Please specify bootstrap server address in the correct format. ");
                System.out.println("e.g.: `-bs 192.168.1.2:5000`");
                bsAddr = readAddress(scanner);
                bsPort = readPort(scanner);
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
                    case ":test":
                        System.out.print("Enter test iteration count :");
                        int iterations = 100;
                        try {
                            iterations = scanner.nextInt();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error when selecting inputs.");
                            System.exit(78);
                        }
                        System.out.print("Skip Cache? 1-Yes 0-No :");
                        int skipCache = 0;
                        try {
                            skipCache = scanner.nextInt();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error when selecting inputs.");
                            System.exit(78);
                        }
                        assert peer != null;
                        peer.testQuery(iterations, skipCache == 1);
                        System.out.println("All Queries Submitted!");
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
                    Main.searching = true;
                    Main.queryTask = null;

                    peer.query(new QueryTaskListener() {
                        @Override
                        public void notifyQueryComplete(QueryTask queryTask) {
                            Main.queryTask = queryTask;
                            Main.searching = false;
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

                    System.out.println();

                    if (Main.queryTask != null) {
                        printResults();

                        int fileI = readFileIndex(scanner);
                        if (fileI > 0) {
                            String filenameSelected = Main.queryTask.getQueryResult().getFileNames().get(fileI - 1);
                            printHosts(filenameSelected);

                            int fileH = readFileHost(scanner);
                            RoutingEntry routingEntry = Main.queryTask.getQueryResult().getRoutingEntries(filenameSelected).get(fileH - 1);

                            peer.downloadFile(routingEntry, filenameSelected);
                        }
                    } else {
                        System.out.println("No results found. ");
                    }
                }
            }
        }
    }
}
