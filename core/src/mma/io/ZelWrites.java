package mma.io;

import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;

import java.io.DataOutputStream;

public class ZelWrites extends Writes {
    final ReusableByteOutStream r = new ReusableByteOutStream(8192);

    public ZelWrites() {
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
