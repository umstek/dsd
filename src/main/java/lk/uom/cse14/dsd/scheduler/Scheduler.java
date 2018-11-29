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
            log.info("Response type : {}", message.getType());
            log.info("Response sent to: {}", message.getDestination());
            return;
        }
        log.info("Request type : {}", message.getType());
        MessageTracker messageTracker = new MessageTracker(message);
        messageTrackerMap.put(messageTracker.getUuid(), messageTracker);
        udpSender.sendMessage(message);
        messageTracker.setStatus(Status.SENT);
        log.info("Request sent to: {}", message.getDestination());
        switch (message.getType()){
            case HEARTBEAT:
                log.info("HEARTBEAT Request");
                MessageHandler messageHandlerH = new MessageHandler(messageTracker, udpSender,this.heartbeatHandler);
                this.executorService.submit(messageHandlerH);
                break;

            case QUERY:
                log.info("QUERY Request");
                MessageHandler messageHandlerQ = new MessageHandler(messageTracker, udpSender,this.queryHandler);
                this.executorService.submit(messageHandlerQ);
                break;

            case DISCOVERY:
                log.info("DISCOVERY Request");
                MessageHandler messageHandlerD = new MessageHandler(messageTracker, udpSender,this.peerDiscoveryHandler);
                this.executorService.submit(messageHandlerD);
                break;

        }

    }

    @Override
    public void run() {
        while (true) {
            log.info("Scheduler Up");
            try {
                boolean flag = false;
                synchronized (MessageTracker.class){
                    Message receivedMessage = udpReceiver.getMessage();
                    if (receivedMessage == null) {
                        log.info("Empty udpReceiver");
                        flag = true;
                    } else {
                        log.info("Message Found: {}", receivedMessage.getUuid());
                        MessageType receivedMessageType = receivedMessage.getType();
                        if (isItMyMessage(receivedMessage)) {
                            log.info("Response to my message, uuid: {}", receivedMessage.getUuid());
                            MessageTracker messageTracker = messageTrackerMap.get(receivedMessage.getUuid());
                            Message myMessage = null;
                            //synchronized (MessageTracker.class) {
                            myMessage = messageTracker.getMessage();
                            messageTracker.setStatus(Status.RESPONSED);
                            log.info("Status changed to Request: {}", myMessage.getUuid());
                            //}
                            if (myMessage != null) {
                                Request myRequest = (Request) myMessage;
                                log.info("Retrieved Matching Request");
                                Response receivedResponse = (Response) receivedMessage;
                                log.info("Passing to handleResponseMessage");
                                handleResponseMessage(myRequest, receivedResponse, receivedMessageType);
                            }
                        } else {
                            Request receivedRequest = (Request) receivedMessage;
                            handleRequestMessage(receivedRequest, receivedMessageType);
                            log.info("Passing to handleRequestMessage");
                        }
                    }
                    removeDeadTrackers();
                }
                log.info("Removed Dead Trackers");
                if (flag) {
                    log.info("Scheduler Sleeping for 1 Second");
                    Thread.sleep(1000);
                    log.info("Scheduler Woke Up");
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
                break;

            case DISCOVERY:
                log.info("DISCOVERY Request");
                peerDiscoveryHandler.handle(request);
                break;
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
                break;

            case DISCOVERY:
                log.info("DISCOVERY Response");
                peerDiscoveryHandler.handle(request, response);
                break;
        }
    }

    public void removeDeadTrackers() {
        for (Long k : messageTrackerMap.keySet()) {
            if (messageTrackerMap.get(k).getStatus() == Status.DEAD) {
                messageTrackerMap.remove(k);
            }
        }
    }


}
