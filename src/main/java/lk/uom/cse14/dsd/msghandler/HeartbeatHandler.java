package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.request.HeartbeatRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.HeartbeatResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Handles Heartbeat messages
 */
public class HeartbeatHandler implements IHandler, Runnable {
    private Scheduler scheduler;
    private String ownHost;
    private int ownPort;
    private ArrayList<RoutingEntry> routingEntries;
    private final Logger log = Logger.getLogger(HeartbeatHandler.class);


    public HeartbeatHandler(String ownHost, int ownPort, Scheduler scheduler, ArrayList<RoutingEntry> routingEntries) {
        this.scheduler = scheduler;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
        this.routingEntries = routingEntries;
    }

    /**
     * Handling someone else's response for this peer's own heartbeat request
     *
     * @param request  The original request the current peer sent
     * @param response The response received from the remote peer
     */
    @Override
    public void handle(Request request, Response response) {
        // N.B.: Destination of the original request and the source of its reply response are the same and vice versa.
        try{
            synchronized (RoutingEntry.class){
                if (response == null) {
                    for (RoutingEntry routingEntry : routingEntries) {
                        if (routingEntry.getPeerIP().equals(request.getDestination())
                                && routingEntry.getPeerPort() == request.getDestinationPort()) {
                            log.info("RoutingEntry for "+routingEntry.getPeerIP()+","+routingEntry.getPeerPort()+" OFFLINE");
                            routingEntry.setStatus(RoutingEntry.Status.OFFLINE);
                            routingEntry.setRetryCount(routingEntry.getRetryCount() + 1);
                            if(routingEntry.getRetryCount()>4){
                                routingEntries.remove(routingEntry);
                            }
                            break;
                        }else{
                            log.info("Non Existing RoutingEntry was tried to remove");
                        }
                    }
                } else {
                    for (RoutingEntry routingEntry : routingEntries) {
                        if (routingEntry.getPeerIP().equals(response.getSource())
                                && routingEntry.getPeerPort() == response.getSourcePort()) {
                            log.info("RoutingEntry for {},{} ONLINE");
                            routingEntry.setStatus(RoutingEntry.Status.ONLINE);
                            routingEntry.setRetryCount(0);
                            break;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Handling someone else requesting the current peer to send a heartbeat
     *
     * @param request a request to send a heartbeat
     */
    @Override
    public void handle(Request request) {
        try{
            if (request == null) { // XXX
                System.out.println("Heartbeat handler received a null request. ");
                return;
            }

            HeartbeatRequest heartbeatRequest = (HeartbeatRequest) request;

            HeartbeatResponse heartbeatResponse = new HeartbeatResponse(
                    ownHost, ownPort, heartbeatRequest.getSource(), heartbeatRequest.getSourcePort()
            );
            heartbeatResponse.setUuid(heartbeatRequest.getUuid());
            scheduler.schedule(heartbeatResponse);

            /*
             * It is possible that someone who we don't have in our routing table sent us the request.
             * If that's the case, add a new record to the routing table.
             * We don't do this if we have 7+ peers.
             */
            if (routingEntries.size() >= 7) {
                return;
            }

            synchronized (RoutingEntry.class) {

                boolean peerExists = false;
                for (RoutingEntry routingEntry : routingEntries) {
                    if (routingEntry.getPeerIP().equals(heartbeatRequest.getSource())
                            && routingEntry.getPeerPort() == heartbeatRequest.getSourcePort()) {
                        peerExists = true;
                    }
                }

                if (!peerExists) {
                    RoutingEntry routingEntry = new RoutingEntry(
                            heartbeatRequest.getSource(),
                            heartbeatRequest.getSourcePort(),
                            RoutingEntry.Status.ONLINE,
                            0);
                    routingEntries.add(routingEntry);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (true) {
            boolean flag1 = false;
            boolean flag2 = false;
            int entryCount = 0;
            synchronized (RoutingEntry.class){
                try {
                    entryCount = routingEntries.size();
                    if (entryCount < 1) {
                        flag1 = true;
                    } else {
                        for (RoutingEntry routingEntry : routingEntries) {
                            HeartbeatRequest heartbeatRequest = new HeartbeatRequest(
                                    ownHost, ownPort, routingEntry.getPeerIP(), routingEntry.getPeerPort()
                            );
                            scheduler.schedule(heartbeatRequest);
                            flag2 = true;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(flag1){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(flag2){
                try {
                    Thread.sleep(10000 / entryCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
