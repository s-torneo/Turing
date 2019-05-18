import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegisteredUser {

	public String password;
	public List<String> inviti;
	public List<Documento> collaborazioni;

	public RegisteredUser(String password) {
		this.password=password;
		this.inviti=Collections.synchronizedList(new ArrayList<String>());
		this.collaborazioni=Collections.synchronizedList(new ArrayList<Documento>());
	}
}