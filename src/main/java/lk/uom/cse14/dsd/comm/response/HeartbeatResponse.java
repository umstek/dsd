package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.comm.MessageType;

public class HeartbeatResponse extends Response {
    public HeartbeatResponse(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
        this.setType(MessageType.HEARTBEAT);
    }
}
