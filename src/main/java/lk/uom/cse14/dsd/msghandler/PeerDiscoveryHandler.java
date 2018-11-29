package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.bscom.PeerInfo;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.request.DiscoveryRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.DiscoveryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PeerDiscoveryHandler implements Runnable, IHandler {
    private final ArrayList<RoutingEntry> routingTable;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(PeerDiscoveryHandler.class);
    private Scheduler scheduler;
    private int peerLimit = 10;
    private boolean running = true;
    private String ownHost;
    private int ownPort;

    public PeerDiscoveryHandler(ArrayList<RoutingEntry> routingTable,
                                String ownHost, int ownPort, Scheduler scheduler, List<PeerInfo> peersList) {
        this.routingTable = routingTable;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
        this.scheduler = scheduler;
        this.init(peersList);
    }

    public void init(List<PeerInfo> peersList) {
        if (peersList.size() < 3) {
            for (PeerInfo info : peersList) {
                RoutingEntry routingEntry = new RoutingEntry();
                routingEntry.setPeerIP(info.getHost());
                routingEntry.setPeerPort(info.getPort());
                routingEntry.setStatus(RoutingEntry.Status.UNKNOWN);
                routingEntry.setRetryCount(0);
                routingTable.add(routingEntry);
            }


        } else {
            int random1 = ((int) (Math.random() * 100)) % peersList.size();
            int random2 = ((int) (Math.random() * 100)) % peersList.size();
            while (random1 == random2) {
                random2 = ((int) (Math.random() * 100)) % peersList.size();
            }
            PeerInfo info1 = peersList.get(random1);
            RoutingEntry routingEntry1 = new RoutingEntry();
            routingEntry1.setPeerIP(info1.getHost());
            routingEntry1.setPeerPort(info1.getPort());
            routingEntry1.setStatus(RoutingEntry.Status.UNKNOWN);
            routingEntry1.setRetryCount(0);
            routingTable.add(routingEntry1);
            PeerInfo info2 = peersList.get(random2);
            RoutingEntry routingEntry2 = new RoutingEntry();
            routingEntry2.setPeerIP(info2.getHost());
            routingEntry2.setPeerPort(info2.getPort());
            routingEntry2.setStatus(RoutingEntry.Status.UNKNOWN);
            routingEntry2.setRetryCount(0);
            routingTable.add(routingEntry2);
        }
    }

    @Override
    public void run() {
        while (running) {
            if (routingTable.size() < peerLimit - 3 && !routingTable.isEmpty()) {
                logger.info("Trying to find neighbours. Routing table size:" + routingTable.size());
                RoutingEntry entry = routingTable.get((int) (Math.random() * 100) % routingTable.size());
                if (entry.getStatus().equals(RoutingEntry.Status.ONLINE)) {
                    DiscoveryRequest request = new DiscoveryRequest(this.ownHost, this.ownPort, entry.getPeerIP(), entry.getPeerPort());
                    request.setType(MessageType.DISCOVERY);
                    request.setRequestedPeerCount(2);
                    logger.info("Sending discovery request to:" + request.getDestination() + ":" + request.getDestinationPort());
                    scheduler.schedule(request);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handle(Request request, Response response) {
        synchronized (PeerDiscoveryHandler.class) {
            if (response.getStatus() == Response.SUCCESS &&
                    response instanceof DiscoveryResponse) {
                DiscoveryResponse dResponse = (DiscoveryResponse) response;
                for (RoutingEntry discoveredEntry : dResponse.getDiscoveredPeers()) {
                    boolean entryInRoutingTable = false;
                    for (RoutingEntry routingEntry : routingTable) {
                        if (routingEntry.getPeerIP().equals(discoveredEntry.getPeerIP()) &&
                                routingEntry.getPeerPort() == discoveredEntry.getPeerPort()) {
                            entryInRoutingTable = true;

                        }
                    }
                    if (!entryInRoutingTable) {
                        this.routingTable.add(discoveredEntry);
                    }
                }
                logger.info("Got Response for peer discovery request.");
                logger.info("Response:" + response.toString());
                logger.info("Routing Table Status: ");
                logger.info(routingTable.toString());
            }
        }
    }

    @Override
    public void handle(Request request) {
        DiscoveryResponse response = new DiscoveryResponse(this.ownHost, this.ownPort,
                request.getSource(), request.getSourcePort());
        response.setStatus(Response.SUCCESS);
        response.setType(MessageType.DISCOVERY);
        ArrayList<RoutingEntry> discoveredPeersList = new ArrayList<>();
        synchronized (PeerDiscoveryHandler.class) {
            for (int i = 0; i < routingTable.size(); i++) {
                if (discoveredPeersList.size() >= ((DiscoveryRequest) request).getRequestedPeerCount()) {
                    break;
                }
                if (routingTable.get(i).getStatus() == RoutingEntry.Status.ONLINE) {
                    discoveredPeersList.add(routingTable.get(i).clone());
                }
            }
        }
        logger.info("Sending Response for Request");
        logger.info(request.toString());
        logger.info(response.toString());
        response.setDiscoveredPeers(discoveredPeersList);
        scheduler.schedule(response);


    }
}
