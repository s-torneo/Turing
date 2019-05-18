import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class OnlineUser {
	
	public Socket notifiche;
	public ObjectOutputStream out; //stream di output usato per inviare le notifiche all'utente
	public ReentrantLock l; //usata per lockare l'outputstream quando si inviano notifiche al thread Notifiche

	public OnlineUser() {
		this.notifiche=null;
		this.out = null;
		this.l = new ReentrantLock();
	}
}