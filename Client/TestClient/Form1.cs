using Socketlib;
using System.Text;

namespace TestClient {
    public partial class Form1 : Form {
        Client client;
        StringBuilder stringBuilder;

        public Form1() {
            InitializeComponent();
            client = new Client(UpdatePacket, ExceptionCaught);
            stringBuilder = new();
        }

        private void button1_Click(object sender, EventArgs e) {
            client.Connect(textBox1.Text, Convert.ToInt32(textBox2.Text), true);
        }

        private void UpdatePacket(Packet packet) {
            stringBuilder.AppendLine(packet.ToString());
            packet.DecodeShort();
            packet.DecodeInt();
            WriteLog();
        }

        private void ExceptionCaught(Exception ex) {
            stringBuilder.AppendLine(ex.Message);
            WriteLog();
        }

        private void WriteLog() {
            if (textBox4.InvokeRequired) {
                textBox4.Invoke((MethodInvoker) delegate { textBox4.Text = stringBuilder.ToString(); });
            } else {
                textBox4.Text = stringBuilder.ToString();
            }
        }

        private void button2_Click(object sender, EventArgs e) {
            Packet packet = new();
            /*
            packet.EncodeShort(Convert.ToInt16(!checkBox1.Checked));
            packet.EncodeString(textBox3.Text);
            */
            packet.EncodeShort(2);

            client.Send(packet);
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e) {
            client.Close();
        }
    }
}