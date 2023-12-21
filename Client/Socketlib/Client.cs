using System.Net.Sockets;
using System.Text;

namespace Socketlib {
    public delegate void Recv(byte[] packet);
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
            stream.Write(packet.ToArray(), 0, packet.Length());
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
                byte[] result = new byte[length];
                Array.Copy(buffer, result, length);

                recv?.Invoke(result);
            }
        }

    }
}