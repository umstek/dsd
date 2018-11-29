package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryProcessor {

    /**
     * constructs the query result for local search results
     *
     * @param files list of files matching the query
     * @return QueryResultSet object
     */

    public static QueryResultSet constructFileQueryResult(ArrayList<String> files) {
        QueryResultSet queryResultSet = new QueryResultSet();
        queryResultSet.setFileNames(files);
        /**
         * todo: populate queryResultSet with node ip and port
         **/
        return queryResultSet;
    }

    /**
     * constructs the query result for cache hits
     *
     * @param peers list of peers containing the file
     * @return QueryResultSet object
     */
    public static QueryResultSet constructCacheQueryResult(HashMap<String, ArrayList<RoutingEntry>> peers) {
        QueryResultSet queryResultSet = new QueryResultSet();
        queryResultSet.setCacheResult(peers);
        return queryResultSet;
    }
}
