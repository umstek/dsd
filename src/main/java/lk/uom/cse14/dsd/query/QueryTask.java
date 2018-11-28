package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.ui.QueryTaskListener;

public class QueryTask implements Runnable{
    private String query;
    private boolean skipCache;
    private RoutingEntry queryResult = null;
    private int retryCount = 50;
    private QueryTaskListener listener;

    public QueryTask(QueryTaskListener listener,String query,boolean skipCache){
        this.listener = listener;
        this.query = query;
        this.skipCache = skipCache;
    }

    @Override
    public void run() {
        int count = 0;
        while (count<retryCount){
            if(queryResult != null){
                //listener.notifyQueryComplete(this);
                break;
            } else {
                try {
                    Thread.sleep(1500);
                    retryCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(queryResult == null){
            listener.notifyQueryComplete(this);
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public RoutingEntry getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(RoutingEntry queryResult) {
        this.queryResult = queryResult;
    }

    public boolean isSkipCache() {
        return skipCache;
    }

    public void setSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
    }

    public QueryTaskListener getListener() {
        return listener;
    }
}
