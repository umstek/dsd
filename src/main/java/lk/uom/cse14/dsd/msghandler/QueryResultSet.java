package lk.uom.cse14.dsd.msghandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryResultSet implements Serializable {
    /**
     * This class represents the result of a query.
     */

//    private RoutingEntry routingEntry;
//    private ArrayList<String> fileNames;
    private HashMap<String, ArrayList<RoutingEntry>> results;

    public QueryResultSet() {
        this.results = new HashMap<>();
    }

    public HashMap<String, ArrayList<RoutingEntry>> getResults() {
        return results;
    }

//    public QueryResultSet() {
//        this.fileNames = new ArrayList<>();
//    }

    public void setResults(HashMap<String, ArrayList<RoutingEntry>> results) {
        this.results = results;
    }

    public ArrayList<RoutingEntry> getRoutingEntries(String file) {
        return results.get(file);
    }

    public ArrayList<String> getFileNames() {
        return new ArrayList<>(this.results.keySet());
    }

    public void addEntry(String file, ArrayList<RoutingEntry> routingEntries) {
        this.results.put(file, routingEntries);
    }

}
