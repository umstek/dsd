package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.msghandler.IHandler;
import org.apache.log4j.Logger;

public class MessageHandler extends Thread {

    private final Logger log = Logger.getLogger(MessageHandler.class);
    private boolean active;
    private UdpSender udpSender;
    private IHandler handler;
    private long uuid;
    private Message message;
    private volatile Status status;
    private int retryCount = 0;

    public MessageHandler(UdpSender udpSender,
                          IHandler handler) {
        active = true;
        this.udpSender = udpSender;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (active) {
            try {
                boolean flag = false;
                if (this.getRetryCount() < 3) {
                    if (this.getStatus() == Status.SENT) {
                        udpSender.sendMessage(message);
                        log.info("{} Message Resent to: {}");
                        log.info("Handler sleeping uuid: {}");
                    } else if (this.getStatus() == Status.RESPONSED) {
                        this.setStatus(Status.DEAD);
                        active = false;
                    }
                    this.setStatus(Status.SENT);
                    this.incrementRetryCount();
                    Thread.sleep(5000);
                } else {
                    this.setStatus(Status.DEAD);
                    this.handler.handle((Request) this.getMessage(), null);
                    log.info("Retry count exceeded. Status set to DEAD, uuid: {}");
                    active = false;
                }
            } catch (InterruptedException e) {
                log.info("Woke up thread from interrupt. Tracker ID: " + this.getUuid());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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

    public void incrementRetryCount() {
        retryCount++;
    }
}
