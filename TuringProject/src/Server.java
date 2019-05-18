 import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server { 

	public static void ServerRMI() {
		//REGISTRAZIONE RMI
		try {
			RegistrationServiceImpl statsService = new RegistrationServiceImpl();
			/* Esportazione dell'Oggetto */
			RegistrationService stub = (RegistrationService)UnicastRemoteObject.exportObject(statsService, 0);
			/* Creazione di un registry */
			LocateRegistry.createRegistry(1099);
			Registry r=LocateRegistry.getRegistry();
			/* Pubblicazione dello stub nel registry */
			r.rebind("REGISTRY-SERVER", (Remote) stub);
		}catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void TerminaServer(TCPServer server, ThreadPool tp) {
		if(tp!=null)
			tp.endServer();
		if(server!=null)
			server.Cleanup();
	}

	public static void main(String[] args) { 
		TCPServer server = null;
		ThreadPool tp = null;
		String ServerAddress = "127.0.0.1";
		int ServerPort = 49999;
		int ServerPort2 = 49998;
		try {
			//REGISTRAZIONE RMI
			ServerRMI();
			//SERVER TCP
			server = new TCPServer(ServerAddress, ServerPort, ServerPort2);
			System.out.println("\r\nRunning Server: " + "Host=" + server.getSocketAddress().getHostAddress() + " Port=" + server.getPort());
			//istanzio un oggetto di tipo ThreadPool
			tp = new ThreadPool();
			server.listen(tp);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			TerminaServer(server, tp);
		}
	}  
}