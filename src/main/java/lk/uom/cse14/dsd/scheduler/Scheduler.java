package lk.uom.cse14.dsd.scheduler;

import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.msghandler.IHandler;

public class Scheduler {
    private IHandler queryHandler;
    private IHandler heartbeatHandler;
    private IHandler peerDiscoveryHandler;


    public void setQueryHandler(IHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public void setHeartbeatHandler(IHandler heartbeatHandler) {
        this.heartbeatHandler = heartbeatHandler;
    }

    public void setPeerDiscoveryHandler(IHandler peerDiscoveryHandler) {
        this.peerDiscoveryHandler = peerDiscoveryHandler;
    }

    public void schedule(Request request){

    }
}
