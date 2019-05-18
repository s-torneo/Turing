import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

	private ServerSocket server;
	private ServerSocket serverNotify;

	public TCPServer(String ipAddress, int port, int port2) throws Exception {
		this.server = new ServerSocket();
		this.server.bind(new InetSocketAddress(ipAddress, port));
		this.serverNotify = new ServerSocket();
		this.serverNotify.bind(new InetSocketAddress(ipAddress, port2));
	}

	public void listen(ThreadPool tp) throws Exception {
		while (true) {
			Socket client = this.server.accept();
			Socket notify = this.serverNotify.accept();
			Worker w=new Worker(client, notify); //creo un oggetto della classe Worker e gli passo le due socket
			tp.Task(w); //passo il task al threadpool
		}
	} 

	public InetAddress getSocketAddress() {
		return this.server.getInetAddress();
	}

	public int getPort() {
		return this.server.getLocalPort();
	}
	
	public void Cleanup() {
		try {
			this.server.close();
			this.serverNotify.close();
		} catch (IOException e) {
			return;
		}
	}
}