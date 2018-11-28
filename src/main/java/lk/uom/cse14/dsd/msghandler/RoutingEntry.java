package lk.uom.cse14.dsd.msghandler;

import static lk.uom.cse14.dsd.msghandler.RoutingEntry.Status.ONLINE;
import static lk.uom.cse14.dsd.msghandler.RoutingEntry.Status.UNKNOWN;

public class RoutingEntry implements Cloneable{
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
        ONLINE, OFFLINE, UNKNOWN
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Peer : "+this.peerIP+":"+peerPort+(this.status == ONLINE?"Online":"Offline")+" Retry Count"+retryCount+"\n");
        return super.toString();
    }

    @Override
    public RoutingEntry clone(){
        RoutingEntry entry = new RoutingEntry();
        entry.setPeerIP(this.peerIP);
        entry.setPeerPort(this.peerPort);
        entry.setStatus(UNKNOWN);
        entry.setRetryCount(0);
        return entry;
    }
}
