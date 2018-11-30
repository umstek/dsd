package lk.uom.cse14.dsd.comm.request;

import lk.uom.cse14.dsd.comm.MessageType;

public class DownloadRequest extends Request {

    String filename;
    String hostIP;
    int hostPort;

    public DownloadRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
        this.setType(MessageType.DOWNLOAD);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }


}
