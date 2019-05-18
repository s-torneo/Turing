import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Sezione {

	public int sezione;
	public String utente; //utente che sta editando la sezione
	public boolean editata; //booleano che indica se la sezione è editata in questo momento o no
	public ReadWriteLock readWriteLock; /* usata per eseguire in mutua-esclusione la modifica e/o la lettura 
	delle variabili editore e editata */

	public Sezione(int sezione) {
		this.sezione=sezione;
		this.utente=null;
		this.editata=false;
		this.readWriteLock = new ReentrantReadWriteLock();
	}
}