package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.discovery.RoutingEntry;

import java.util.ArrayList;

public class DiscoveryResponse extends Response {
    private ArrayList<RoutingEntry> discoveredPeers;

    public DiscoveryResponse(String source, int sourePort, String destination, int destinationPort) {
        super(source, sourePort, destination, destinationPort);
    }

    public ArrayList<RoutingEntry> getDiscoveredPeers() {
        return discoveredPeers;
    }

    public void setDiscoveredPeers(ArrayList<RoutingEntry> discoveredPeers) {
        this.discoveredPeers = discoveredPeers;
    }
}
