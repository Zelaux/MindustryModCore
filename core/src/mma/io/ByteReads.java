package mma.io;

import arc.util.io.Reads;
import arc.util.io.ReusableByteInStream;

import java.io.DataInputStream;

public class ByteReads extends Reads {
    final ReusableByteInStream r = new ReusableByteInStream();

    public ByteReads() {
        super(null);
        input = new DataInputStream(r);
    }

    public ByteReads(byte[] bytes) {
        this();
        setBytes(bytes);
    }

    public void setBytes(byte[] bytes) {
        r.setBytes(bytes);
    }
}
