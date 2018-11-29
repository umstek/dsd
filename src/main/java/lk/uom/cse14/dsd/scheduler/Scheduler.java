package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.message.Request;
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
        this.executorService = Executors.newFixedThreadPool(10);
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
            return;
        }

        MessageTracker messageTracker = new MessageTracker(message);
        messageTrackerMap.put(messageTracker.getUuid(), messageTracker);
        udpSender.sendMessage(message);
        messageTracker.setStatus(Status.SENT);
        MessageHandler messageHandler = new MessageHandler(messageTracker, udpSender);
        this.executorService.submit(messageHandler);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Scheduler.class) {
                try {
                    Message receivedMessage = udpReceiver.getMessage();
                    if (receivedMessage == null) {
                        Thread.sleep(1000);
                    } else {
                        MessageType receivedMessageType = receivedMessage.getType();
                        if (isItMyMessage(receivedMessage)) {
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                removeDeadTrackers();
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
                log.info("HEARTBEAT handler");
                heartbeatHandler.handle(request);
                break;

            case QUERY:
                log.info("QUERY handler");
                queryHandler.handle(request);

            case DISCOVERY:
                log.info("DISCOVERY handler");
                peerDiscoveryHandler.handle(request);
        }
    }

    public void handleResponseMessage(Request request, Response response, MessageType messageType) {
        switch (messageType) {
            case HEARTBEAT:
                log.info("HEARTBEAT request");
                heartbeatHandler.handle(request, response);
                break;

            case QUERY:
                log.info("QUERY request");
                queryHandler.handle(request, response);

            case DISCOVERY:
                log.info("QUERY request");
                peerDiscoveryHandler.handle(request, response);
        }
    }

    public void removeDeadTrackers() {
        for (MessageTracker m : messageTrackerMap.values()) {
            if (m.getStatus() == Status.DEAD) {
                //messageTrackerMap.remove(m);
            }
        }
    }


}
