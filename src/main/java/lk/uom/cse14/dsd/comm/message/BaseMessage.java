package lk.uom.cse14.dsd.comm.message;

import java.io.Serializable;

/*
 * Abstract Base form of a message
 * Message Type is used for identifying the correct type of message msghandler to be used
 * host and port fields are used to define the destination of the message
 * origin field is used to define the source of the message
 * */
public abstract class BaseMessage implements Serializable {

    private MessageType type;
    private String host;
    private int port;
    private String origin;
    public BaseMessage(String ownHost) {
        this.origin = ownHost;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getOrigin() {
        return origin;
    }

    public enum MessageType {
        HEARTBEAT, HEARTBEATRESP, QUERY, DISCOVERY
    }
}
