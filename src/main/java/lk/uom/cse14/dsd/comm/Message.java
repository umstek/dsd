package lk.uom.cse14.dsd.comm;

public class Message {
    private MessageType type;
    private String source;
    private int sourcePort;
    private String destination;
    private int destinationPort;

    public Message(String source, int sourcePort, String destination, int destinationPort) {
        this.source = source;
        this.sourcePort = sourcePort;
        this.destination = destination;
        this.destinationPort = destinationPort;

    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public String getDestination() {
        return destination;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
}
