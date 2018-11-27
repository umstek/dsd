package lk.uom.cse14.dsd.discovery;

public class RoutingEntry {
    private String peerIP;
    private int peerPort;
    private Status status;
    private int retryCount;

    public String getPeerIP() {
        return peerIP;
    }

    public void setPeerIP(String peerIP) {
        this.peerIP = peerIP;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public enum Status {
        ONLINE, OFFLINE
    }

}
