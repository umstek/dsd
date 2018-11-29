package lk.uom.cse14.dsd.comm.response;

import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public class QueryResponse extends Response {
    private QueryResultSet queryResultSet;

    public QueryResponse(String source, int sourcePort, String destination, int destinationPort) {
        super(source, sourcePort, destination, destinationPort);
        this.setType(MessageType.QUERY);
    }

    public QueryResultSet getQueryResultSet() {
        return this.queryResultSet;
    }

    public void setQueryResultSet(QueryResultSet queryResultSet) {
        this.queryResultSet = queryResultSet;
    }
}
