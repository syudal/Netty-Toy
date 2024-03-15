package network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class Packet{
    private static final int BLOCK_SIZE = 0x10000;
    private final ByteBuf buf;
    private int state;
    private int length;
    private int rawSeq;
    private int dataLen;

    public Packet() {
        this(BLOCK_SIZE);
    }

    public Packet(int size) {
        this.buf = Unpooled.buffer(size);
    }

    public void copyTo(Packet packet) {
        byte[] buff = buf.array();
        byte[] sendBuff = new byte [buff.length];
        int offset = buf.readerIndex();
        int size = buff.length - offset;
        System.arraycopy(buff, offset, sendBuff, 0, size);
        packet.encodeBuffer(sendBuff);
    }

    public boolean decodeBool() {
        return buf.readBoolean();
    }

    public byte decodeByte() {
        return buf.readByte();
    }

    public int decodeByte(boolean unsigned) {
        if (!unsigned) {
            return decodeByte();
        }
        return buf.readUnsignedByte();
    }

    public short decodeShort() {
        return buf.readShortLE();
    }

    public int decodeShort(boolean unsigned) {
        if (!unsigned) {
            return decodeShort();
        }
        return buf.readUnsignedShortLE();
    }

    public int decodeInt() {
        return buf.readIntLE();
    }

    public float decodeFloat() {
        return buf.readFloatLE();
    }

    public long decodeLong() {
        return buf.readLongLE();
    }

    public double decodeDouble() {
        return buf.readDoubleLE();
    }

    private String decodeString(int size) {
        byte[] arr = new byte[size];
        int i = 0;
        while (i < size) {
            arr[i++] = (byte) (decodeByte(true) & 0xFF);
        }
        return new String(arr, StandardCharsets.UTF_8).replaceAll("\0", "");
    }

    public String decodeString() {
        return decodeString(decodeShort());
    }

    public void decodePadding(int length) {
        buf.skipBytes(length);
    }

    public void encodeBool(boolean b) {
        buf.writeBoolean(b);
    }

    public void encodeByte(byte b) {
        buf.writeByte(b);
    }

    public void encodeByte(int b) {
        buf.writeByte(b);
    }

    public void encodeShort(short n) {
        buf.writeShortLE(n);
    }

    public void encodeShort(int n) {
        buf.writeShortLE(n);
    }

    public void encodeInt(int n) {
        buf.writeIntLE(n);
    }

    public void encodeLong(long l) {
        buf.writeLongLE(l);
    }

    public void encodeFloat(float f) {
        buf.writeFloatLE(f);
    }

    public void encodeDouble(double d) {
        buf.writeDoubleLE(d);
    }

    public void encodeString(String str) {
        byte[] src = str.getBytes(StandardCharsets.UTF_8);

        encodeShort(src.length);
        encodeBuffer(src);
    }

    public void encodeString(String str, int size) {
        byte[] src = str.getBytes(StandardCharsets.UTF_8);

        encodeShort(size);

        for (int i = 0; i < size; i++) {
            if (i >= src.length) {
                encodeByte('\0');
            } else {
                encodeByte(src[i]);
            }
        }
    }

    public void encodeBuffer(byte[] buffer) {
        buf.writeBytes(buffer);
    }

    public void encodePadding(int count) {
        for (int i = 0; i < count; i++) {
            encodeByte(0);
        }
    }

    public void rawAppendBuffer(ByteBuf buff, int size) {
        if (size + length > buf.readableBytes()) {
            buf.writeBytes(buff);
        }
        length += size;
    }

    public void setDataLen(int len) {
        this.dataLen = len;
    }

    public int getDataLen() {
        return dataLen;
    }

    public int getRawSeq() {
        return rawSeq;
    }

    public byte[] toArray() {
        byte[] src = new byte[buf.writerIndex()];
        buf.readBytes(src);
        buf.resetReaderIndex();
        return src;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (byte data : toArray()) {
            builder.append(String.format("%02X ", data));
        }

        return builder.toString();
    }
}