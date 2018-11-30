package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.comm.MessageType;

public class DownloadResponse extends Response {

    public DownloadResponse(String source, int sourePort, String destination, int destinationPort) {
        super(source, sourePort, destination, destinationPort);
        this.setType(MessageType.DOWNLOAD);
    }


}
