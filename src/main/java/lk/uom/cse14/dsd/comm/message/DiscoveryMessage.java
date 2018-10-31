package lk.uom.cse14.dsd.comm.message;

/*
 * Discovery Message is used for discovering new neighbours
 * */
public class DiscoveryMessage extends BaseMessage {

    public DiscoveryMessage(String ownHost) {
        super(ownHost);
    }
}
