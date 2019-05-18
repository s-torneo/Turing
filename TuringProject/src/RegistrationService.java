import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationService extends Remote {
	
	public boolean Registra(String username, String password)throws RemoteException;
}