import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Worker implements Runnable{

	private Socket client;
	private Socket notify;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String clientAddress;
	private String username;

	public Worker(Socket client, Socket notify) {
		this.client=client;
		this.notify=notify;
		clientAddress = client.getInetAddress().getHostAddress();
		System.out.println("\r\nNew connection from " + clientAddress);
		try {
			this.in = new ObjectInputStream(client.getInputStream());
			this.out=new ObjectOutputStream (client.getOutputStream());
		} catch (IOException e) {
			return;
		} 
	}

	public RegisteredUser Cerca(String username) {
		return Strutture.registrati.get(username);
	}

	public Documento CercaDoc(String nome, String creatore) {
		return Strutture.documenti.get(nome+"-"+creatore);
	}

	public Sezione CercaSez(int sezione, String documento, String creatore) {
		Documento d = CercaDoc(documento,creatore);
		if(d!=null && sezione>0 && d.nsezioni>=sezione)
			return d.sezioni.get(sezione-1);
		return null;
	}

	public void SendResponse(String s, ObjectOutputStream out, int tipo) {
		Richiesta o = new Richiesta(tipo,s);
		try {
			out.writeObject(o);
			out.flush();  
		} catch (IOException e) {
			return;
		}
	}

	public String LeggiFile(String creatore, String documento, int sezione) throws IOException {
		Path p = Paths.get(Strutture.path+creatore, documento+sezione+Strutture.estensione);
		//creo un channel a cui gli associo il file richiesto
		FileChannel ch = FileChannel.open(p);
		long size=ch.size();
		ByteBuffer buf = ByteBuffer.allocate((int)size);
		//leggo il contenuto del file dal file channel e lo inserisco nel buffer
		ch.read(buf);
		buf.flip();
		String s = "";
		//metto il contenuto del buffer in una stringa
		while(buf.hasRemaining()){
			s+=(char) buf.get();
		}
		buf.clear();
		//chiudo il channel
		ch.close();
		return s;
	}

	public void ScriviFile(String[] dataSplit) {
		String creatore=dataSplit[2];
		String sezione=dataSplit[1];
		String doc=dataSplit[0];
		Path p = Paths.get(Strutture.path+creatore, doc+sezione+Strutture.estensione);
		String text = "";
		for(int i=3;i<dataSplit.length;i++) {
			text+=dataSplit[i]+" ";
		}
		//elimino l'ultimo carattere
		if(text.length()>0)
			text = text.substring(0, text.length()-1);
		//alloco il buffer
		ByteBuffer buffer = ByteBuffer.allocate(text.length());
		FileChannel out = null;
		try {
			out = FileChannel.open(p,StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			buffer.put(text.getBytes());
			buffer.flip(); //flip passa da modalità scrittura a lettura del buffer
			//leggo il contenuto del buffer e lo scrivo nel file
			while (buffer.hasRemaining())
				out.write(buffer);
			buffer.clear();
		} catch (IOException e) {
			Logout(username);
			return;
		}finally {ReleaseLock(dataSplit);}
	}

	public void ShowDoc(String[] dataSplit) throws IOException{
		String documento=dataSplit[0];
		String utente=dataSplit[1];
		String creatore=dataSplit[2];
		Documento d = CercaDoc(documento,creatore);
		//controllo che il documento esista
		if(d==null) {
			SendResponse("nodoc",out,-1);
			return;
		}
		d.readWriteLock.readLock().lock();
		//controllo che l'utente che vuole modificare sia invitato o creatore
		if(((!d.creatore.equals(utente))&&(!d.collaboratori.contains(utente)))){
			SendResponse("noperm",out,-1);
			d.readWriteLock.readLock().lock();
			return;
		}
		d.readWriteLock.readLock().unlock();
		int nsezioni = d.nsezioni;
		String s = "";
		List<Integer> editate = new ArrayList<>(); //creo un arrayList per contenere le sezioni editate
		for(int i=0;i<nsezioni;i++) {
			Sezione sez = CercaSez(i+1,documento,creatore); //cerco la sezione
			String str = LeggiFile(creatore, documento, i+1);//leggo il contenuto del file corrispondente alla sezione
			if(!str.equals(""))
				s+=str+" ";
			//controllo se la sezione è editata, in tal caso l'aggiungo all'arrayList creato
			sez.readWriteLock.readLock().lock();
			if(sez.editata==true)
				editate.add(i+1);
			sez.readWriteLock.readLock().unlock();
		}
		//elimino lo spazio finale da s
		if(s.length()>0)
			s = s.substring(0, s.length()-1);
		s+="- ";
		//aggiungo al contenuto da inviare al client, le sezioni editate
		if(editate.size()>0) {
			for(int j=0;j<editate.size();j++)
				s+=editate.get(j)+", ";
			//elimino gli ultimi due caratteri
			s = s.substring(0, s.length()-2);
		}
		//invio il contenuto del buffer al client
		SendResponse(s,out,-1);
	}

	public void ShowSez(String[] dataSplit) throws IOException{
		int sezione=0;
		try {
			sezione=Integer.parseInt(dataSplit[0]);
		}catch (NumberFormatException e) {
			SendResponse("errore",out,-1);
			return;
		}
		String documento=dataSplit[1];
		String utente=dataSplit[2];
		String creatore=dataSplit[3];
		Documento d = CercaDoc(documento,creatore);
		//controllo che il documento esista
		if(d==null) {
			SendResponse("nodoc",out,-1);
			return;
		}
		d.readWriteLock.readLock().lock();
		//controllo che l'utente che vuole modificare sia invitato o creatore
		if(((!d.creatore.equals(utente))&&(!d.collaboratori.contains(utente)))){
			SendResponse("noperm",out,-1);
			d.readWriteLock.readLock().unlock();
			return;
		}
		d.readWriteLock.readLock().unlock();
		Sezione sez = CercaSez(sezione,documento,creatore);
		//controllo che la sezione del documento esista
		if(sez==null) {
			SendResponse("nosez",out,-1);
			return;
		}
		String s=LeggiFile(creatore,documento,sezione); //leggo il contenuto del file corrispondente alla sezione
		//controllo se la sezione è editata, in tal caso aggiungo l'utente che la sta editando alla stringa da inviare
		sez.readWriteLock.readLock().lock();
		if(sez.editata==true)
			s+=" "+sez.utente;
		else s+=" -";
		sez.readWriteLock.readLock().unlock();
		//invio il contenuto al client
		SendResponse(s,out,-1);
	}

	public void ListaDoc(String[] dataSplit) throws IOException{
		String utente=dataSplit[0];
		RegisteredUser u=Cerca(utente);
		String collaborazioni="";
		synchronized(u.collaborazioni) {
			int size = u.collaborazioni.size();
			for(int i=0;i<size;i++) {
				Documento d = u.collaborazioni.get(i);
				collaborazioni+=d.nome+"-"+d.creatore+"-";
				d.readWriteLock.readLock().lock();
				for (String s : d.collaboratori)
					collaborazioni+=s+"-";
				d.readWriteLock.readLock().unlock();
				collaborazioni+=" ";
			}
		}
		SendResponse(collaborazioni,out,-1); 
	}

	public void ListaInviti(String[] dataSplit) throws IOException{
		String utente=dataSplit[0];
		RegisteredUser u=Cerca(utente);
		String inviti="";
		synchronized(u.inviti) {
			for(int i=0;i<u.inviti.size();i++)
				inviti+=u.inviti.get(i)+" ";
			u.inviti.removeAll(u.inviti);
		}
		SendResponse(inviti,out,-1);  
	}

	public void NewUser(String[] dataSplit) throws IOException{
		String doc=dataSplit[0];
		String creatore=dataSplit[2];
		Documento d = CercaDoc(doc,creatore);
		if(d==null) {
			SendResponse("nodoc",out,-1);
			return;
		}
		String utente=dataSplit[1]; //utente invitato
		RegisteredUser u=Cerca(utente); //cerco l'utente invitato tra quelli registrati
		//controllo se l'utente invitato esiste
		if(u==null) {
			SendResponse("nouser",out,-1);
			return;
		}
		//controllo che l'utente invitato non sia il creatore stesso
		if(d.creatore.equals(utente)){
			SendResponse("creator",out,-1);
			return;
		}
		//controllo che l'utente non sia stato già invitato
		d.readWriteLock.readLock().lock();
		if(d.collaboratori.contains(utente)){
			SendResponse("nodupl",out,-1);
			d.readWriteLock.readLock().unlock();
			return;
		}
		d.readWriteLock.readLock().unlock();
		//aggiungo l'utente alla lista dei collaboratori
		d.readWriteLock.writeLock().lock();
		d.collaboratori.add(utente);
		d.readWriteLock.writeLock().unlock();
		SendResponse("invitato",out,-1);
		//se l'utente invitato è online allora gli invio la notifica
		OnlineUser o = Strutture.online.get(utente);
		if(o != null) {
			o.l.lock();
			SendResponse(creatore+" "+doc,o.out,-1);
			o.l.unlock();
		}
		else
			//aggiungo il documento agli inviti di quell'utente
			u.inviti.add(doc);
		//aggiungo il documento alla lista delle collaborazioni di quell'utente
		u.collaborazioni.add(d);
	}

	public void RemoveAddress(Documento d) {
		//decremento il numero di sezioni in editing
		d.lsez.lock();
		d.inEditing--;
		System.out.println("sezioni in editing: "+d.inEditing);
		//se il numero di sezioni in editing è 0 allora
		if(d.inEditing==0){
			d.lsez.unlock();
			System.out.println("non ci sono sezioni in editing");
			Gruppo g = Strutture.gruppi.get(d.nome+"-"+d.creatore);
			try {
				//rimuovo l'indirizzo dalla struttura
				Strutture.indirizzi.remove(g.indirizzo);
			}catch(NullPointerException e) {}
			//e imposto a null l'indirizzo del gruppo
			g.indirizzo=null;
		}
		else 
			d.lsez.unlock();
	}

	public void ReleaseLock(String[] dataSplit) {
		String doc=dataSplit[0];
		int nsez = Integer.parseInt(dataSplit[1]);
		String creatore=dataSplit[2];
		Sezione s = CercaSez(nsez,doc,creatore);
		Documento d = CercaDoc(doc,creatore);
		ReentrantLock l = d.locks[nsez-1];
		l.unlock(); //rilascio la lock della sezione
		//in mutua-esclusione assegno null alla variabile editore e false alla variabile editata
		s.readWriteLock.writeLock().lock();
		s.utente=null;
		s.editata=false;
		s.readWriteLock.writeLock().unlock();
		//vedo se bisogna rimuovere l'indirizzo del gruppo
		RemoveAddress(d);
	}

	public String NewAddress() {
		String indirizzo;
		//prendo la mutua-esclusione sulla lista degli indirizzi utilizzati
		synchronized(Strutture.indirizzi) {
			if(Strutture.indirizzi.size()==Strutture.maxgroup)
				return null;
			do {
				int x = (int)(Math.random()*254)+1; //genero un numero casuale tra 1 e 254
				indirizzo = "224.1.1."+x; 
			}while(Strutture.indirizzi.contains(indirizzo));
			Strutture.indirizzi.add(indirizzo); //aggiungo l'indirizzo alla lista
		}
		return indirizzo;
	}

	public int NewPort() {
		int porta = (int)(Math.random()*4000)+2000;
		return porta;
	}

	public Gruppo CheckGroup(String nome, String creatore) {
		//cerco se è stato già assegnato un indirizzo al gruppo, altrimenti glielo assegno
		Gruppo g = Strutture.gruppi.get(nome+"-"+creatore);
		if(g.indirizzo==null) {
			g.indirizzo=NewAddress();
			g.porta=NewPort();
			System.out.println("assegnato al gruppo: "+ nome+"-"+creatore +" l'indirizzo: "+g.indirizzo);
		}
		System.out.println("gruppo: " +nome+"-"+creatore +" - indirizzo: "+g.indirizzo+" - porta:"+g.porta);
		return g;
	}

	public boolean ModDoc(String[] dataSplit) throws IOException{
		int sezione=0;
		String nome=dataSplit[0];
		String creatore=dataSplit[2];
		try {
			sezione=Integer.parseInt(dataSplit[1]);
		}catch (NumberFormatException e) {
			SendResponse("errore",out, -1);  
			return false;
		}
		Documento d = CercaDoc(nome,creatore);
		//controllo che il documento esista
		if(d==null) {
			SendResponse("nodoc",out,-1);
			return false;
		}
		d.readWriteLock.readLock().lock();
		//controllo che l'utente che vuole modificare sia invitato o creatore
		if(((!d.creatore.equals(this.username))&&(!d.collaboratori.contains(this.username)))){
			SendResponse("noperm",out,-1);
			d.readWriteLock.readLock().unlock();
			return false;
		}
		d.readWriteLock.readLock().unlock();
		Sezione sez = CercaSez(sezione,nome,creatore);
		//controllo che la sezione del documento esista
		if(sez==null) {
			SendResponse("nosez",out,-1);
			return false;
		}
		ReentrantLock l = d.locks[sezione-1];
		if(!l.tryLock()){
			SendResponse("editata",out,-1);
			return false;
		}
		//in mutua-esclusione assegno il nome utente alla variabile editore e true alla variabile editata
		sez.readWriteLock.writeLock().lock();
		sez.utente=this.username;
		sez.editata=true;
		sez.readWriteLock.writeLock().unlock();
		//aumento il numero di sezioni editate
		d.lsez.lock();
		d.inEditing++;
		d.lsez.unlock();
		String s = LeggiFile(creatore, nome, sezione);
		//invio il contenuto del buffer al client
		SendResponse(s,out,-1);
		Gruppo g = CheckGroup(nome, creatore);
		//invio l'indirizzo al client
		SendResponse(g.indirizzo,out,-1);
		//invio la porta al client
		SendResponse(String.valueOf(g.porta),out,-1);
		return true;
	}

	public void NewDoc(String[] dataSplit) throws IOException{
		int nsez=0;
		String nome=dataSplit[0];
		String creatore=dataSplit[2];
		try {
			nsez=Integer.parseInt(dataSplit[1]);
		}catch (NumberFormatException e) {
			SendResponse("errore",out,-1);
			return;
		}
		if(nsez==0){
			SendResponse("errore",out,-1);
			return;
		}
		Documento nuovo = new Documento(nome,nsez,creatore);
		//controllo che il documento non esisti già
		if(Strutture.documenti.putIfAbsent(nome+"-"+creatore,nuovo)!=null) {
			SendResponse("esistente",out,-1);
			return;
		}
		SendResponse("creato",out,-1);
		//aggiungo il documento alle collaborazioni dell'utente
		RegisteredUser u = Cerca(creatore);
		u.collaborazioni.add(nuovo);
		//creo il gruppo per la chat del documento
		Gruppo g = new Gruppo();
		Strutture.gruppi.putIfAbsent(nome+"-"+creatore,g);
	}

	public void Login(String[] dataSplit) throws IOException{
		String username = dataSplit[0];
		String password = dataSplit[1];
		RegisteredUser t=Cerca(username); //cerco l'utente nell'hash degli utenti registrati
		this.username=username;
		//se lo trovo
		if(t!=null) {
			//controllo se la password coincida con quella immessa dall'utente
			if(!t.password.equals(password))
				//se le due password non corrispondono invio una risposta di errore
				SendResponse("wrong",out,-1);
			else {
				//altrimenti inserisco l'utente nella hash degli utenti online se non è già presente
				OnlineUser o = new OnlineUser();
				if(Strutture.online.putIfAbsent(username, o)==null) {
					//invio un messaggio di successo
					SendResponse("true",out,-1);
					//imposto il socket delle notifiche dell'utente
					o.notifiche = this.notify;
					//creo un nuovo output stream per l'utente
					o.out=new ObjectOutputStream (o.notifiche.getOutputStream());
				}
				else //se è già presente
					SendResponse("loggato",out,-1); //invio un messaggio che indica che l'utente è già online
			}
		}
		else
			SendResponse("false",out,-1); //invio un messaggio che indica che l'utente non è registrato
	}

	public void SendTermination(){
		OnlineUser t = null;
		try {
			t=Strutture.online.get(this.username);
			t.l.lock();
		}catch(NullPointerException e) {
			return;
		}
		//dico al thread delle notifiche di quel client che deve terminare
		SendResponse("",t.out,-2);
		try {
			t.out.close();
		} catch (IOException e) {
			return;
		}finally {t.l.unlock();}
	}

	public void Logout(String username){
		Strutture.online.remove(username);
		try {
			this.in.close();
			this.out.close();
			this.client.close();
		} catch (IOException e) {
			return;
		}
	}

	public void run() {
		Object data = null;
		while(true) {
			try {
				this.client.setSoTimeout(300000); //5 min timeout per il client
			}catch (SocketException e2) {
				return;
			} 
			try {
				data = in.readObject(); 
				this.client.setSoTimeout(0);
			} catch (ClassNotFoundException e) {
				Logout(this.username);
				return;
			} catch (SocketException e) { //nel caso in cui il client crasha
				Logout(this.username);
				return;
			} catch (SocketTimeoutException e) {
				System.out.println("timeout client: "+this.username);
				SendTermination(); //mando un messaggio di terminazione al thread notifiche
				Logout(this.username);
				return;
			}catch(IOException e) {
				Logout(this.username);
				return;
			}
			Richiesta r = (Richiesta) data; 
			System.out.println("\r\nMessage from " + clientAddress + ": " + r.getTipo()+" "+r.getRichiesta());
			String dataSplit[]= r.getRichiesta().split(" ");
			try {
				switch(r.getTipo()) {
				case 0: {Login(dataSplit);break;}
				case 1: {
					SendTermination();//mando un messaggio di terminazione al thread notifiche
					Logout(dataSplit[0]);
					return;
				}
				case 2: {NewDoc(dataSplit);break;}
				case 3: {
					if(!ModDoc(dataSplit))
						break;
					this.client.setSoTimeout(180000); //3 min timeout per la edit
					try {
						data = in.readObject();
					} catch (ClassNotFoundException | SocketException e1) {
						ReleaseLock(dataSplit);
						Logout(this.username);
						return;
					}catch (SocketTimeoutException e1) {
						ReleaseLock(dataSplit);
						SendTermination(); //mando un messaggio di terminazione al thread notifiche
						Logout(this.username);
						System.out.println("timeout edit per l'utente: "+this.username);
						return;
					}
					this.client.setSoTimeout(0);
					r = (Richiesta) data; 
					System.out.println("\r\nMessage from " + clientAddress + ": " + r.getTipo()+" "+r.getRichiesta());
					dataSplit= r.getRichiesta().split(" ");
					if(r.getTipo()==4)
						ScriviFile(dataSplit);
					else if(r.getTipo()==10)
						ReleaseLock(dataSplit);
					break;
				}
				case 5: {NewUser(dataSplit);break;}
				case 6: {ListaInviti(dataSplit);break;}
				case 7: {ShowSez(dataSplit);break;}
				case 8: {ListaDoc(dataSplit);break;}
				case 9: {ShowDoc(dataSplit);break;}
				case 10: {ReleaseLock(dataSplit);break;}
				}
			}catch(IOException e2) {
				Logout(this.username);
				return;
			}
		}
	}
}