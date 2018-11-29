package lk.uom.cse14.dsd.comm.request;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageKind;

/*
 * Abstract Base form of a request
 * Request Type is used for identifying the correct type of request msghandler to be used
 * host and port fields are used to define the destination of the request
 * origin field is used to define the source of the request
 * */
public abstract class Request extends Message {

    public Request(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
        this.setDirection(MessageKind.REQUEST);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
