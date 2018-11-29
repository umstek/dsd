package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.request.QueryRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.QueryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.query.ICacheQuery;
import lk.uom.cse14.dsd.query.IFileQuery;
import lk.uom.cse14.dsd.query.QueryTask;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import lk.uom.cse14.dsd.ui.QueryTaskListener;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueryHandler implements IHandler {
    private final Logger logger = Logger.getLogger(QueryHandler.class);
    private ArrayList<RoutingEntry> routingTable;
    private Scheduler scheduler;
    private ICacheQuery cacheQueryProcessor;
    private IFileQuery fileQueryProcessor;
    private String ownHost;
    private ExecutorService executorService;
    private HashMap<String, QueryTask> queryTasks;
    private int ownPort;
    private int maxHopCount = 15;

    public QueryHandler(ArrayList<RoutingEntry> routingTable, Scheduler scheduler, ICacheQuery cacheQueryProcessor,
                        IFileQuery fileQueryProcessor, String ownHost, int ownPort) {
        this.routingTable = routingTable;
        this.scheduler = scheduler;
        this.cacheQueryProcessor = cacheQueryProcessor;
        this.fileQueryProcessor = fileQueryProcessor;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
        this.executorService = Executors.newFixedThreadPool(10);
        this.queryTasks = new HashMap<>();
    }

    @Override
    public void handle(Request request, Response response) {
        QueryRequest queryRequest = (QueryRequest) request;
        QueryResponse queryResponse = null;
        if(response != null){
            queryResponse  = (QueryResponse) response;
        }
        if (queryResponse != null) { // if successful response, update cache
            cacheQueryProcessor.updateCache(queryResponse.getQueryResultSet());
        }
        if (this.ownHost.equals(queryRequest.getRequesterHost()) && // originated from this Host/Port, no redirection
                this.ownPort == queryRequest.getGetRequesterPort()) {
            // UI.show result of notify file downloads handler
            QueryTask qt = this.queryTasks.get(queryRequest.getRequestID());
            if(qt != null && queryResponse != null){
                qt.setQueryResult(queryResponse.getQueryResultSet());
            }
        } else { // originated from somewhere else. should redirect to the requester
            QueryResponse response1 = new QueryResponse(ownHost, ownPort, request.getSource(), request.getSourcePort());
            response1.setUuid(request.getUuid());
            response1.setStatus(Response.FAIL);
            scheduler.schedule(queryResponse);
        }
    }

    @Override
    public void handle(Request request) {
        QueryRequest queryRequest = (QueryRequest) request;
        if (queryRequest.getHopCount() > maxHopCount) {
            QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(), queryRequest.getSourcePort());
            response.setStatus(QueryResponse.FAIL);
            scheduler.schedule(response);
            return;
        }
        QueryResultSet result = fileQueryProcessor.query(queryRequest.getQuery()); // check query in local files
        if (result == null && !queryRequest.isSkipCache()) { // check query in local cache if cache is not skipped
            result = cacheQueryProcessor.query(queryRequest.getQuery());
        }
        if (result != null && this.ownHost.equals(queryRequest.getRequesterHost()) && // Result found. Request originated from this Host/Port
                this.ownPort == queryRequest.getGetRequesterPort()) {
            QueryTask qt = this.queryTasks.get(queryRequest.getRequestID());
            if(qt != null){
                qt.setQueryResult(result);
            }
        } else if (result != null && !this.ownHost.equals(queryRequest.getRequesterHost()) && // Result found, but originated from another Host/Port
                this.ownPort != queryRequest.getGetRequesterPort()) {
            QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(),
                    queryRequest.getSourcePort());
            response.setStatus(QueryResponse.SUCCESS);
            response.setQueryResultSet(result);
            response.setUuid(request.getUuid());
            scheduler.schedule(response);
        } else if (result == null) { // result not found in own files or cache. Try to redirect to a random neighbour
            RoutingEntry destinationEntry = null;
            int count = 0;
            while (count < 50) {  // find a random neighbour who is online
                RoutingEntry tempEntry = routingTable.get((int) (Math.random() * 100) % routingTable.size());
                if (tempEntry.getStatus() == RoutingEntry.Status.ONLINE) {
                    destinationEntry = tempEntry;
                    break;
                }
                count++;
            }
            if (destinationEntry == null) { // cant find a neighbour OR hop count exceeded
                QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(), queryRequest.getSourcePort());
                response.setStatus(QueryResponse.FAIL);
                scheduler.schedule(response);
            } else { // neighbour is found AND can redirect query to the neighbour
                request.redirectRequest(ownHost, ownPort, destinationEntry.getPeerIP(), destinationEntry.getPeerPort());
                scheduler.schedule(request);
            }
        } else {
            logger.info("Unsupported Request");
        }
    }

    public void submitQuery(QueryTask queryTask) {
        QueryRequest request = new QueryRequest(ownHost, ownPort, "", 0, queryTask.getQuery());
        request.setGetRequesterPort(ownPort);
        request.setRequesterHost(ownHost);
        request.setSkipCache(queryTask.isSkipCache());
        String uuid = UUID.randomUUID().toString();
        request.setRequestID(uuid);
        queryTasks.put(uuid, queryTask);
        executorService.submit(queryTask);
        handle(request);
    }


}
