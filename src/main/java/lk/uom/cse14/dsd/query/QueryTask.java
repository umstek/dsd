package lk.uom.cse14.dsd.query;

import lk.uom.cse14.dsd.msghandler.QueryResultSet;
import lk.uom.cse14.dsd.main.QueryTaskListener;

public class QueryTask implements Runnable {
    private String query;
    private boolean skipCache;
    private QueryResultSet queryResult = null;
    private int retryCount = 60000;
    private QueryTaskListener listener;
    private boolean isDone = false;
    private long startTime;
    private int hopCount;

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
                    //System.out.println(" ");
                    Thread.sleep(1);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(count>=retryCount){
            this.queryResult = new QueryResultSet();
        }
        if(!isDone){
            isDone = true;
            listener.notifyQueryComplete(this);
        }
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }
}
