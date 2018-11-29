package lk.uom.cse14.dsd.comm.message;

import lk.uom.cse14.dsd.comm.MessageType;

/**
 * Heartbeat message is used to test the liveliness of the neighboring peers.
 */
public class HeartbeatRequest extends Request {

    public HeartbeatRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
        this.setType(MessageType.HEARTBEAT);
    }

}
