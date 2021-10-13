package mma.io;

import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;

import java.io.DataOutputStream;

public class ByteWrites extends Writes {
    final ReusableByteOutStream r = new ReusableByteOutStream(8192);

    public ByteWrites() {
        super(null);
        output = new DataOutputStream(r);
    }

    public void reset() {
        r.reset();
    }

    public byte[] getBytes() {
        return r.getBytes();
    }
}
