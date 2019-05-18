import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Documento {
	
	public String nome;
	public List<Sezione> sezioni;
	public int nsezioni;
	public String creatore;
	public List<String> collaboratori;
	public ReentrantLock[] locks; //array usato per utilizzare in mutua-esclusione le sezioni
	public ReadWriteLock readWriteLock; //usata per leggere e scrivere l'array collaboratori
 	public ReentrantLock lsez; //usata per eseguire in mutua-esclusione gli incrementi di inEditing
	public int inEditing; //indica il numero di sezioni in editing

	public void CreaFile(String creatore, String sezione) {
		String fileData = "";
		try {
			Files.createDirectories(Paths.get(Strutture.path+creatore));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		Path p = Paths.get(Strutture.path+creatore, sezione+Strutture.estensione);
		try {
			Files.write(p, fileData.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public Documento(String nome, int sezioni, String creatore) {
		this.nome=nome;
		this.nsezioni=sezioni;
		this.creatore=creatore;
		this.collaboratori=new ArrayList<String>();
		this.sezioni=new ArrayList<Sezione>();
		for(int i=0;i<sezioni;i++) {
			String sezione=nome+""+(i+1);
			Sezione s = new Sezione(i+1);
			this.sezioni.add(s);
			CreaFile(creatore, sezione);
		}
		//inizializzo l'array di lock per le sezioni
		this.locks = new ReentrantLock[sezioni];
		for (int i = 0; i < sezioni; i++)
            this.locks[i] = new ReentrantLock();
		this.readWriteLock = new ReentrantReadWriteLock();
		this.lsez = new ReentrantLock();
		this.inEditing=0;
	}
}