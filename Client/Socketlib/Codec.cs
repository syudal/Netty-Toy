namespace Socketlib {
    internal class Codec {
        internal static Packet Encoder(Packet packet) {
            Packet temp = new(packet.Length() + 4);
            temp.EncodeInt(packet.Length());
            temp.EncodeBuffer(packet.ToArray());

            return temp;
        }

        internal static Packet Decoder(byte[] packet, int length) {
            length -= 4;
            byte[] bytes = new byte[length];

            Array.Copy(packet, 4, bytes, 0, length);

            Packet temp = new(length);
            temp.EncodeBuffer(bytes);

            return temp;
        }
    }
}
