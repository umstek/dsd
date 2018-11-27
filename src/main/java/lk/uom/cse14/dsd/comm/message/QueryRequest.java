package lk.uom.cse14.dsd.comm.message;

/*
 * Query message is used for searching for files in neighbour peers
 * */
public class QueryRequest extends Request {
    private String query;

    public QueryRequest(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
    }
}
