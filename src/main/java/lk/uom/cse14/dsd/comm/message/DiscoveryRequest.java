package lk.uom.cse14.dsd.comm.message;

/*
 * Discovery Request is used for discovering new neighbours
 * */
public class DiscoveryRequest extends Request {
    private int requestedPeerCount;
    public DiscoveryRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }


    public int getRequestedPeerCount() {
        return requestedPeerCount;
    }

    public void setRequestedPeerCount(int requestedPeerCount) {
        this.requestedPeerCount = requestedPeerCount;
    }
}
