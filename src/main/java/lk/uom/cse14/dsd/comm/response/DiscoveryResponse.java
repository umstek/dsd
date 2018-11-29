package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.msghandler.RoutingEntry;

import java.util.ArrayList;

public class DiscoveryResponse extends Response {
    private ArrayList<RoutingEntry> discoveredPeers;

    public DiscoveryResponse(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }

    public ArrayList<RoutingEntry> getDiscoveredPeers() {
        return discoveredPeers;
    }

    public void setDiscoveredPeers(ArrayList<RoutingEntry> discoveredPeers) {
        this.discoveredPeers = discoveredPeers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (RoutingEntry entry : discoveredPeers) {
            builder.append(entry.toString());
        }
        return super.toString() + builder.toString();
    }
}
