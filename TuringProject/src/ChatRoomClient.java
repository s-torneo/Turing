import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ChatRoomClient{

	public String address;
	public int port;

	public ChatRoomClient(String address, JTextArea area, int port, String username) {
		this.address=address;
		this.port=port;
		ChatMulticastReceiver r = new ChatMulticastReceiver(this.address, area, this.port, username);
		Thread t = new Thread(r);
		t.start();
	}

	public void Send(String message, String sender){
		try{
			DatagramSocket s= new DatagramSocket();
			InetAddress address= InetAddress.getByName(this.address);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream (bout);
			byte[] data = new byte[200];
			dout.writeUTF(sender); 
			dout.writeUTF(message); 
			data = bout.toByteArray(); 
			DatagramPacket p= new DatagramPacket(data,data.length,address,this.port);
			s.send(p);
			s.close();
		} catch (IOException e) {
			JFrame w = new JFrame();
			JOptionPane.showMessageDialog(w,"Errore di chat.","Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
	}

}
