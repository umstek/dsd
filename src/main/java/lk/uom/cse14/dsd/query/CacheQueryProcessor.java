package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.util.SearchUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class CacheQueryProcessor implements ICacheQuery {

    private HashMap<String, ArrayList<RoutingEntry>> QueryCache = new HashMap<>();

    @Override
    public void updateCache(QueryResultSet resultSet) {
        ArrayList<String> files = resultSet.getFileNames();
        RoutingEntry entry = resultSet.getRoutingEntry();
        String IP = entry.getPeerIP();

        for (String file : files) {
            ArrayList<RoutingEntry> cacheEntries = QueryCache.get(file);
            if (cacheEntries != null) {
                for (RoutingEntry re : cacheEntries) {
                    String cachedIP = re.getPeerIP();
                    if (cachedIP.equals(IP)) {
                        break;
                    } else {
                        cacheEntries.add(entry);
                    }
                }
            } else {
                cacheEntries = new ArrayList<>();
            }
            QueryCache.put(file, cacheEntries);
        }

    }

    @Override
    public QueryResultSet query(String query) {
        ArrayList<String> cachedFiles = new ArrayList<>(QueryCache.keySet());
        ArrayList<String> cacheHitFiles = SearchUtils.search(query, cachedFiles);

        if (!cacheHitFiles.isEmpty()) {
            HashMap<String, ArrayList<RoutingEntry>> cacheHits = new HashMap<>();
            for (String file : cacheHitFiles) {
                ArrayList<RoutingEntry> peers = QueryCache.get(query);
                cacheHits.put(file, peers);
            }
            return QueryProcessor.constructCacheQueryResult(cacheHits);
        }
        System.out.println("Cache miss");
        return null;
    }

}
