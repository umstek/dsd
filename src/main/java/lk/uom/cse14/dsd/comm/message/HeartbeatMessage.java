package lk.uom.cse14.dsd.comm.message;

/*
 * Heartbeat message is used for periodical heartbeats to check if neighbours are alive
 * */
public class HeartbeatMessage extends BaseMessage {
    public HeartbeatMessage(String ownHost) {
        super(ownHost);
    }

    public static HeartbeatMessage newHeartbeatMessage(MessageType messageType, String ownHost, String host, int port) {
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage(ownHost);
        heartbeatMessage.setHost(host);
        heartbeatMessage.setType(messageType);
        heartbeatMessage.setPort(port);
        return heartbeatMessage;
    }
}
