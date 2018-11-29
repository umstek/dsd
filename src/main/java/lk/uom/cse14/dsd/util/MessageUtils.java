package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.comm.Message;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MessageUtils provide the utility functions for sending and receiving messages through UDP
 */
public class MessageUtils {
    public static byte[] serializeMessage(Message message) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] messageBytes;
        out = new ObjectOutputStream(bos);
        out.writeObject(message);
        out.flush();
        messageBytes = bos.toByteArray();
        return messageBytes;
    }

    public static Message deSerializeMessage(byte[] messageBytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(messageBytes);
        ObjectInput in;
        Message requestObject;
        in = new ObjectInputStream(bis);
        requestObject = (Message) in.readObject();
        in.close();
        return requestObject;
    }

    public static void sendUdpMessage(DatagramSocket socket, byte[] message, String host, int port) throws Exception {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace(); //todo: handle exceptions properly
            throw e;
        }
        DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
        socket.send(packet);
    }

}
