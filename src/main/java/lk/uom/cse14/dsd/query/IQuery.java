package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public interface IQuery {
    QueryResultSet query(String query);
}
