using System;
using System.Collections.Generic;
using System.Text;

namespace Socketlib {
    public class Packet {
        private static int BLOCK_SIZE = 0x10000;
        private List<byte> buffer;

        public Packet() {
            buffer = new List<byte>(BLOCK_SIZE);
        }

        public Packet(int size) {
            buffer = new List<byte>(size);
        }

        public void CopyTo(Packet packet) {
            packet.EncodeBuffer(buffer.ToArray());
        }

        public bool DecodeBool() {
            return BitConverter.ToBoolean(ReadBuffer(1));
        }

        public byte DecodeByte() {
            return ReadBuffer(1)[0];
        }

        public short DecodeShort() {
            return BitConverter.ToInt16(ReadBuffer(2));
        }

        public int DecodeInt() {
            return BitConverter.ToInt32(ReadBuffer(4));
        }

        public long DecodeLong() {
            return BitConverter.ToInt64(ReadBuffer(8));
        }

        public float DecodeFloat() {
            return BitConverter.ToSingle(ReadBuffer(4));
        }

        public double DecodeDouble() {
            return BitConverter.ToDouble(ReadBuffer(8));
        }

        private string DecodeString(int size) {
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

            EncodeShort((short)size);

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

            buffer.CopyTo(0, temp, 0, length);
            buffer.RemoveRange(0, length);

            return temp;
        }

        private void WriteBuffer(object item) {

            switch (item) {
                case bool b:
                    buffer.Add(BitConverter.GetBytes(b)[0]);
                    break;

                case byte b:
                    buffer.Add(b);
                    break;

                case short s:
                    buffer.AddRange(BitConverter.GetBytes(s));
                    break;

                case int i:
                    buffer.AddRange(BitConverter.GetBytes(i));
                    break;

                case long l:
                    buffer.AddRange(BitConverter.GetBytes(l));
                    break;

                case float f:
                    buffer.AddRange(BitConverter.GetBytes(f));
                    break;

                case double d:
                    buffer.AddRange(BitConverter.GetBytes(d));
                    break;

                case byte[] b:
                    buffer.AddRange(b);
                    break;
            }

        }

        public int Length() {
            return buffer.Count;
        }


        public byte[] ToArray() {
            return buffer.ToArray();
        }

        public override string ToString() {
            StringBuilder builder = new StringBuilder();

            foreach (byte data in ToArray()) {
                builder.Append(string.Format("{0:X2} ", data));
            }

            return builder.ToString();
        }
    }
}
