package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.UdpSender;

public class MessageHandler implements Runnable{

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
            try{
                synchronized (Scheduler.class){
                    if(messageTracker.getRetryCount() < 5) {
                        if(messageTracker.getStatus() == Status.SENT) {
                            udpSender.sendMessage(messageTracker.getMessage());
                            messageTracker.incrementRetryCount();
                            Thread.sleep(10000);
                        } else if(messageTracker.getStatus() == Status.RESPONSED) {
                            messageTracker.setStatus(Status.DEAD);
                            active = false;
                        }
                    } else {
                        System.out.println("No Response");
                        messageTracker.setStatus(Status.DEAD);
                        active = false;
                    }
                }


            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
