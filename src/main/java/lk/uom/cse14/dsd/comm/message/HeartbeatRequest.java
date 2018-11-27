package lk.uom.cse14.dsd.comm.message;

import lk.uom.cse14.dsd.comm.MessageType;

/*
 * Heartbeat message is used for periodical heartbeats to check if neighbours are alive
 * */
public class HeartbeatRequest extends Request {
    public HeartbeatRequest(String ownHost) {
        super(ownHost);
    }

    public static HeartbeatRequest newHeartbeatMessage(MessageType messageType, String ownHost, String host, int port) {
        HeartbeatRequest heartbeatMessage = new HeartbeatRequest(ownHost);
        heartbeatMessage.setHost(host);
        heartbeatMessage.setType(messageType);
        heartbeatMessage.setPort(port);
        return heartbeatMessage;
    }
}
