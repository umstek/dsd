package lk.uom.cse14.dsd.comm;

public class Message {
    private MessageType type;
    private String host;
    private int port;
    private String origin;
    public Message(String ownHost) {
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
}
