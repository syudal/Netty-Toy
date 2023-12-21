using System.Text;

namespace Socketlib {
    public class Packet {
        private static int BLOCK_SIZE = 0x10000;
        private byte[] buffer;
        private int pointer = 0;

        public Packet() {
            this.buffer = new byte[BLOCK_SIZE];
        }

        public Packet(int size) {
            this.buffer = new byte[size];
        }

        public void CopyTo(Packet packet) {
            byte[] sendBuff = new byte[buffer.Length];
            Array.Copy(buffer, sendBuff, buffer.Length);
            packet.EncodeBuffer(sendBuff);
        }

        public bool DecodeBool() {
            return Convert.ToBoolean(ReadBuffer(1));
        }

        public byte DecodeByte() {
            return Convert.ToByte(ReadBuffer(1));
        }

        public short DecodeShort() {
            return Convert.ToSByte(ReadBuffer(2));
        }

        public int DecodeInt() {
            return Convert.ToInt32(ReadBuffer(4));
        }

        public long DecodeLong() {
            return Convert.ToInt64(ReadBuffer(8));
        }

        public float DecodeFloat() {
            return Convert.ToSingle(ReadBuffer(4));
        }

        public double DecodeDouble() {
            return Convert.ToDouble(ReadBuffer(8));
        }

        public string DecodeString(int size) {
            byte[] arr = new byte[size];
            int i = 0;
            while (i < size) {
                arr[i++] = (byte)(DecodeByte() & 0xFF);
            }
            return Encoding.UTF8.GetString(arr).Replace("\0", "");
        }

        public string DecodeString() {
            return DecodeString(DecodeShort());
        }

        public void DecodePadding(int length) {
            ReadBuffer(length);
        }

        public void EncodeBool(bool b) { 
            WriteBuffer(b);
        }

        public void EncodeByte(byte b) { 
            WriteBuffer(b);
        }

        public void EncodeShort(short s) {
            WriteBuffer(s);
        }

        public void EncodeInt(int i) { 
            WriteBuffer(i);
        }

        public void EncodeLong(long l) {  
            WriteBuffer(l);
        }

        public void EncodeFloat(float f) { 
            WriteBuffer(f);
        }

        public void EncodeDouble(double d) {
            WriteBuffer(d);
        }

        public void EncodeString(string s) {
            byte[] src = Encoding.UTF8.GetBytes(s);

            EncodeShort((short)src.Length);
            WriteBuffer(src); 
        }

        public void EncodeString(string s, int size) {
            byte[] src = Encoding.UTF8.GetBytes(s);

            for (int i = 0; i < size; i++) {
                if (i >= src.Length) {
                    EncodeByte(0);
                } else {
                    EncodeByte(src[i]);
                }
            }
        }

        public void EncodeBuffer(byte[] src) {
            WriteBuffer(src);
        }

        public void EncodePadding(int count) {
            for (int i = 0; i < count; i++) {
                EncodeByte(0);
            }
        }

        private byte[] ReadBuffer(int length) {
            byte[] temp = new byte[length];

            Array.Copy(buffer, pointer, temp, 0, length);
            pointer += length;

            return temp;
        }

        private void WriteBuffer(object item) {

            switch (item) {
                case bool b: {
                        foreach (byte temp in BitConverter.GetBytes(b)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case byte b:
                    buffer[pointer++] = b;
                    break;

                case short s: {
                        foreach (byte temp in BitConverter.GetBytes(s)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case int i: {
                        foreach (byte temp in BitConverter.GetBytes(i)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case long l: {
                        foreach (byte temp in BitConverter.GetBytes(l)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case float f: {
                        foreach (byte temp in BitConverter.GetBytes(f)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case double d: {
                        foreach (byte temp in BitConverter.GetBytes(d)) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }

                case byte[] b: {
                        foreach (byte temp in b) {
                            buffer[pointer++] = temp;
                        }
                        break;
                    }
            }

        }

        public int Length() { 
            return buffer.Length;
        }


        public byte[] ToArray() {
            return buffer;
        }

        public override string ToString() {
            StringBuilder builder = new();

            foreach (byte data in ToArray()) {
                builder.Append(string.Format("%02X ", data));
            }

            return builder.ToString();
        }
    }
}
