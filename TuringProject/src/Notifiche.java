import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Notifiche implements Runnable{

	public Socket socket;
	public ObjectInputStream in;

	public Notifiche(Socket socket) {
		this.socket=socket;
	}

	public void run() {
		System.out.println("sto ascoltando");
		try {
			this.in = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e1) {
			return;
		}
		Object data=null;
		while(true) {
			try {
				try {
					//in attesa di notifiche
					data = this.in.readObject();
				}catch (SocketException e1) {
					System.out.println("sto chiudendo ascolto notifiche per eccezione");
					this.socket.close();
					this.in.close();
					return;
				} 
				Richiesta r = (Richiesta) data;
				System.out.println("tipo: "+r.getTipo());
				String data2[] = r.getRichiesta().split(" ");
				//termina thread
				if(r.getTipo()==-2) {
					System.out.println("sto chiudendo ascolto notifiche");
					this.in.close();
					return;
				}
				JFrame f=new JFrame("Notifica invito");    
				JOptionPane.showMessageDialog(f,"Sei stato invitato dall'utente: "+data2[0]+"\n a collaborare al seguente documento: "+data2[1]);
			} catch (IOException | ClassNotFoundException e) {
				JFrame w = new JFrame();
				JOptionPane.showMessageDialog(w,"Errore nell'ascolto delle notifiche.","Warning",JOptionPane.WARNING_MESSAGE);
			} 
		}
	}
}