using System.Net.Sockets;

namespace Socketlib {
    public delegate void Recv(Packet packet);
    public delegate void ExceptionCaught(Exception ex);

    public class Client {

        private TcpClient client;
        private NetworkStream stream;

        private static Recv recv;
        private static ExceptionCaught exceptionCaught;

        public void Connect(string ip, int port, Recv recvFunction, ExceptionCaught exceptionCaughtFunction) {
            recv = recvFunction;
            exceptionCaught = exceptionCaughtFunction;

            try {
                client = new TcpClient(ip, port);
                stream = client.GetStream();

                Thread recvThread = new(Recv) {
                    IsBackground = true
                };
                recvThread.Start();
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

        public void Send(Packet packet) {
            try {
                Packet sendPacket = Codec.Encoder(packet);

                stream.Write(sendPacket.ToArray(), 0, sendPacket.Length());
                stream.Flush();
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

        public void Close() {
            try {
                stream.Close();
                client.Close();
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

        private void Recv() {
            try {
                while (true) {
                    byte[] buffer = new byte[client.ReceiveBufferSize];
                    int length = stream.Read(buffer, 0, buffer.Length);

                    Packet recvPacket = Codec.Decoder(buffer, length);

                    recv?.Invoke(recvPacket);
                }
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

    }
}