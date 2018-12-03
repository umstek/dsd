package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.util.QueryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CacheQueryProcessor implements ICacheQuery {

    private HashMap<String, ArrayList<RoutingEntry>> QueryCache = new HashMap<>();

    @Override
    public void updateCache(QueryResultSet resultSet,String query) {
        ArrayList<String> fileToUpdate = resultSet.getFileNames();

        for (String file : fileToUpdate) {
            ArrayList<RoutingEntry> entriesToUpdate = resultSet.getRoutingEntries(file);
            ArrayList<RoutingEntry> cachedFileEntries = QueryCache.get(file);

            for (RoutingEntry re : entriesToUpdate) {
                String IP = re.getPeerIP();
                if (cachedFileEntries != null) {
                    for (RoutingEntry cre : cachedFileEntries) {
                        String cachedIP = cre.getPeerIP();
                        if (cachedIP.equals(IP)) {
                            break;
                        } else {
                            cachedFileEntries.add(re);
                        }
                    }
                } else {
                    cachedFileEntries = entriesToUpdate;
                }
            }
            QueryCache.put(file, cachedFileEntries);
        }

    }

    @Override
    public QueryResultSet query(String query) {
        ArrayList<String> cachedFiles = new ArrayList<>(QueryCache.keySet());
        ArrayList<String> cacheHitFiles = QueryUtils.search(query, cachedFiles);

        if (!cacheHitFiles.isEmpty()) {
            HashMap<String, ArrayList<RoutingEntry>> cacheHits = new HashMap<>();
            for (String file : cacheHitFiles) {
                ArrayList<RoutingEntry> peers = QueryCache.get(file);
                cacheHits.put(file, peers);
            }
            return QueryProcessor.constructCacheQueryResult(cacheHits);
        }
        //System.out.println("Cache miss");
        return null;
    }

}
