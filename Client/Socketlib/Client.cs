﻿using System;
using System.Net.Sockets;
using System.Threading;

namespace Socketlib {
    public delegate void Recv(Packet packet);
    public delegate void ExceptionCaught(Exception ex);

    public class Client {
        private TcpClient? client;
        private NetworkStream? stream;

        private static Recv? recv;
        private static ExceptionCaught? exceptionCaught;

        public Client(Recv recvFunction, ExceptionCaught exceptionCaughtFunction) {
            recv = recvFunction;
            exceptionCaught = exceptionCaughtFunction;
        }

        public void Connect(string ip, int port, bool noDelay = false) {
            try {
                Close();

                client = new TcpClient(ip, port) {
                    NoDelay = noDelay
                };
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
                if (stream.CanWrite) {
                    Packet sendPacket = Codec.Encoder(packet);

                    stream.Write(sendPacket.ToArray(), 0, sendPacket.Length());
                    stream.Flush();
                }
            } catch (Exception ex) {
                Close();
                exceptionCaught?.Invoke(ex);
            }
        }

        public void Close() {
            try {
                stream?.Dispose();
                client?.Dispose();
            } catch (Exception ex) {
                exceptionCaught?.Invoke(ex);
            }
        }

        private void Recv() {
            if (client == null || stream == null) {
                return;
            }

            try {
                while (client.Client != null & stream.CanRead) {
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

        public bool Connected() {
            if (client == null) {
                return false;
            }
            return client.Connected;
        }

    }
}