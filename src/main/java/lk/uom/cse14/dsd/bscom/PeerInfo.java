package lk.uom.cse14.dsd.bscom;

public class PeerInfo {
    private String host;
    private int port;

    PeerInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return Host name/IP Address
     */
    public String getHost() {
        return host;
    }

    /**
     * @return Port number
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
