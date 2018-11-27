package lk.uom.cse14.dsd.discovery;

import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.message.DiscoveryRequest;
import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.comm.response.DiscoveryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.msghandler.IHandler;
import lk.uom.cse14.dsd.scheduler.Scheduler;

import java.util.ArrayList;

public class PeerDiscovery implements Runnable, IHandler {
    private final ArrayList<RoutingEntry> routingTable;
    private Scheduler scheduler;
    private int peerLimit = 10;
    private boolean running = true;
    private String ownHost;
    private int ownPort;

    public PeerDiscovery(ArrayList<RoutingEntry> routingTable,String ownHost,int ownPort, Scheduler scheduler){
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
                RoutingEntry entry = routingTable.get((int)(Math.random()*100)%routingTable.size());
                if(entry.getStatus().equals(RoutingEntry.Status.ONLINE)){
                    DiscoveryRequest request = new DiscoveryRequest(this.ownHost,this.ownPort,entry.getPeerIP(),entry.getPeerPort());
                    request.setType(MessageType.DISCOVERY);
                    request.setRequestedPeerCount(2);
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
            this.routingTable.addAll(dResponse.getDiscoveredPeers());
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
            for(int i = 0;i<((DiscoveryRequest)request).getRequestedPeerCount();i++){
                if(i < routingTable.size()){
                    discoveredPeersList.add(routingTable.get(i));
                }else{
                    break;
                }
            }
        }
        response.setDiscoveredPeers(discoveredPeersList);
        scheduler.schedule(request);


    }
}
