package lk.uom.cse14.dsd.comm.request;

/*
 * IFileQuery request is used for searching for files in neighbour peers
 * */
public class QueryRequest extends Request {
    private String query;
    private String requesterHost;
    private int getRequesterPort;
    private boolean skipCache;
    private String requestID;

    public QueryRequest(String source, int sourcePort, String destination, int destinationPort, String query) {
        super(source, sourcePort, destination, destinationPort);
        this.query = query;
    }


    public String getQuery() {
        return query;
    }

    public String getRequesterHost() {
        return requesterHost;
    }

    public void setRequesterHost(String requesterHost) {
        this.requesterHost = requesterHost;
    }

    public int getGetRequesterPort() {
        return getRequesterPort;
    }

    public void setGetRequesterPort(int getRequesterPort) {
        this.getRequesterPort = getRequesterPort;
    }

    public boolean isSkipCache() {
        return skipCache;
    }

    public void setSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
}
