﻿using System.Text;

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

            buffer.CopyTo(0, temp, 0, length);
            buffer.RemoveRange(0, length);

            return temp;
        }

        private void WriteBuffer(object item) {

            switch (item) {
                case bool b:
                    buffer.AddRange(BitConverter.GetBytes(b).Reverse());
                    break;

                case byte b:
                    buffer.Add(b);
                    break;

                case short s:
                    buffer.AddRange(BitConverter.GetBytes(s).Reverse());
                    break;

                case int i:
                    buffer.AddRange(BitConverter.GetBytes(i).Reverse());
                    break;

                case long l:
                    buffer.AddRange(BitConverter.GetBytes(l).Reverse());
                    break;

                case float f:
                    buffer.AddRange(BitConverter.GetBytes(f).Reverse());
                    break;

                case double d:
                    buffer.AddRange(BitConverter.GetBytes(d).Reverse());
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
            StringBuilder builder = new();

            foreach (byte data in ToArray()) {
                builder.Append(string.Format("{0:X2} ", data));
            }

            builder.AppendLine();

            return builder.ToString();
        }
    }
}
