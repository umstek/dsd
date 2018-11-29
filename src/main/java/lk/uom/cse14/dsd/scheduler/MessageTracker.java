package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;

public class MessageTracker {

    private long uuid;
    private Message message;
    private Status status;
    private int retryCount = 0;


    public MessageTracker(Message message) {
        uuid = message.getUuid();
        this.message = message;
        this.status = Status.SCHEDULED;
    }

    public Status getStatus() {
        return status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public long getUuid() {
        return uuid;
    }

    public Message getMessage() {
        return message;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        retryCount++;
    }

    public void resetCount() {
        retryCount = 0;
    }
}
