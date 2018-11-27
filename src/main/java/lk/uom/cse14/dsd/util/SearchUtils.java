package lk.uom.cse14.dsd.util;

import lk.uom.cse14.dsd.comm.message.QueryRequest;
import lk.uom.cse14.dsd.fileio.TextFileHandler;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SearchUtils provide the utility functions for searching
 */
public class SearchUtils {

    private static final String QUERY_LIST = "/src/main/java/lk/uom/cse14/dsd/UTIL/Queries.txt";
    private static ArrayList<String> hostedFiles;
    private static ArrayList<String> queries;

    public static void initialize(ArrayList<String> hostedFiles) {
        SearchUtils.hostedFiles = hostedFiles;
        try {
            SearchUtils.queries = TextFileHandler.readFileContent(QUERY_LIST);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(QUERY_LIST + " file is missing");
        }
    }

    /**
     * @param queryRequest the query message received from the overlay
     * @return a list of matched filenames for the query
     */
    public static ArrayList<String> runSearchQuery(QueryRequest queryRequest) {
        ArrayList<String> matches = new ArrayList<>();

        String query = queryRequest.getQuery();

        for (String filename : hostedFiles) {
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

    public static QueryRequest issueSearchQuery() {
        return null;
    }


}
