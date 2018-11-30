package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.MessageType;
import lk.uom.cse14.dsd.comm.request.QueryRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.QueryResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.query.ICacheQuery;
import lk.uom.cse14.dsd.query.IFileQuery;
import lk.uom.cse14.dsd.query.QueryTask;
import lk.uom.cse14.dsd.scheduler.Scheduler;
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
    private HashMap<Long,Request> oldRequestMap;

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
        this.oldRequestMap = new HashMap<>();
    }

    @Override
    public void handle(Request request, Response response) {
        try{
            QueryRequest queryRequest = (QueryRequest) request;
            QueryResponse queryResponse = null;
            if(response != null){
                queryResponse  = (QueryResponse) response;
            }
            if (queryResponse != null && queryResponse.getStatus() == Response.SUCCESS) { // if successful response, update cache
                cacheQueryProcessor.updateCache(queryResponse.getQueryResultSet(),queryRequest.getQuery());
            }

            if(queryResponse == null){
                if (this.ownHost.equals(queryRequest.getRequesterHost()) && // originated from this Host/Port, no redirection
                        this.ownPort == queryRequest.getGetRequesterPort()) {
                    // UI.show result of notify file downloads handler
                    QueryTask qt = this.queryTasks.get(queryRequest.getRequestID());
                    if(qt != null){
                        qt.setQueryResult(new QueryResultSet());
                    }
                } else { // originated from somewhere else. should redirect to the requester
                    Request oldRequest = oldRequestMap.get(request.getUuid());
                    if(oldRequest != null){
                        QueryResponse response1 = new QueryResponse(ownHost, ownPort, oldRequest.getSource(), oldRequest.getSourcePort());
                        response1.setUuid(oldRequest.getUuid());
                        response1.setStatus(Response.SUCCESS);
                        response1.setHopCount(oldRequest.getHopCount());
                        scheduler.schedule(response1);
                    }

                }
            }else {
                if (this.ownHost.equals(queryRequest.getRequesterHost()) && // originated from this Host/Port, no redirection
                        this.ownPort == queryRequest.getGetRequesterPort()) {
                    // UI.show result of notify file downloads handler
                    QueryTask qt = this.queryTasks.get(queryRequest.getRequestID());
                    if(qt != null){
                        qt.setQueryResult(queryResponse.getQueryResultSet());
                    }
                } else { // originated from somewhere else. should redirect to the requester
                    Request oldRequest = oldRequestMap.get(request.getUuid());
                    if(oldRequest != null){
                        QueryResponse response1 = new QueryResponse(ownHost, ownPort, oldRequest.getSource(), oldRequest.getSourcePort());
                        response1.setUuid(oldRequest.getUuid());
                        response1.setHopCount(oldRequest.getHopCount());
                        scheduler.schedule(response1);
                    }

                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void handle(Request request) {
        try{
            QueryRequest queryRequest = (QueryRequest) request;
            if (queryRequest.getHopCount() > maxHopCount) {
                QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(), queryRequest.getSourcePort());
                response.setStatus(QueryResponse.FAIL);
                response.setQueryResultSet(new QueryResultSet());
                response.setUuid(request.getUuid());
                response.setHopCount(request.getHopCount());
                scheduler.schedule(response);
                return;
            }
            QueryResultSet result = null;
            if(!(this.ownHost.equals(queryRequest.getRequesterHost()) && this.ownPort == queryRequest.getGetRequesterPort())){
                result = fileQueryProcessor.query(queryRequest.getQuery(), ownHost, ownPort);
            }
            if (result == null && !queryRequest.isSkipCache()) { // check query in local cache if cache is not skipped
                result = cacheQueryProcessor.query(queryRequest.getQuery());
            }
            if(result == null){
                RoutingEntry destinationEntry = null;
                int count = 0;
                synchronized (RoutingEntry.class){
                    while (count < 50) {  // find a random neighbour who is online
                        if(!routingTable.isEmpty()){
                            RoutingEntry tempEntry = routingTable.get((int) (Math.random() * 100) % routingTable.size());
                            if (tempEntry.getStatus() == RoutingEntry.Status.ONLINE &&
                                    !(tempEntry.getPeerIP().equals(request.getSource()) &&
                                            tempEntry.getPeerPort() == request.getSourcePort()) &&
                                    !(tempEntry.getPeerIP().equals(((QueryRequest) request).getRequesterHost()) &&
                                            tempEntry.getPeerPort() == ((QueryRequest) request).getGetRequesterPort())) {
                                destinationEntry = tempEntry;
                                break;
                            }
                        }
                        count++;
                    }
                    if (destinationEntry == null) { // cant find a neighbour OR hop count exceeded
                        QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(), queryRequest.getSourcePort());
                        response.setStatus(QueryResponse.FAIL);
                        response.setUuid(request.getUuid());
                        response.setHopCount(request.getHopCount());
                        scheduler.schedule(response);
                    } else { // neighbour is found AND can redirect query to the neighbour
                        QueryRequest request1 = new QueryRequest(ownHost, ownPort, destinationEntry.getPeerIP(), destinationEntry.getPeerPort(),
                                ((QueryRequest) request).getQuery());
                        request1.setRequesterHost(((QueryRequest) request).getRequesterHost());
                        request1.setGetRequesterPort(((QueryRequest) request).getGetRequesterPort());
                        request1.setRequestID(((QueryRequest) request).getRequestID());
                        //request1.setUuid(request.getUuid());
                        request1.setHopCount(request.getHopCount()+1);
                        request1.setType(MessageType.QUERY);
                        scheduler.schedule(request1);
                    }
                }
            }else{
                if (this.ownHost.equals(queryRequest.getRequesterHost()) &&
                        this.ownPort == queryRequest.getGetRequesterPort()) {
                    QueryTask qt = this.queryTasks.get(queryRequest.getRequestID());
                    if(qt != null){
                        qt.setQueryResult(result);
                    }
                } else {
                    QueryResponse response = new QueryResponse(ownHost, ownPort, queryRequest.getSource(),
                            queryRequest.getSourcePort());
                    response.setStatus(QueryResponse.SUCCESS);
                    response.setQueryResultSet(result);
                    response.setUuid(request.getUuid());
                    scheduler.schedule(response);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submitQuery(QueryTask queryTask) {
        QueryRequest request = new QueryRequest(ownHost, ownPort, "", 0, queryTask.getQuery());
        request.setGetRequesterPort(ownPort);
        request.setRequesterHost(ownHost);
        request.setSkipCache(queryTask.isSkipCache());
        request.setType(MessageType.QUERY);
        String uuid = UUID.randomUUID().toString();
        request.setRequestID(uuid);
        queryTasks.put(uuid, queryTask);
        executorService.submit(queryTask);
        handle(request);
    }


}
