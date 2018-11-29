package lk.uom.cse14.dsd.comm.message;

import lk.uom.cse14.dsd.comm.Message;

/*
 * Abstract Base form of a message
 * Request Type is used for identifying the correct type of message msghandler to be used
 * host and port fields are used to define the destination of the message
 * origin field is used to define the source of the message
 * */
public abstract class Request extends Message {

    public Request(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
