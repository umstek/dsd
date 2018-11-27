package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageType;

public abstract class Response extends Message {
    public Response(String ownHost) {
        super(ownHost);
    }
}
