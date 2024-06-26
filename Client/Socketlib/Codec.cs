﻿using System;
using System.Linq;

namespace Socketlib {
    internal class Codec {
        internal static Packet Encoder(Packet packet) {
            Packet temp = new Packet(packet.Length() + 4);
            temp.EncodeBuffer(BitConverter.GetBytes(packet.Length()).Reverse());
            temp.EncodeBuffer(packet);

            return temp;
        }

        internal static Packet Decoder(byte[] packet, int length) {
            length -= 4;
            byte[] bytes = new byte[length];

            Array.Copy(packet, 4, bytes, 0, length);

            Packet temp = new Packet(length);
            temp.EncodeBuffer(bytes);

            return temp;
        }
    }
}
