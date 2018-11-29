package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.util.QueryUtils;

import java.util.ArrayList;

public class FileQueryProcessor implements IFileQuery {

    @Override
    public QueryResultSet query(String query) {
        ArrayList<String> files = null;
        files = QueryUtils.runSearchQuery(query, true);
        if (files == null) {
            files = QueryUtils.runSearchQuery(query, false);
        }
        return QueryProcessor.constructFileQueryResult(files);
    }
}
