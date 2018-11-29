package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.msghandler.RoutingEntry;
import lk.uom.cse14.dsd.ui.QueryTaskListener;

public class QueryTask implements Runnable {
    private String query;
    private boolean skipCache;
    private QueryResultSet queryResult = null;
    private int retryCount = 10;
    private QueryTaskListener listener;

    public QueryTask(QueryTaskListener listener, String query, boolean skipCache) {
        this.listener = listener;
        this.query = query;
        this.skipCache = skipCache;
    }

    @Override
    public void run() {
        int count = 0;
        while (count < retryCount) {
            if (queryResult != null) {
                break;
            } else {
                try {
                    System.out.println(" ");
                    Thread.sleep(1500);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        listener.notifyQueryComplete(this);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public QueryResultSet getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(QueryResultSet queryResult) {
        this.queryResult = queryResult;
    }
}
