package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.message.HeartbeatRequest;
import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.comm.response.HeartbeatResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;

import java.util.ArrayList;

/**
 * Handles Heartbeat messages
 */
public class HeartbeatHandler implements IHandler, Runnable {
    private Scheduler scheduler;
    private String ownHost;
    private int ownPort;
    private ArrayList<RoutingEntry> routingEntries;

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

        if (response == null) {
            for (RoutingEntry routingEntry : routingEntries) {
                if (routingEntry.getPeerIP().equals(request.getDestination())
                        && routingEntry.getPeerPort() == request.getDestinationPort()) {
                    routingEntry.setStatus(RoutingEntry.Status.OFFLINE);
                    routingEntry.setRetryCount(routingEntry.getRetryCount() + 1);
                    break;
                }
            }
        } else {
            for (RoutingEntry routingEntry : routingEntries) {
                if (routingEntry.getPeerIP().equals(response.getSource())
                        && routingEntry.getPeerPort() == response.getSourcePort()) {
                    routingEntry.setStatus(RoutingEntry.Status.ONLINE);
                    routingEntry.setRetryCount(0);
                    break;
                }
            }
        }
    }

    /**
     * Handling someone else requesting the current peer to send a heartbeat
     *
     * @param request a request to send a heartbeat
     */
    @Override
    public void handle(Request request) {
        if (request == null) { // XXX
            System.out.println("Heartbeat handler received a null request. ");
            return;
        }

        HeartbeatRequest heartbeatRequest = (HeartbeatRequest) request;

        HeartbeatResponse heartbeatResponse = new HeartbeatResponse(
                ownHost, ownPort, heartbeatRequest.getSource(), heartbeatRequest.getSourcePort()
        );
        scheduler.schedule(heartbeatResponse);
    }

    @Override
    public void run() {
        while (true) {
            int entryCount = routingEntries.size();
            if (entryCount < 1) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace(); // XXX
                }
            } else {
                for (RoutingEntry routingEntry : routingEntries) {
                    try {
                        HeartbeatRequest heartbeatRequest = new HeartbeatRequest(
                                ownHost, ownPort, routingEntry.getPeerIP(), routingEntry.getPeerPort()
                        );

                        scheduler.schedule(heartbeatRequest);
                        Thread.sleep(10000 / entryCount);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
