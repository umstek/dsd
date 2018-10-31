package lk.uom.cse14.dsd.comm;

import lk.uom.cse14.dsd.comm.message.BaseMessage;
import lk.uom.cse14.dsd.util.MessageUtils;

import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * UdpSender polls the senderQueue from time to time and sends the messages in the queue
 * to the defined address through UDP.
 * In case of failure, it pushes the failed message into the end of the queue
 * in order to retry sending it later.
 * todo: Use retryCount and limit the number of times a particular message is retried to send
 * */
public class UdpSender implements Runnable {
    private int sleepTime;
    private int retryCount;
    private boolean running = true;
    private ConcurrentLinkedQueue<BaseMessage> senderQueue;
    private DatagramSocket socket;

    public UdpSender(int maxSleepTime, int maxRetryCount, DatagramSocket socket) {
        this.sleepTime = maxSleepTime;
        this.retryCount = maxRetryCount;
        this.socket = socket;
        senderQueue = new ConcurrentLinkedQueue<>();
    }

    public void sendMessage(BaseMessage message) {
        senderQueue.add(message);
    }


    @Override
    public void run() {
        while (running) {
            if (senderQueue.isEmpty()) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            BaseMessage message = senderQueue.poll();
            try {
                byte[] messageBytes = MessageUtils.serializeMessage(message);
                MessageUtils.sendUdpMessage(socket, messageBytes, message.getHost(), message.getPort());
            } catch (Exception e) {
                e.printStackTrace();
                senderQueue.add(message);
            }
        }

    }
}
