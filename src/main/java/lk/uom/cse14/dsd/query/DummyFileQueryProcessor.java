package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public class DummyFileQueryProcessor implements IFileQuery {
    @Override
    public QueryResultSet query(String query) {
        System.out.println("File Query");
        return null;
    }
}
