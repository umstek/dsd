package lk.uom.cse14.dsd.comm.message;

/*
 * Discovery Request is used for discovering new neighbours
 * */
public class DiscoveryRequest extends Request {

    public DiscoveryRequest(String ownHost) {
        super(ownHost);
    }
}
