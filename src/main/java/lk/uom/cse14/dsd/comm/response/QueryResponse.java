package lk.uom.cse14.dsd.comm.response;

public class QueryResponse extends Response {
    public QueryResponse(String source, int sourePort, String destination, int destinationPort) {
        super(source, sourePort, destination, destinationPort);
    }
}
