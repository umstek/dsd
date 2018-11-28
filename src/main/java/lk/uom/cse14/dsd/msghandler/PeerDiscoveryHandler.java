package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.message.DiscoveryRequest;
import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.comm.response.DiscoveryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.logging.Logger;

public class PeerDiscoveryHandler implements Runnable, IHandler {
    private final ArrayList<RoutingEntry> routingTable;
    private Logger logger = Logger.getLogger(this.getClass().toString());
    private Scheduler scheduler;
    private int peerLimit = 10;
    private boolean running = true;
    private String ownHost;
    private int ownPort;

    public PeerDiscoveryHandler(ArrayList<RoutingEntry> routingTable, String ownHost, int ownPort, Scheduler scheduler){
        this.routingTable = routingTable;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
        this.scheduler = scheduler;
    }

    public void init(){
        // todo: communicate with BS server and populate first two entries of the table
    }

    @Override
    public void run() {
        while(running){
            if(routingTable.size() < peerLimit - 3 && !routingTable.isEmpty()){
                logger.info("Trying to find neighbours. Routing table size:"+routingTable.size());
                RoutingEntry entry = routingTable.get((int)(Math.random()*100)%routingTable.size());
                if(entry.getStatus().equals(RoutingEntry.Status.ONLINE)){
                    DiscoveryRequest request = new DiscoveryRequest(this.ownHost,this.ownPort,entry.getPeerIP(),entry.getPeerPort());
                    request.setType(MessageType.DISCOVERY);
                    request.setRequestedPeerCount(2);
                    logger.info("Sending discovery message to:"+request.getDestination()+":"+request.getDestinationPort());
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
    public synchronized void handle(Request request, Response response) {
        if(response.getStatus() == Response.SUCCESS &&
                response instanceof DiscoveryResponse){
            DiscoveryResponse dResponse = (DiscoveryResponse)response;
            for (RoutingEntry discoveredEntry:dResponse.getDiscoveredPeers()) {
                boolean entryInRoutingTable = false;
                for (RoutingEntry routingEntry:routingTable) {
                    if(routingEntry.getPeerIP().equals(discoveredEntry.getPeerIP()) &&
                            routingEntry.getPeerPort() == discoveredEntry.getPeerPort()){
                        entryInRoutingTable = true;

                    }
                }
                if(!entryInRoutingTable){
                    this.routingTable.add(discoveredEntry);
                }
            }
            logger.info("Got Response for peer discovery message.");
            logger.info("Response:"+response.toString());
            logger.info("Routing Table Status: ");
            logger.info(routingTable.toString());
        }
    }

    @Override
    public void handle(Request request) {
        DiscoveryResponse response = new DiscoveryResponse(this.ownHost,this.ownPort,
                request.getSource(), request.getSourcePort());
        response.setStatus(Response.SUCCESS);
        response.setType(MessageType.DISCOVERY);
        ArrayList<RoutingEntry> discoveredPeersList = new ArrayList<>();
        synchronized (routingTable){
            for(int i = 0;i<routingTable.size();i++){
                if(discoveredPeersList.size()>=((DiscoveryRequest)request).getRequestedPeerCount()){
                    break;
                }
                if(routingTable.get(i).getStatus() == RoutingEntry.Status.ONLINE){
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
