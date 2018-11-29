package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public class DummyCacheQueryProcessor implements ICacheQuery {
    @Override
    public void updateCache(QueryResultSet resultSet) {
        System.out.println("Update Cache");
    }

    @Override
    public QueryResultSet query(String query) {
        System.out.println("Cache Query");
        return null;
    }
}
