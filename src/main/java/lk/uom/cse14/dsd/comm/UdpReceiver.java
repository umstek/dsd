package lk.uom.cse14.dsd.comm;

import lk.uom.cse14.dsd.util.MessageUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * UdpReceiver listens on the predefined UDP port. On a new UDP request, it receives the request
 * and de-serializes the request into Request form and pushes it to the receiverQueue.
 * In case of failure in receiving and/or de-serialization, it only logs the stack trace and
 * continues its operation as usual.
 * Users can check for new messages by polling the receiverQueue.
 * */
public class UdpReceiver implements Runnable {

    private boolean running = true;
    private ConcurrentLinkedQueue<Message> receiverQueue;
    private DatagramSocket socket;

    public UdpReceiver(DatagramSocket socket) {
        receiverQueue = new ConcurrentLinkedQueue<>();
        this.socket = socket;
    }

    public Message getMessage() {
        return receiverQueue.poll();
    }

    @Override
    public void run() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (running) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            byte[] receivedPacket = packet.getData();
            try {
                Message message = MessageUtils.deSerializeMessage(receivedPacket);
                receiverQueue.add(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
