package lk.uom.cse14.dsd.fileio;


import java.io.*;

/**
 * This class represents the dummy file object that is generated
 */

public class DummyFile extends Object implements Serializable {

    private String data;
    private int size;

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

    public byte[] toByteArray() throws IOException {
        ObjectOutput oOut;
        byte[] fileBytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //calculating the byte array of the dummy file object
        oOut = new ObjectOutputStream(bos);
        oOut.writeObject(this);
        oOut.flush();
        return bos.toByteArray();
    }

}
