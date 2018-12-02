package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.comm.request.QueryRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * QueryUtils provide the utility functions for searching
 */
public class QueryUtils {

    public static final String QUERY_LIST = "/Queries.txt";
    public static final String FILE_LIST = "/File Names.txt";

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

    public static void updateHostedFilesConfig() {
        try {
            QueryUtils.hostedFiles = TextFileUtils.readFileContent(FILE_LIST);
        } catch (IOException e) {
            e.printStackTrace();
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
        Set<String> matches = new HashSet<>();

        for (String filename : files) {
            //full match
            if (query.equalsIgnoreCase(filename)) {
                matches.add(filename);
                break;
            }

            int score = 0;
            //partial match
            String[] words = filename.split(" ");
            String[] queryTerms = query.split(" ");
            for (String queryTerm : queryTerms) {
                for (String word : words) {
                    if (queryTerm.equalsIgnoreCase(word)) {
                        matches.add(filename);
                        score++;
                    }
                }
            }
        }
        return new ArrayList<>(matches);
    }

    /**
     * this method randomly selects a query from the configuration file and return it
     *
     * @return a query string
     */
    public static String issueRandomSearchQuery() {
        int index = ThreadLocalRandom.current().nextInt(QueryUtils.queries.size());
        return QueryUtils.queries.get(index);
    }

    public static ArrayList<String> getHostedFiles() {
        return hostedFiles;
    }

    public static ArrayList<String> getQueries() {
        return queries;
    }


}
