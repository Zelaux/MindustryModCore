package mma.io;

import arc.util.io.Reads;
import arc.util.io.ReusableByteInStream;

import java.io.DataInputStream;

public class ZelReads extends Reads {
    final ReusableByteInStream r = new ReusableByteInStream();

    public ZelReads() {
        super(null);
        input = new DataInputStream(r);
    }

    public ZelReads(byte[] bytes) {
        this();
        setBytes(bytes);
    }

    public void setBytes(byte[] bytes) {
        r.setBytes(bytes);
    }
}
