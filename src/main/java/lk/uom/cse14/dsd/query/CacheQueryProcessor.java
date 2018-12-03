package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.util.QueryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CacheQueryProcessor implements ICacheQuery {

    private volatile ConcurrentHashMap<String, ArrayList<RoutingEntry>> QueryCache = new ConcurrentHashMap<>();

    @Override
    public void updateCache(QueryResultSet resultSet,String query) {
        synchronized (CacheQueryProcessor.class){
            ArrayList<String> fileToUpdate = resultSet.getFileNames();

            for (String file : fileToUpdate) {
                ArrayList<RoutingEntry> entriesToUpdate = resultSet.getRoutingEntries(file);
                ArrayList<RoutingEntry> cachedFileEntries = QueryCache.get(file);

                for (RoutingEntry re : entriesToUpdate) {
                    String IP = re.getPeerIP();
                    if (cachedFileEntries != null) {
                        ArrayList<RoutingEntry> tempList = new ArrayList<>();
                        for (RoutingEntry cre : cachedFileEntries) {
                            String cachedIP = cre.getPeerIP();
                            if (cachedIP.equals(IP)) {
                                break;
                            } else {
                                tempList.add(re);
                            }
                        }
                        cachedFileEntries.addAll(tempList);
                    } else {
                        cachedFileEntries = entriesToUpdate;
                    }
                }
                QueryCache.put(file, cachedFileEntries);
            }
        }
    }

    @Override
    public synchronized QueryResultSet query(String query) {
        synchronized (CacheQueryProcessor.class){
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
        }
        //System.out.println("Cache miss");
        return null;
    }

}
