import java.rmi.server.*; // Classes and support for RMI servers

public class RegistrationServiceImpl extends RemoteServer implements RegistrationService {
	
	private static final long serialVersionUID = 1L;

	/* Constructor - set up database */
	RegistrationServiceImpl(){}

	public boolean Registra(String username, String password) {
		//inserisco l'utente nella lista di utenti se non c'è già
		if(Strutture.registrati.putIfAbsent(username,new RegisteredUser(password))!=null)
			return false;
		return true;
	}
}