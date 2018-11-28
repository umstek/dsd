package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.msghandler.IHandler;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.util.NetworkInterfaceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler implements Runnable {
    private IHandler queryHandler;
    private IHandler heartbeatHandler;
    private IHandler peerDiscoveryHandler;
    private UdpReceiver udpReceiver;
    private UdpSender udpSender;
    private Map<Long, MessageTracker> messageTrackerMap = new ConcurrentHashMap<>();
    private final Logger log = LoggerFactory.getLogger(Scheduler.class);
    ExecutorService executorService;

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
        MessageTracker messageTracker = new MessageTracker(message);
        messageTrackerMap.put(messageTracker.getUuid(), messageTracker);
        udpSender.sendMessage(message);
        messageTracker.setStatus(Status.SENT);
        MessageHandler messageHandler = new MessageHandler(messageTracker, udpSender);
        this.executorService.submit(messageHandler);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Message receivedMessage = udpReceiver.getMessage();
                if (receivedMessage == null) {
                    Thread.sleep(1000);
                }else {
                    MessageType receivedMessageType = receivedMessage.getType();
                    if(isItMyMessage(receivedMessage)) {
                        MessageTracker messageTracker = messageTrackerMap.get(receivedMessage.getUuid());
                        Message myMessage = null;
                        synchronized (messageTracker) {
                            myMessage = messageTracker.getMessage();
                            messageTracker.setStatus(Status.RESPONSED);
                        }
                        if(myMessage != null) {
                            Request myRequest = (Request) myMessage;
                            Response receivedResponse = (Response) receivedMessage;
                            handleResponseMessage(myRequest, receivedResponse, receivedMessageType);
                        }
                    }else {
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

    public boolean isItMyMessage(Message message) {
        long uuid = message.getUuid();
        if(uuid != 0) {
            if(messageTrackerMap.containsKey(uuid)) {
                return true;
            }
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
        for (MessageTracker m: messageTrackerMap.values()) {
            if(m.getStatus() == Status.DEAD) {
                messageTrackerMap.remove(m);
            }
        }
    }


}
