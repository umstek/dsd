package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.UdpReceiver;
import lk.uom.cse14.dsd.comm.UdpSender;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.msghandler.IHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler implements Runnable {
    private final Logger log = Logger.getLogger(Scheduler.class);
    ExecutorService executorService;
    private IHandler queryHandler;
    private IHandler heartbeatHandler;
    private IHandler peerDiscoveryHandler;
    private UdpReceiver udpReceiver;
    private UdpSender udpSender;
    private ConcurrentHashMap<Long, MessageHandler> messageHandlerMap;
    private IHandler downloadHandler;

    public Scheduler(UdpReceiver udpReceiver, UdpSender udpSender, ConcurrentHashMap<Long, MessageHandler> messageHandlerMap) {
        this.udpReceiver = udpReceiver;
        this.udpSender = udpSender;
        this.executorService = Executors.newFixedThreadPool(100);
        this.messageHandlerMap = messageHandlerMap;
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

    public void setDownloadHandler(IHandler downloadHandler) {
        this.downloadHandler = downloadHandler;
    }

    public void schedule(Message message) {
        if (message != null) {
            log.info("Inside Schedule");
            if (message instanceof Response) {
                udpSender.sendMessage(message);
                log.info("Response type : {}");
                log.info("Response sent to: {}");
                return;
            }
            log.info("Request type : {}");
            udpSender.sendMessage(message);
            //messageTracker.setStatus(Status.SENT);
            log.info("Request sent to: {}");
            switch (message.getType()) {
                case HEARTBEAT:
                    log.info("HEARTBEAT Request");
                    MessageHandler messageHandlerH = new MessageHandler(udpSender, this.heartbeatHandler);
                    log.info("Handler created");
                    messageHandlerH.setMessage(message);
                    messageHandlerH.setStatus(Status.SCHEDULED);
                    messageHandlerMap.put(message.getUuid(), messageHandlerH);
                    this.executorService.submit(messageHandlerH);
                    break;

                case QUERY:
                    log.info("QUERY Request");
                    MessageHandler messageHandlerQ = new MessageHandler(udpSender, this.queryHandler);
                    messageHandlerQ.setMessage(message);
                    messageHandlerQ.setStatus(Status.SCHEDULED);
                    messageHandlerMap.put(message.getUuid(), messageHandlerQ);
                    this.executorService.submit(messageHandlerQ);
                    break;

                case DISCOVERY:
                    log.info("DISCOVERY Request");
                    MessageHandler messageHandlerD = new MessageHandler(udpSender, this.peerDiscoveryHandler);
                    messageHandlerD.setMessage(message);
                    messageHandlerMap.put(message.getUuid(), messageHandlerD);
                    messageHandlerD.setStatus(Status.SCHEDULED);
                    this.executorService.submit(messageHandlerD);
                    break;

                case DOWNLOAD:
                    log.info("DOWNLOAD Request");
                    MessageHandler messageHandlerDo = new MessageHandler(udpSender, this.downloadHandler);
                    messageHandlerDo.setMessage(message);
                    messageHandlerMap.put(message.getUuid(), messageHandlerDo);
                    messageHandlerDo.setStatus(Status.SCHEDULED);
                    this.executorService.submit(messageHandlerDo);
                    break;

            }
        } else {
            log.info("Tried to schedule a null message");
        }

    }

    @Override
    public void run() {
        while (true) {
            log.info("Scheduler Up ->->->->->->->->->->->->->->->->->->->->->->->->->->->");
            try {
                boolean flag = false;
                synchronized (MessageTracker.class) {
                    Message receivedMessage = udpReceiver.getMessage();
                    if (receivedMessage == null) {
                        log.info("Empty udpReceiver");
                        flag = true;
                    } else {
                        log.info("Message Found: {}");
                        MessageType receivedMessageType = receivedMessage.getType();
                        if (isItMyMessage(receivedMessage)) {
                            log.info("Response to my message, uuid: {}");
                            MessageHandler messageHandler = messageHandlerMap.get(receivedMessage.getUuid());
                            Message myMessage = null;
                            //synchronized (MessageTracker.class) {
                            myMessage = messageHandler.getMessage();
                            messageHandler.setStatus(Status.RESPONSED);
                            messageHandler.interrupt();
                            log.info("Status changed to Request: {}");
                            //}
                            if (myMessage != null) {
                                Request myRequest = (Request) myMessage;
                                log.info("Retrieved Matching Request");
                                Response receivedResponse = (Response) receivedMessage;
                                log.info("Passing to handleResponseMessage");
                                handleResponseMessage(myRequest, receivedResponse, receivedMessageType);
                            }
                        } else {
                            log.info("Casting to RequestMessage");
                            if (receivedMessage instanceof Request) {
                                Request receivedRequest = (Request) receivedMessage;
                                log.info("Passing to handleRequestMessage");
                                handleRequestMessage(receivedRequest, receivedMessageType);
                                log.info("Passing to handleRequestMessage done");
                            } else {
                                log.info("Ignoring unknown request message");
                            }

                        }
                    }
                    removeDeadTrackers();
                }
                if (flag) {
                    log.info("Scheduler Sleeping for 1 Second");
                    Thread.sleep(1);
                    log.info("Scheduler Woke Up");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isItMyMessage(Message message) {
        long uuid = message.getUuid();
        if (uuid != 0) {
            return messageHandlerMap.containsKey(uuid);
        }
        return false;
    }

    public void handleRequestMessage(Request request, MessageType messageType) {
        if (request != null) {
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

                case DOWNLOAD:
                    log.info("DOWNLOAD Request");
                    downloadHandler.handle(request);
                    break;


            }
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

            case DOWNLOAD:
                log.info("DOWNLOAD Request");
                downloadHandler.handle(request, response);
                break;
        }
    }

    public void removeDeadTrackers() {
        for (Long k : messageHandlerMap.keySet()) {
            if (messageHandlerMap.get(k).getStatus() == Status.DEAD) {
                log.info("Removing Dead Tracker: {}");
                messageHandlerMap.remove(k);
            }
        }
    }


}
