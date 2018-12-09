package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.request.DownloadRequest;
import lk.uom.cse14.dsd.comm.request.Request;
import lk.uom.cse14.dsd.comm.response.DownloadResponse;
import lk.uom.cse14.dsd.comm.response.Response;
import lk.uom.cse14.dsd.peer.Peer;
import lk.uom.cse14.dsd.scheduler.Scheduler;
import lk.uom.cse14.dsd.util.FileTransferUtils;
import lk.uom.cse14.dsd.util.TextFileUtils;
import org.apache.log4j.Logger;

public class DownloadHandler implements IHandler {
    private final Logger log = Logger.getLogger(TextFileUtils.class);
    private Scheduler scheduler;
    private String ownHost;
    private int ownPort;
    private Peer peer;

    public DownloadHandler(Scheduler scheduler, String ownHost, int ownPort, Peer peer) {
        this.scheduler = scheduler;
        this.ownHost = ownHost;
        this.ownPort = ownPort;
        this.peer = peer;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            if (response == null) {
                System.out.println("Cannot reach the host: " + request.getDestination() + ":" + (request.getDestinationPort() + 1));
                System.out.println("Try again after host comes online!");
                return;
            }
            DownloadRequest req = (DownloadRequest) request;
            DownloadResponse res = (DownloadResponse) response;

            String filename = req.getFilename();
            String hostIP = res.getSource();
            int hostPort = res.getSourcePort();
            FileTransferUtils.downloadFile(hostIP, hostPort, filename);
            peer.updatePeer();
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
