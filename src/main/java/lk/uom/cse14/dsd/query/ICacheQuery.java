package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public interface ICacheQuery extends IQuery {
    void updateCache(QueryResultSet resultSet,String query);
}
