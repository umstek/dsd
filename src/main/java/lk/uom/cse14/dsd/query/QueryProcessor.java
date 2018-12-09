package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class QueryProcessor {

    /**
     * constructs the query result for local search results
     *
     * @param files   list of files matching the query
     * @param ownIP
     * @param ownPort
     * @return QueryResultSet object
     */

    public static QueryResultSet constructFileQueryResult(ArrayList<String> files, String ownIP, int ownPort) {
        QueryResultSet queryResultSet = new QueryResultSet();
        HashMap<String, ArrayList<RoutingEntry>> results = new HashMap<>();

        RoutingEntry re = new RoutingEntry(ownIP, ownPort, RoutingEntry.Status.ONLINE, 0);
        for (String filename : files) {
            results.put(filename, new ArrayList<>(
                    Arrays.asList(re)));
        }
        queryResultSet.setResults(results);

        if (queryResultSet.getFileNames().isEmpty()) {
            return null;
        }
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
        queryResultSet.setResults(peers);
        if (queryResultSet.getFileNames().isEmpty()) {
            return null;
        }
        return queryResultSet;
    }
}
