package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.msghandler.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Scheduler.class);
    ExecutorService executorService;
    private IHandler queryHandler;
    private IHandler heartbeatHandler;
    private IHandler peerDiscoveryHandler;
    private UdpReceiver udpReceiver;
    private UdpSender udpSender;
    private Map<Long, MessageTracker> messageTrackerMap = new ConcurrentHashMap<>();

    public Scheduler(UdpReceiver udpReceiver, UdpSender udpSender) {
        this.udpReceiver = udpReceiver;
        this.udpSender = udpSender;
        this.executorService = Executors.newFixedThreadPool(70);
    }

    public void setQueryHandler(IHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public void setHeartbeatHandler(IHandler heartbeatHandler) {
        this.heartbeatHandler = heartbeatHandler;
    }

    public void setPeerDiscoveryHandler(IHandler peerDiscoveryHandler) {
        this.peerDiscoveryHandler = peerDiscoveryHandler;
    }

    public void schedule(Message message) {
        log.info("Inside Schedule");
        if (message instanceof Response) {
            udpSender.sendMessage(message);
            log.info("Response type : {}", message.getType() );
            log.info("Response sent to: {}", message.getDestination() );
            return;
        }
        log.info("Request type : {}", message.getType() );
        MessageTracker messageTracker = new MessageTracker(message);
        messageTrackerMap.put(messageTracker.getUuid(), messageTracker);
        udpSender.sendMessage(message);
        messageTracker.setStatus(Status.SENT);
        log.info("Request sent to: {}", message.getDestination() );
        MessageHandler messageHandler = new MessageHandler(messageTracker, udpSender);
        this.executorService.submit(messageHandler);
    }

    @Override
    public void run() {
        while (true) {
            try {
                boolean flag = false;
                Message receivedMessage = udpReceiver.getMessage();
                if (receivedMessage == null) {
                    log.info("Empty udpReceiver");
                    flag = true;
                } else {
                    MessageType receivedMessageType = receivedMessage.getType();
                    if (isItMyMessage(receivedMessage)) {
                        log.info("Response to my message, uuid: {}", receivedMessage.getUuid());
                        MessageTracker messageTracker = messageTrackerMap.get(receivedMessage.getUuid());
                        Message myMessage = null;
                        //synchronized (MessageTracker.class) {
                        myMessage = messageTracker.getMessage();
                        messageTracker.setStatus(Status.RESPONSED);
                        //}
                        if (myMessage != null) {
                            Request myRequest = (Request) myMessage;
                            Response receivedResponse = (Response) receivedMessage;
                            handleResponseMessage(myRequest, receivedResponse, receivedMessageType);
                        }
                    } else {
                        Request receivedRequest = (Request) receivedMessage;
                        handleRequestMessage(receivedRequest, receivedMessageType);
                    }
                }
                removeDeadTrackers();
                if(flag){
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isItMyMessage(Message message) {
        long uuid = message.getUuid();
        if (uuid != 0) {
            return messageTrackerMap.containsKey(uuid);
        }
        return false;
    }

    public void handleRequestMessage(Request request, MessageType messageType) {
        switch (messageType) {
            case HEARTBEAT:
                log.info("HEARTBEAT Request");
                heartbeatHandler.handle(request);
                break;

            case QUERY:
                log.info("QUERY Request");
                queryHandler.handle(request);

            case DISCOVERY:
                log.info("DISCOVERY Request");
                peerDiscoveryHandler.handle(request);
        }
    }

    public void handleResponseMessage(Request request, Response response, MessageType messageType) {
        switch (messageType) {
            case HEARTBEAT:
                log.info("HEARTBEAT Response");
                heartbeatHandler.handle(request, response);
                break;

            case QUERY:
                log.info("QUERY Response");
                queryHandler.handle(request, response);

            case DISCOVERY:
                log.info("QUERY Response");
                peerDiscoveryHandler.handle(request, response);
        }
    }

    public void removeDeadTrackers() {
        for (MessageTracker m : messageTrackerMap.values()) {
            if (m.getStatus() == Status.DEAD) {
                messageTrackerMap.remove(m);
            }
        }
    }


}
