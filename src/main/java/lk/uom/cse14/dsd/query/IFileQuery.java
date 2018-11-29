package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;

public interface IFileQuery extends IQuery {

    QueryResultSet query(String query, String ownIP, int ownPort);
}
