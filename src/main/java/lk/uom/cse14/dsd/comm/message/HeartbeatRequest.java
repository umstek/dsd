package lk.uom.cse14.dsd.comm.message;

import lk.uom.cse14.dsd.comm.MessageType;

/*
 * Heartbeat message is used for periodical heartbeats to check if neighbours are alive
 * */
public class HeartbeatRequest extends Request {

    public HeartbeatRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }

}
