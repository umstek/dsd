package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.comm.request.QueryRequest;

import java.io.IOException;
import java.util.ArrayList;

/**
 * QueryUtils provide the utility functions for searching
 */
public class QueryUtils {

    private static final String QUERY_LIST = "/config/Queries.txt";
    private static final String FILE_LIST = "/config/File Names.txt";
    private static ArrayList<String> hostedFiles;
    private static ArrayList<String> queries;

    /**
     * this method initializes the cache for searching and querying
     */
    public static void initializeCache() {
//        QueryUtils.hostedFiles = hostedFiles;
        try {
            QueryUtils.hostedFiles = TextFileUtils.readFileContent(FILE_LIST);
            QueryUtils.queries = TextFileUtils.readFileContent(QUERY_LIST);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(QUERY_LIST + " file is missing");
        }
    }

    /**
     * @param queryRequest the query request received from the overlay
     * @return a list of matched filenames for the query
     */
    public static ArrayList<String> runSearchQuery(QueryRequest queryRequest, boolean cache) {
        String query = queryRequest.getQuery();
        return runSearchQuery(query, cache);
    }

    /**
     * @param query the query string received from the overlay
     * @return a list of matched filenames for the query
     */
    public static ArrayList<String> runSearchQuery(String query, boolean cache) {
        if (cache) {
            return QueryUtils.search(query, QueryUtils.hostedFiles);
        } else {
            ArrayList<String> files = null;
            try {
                files = TextFileUtils.readFileContent(FILE_LIST);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return QueryUtils.search(query, files);
        }
    }

    /**
     * this method searches the given query in the given set of files
     *
     * @param query the query string
     * @return a list of matched filenames for the query
     */
    public static ArrayList<String> search(String query, ArrayList<String> files) {
        ArrayList<String> matches = new ArrayList<>();

        for (String filename : files) {
            String[] words = filename.split(" ");
            for (String word : words) {
                if (query.equalsIgnoreCase(word)) {
                    matches.add(filename);
                    break;
                }
            }
        }
        return matches;
    }


//    public static QueryRequest issueSearchQuery() {
//        int index = ThreadLocalRandom.current().nextInt(QueryUtils.queries.size());
//        String query = QueryUtils.queries.get(index);
//        QueryRequest qr = new QueryRequest();
//    }


}
