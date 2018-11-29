package lk.uom.cse14.dsd.comm;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private long uuid;
    private String source;
    private int sourcePort;
    private String destination;
    private int destinationPort;
    private int hopCount = 0;

    public Message() {

    }

    public Message(String source, int sourcePort, String destination, int destinationPort) {
        this.source = source;
        this.sourcePort = sourcePort;
        this.destination = destination;
        this.destinationPort = destinationPort;
        uuid = System.nanoTime();

    }

    public void redirectRequest(String source, int sourcePort, String destination, int destinationPort) {
        this.setSource(source);
        this.setSourcePort(sourcePort);
        this.setDestination(destination);
        this.setDestinationPort(destinationPort);
        this.hopCount++;
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

    public void setSource(String source) {
        this.source = source;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Message:\n");
        builder.append("From: " + this.getSource() + ":" + this.getSourcePort() + "\n");
        builder.append("To: " + this.getDestination() + ":" + this.getDestinationPort() + "\n");
        return builder.toString();
    }

    public int getHopCount() {
        return hopCount;
    }

    public long getUuid() {
        return uuid;
    }
}
