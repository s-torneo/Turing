import java.io.Serializable;

//classe utilizzata per definire una richiesta o una risposta nella comunicazione client-server.
public class Richiesta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int tipo;
	private String richiesta;
	
	public Richiesta(int tipo, String richiesta)  {
		this.tipo=tipo;
		this.richiesta=richiesta;
	}
	
	public int getTipo() {
		return this.tipo;
	}
	
	public String getRichiesta() {
		return this.richiesta;
	}
	

}