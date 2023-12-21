using System.Net.Sockets;

namespace Socketlib {
    public delegate void Recv(Packet packet);
    public class Client {

        private TcpClient client;
        private NetworkStream stream;

        private static Recv recv;

        public void Connect(string ip, int port, Recv recvFunction) { 
            client = new TcpClient(ip, port);
            stream = client.GetStream();
            recv = recvFunction;

            Thread recvThread = new(Recv) {
                IsBackground = true
            };
            recvThread.Start();
        }

        public void Send(Packet packet) {
            Packet sendPacket = Codec.Encoder(packet);

            stream.Write(sendPacket.ToArray(), 0, sendPacket.Length());
            stream.Flush();
        }

        public void Close() { 
            stream.Close();
            client.Close();
        }

        private void Recv() {
            while (true) {
                byte[] buffer = new byte[client.ReceiveBufferSize];
                int length = stream.Read(buffer, 0, buffer.Length);

                Packet recvPacket = Codec.Decoder(buffer, length);

                recv?.Invoke(recvPacket);
            }
        }

    }
}