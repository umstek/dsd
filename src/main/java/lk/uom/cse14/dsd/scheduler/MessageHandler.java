package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.msghandler.IHandler;
import org.apache.log4j.Logger;

public class MessageHandler implements Runnable {

    private final Logger log = Logger.getLogger(MessageHandler.class);
    private MessageTracker messageTracker;
    private boolean active;
    private UdpSender udpSender;
    private IHandler handler;

    public MessageHandler(MessageTracker messageTracker, UdpSender udpSender,
                          IHandler handler) {
        this.messageTracker = messageTracker;
        active = true;
        this.udpSender = udpSender;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (active) {
            try {
                boolean flag = false;
                if (messageTracker.getRetryCount() < 6) {
                    if (messageTracker.getStatus() == Status.SENT) {
                        udpSender.sendMessage(messageTracker.getMessage());
                        log.info("{} Message Resent to: {}");
                        log.info("Handler sleeping uuid: {}");
                    } else if (messageTracker.getStatus() == Status.RESPONSED) {
                        messageTracker.setStatus(Status.DEAD);
                        active = false;
                    }
                    messageTracker.setStatus(Status.SENT);
                    messageTracker.incrementRetryCount();
                    Thread.sleep(25000);
                } else {
                    messageTracker.setStatus(Status.DEAD);
                    this.handler.handle((Request) this.messageTracker.getMessage(),null);
                    log.info("Retry count exceeded. Status set to DEAD, uuid: {}");
                    active = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
