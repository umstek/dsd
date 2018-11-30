package lk.uom.cse14.dsd.comm.request;

public class DownloadRequest extends Request {

    String filename;
    String hostIP;
    int hostPort;

    public DownloadRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }

}
