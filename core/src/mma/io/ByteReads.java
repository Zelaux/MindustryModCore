package mma.io;

import arc.util.io.*;

import java.io.*;

public class ByteReads extends Reads {
    public final ReusableByteInStream r = new ReusableByteInStream();

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
