package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.request.DownloadRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.DownloadResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import lk.uom.cse14.dsd.util.FileTransferUtils;

public class DownloadHandler implements IHandler {
    private Scheduler scheduler;
    private String ownHost;
    private int ownPort;

    public DownloadHandler(Scheduler scheduler, String ownHost, int ownPort) {
        this.scheduler = scheduler;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            DownloadRequest req = (DownloadRequest) request;
            DownloadResponse res = (DownloadResponse) response;

            String filename = req.getFilename();
            String hostIP = res.getSource();
            int hostPort = res.getSourcePort();
            FileTransferUtils.downloadFile(hostIP, hostPort, filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handle(Request request) {
        try {
            DownloadRequest req = (DownloadRequest) request;
            int destPort = req.getSourcePort();
            String destIP = req.getSource();
            req.setHostIP(destIP);
            req.setHostPort(destPort);
            String filename = req.getFilename();
            DownloadResponse res = new DownloadResponse(ownHost, ownPort + 1, destIP, destPort);
            res.setUuid(req.getUuid());
            scheduler.schedule(res);
            FileTransferUtils.serveFile(ownPort + 1, filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
