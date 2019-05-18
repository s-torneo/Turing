import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Client {
	
	public Socket socket;
	public Socket notify;
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public ChatRoomClient chat;
	public String username;
	public String endMessage;
	public static String ServerAddress = "127.0.0.1";
	public static int ServerPort = 49999;
	public static int ServerPort2 = 49998;
	
	public Client () throws Exception {
		this.socket = new Socket(ServerAddress, ServerPort); //socket per la comunicazione client-server
		this.notify = new Socket(ServerAddress, ServerPort2); //socker per le notifiche
		this.out = new ObjectOutputStream (this.socket.getOutputStream()); //stream di input
		this.in = new ObjectInputStream(this.socket.getInputStream());  //stream di output
	}

	public String Login(String username, String password) throws Exception {
		Richiesta o = new Richiesta(0,username+" "+password);
		out.writeObject(o);
		out.flush();
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		//se l'utente è stato loggato allora creo il thread per le notifiche
		if(r.getRichiesta().equals("true")) {
			this.username=username;
			//messaggio di terminazione per la chat
			this.endMessage=username+"-quit";
			Notifiche n = new Notifiche(this.notify);
			Thread t = new Thread(n);
			t.start();
		}
		return r.getRichiesta();
	}

	public void Logout(String username) throws Exception {
		Richiesta o = new Richiesta(1,username);
		out.writeObject(o);
		out.flush();
	}

	public String NewDoc(String nome, String sezioni, String username) throws Exception {
		Richiesta o = new Richiesta(2,nome+" "+sezioni+" "+username);
		out.writeObject(o);
		out.flush();       
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		return r.getRichiesta();
	}

	public String ModDoc(String nome, String sezione, String creatore) throws Exception {
		Richiesta o = new Richiesta(3,nome+" "+sezione+" "+creatore);
		out.writeObject(o);
		out.flush();       
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		return r.getRichiesta();
	}

	public String GetAddress() throws Exception {
		Object data = in.readObject();
		Richiesta r = (Richiesta) data;
		return r.getRichiesta();
	}
	
	public int GetPort() throws Exception {
		Object data = in.readObject();
		Richiesta r = (Richiesta) data;
		return Integer.parseInt(r.getRichiesta());
	}

	public String ShowSez(String sezione, String documento, String utente, String creatore) throws Exception {
		Richiesta o = new Richiesta(7,sezione+" "+documento+" "+utente+" "+creatore);
		out.writeObject(o);
		out.flush();       
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		return r.getRichiesta();
	}

	public String ShowDoc(String documento, String utente, String creatore) throws Exception {
		Richiesta o = new Richiesta(9,documento+" "+utente+" "+creatore);
		out.writeObject(o);
		out.flush();       
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		return r.getRichiesta();
	}

	public boolean ScriviFile(String contenuto, String nome, String sezione, String creatore) throws IOException {
		try {
			Richiesta o = new Richiesta(4,nome+" "+sezione+" "+creatore+" "+contenuto);
			out.writeObject(o);
			out.flush();
		}catch(SocketException e) {
			return false;
		}
		return true;
	}

	public boolean ReleaseLock(String nome, String sezione, String creatore) throws IOException {
		try {
			Richiesta o = new Richiesta(10,nome+" "+sezione+" "+creatore);
			out.writeObject(o);
			out.flush();
		}catch(SocketException e) {
			return false;
		}
		return true;
	}

	public String NewUser(String doc, String invitato, String username) throws Exception {
		Richiesta o = new Richiesta(5,doc+" "+invitato+" "+username);
		out.writeObject(o);
		out.flush();     
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		return r.getRichiesta();
	}

	public String[] Inviti(String username) throws Exception{
		Richiesta o = new Richiesta(6,username);
		out.writeObject(o);
		out.flush();
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		String data2[]=r.getRichiesta().split(" ");
		return data2;
	}

	public String[] ListaDoc(String username) throws Exception{
		//creo una nuova richiesta
		Richiesta o = new Richiesta(8,username);
		//invio la richiestra tramite stream out
		out.writeObject(o);
		out.flush();
		//aspetto la ricezione della risposta
		Object data = in.readObject();
		Richiesta r = (Richiesta) data; 
		String data2[]=r.getRichiesta().split(" ");
		return data2;
	}

	public void Close() {
		try {
			this.socket.close();
			this.notify.close();
			this.in.close();
			this.out.close();
		} catch (IOException e) {
			return;
		}
	}

	//creo una nuova chat per il client
	public void NewChat(String indirizzo, JTextArea area, int port) {
		this.chat = new ChatRoomClient(indirizzo,area,port,this.username);
	}

	//invio un messaggio nella chat
	public boolean SendMessage(String message, String utente) {
		this.chat.Send(message, utente);
		if(message.equals(this.endMessage))
			return false;
		return true;
	}

	//invio un messaggio di terminazione nella chat
	public void CloseChat() {
		this.chat.Send(this.endMessage,this.username);
	}
	
	public static void main(String args[]) { 
		Remote RemoteObject;
		RegistrationService serverObject = null;
		try {
			Registry r = LocateRegistry.getRegistry();
			RemoteObject = r.lookup("REGISTRY-SERVER");
			serverObject = (RegistrationService) RemoteObject;
		}catch (Exception e) {
			JFrame w = new JFrame();
			JOptionPane.showMessageDialog(w,"Servizio non disponibile.","Warning",JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
		new Window(serverObject);
	}
}