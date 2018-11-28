package lk.uom.cse14.dsd.comm.response;

public class HeartbeatResponse extends Response {
    public HeartbeatResponse(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }
}
