import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ChatMulticastReceiver implements Runnable{

	public String address;
	public JTextArea area;
	public int port;
	public String username;

	public ChatMulticastReceiver(String address, JTextArea area, int port, String username) {
		this.address=address;
		this.area=area;
		this.port=port;
		this.username=username;
	}

	public String Ora() {
		Calendar cal = Calendar.getInstance();
		int ora = cal.get(Calendar.HOUR_OF_DAY);
		int minuti = cal.get(Calendar.MINUTE);
		return ora+"."+minuti;
	}

	public void run() {
		try(MulticastSocket socket = new MulticastSocket(this.port);){

			InetAddress multicastGroup= InetAddress.getByName(this.address);
			socket.setSoTimeout(100000000);
			socket.joinGroup(multicastGroup);
			socket.setTimeToLive(1);
			while(true){
				byte[] data = new byte[200];
				DatagramPacket packet= new DatagramPacket(data,data.length);
				ByteArrayInputStream bin= new ByteArrayInputStream(data);
				DataInputStream ddis= new DataInputStream(bin);
				socket.receive(packet);
				String sender = ddis.readUTF();
				String message = ddis.readUTF();
				if(message.equals(username+"-quit")&&sender.equals(username)) {
					System.out.println("thread chat interrotto");
					return;
				}
				area.append("["+sender+"] alle "+Ora()+" : "+message+"\n");
			}
		} catch (IOException e) {
			JFrame w = new JFrame();
			JOptionPane.showMessageDialog(w,"Errore di chat.","Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
	}
}
