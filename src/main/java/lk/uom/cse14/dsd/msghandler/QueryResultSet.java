package lk.uom.cse14.dsd.msghandler;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryResultSet {
    private RoutingEntry routingEntry;
    private ArrayList<String> fileNames;
    private HashMap<String, ArrayList<RoutingEntry>> cacheResult;

    public HashMap<String, ArrayList<RoutingEntry>> getCacheResult() {
        return cacheResult;
    }

    public void setCacheResult(HashMap<String, ArrayList<RoutingEntry>> cacheResult) {
        this.cacheResult = cacheResult;
    }

    public QueryResultSet() {
        this.fileNames = new ArrayList<>();
    }

    public RoutingEntry getRoutingEntry() {
        return routingEntry;
    }

    public void setRoutingEntry(RoutingEntry routingEntry) {
        this.routingEntry = routingEntry;
    }

    public ArrayList<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(ArrayList<String> fileNames) {
        this.fileNames = fileNames;
    }
}
