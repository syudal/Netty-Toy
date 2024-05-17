package network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

import java.nio.charset.StandardCharsets;

class Packet @JvmOverloads constructor(size: Int = BLOCK_SIZE) {
    private val buf: ByteBuf = Unpooled.buffer(size)
    private val state = 0
    private var length = 0
    val rawSeq: Int = 0
    var dataLen: Int = 0

    fun copyTo(packet: Packet) {
        val buff = buf.array()
        val sendBuff = ByteArray(buff.size)
        val offset = buf.readerIndex()
        val size = buff.size - offset
        System.arraycopy(buff, offset, sendBuff, 0, size)
        packet.encodeBuffer(sendBuff)
    }

    fun decodeBool(): Boolean {
        return buf.readBoolean()
    }

    fun decodeByte(): Byte {
        return buf.readByte()
    }

    fun decodeByte(unsigned: Boolean): Int {
        if (!unsigned) {
            return decodeByte().toInt()
        }
        return buf.readUnsignedByte().toInt()
    }

    fun decodeShort(): Short {
        return buf.readShortLE()
    }

    fun decodeShort(unsigned: Boolean): Int {
        if (!unsigned) {
            return decodeShort().toInt()
        }
        return buf.readUnsignedShortLE()
    }

    fun decodeInt(): Int {
        return buf.readIntLE()
    }

    fun decodeFloat(): Float {
        return buf.readFloatLE()
    }

    fun decodeLong(): Long {
        return buf.readLongLE()
    }

    fun decodeDouble(): Double {
        return buf.readDoubleLE()
    }

    private fun decodeString(size: Int): String {
        val arr = ByteArray(size)
        var i = 0
        while (i < size) {
            arr[i++] = (decodeByte(true) and 0xFF).toByte()
        }
        return String(arr, StandardCharsets.UTF_8).replace("\u0000".toRegex(), "")
    }

    fun decodeString(): String {
        return decodeString(decodeShort().toInt())
    }

    fun decodePadding(length: Int) {
        buf.skipBytes(length)
    }

    fun encodeBool(b: Boolean) {
        buf.writeBoolean(b)
    }

    fun encodeByte(b: Byte) {
        buf.writeByte(b.toInt())
    }

    fun encodeByte(b: Int) {
        buf.writeByte(b)
    }

    fun encodeShort(n: Short) {
        buf.writeShortLE(n.toInt())
    }

    fun encodeShort(n: Int) {
        buf.writeShortLE(n)
    }

    fun encodeInt(n: Int) {
        buf.writeIntLE(n)
    }

    fun encodeLong(l: Long) {
        buf.writeLongLE(l)
    }

    fun encodeFloat(f: Float) {
        buf.writeFloatLE(f)
    }

    fun encodeDouble(d: Double) {
        buf.writeDoubleLE(d)
    }

    fun encodeString(str: String) {
        val src: ByteArray = str.toByteArray(StandardCharsets.UTF_8)

        encodeShort(src.size)
        encodeBuffer(src)
    }

    fun encodeString(str: String, size: Int) {
        val src: ByteArray = str.toByteArray(StandardCharsets.UTF_8)

        encodeShort(size)

        for (i in 0 until size) {
            if (i >= src.size) {
                encodeByte('\u0000'.code)
            } else {
                encodeByte(src[i])
            }
        }
    }

    fun encodeBuffer(buffer: ByteArray?) {
        buf.writeBytes(buffer)
    }

    fun encodeBuffer(packet: Packet) {
        encodeBuffer(packet.toArray())
    }

    fun encodePadding(count: Int) {
        for (i in 0 until count) {
            encodeByte(0)
        }
    }

    fun rawAppendBuffer(buff: ByteBuf?, size: Int) {
        if (size + length > buf.readableBytes()) {
            buf.writeBytes(buff)
        }
        length += size
    }

    fun length(): Int {
        return buf.writerIndex() - buf.readerIndex()
    }

    fun toArray(): ByteArray {
        val src = ByteArray(buf.writerIndex())
        buf.readBytes(src)
        buf.resetReaderIndex()
        return src
    }

    override fun toString(): String {
        val builder = StringBuilder()

        for (data in toArray()) {
            builder.append(String.format("%02X ", data))
        }

        return builder.toString()
    }

    companion object {
        private const val BLOCK_SIZE = 0x10000
    }
}