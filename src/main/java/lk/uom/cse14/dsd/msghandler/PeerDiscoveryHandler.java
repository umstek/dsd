package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.bscom.PeerInfo;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.request.DiscoveryRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.DiscoveryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.ArrayList;
import java.util.List;

public class PeerDiscoveryHandler implements Runnable, IHandler {
    private final ArrayList<RoutingEntry> routingTable;
    private final Logger logger = Logger.getLogger(PeerDiscoveryHandler.class);
    private Scheduler scheduler;
    private int peerLimit = 7;
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
        try {
            synchronized (RoutingEntry.class){
                ArrayList<PeerInfo> peers = new ArrayList<>();
                logger.info("Peers found from BS server SIZE:" + peers.size());
                for(PeerInfo peer:peersList){
                    if(!peer.getHost().equals(this.ownHost) || peer.getPort() != this.ownPort){
                        peers.add(peer);
                    }
                }
                routingTable.clear();
                if (peers.size() < 3) {
                    for (PeerInfo info : peers) {
                        RoutingEntry routingEntry = new RoutingEntry();
                        routingEntry.setPeerIP(info.getHost());
                        routingEntry.setPeerPort(info.getPort());
                        routingEntry.setStatus(RoutingEntry.Status.UNKNOWN);
                        routingEntry.setRetryCount(0);
                        routingTable.add(routingEntry);
                    }
                } else {
                    int random1 = ((int) (Math.random() * 100)) % peers.size();
                    int random2 = ((int) (Math.random() * 100)) % peers.size();
                    while (random1 == random2) {
                        random2 = ((int) (Math.random() * 100)) % peers.size();
                    }
                    PeerInfo info1 = peers.get(random1);
                    RoutingEntry routingEntry1 = new RoutingEntry();
                    routingEntry1.setPeerIP(info1.getHost());
                    routingEntry1.setPeerPort(info1.getPort());
                    routingEntry1.setStatus(RoutingEntry.Status.UNKNOWN);
                    routingEntry1.setRetryCount(0);
                    routingTable.add(routingEntry1);
                    PeerInfo info2 = peers.get(random2);
                    RoutingEntry routingEntry2 = new RoutingEntry();
                    routingEntry2.setPeerIP(info2.getHost());
                    routingEntry2.setPeerPort(info2.getPort());
                    routingEntry2.setStatus(RoutingEntry.Status.UNKNOWN);
                    routingEntry2.setRetryCount(0);
                    routingTable.add(routingEntry2);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            boolean flag1 = false;
            try {
                synchronized (RoutingEntry.class){
                    logger.info("Trying to find neighbours. Routing table size:" + routingTable.size());
                    if (routingTable.size() < peerLimit - 3 && !routingTable.isEmpty()) {
                        int randomEntryIndex = (int) (Math.random() * 100) % routingTable.size();
                        RoutingEntry entry = routingTable.get(randomEntryIndex);
                        if (entry.getStatus().equals(RoutingEntry.Status.ONLINE)) {
                            DiscoveryRequest request = new DiscoveryRequest(this.ownHost, this.ownPort, entry.getPeerIP(), entry.getPeerPort());
                            request.setType(MessageType.DISCOVERY);
                            request.setRequestedPeerCount(5);
                            logger.info("Sending discovery request to:" + request.getDestination() + ":" + request.getDestinationPort());
                            scheduler.schedule(request);
                            flag1 = true;

                        }
                    }
                }
                if(flag1){
                    Thread.sleep(5000);
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handle(Request request, Response response) {
        try{
            synchronized (RoutingEntry.class) {
                if (response != null &&
                        response instanceof DiscoveryResponse) {
                    DiscoveryResponse dResponse = (DiscoveryResponse) response;
                    for (RoutingEntry discoveredEntry : dResponse.getDiscoveredPeers()) {
                        if(routingTable.size() > peerLimit){
                            break;
                        }
                        boolean entryInRoutingTable = false;
                        for (RoutingEntry routingEntry : routingTable) {
                            if (routingEntry.getPeerIP().equals(discoveredEntry.getPeerIP()) &&
                                    routingEntry.getPeerPort() == discoveredEntry.getPeerPort()) {
                                entryInRoutingTable = true;

                            }
                        }
                        discoveredEntry.setStatus(RoutingEntry.Status.UNKNOWN);
                        if (!entryInRoutingTable) {
                            this.routingTable.add(discoveredEntry);
                        }
                    }
                    logger.info("Got Response for peer discovery request.");
                    //logger.info("Response:" + response.toString());
                    logger.info("Routing Table Status: ");
                    //logger.info(routingTable.toString());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void handle(Request request) {
        try {
            DiscoveryResponse response = new DiscoveryResponse(this.ownHost, this.ownPort,
                    request.getSource(), request.getSourcePort());
            response.setStatus(Response.SUCCESS);
            response.setType(MessageType.DISCOVERY);
            response.setUuid(request.getUuid());
            ArrayList<RoutingEntry> discoveredPeersList = new ArrayList<>();
            synchronized (RoutingEntry.class) {
                for (int i = 0; i < routingTable.size(); i++) {
                    if (discoveredPeersList.size() >= ((DiscoveryRequest) request).getRequestedPeerCount()) {
                        break;
                    }
                    RoutingEntry checkingPeer = routingTable.get(i);
                    if (checkingPeer.getStatus() == RoutingEntry.Status.ONLINE
                            && (!checkingPeer.getPeerIP().equals(request.getSource()) ||
                            checkingPeer.getPeerPort() != request.getSourcePort()) ) {
                        discoveredPeersList.add(checkingPeer.clone());
                    }
                }
            }
            logger.info("Sending Response for Request");
            //logger.info(request.toString());
            //logger.info(response.toString());
            response.setDiscoveredPeers(discoveredPeersList);
            scheduler.schedule(response);
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
