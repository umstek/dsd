package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public class DummyCacheQueryProcessor implements ICacheQuery {


    @Override
    public QueryResultSet query(String query) {
        System.out.println("Cache Query");
        return null;
    }

    @Override
    public void updateCache(QueryResultSet resultSet, String query) {

    }
}
