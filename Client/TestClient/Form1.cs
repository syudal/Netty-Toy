using Socketlib;
using System.Text;

namespace TestClient {
    public partial class Form1 : Form {
        Client client;
        public Form1() {
            InitializeComponent();
            client = new Client(UpdatePacket, ExceptionCaught);
        }

        private void button1_Click(object sender, EventArgs e) {
            client.Connect(textBox1.Text, Convert.ToInt32(textBox2.Text));
        }

        private void UpdatePacket(Packet packet) {
            WriteLog(packet.ToString());
        }

        private void ExceptionCaught(Exception ex) {
            WriteLog(ex.Message);
        }

        private void WriteLog(string log) {
            log += Environment.NewLine;

            if (textBox4.InvokeRequired) {
                textBox4.Invoke((MethodInvoker) delegate { textBox4.Text += log; });
            } else {
                textBox4.Text += log;
            }
        }

        private void button2_Click(object sender, EventArgs e) {
            byte[] bytes = Encoding.UTF8.GetBytes(textBox3.Text);
            Packet packet = new();
            packet.EncodeShort(Convert.ToInt16(!checkBox1.Checked));
            packet.EncodeBuffer(bytes);

            client.Send(packet);
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e) {
            client.Close();
        }
    }
}