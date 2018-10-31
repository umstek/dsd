package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.comm.message.BaseMessage;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * MessageUtile provide the utility functions for sending and receiving messages through UDP
 * */
public class MessageUtils {
    public static byte[] serializeMessage(BaseMessage message) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] messageBytes;
        out = new ObjectOutputStream(bos);
        out.writeObject(message);
        out.flush();
        messageBytes = bos.toByteArray();
        return messageBytes;
    }

    public static BaseMessage deSerializeMessage(byte[] messageBytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(messageBytes);
        ObjectInput in;
        BaseMessage messageObject;
        in = new ObjectInputStream(bis);
        messageObject = (BaseMessage) in.readObject();
        in.close();
        return messageObject;
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
