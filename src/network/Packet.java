package network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import util.Pointer;

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

    public int appendBuffer(ByteBuf buff, Pointer<Integer> lastState) {
        final int HEADER = Short.BYTES + Short.BYTES;//uRawSeq + uDataLen

        if (lastState != null)
            lastState.set(state);
        int size = buff.readableBytes();
        if (state == 0) {
            int len = Math.min(size, HEADER - length);
            rawAppendBuffer(buff, len);
            if (size >= HEADER) {
                state = 1;
                rawSeq = decodeShort();
                dataLen = decodeShort();
                size -= len;
                if (size == 0)  {
                    return state;
                }
            }
        }
        int append = Math.min(size, dataLen + HEADER - length);
        rawAppendBuffer(buff, append);
        if (length >= dataLen + HEADER) {
            state = 2;
        }
        size -= append;
        return state;
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

    public void rawAppendBuffer(ByteBuf buff, int size) {
        if (size + length > buf.readableBytes()) {
            buf.writeBytes(buff);
        }
        length += size;
    }

    public void setDataLen(int len) {
        this.dataLen = len;
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