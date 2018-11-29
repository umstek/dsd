package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.UdpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandler implements Runnable {

    private final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private MessageTracker messageTracker;
    private boolean active;
    private UdpSender udpSender;

    public MessageHandler(MessageTracker messageTracker, UdpSender udpSender) {
        this.messageTracker = messageTracker;
        active = true;
        this.udpSender = udpSender;
    }

    @Override
    public void run() {
        while (active) {
            try {
                boolean flag = false;
                if (messageTracker.getRetryCount() < 5) {
                    if (messageTracker.getStatus() == Status.SENT) {
                        udpSender.sendMessage(messageTracker.getMessage());
                        messageTracker.incrementRetryCount();
                        log.info("Message Resent to: {}", messageTracker.getMessage().getDestination());
                        flag = true;
                    } else if (messageTracker.getStatus() == Status.RESPONSED) {
                        messageTracker.setStatus(Status.DEAD);
                        active = false;
                    }
                } else {
                    messageTracker.setStatus(Status.DEAD);
                    log.info("Retry count exceeded. Status set to DEAD");
                    active = false;
                }
                if(flag){
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
