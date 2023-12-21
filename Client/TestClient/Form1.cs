using Socketlib;
using System.Text;

namespace TestClient {
    public partial class Form1 : Form {
        Client client;
        public Form1() {
            InitializeComponent();
            client = new Client();
        }

        private void button1_Click(object sender, EventArgs e) {
            client.Connect(textBox1.Text, Convert.ToInt32(textBox2.Text), UpdatePacket);
        }

        private void UpdatePacket(Packet packet) {
            if (textBox4.InvokeRequired) {
                textBox4.Invoke((MethodInvoker)delegate { textBox4.Text += packet.ToString(); });
            } else {
                textBox4.Text += packet.ToString();
            }
        }

        private void button2_Click(object sender, EventArgs e) {
            byte[] bytes = Encoding.UTF8.GetBytes(textBox3.Text);
            Packet packet = new(bytes.Length);
            packet.EncodeBuffer(bytes);

            client.Send(packet);
        }
    }
}