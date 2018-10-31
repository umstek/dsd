package lk.uom.cse14.dsd.comm.message;

/*
 * Query message is used for searching for files in neighbour peers
 * */
public class QueryMessage extends BaseMessage {
    private String query;

    public QueryMessage(String ownHost) {
        super(ownHost);
    }
}
