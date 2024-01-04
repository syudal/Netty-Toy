using System;
using System.Net.Sockets;
using System.Threading;

namespace Socketlib {
    public delegate void Recv(Packet packet);
    public delegate void ExceptionCaught(Exception ex);

    public class Client {

        private TcpClient client;
        private NetworkStream stream;

        private static Recv? recv;
        private static ExceptionCaught? exceptionCaught;

        public Client(Recv recvFunction, ExceptionCaught exceptionCaughtFunction) {
            recv = recvFunction;
            exceptionCaught = exceptionCaughtFunction;
        }

        ~Client() {
            recv = null;
            exceptionCaught = null;
        }


        public void Connect(string ip, int port) {
            try {
                client = new TcpClient(ip, port);
                stream = client.GetStream();

                Thread recvThread = new Thread(Recv) {
                    IsBackground = true
                };
                recvThread.Start();
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

        public void Send(Packet packet) {
            if (stream == null) {
                exceptionCaught?.Invoke(new NullReferenceException());
                return;
            }

            try {
                Packet sendPacket = Codec.Encoder(packet);

                stream.Write(sendPacket.ToArray(), 0, sendPacket.Length());
                stream.Flush();
            } catch (Exception ex) {
                Close();
                exceptionCaught?.Invoke(ex);
            }
        }

        public void Close() {
            try {
                stream?.Close();
                client?.Close();
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
                Close();
                exceptionCaught?.Invoke(ex);
            }
        }

    }
}