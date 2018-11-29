package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.comm.Message;
import lk.uom.cse14.dsd.comm.MessageKind;

public abstract class Response extends Message {
    public static final int SUCCESS = 1;
    public static final int FAIL = 0;
    private int status;

    public Response(String source, int sourePort, String destination, int destinationPort) {
        super(source, sourePort, destination, destinationPort);
        this.setDirection(MessageKind.RESPONSE);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
