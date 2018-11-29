package lk.uom.cse14.dsd.fileio;


/**
 * This class represents the dummy file object that is generated
 */

public class DummyFile extends Object {

    private String data;
    private int size;
    private byte[] hash;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

}
