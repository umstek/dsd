package lk.uom.cse14.dsd.msghandler;

import lk.uom.cse14.dsd.comm.message.Request;
import lk.uom.cse14.dsd.comm.response.Response;

public interface IHandler {
     void handle(Request request, Response response);
     void handle(Request request);
}
