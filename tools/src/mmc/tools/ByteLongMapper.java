package mmc.tools;

import arc.math.*;

/**
 * Bits used: 64 / 64
 * <br>  b1 [0..8]
 * <br>  b2 [8..16]
 * <br>  b3 [16..24]
 * <br>  b4 [24..32]
 * <br>  b5 [32..40]
 * <br>  b6 [40..48]
 * <br>  b7 [48..56]
 * <br>  b8 [56..64]
 */
@SuppressWarnings({"RedundantCast", "PointlessArithmeticExpression", "PointlessBitwiseExpression", "unused"})
public final class ByteLongMapper{
  public static byte b1(long teststruct2) {
    return (byte)((teststruct2 >>> 0) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b1(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000000000000000000000000000000000000000000011111111L) | ((long)value << 0L));
  }

  public static byte b2(long teststruct2) {
    return (byte)((teststruct2 >>> 8) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b2(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000000000000000000000000000000000001111111100000000L) | ((long)value << 8L));
  }

  public static byte b3(long teststruct2) {
    return (byte)((teststruct2 >>> 16) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b3(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000000000000000000000000000111111110000000000000000L) | ((long)value << 16L));
  }

  public static byte b4(long teststruct2) {
    return (byte)((teststruct2 >>> 24) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b4(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000000000000000000011111111000000000000000000000000L) | ((long)value << 24L));
  }

  public static byte b5(long teststruct2) {
    return (byte)((teststruct2 >>> 32) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b5(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000000000001111111100000000000000000000000000000000L) | ((long)value << 32L));
  }

  public static byte b6(long teststruct2) {
    return (byte)((teststruct2 >>> 40) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b6(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000000000000111111110000000000000000000000000000000000000000L) | ((long)value << 40L));
  }

  public static byte b7(long teststruct2) {
    return (byte)((teststruct2 >>> 48) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b7(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b0000000011111111000000000000000000000000000000000000000000000000L) | ((long)value << 48L));
  }

  public static byte b8(long teststruct2) {
    return (byte)((teststruct2 >>> 56) & 0b0000000000000000000000000000000000000000000000000000000011111111L);
  }

  public static long b8(long teststruct2, byte value) {
    return (long)((teststruct2 & 0b1111111100000000000000000000000000000000000000000000000000000000L) | ((long)value << 56L));
  }

  public static long get(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
    return (long)((((long)b1 << 0L) & 0b0000000000000000000000000000000000000000000000000000000011111111L) | (((long)b2 << 8L) & 0b0000000000000000000000000000000000000000000000001111111100000000L) | (((long)b3 << 16L) & 0b0000000000000000000000000000000000000000111111110000000000000000L) | (((long)b4 << 24L) & 0b0000000000000000000000000000000011111111000000000000000000000000L) | (((long)b5 << 32L) & 0b0000000000000000000000001111111100000000000000000000000000000000L) | (((long)b6 << 40L) & 0b0000000000000000111111110000000000000000000000000000000000000000L) | (((long)b7 << 48L) & 0b0000000011111111000000000000000000000000000000000000000000000000L) | (((long)b8 << 56L) & 0b1111111100000000000000000000000000000000000000000000000000000000L));
  }
    public static byte[] getBytes(long[] longs){
        int extraSize = (int)longs[0];
        byte[] bytes = new byte[(extraSize == 0 ? (longs.length - 1) * 8 : (longs.length - 2) * 8 + extraSize)];
        for(int i = 0; i < bytes.length / 8 ; i++){
            bytes[i * 8 + 0] =      ByteLongMapper.b1(longs[i + 1]);
            bytes[i * 8 + 1] =      ByteLongMapper.b2(longs[i + 1]);
            bytes[i * 8 + 2] =      ByteLongMapper.b3(longs[i + 1]);
            bytes[i * 8 + 3] =      ByteLongMapper.b4(longs[i + 1]);
            bytes[i * 8 + 4] =      ByteLongMapper.b5(longs[i + 1]);
            bytes[i * 8 + 5] =      ByteLongMapper.b6(longs[i + 1]);
            bytes[i * 8 + 6] =      ByteLongMapper.b7(longs[i + 1]);
            bytes[i * 8 + 7] =      ByteLongMapper.b8(longs[i + 1]);
        }
        int offset = bytes.length - extraSize;
        byte final0 = ByteLongMapper.b1(longs[longs.length - 1]);
        byte final1 = ByteLongMapper.b2(longs[longs.length - 1]);
        byte final2 = ByteLongMapper.b3(longs[longs.length - 1]);
        byte final3 = ByteLongMapper.b4(longs[longs.length - 1]);
        byte final4 = ByteLongMapper.b5(longs[longs.length - 1]);
        byte final5 = ByteLongMapper.b6(longs[longs.length - 1]);
        byte final6 = ByteLongMapper.b7(longs[longs.length - 1]);
        byte final7 = ByteLongMapper.b8(longs[longs.length - 1]);
        byte[] finals = {final0, final1, final2, final3, final4, final5, final6, final7};
        if(extraSize >= 0) System.arraycopy(finals, 0, bytes, offset + 0, extraSize);
        return bytes;
    }

    public static long[] getLongs(byte[] bytes){
        long[] longs = new long[Mathf.ceil(bytes.length / 8f) + 1];
        longs[0] = bytes.length % 8;
        for(int i = 0; i < bytes.length / 8; i++){
            longs[i + 1] = ByteLongMapper.get(
            bytes[i * 8 + 0],
            bytes[i * 8 + 1],
            bytes[i * 8 + 2],
            bytes[i * 8 + 3],
            bytes[i * 8 + 4],
            bytes[i * 8 + 5],
            bytes[i * 8 + 6],
            bytes[i * 8 + 7]);
        }
        int offset = (bytes.length / 8) * 8;
        if (offset<bytes.length){
            byte final0 = bytes[offset];
            byte final1 = bytes.length > offset + 1 ? bytes[offset + 1] : 0;
            byte final2 = bytes.length > offset + 2 ? bytes[offset + 2] : 0;
            byte final3 = bytes.length > offset + 3 ? bytes[offset + 3] : 0;
            byte final4 = bytes.length > offset + 4 ? bytes[offset + 4] : 0;
            byte final5 = bytes.length > offset + 5 ? bytes[offset + 5] : 0;
            byte final6 = bytes.length > offset + 6 ? bytes[offset + 6] : 0;
            byte final7 = bytes.length > offset + 7 ? bytes[offset + 7] : 0;
            longs[longs.length - 1] = ByteLongMapper.get(final0,
            final1,
            final2,
            final3,
            final4,
            final5,
            final6,
            final7);
        }
        return longs;
    }

}
