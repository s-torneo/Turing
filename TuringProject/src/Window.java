import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.*;

public class Window {

	public Client client;
	public boolean nochat;
	public RegistrationService serverObject;

	public Window (RegistrationService serverObject) {
		this.serverObject = serverObject;
		this.nochat = false;
		this.client = null;
		Principale();
	}

	public void CloseWindow(JFrame b, JFrame w) {
		//CHIUSURA FINESTRA
		b.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				w.setVisible(true);
			}
		});
	}
	
	public void SessioneScaduta(JFrame w) {
		client.Close();
		JOptionPane.showMessageDialog(w,"Sessione scaduta","Warning",JOptionPane.WARNING_MESSAGE);
	}
	
	public void GestioneFinestra(JFrame yes, JFrame no) {
		yes.setVisible(true);
		no.setVisible(false);
	}

	public void Documento(JFrame f, String username) {
		JFrame w=new JFrame("Turing - "+username);
		JButton bd = new JButton("Crea Documento");  
		bd.setBounds(10,10, 200,30); 
		JButton bd2 = new JButton("Modifica Documento");  
		bd2.setBounds(10,50, 200,30); 
		JButton bd3 = new JButton("Logout");  
		bd3.setBounds(10,250, 200,30);
		JButton bd4 = new JButton("Invita Utente");  
		bd4.setBounds(10,90, 200,30);
		JButton bd5 = new JButton("Visualizza Sezione");  
		bd5.setBounds(10,170, 200,30); 
		JButton bd6 = new JButton("Lista Documenti");  
		bd6.setBounds(10,130, 200,30); 
		JButton bd7 = new JButton("Visualizza Documento");  
		bd7.setBounds(10,210, 200,30);
		w.add(bd); w.add(bd2);  w.add(bd3); w.add(bd4); w.add(bd5); w.add(bd6); w.add(bd7);
		w.setSize(500,350);    
		w.setLayout(null);    
		w.setVisible(true);
		//CHIUSURA FINESTRA
		w.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					client.Logout(username);
				}catch (SocketException e1) {
					SessioneScaduta(w);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(w,"Errore di Logout.","Warning",JOptionPane.WARNING_MESSAGE);
				}finally {
					GestioneFinestra(f,w);
					client.Close();
				}
				return;
			}
		});
		bd.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				JFrame nd=new JFrame("Nuovo Documento");
				final JLabel label = new JLabel();            
				label.setBounds(20,150, 200,50);    
				JLabel l1=new JLabel("Nome documento");    
				l1.setBounds(20,20, 150,30);    
				final JTextField nome = new JTextField();  
				nome.setBounds(130,20, 100,30);    
				JLabel l2=new JLabel("Numero sezioni");    
				l2.setBounds(20,75, 150,30);   
				final JTextField sez = new JTextField();   
				sez.setBounds(130,75,100,30); 
				JButton cd = new JButton("Crea");  
				cd.setBounds(130,120, 100,30); 
				nd.setBounds(220,120, 100,30); 
				nd.add(nome); nd.add(l1); nd.add(label); nd.add(l2); nd.add(cd); nd.add(sez);  
				nd.setSize(500,300);    
				nd.setLayout(null);    
				GestioneFinestra(nd,w);
				//GESTIONE CHIUSURA FINESTRA
				CloseWindow(nd,w);
				//NUOVO DOCUMENTO
				cd.addActionListener(new ActionListener() {  
					public void actionPerformed(ActionEvent e) { 
						String nomefile = nome.getText().trim();  
						String sezioni = sez.getText().trim();
						String risposta=null;
						if(nomefile.length()==0||sezioni.length()==0) {
							JOptionPane.showMessageDialog(w,"Campi inseriti non validi.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}
						try {
							risposta=client.NewDoc(nomefile,sezioni,username);
						}catch (SocketException e1) {
							SessioneScaduta(w);
							GestioneFinestra(f,nd);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(w,"Errore nella creazione di un nuuovo documento.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}  
						if(risposta.equals("creato"))
							JOptionPane.showMessageDialog(w,"Documento creato con successo.\n\nNome: "+nomefile+"\nNumero di sezioni: "+sezioni);
						else if(risposta.equals("errore")) {
							JOptionPane.showMessageDialog(w,"Numero di sezioni inserite non valido.","Warning",JOptionPane.WARNING_MESSAGE);
						}
						else
							JOptionPane.showMessageDialog(w,"Documento già esistente","Warning",JOptionPane.WARNING_MESSAGE);
						nd.setLayout(null);    
						GestioneFinestra(w,nd);
					} 
				});
			}
		});  
		bd2.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				JFrame md=new JFrame("Modifica Documento");
				final JLabel label = new JLabel();            
				label.setBounds(20,150, 200,50);    
				JLabel l1=new JLabel("Creatore");    
				l1.setBounds(20,20, 150,30);    
				final JTextField creator = new JTextField();  
				creator.setBounds(130,20, 100,30);    
				JLabel l2=new JLabel("Nome Documento");    
				l2.setBounds(20,75, 150,30);   
				final JTextField nome = new JTextField();   
				nome.setBounds(130,75,100,30); 
				JLabel l3=new JLabel("Sezione");    
				l3.setBounds(20,130, 150,30);   
				final JTextField sez = new JTextField();   
				sez.setBounds(130,130,100,30); 
				JButton apri = new JButton("Apri");  
				apri.setBounds(130,180, 100,30); 
				md.setBounds(220,120, 100,30); 
				md.add(nome); md.add(creator); md.add(l3); md.add(l1); md.add(l2); md.add(apri); md.add(sez);  
				md.setSize(500,300);    
				md.setLayout(null);    
				GestioneFinestra(md,w);
				//GESTIONE CHIUSURA FINESTRA
				CloseWindow(md,w);
				//MODIFICA DOCUMENTO
				apri.addActionListener(new ActionListener() {  
					String creatore = null;
					public void actionPerformed(ActionEvent e) { 
						creatore = creator.getText().trim();
						String nomefile = nome.getText().trim();  
						String sezione = sez.getText().trim();
						String risposta=null;
						if(nomefile.length()==0||sezione.length()==0||creatore.length()==0) {
							JOptionPane.showMessageDialog(w,"Campi inseriti non validi.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,md);
							return;
						}
						try {
							risposta=client.ModDoc(nomefile,sezione,creatore);
						}catch (SocketException e1) {
							SessioneScaduta(w);
							w.setVisible(false);
							GestioneFinestra(f,md);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(w,"Errore nella modifica di un documento.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,md);
							return;
						} 
						if(risposta.equals("nodoc")) {
							JOptionPane.showMessageDialog(w,"Documento non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						}
						else if(risposta.equals("nosez")) {
							JOptionPane.showMessageDialog(w,"Sezione non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						}
						else if(risposta.equals("noperm")) {
							JOptionPane.showMessageDialog(w,"L'utente non può modificare il documento.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						}
						else if(risposta.equals("errore")) {
							JOptionPane.showMessageDialog(w,"Parametro inserito non valido.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						}
						else if(risposta.equals("editata")) {
							JOptionPane.showMessageDialog(w,"Sezione non modificabile al momento.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						}
						String indirizzo = null;
						int porta = 0;
						try {
							indirizzo = client.GetAddress();
							porta = client.GetPort();
						}catch (SocketException e1) {
							SessioneScaduta(w);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							return;
						} catch (Exception e2) {
							JOptionPane.showMessageDialog(w,"Errore in fase di ottenimento dell'indirizzo del gruppo.","Warning",JOptionPane.WARNING_MESSAGE);
							md.setLayout(null);   
							GestioneFinestra(w,md);
							client.Close();
							return;
						}
						//APRO FILE
						JFrame md2=new JFrame(nomefile+" Sezione: "+sezione);
						md2.setSize(610,450);   
						md2.setLayout(null); 
						md2.setVisible(true);
						//area di lettura/scrittura
						JPanel jp = new JPanel();  
						jp.setBounds(5, 5, 400, 300);
						JTextArea area=new JTextArea(risposta);  
						jp.add(area);
						JScrollPane scroll = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						scroll.setBounds(20,15,450,280);
						md2.add(scroll);
						//area per il bottone
						JPanel jp2 = new JPanel(); 
						jp2.setBounds(5, 310, 450, 100);
						JButton sd = new JButton("Salva"); 
						jp2.add(sd);
						//aggiungo i panel alla finestra
						md2.add(jp); md2.add(jp2);
						md.setLayout(null);    
						md.setVisible(false);
						JFrame chat = new JFrame("Chat del documento: "+nomefile);
						JTextArea area2=new JTextArea(); 
						if(indirizzo==null) {
							JOptionPane.showMessageDialog(w,"Numero max di gruppi raggiunti.","Warning",JOptionPane.WARNING_MESSAGE);
							nochat=true;
						}
						else
							client.NewChat(indirizzo,area2,porta);
						//SALVA MODIFICHE
						sd.addActionListener(new ActionListener() {  
							public void actionPerformed(ActionEvent e) { 
								String text=area.getText().trim();
								boolean risposta = true;
								try {
									risposta=client.ScriviFile(text,nomefile,sezione,creatore);
									if(!nochat)
										client.CloseChat();
								} catch (IOException e1) {
									JOptionPane.showMessageDialog(w,"Errore nel salvare il file.","Warning",JOptionPane.WARNING_MESSAGE);
								}
								if(!risposta) {
									JOptionPane.showMessageDialog(w,"Timeout per la sezione!","Warning",JOptionPane.WARNING_MESSAGE);
									w.setVisible(false);
									chat.setVisible(false);
									GestioneFinestra(f,md2);
									client.Close();
									return;
								}
								chat.setVisible(false);
								md2.setLayout(null);      
								GestioneFinestra(w,md2);
							} 
						});
						//CHIUSURA FINESTRA
						md2.addWindowListener(new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								boolean risposta = true;
								try {
									risposta=client.ReleaseLock(nomefile,sezione,creatore);
									if(!nochat)
										client.CloseChat();
								} catch (IOException e1) {
									JOptionPane.showMessageDialog(w,"Errore nella fase di chiusura della sezione.","Warning",JOptionPane.WARNING_MESSAGE);
									w.setVisible(false);
									chat.setVisible(false);
									GestioneFinestra(f,md2);
									client.Close();
									return;
								}
								if(!risposta) {
									JOptionPane.showMessageDialog(w,"Timeout per la sezione!","Warning",JOptionPane.WARNING_MESSAGE);
									w.setVisible(false);
									chat.setVisible(false);
									GestioneFinestra(f,md2);
									client.Close();
									return;
								}
								GestioneFinestra(w,chat);
							}
						});
						//APRO CHAT 
						chat.setSize(1000,1000);   
						chat.setLayout(null);
						//area per leggere i messaggi
						JPanel jpc = new JPanel();  
						jpc.setBounds(5, 5, 400, 300);
						area2.setEditable(false); 
						jpc.add(area2);
						JScrollPane scroll2 = new JScrollPane(area2,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						scroll2.setBounds(20,15,450,280);
						chat.add(scroll2);
						//area per scrivere i messaggi
						JTextArea area3=new JTextArea();  
						area3.setBounds(20,300,550,70);
						//bottone
						JButton invia = new JButton("Invia");  
						invia.setBounds(20,375,100,30);
						chat.add(jpc); chat.add(invia); chat.add(area3);
						//imposto la dimensione e la posizione
						chat.setSize(615,500); 
						chat.setLocation(20,500);
						if(!nochat)
							chat.setVisible(true);
						invia.addActionListener(new ActionListener() {  
							public void actionPerformed(ActionEvent e) { 
								String text=area3.getText().trim();  
								if(!client.SendMessage(text,username))
									chat.setVisible(false);
								area3.setText("");
							} 
						});
					} 
				});
			}
		});  
		//LOGOUT
		bd3.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				try {
					client.Logout(username);
				}catch (SocketException e1) {
					SessioneScaduta(w);
					GestioneFinestra(f,w);
					return;
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(f,"Errore in fase di Logout.","Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}  
				JOptionPane.showMessageDialog(w,"Logout avvenuto con successo.");
				GestioneFinestra(f,w);
				client.Close();
				return;
			} 
		});   
		bd4.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				JFrame nd=new JFrame("Invita utente");
				final JLabel label = new JLabel();            
				label.setBounds(20,150, 200,50);    
				JLabel l1=new JLabel("Nome documento");    
				l1.setBounds(20,20, 150,30);    
				final JTextField doc = new JTextField();  
				doc.setBounds(130,20, 100,30);    
				JLabel l2=new JLabel("Nome utente");    
				l2.setBounds(20,75, 150,30);   
				final JTextField utente = new JTextField();   
				utente.setBounds(130,75,100,30); 
				JButton i = new JButton("Invita");  
				i.setBounds(130,120, 100,30); 
				nd.setBounds(220,120, 100,30); 
				nd.add(doc); nd.add(l1); nd.add(label); nd.add(l2); nd.add(i); nd.add(utente);  
				nd.setSize(500,300);    
				nd.setLayout(null);    
				GestioneFinestra(nd,w);
				CloseWindow(nd,w);
				//INVITA
				i.addActionListener(new ActionListener() {  
					public void actionPerformed(ActionEvent e) { 
						String ndoc = doc.getText().trim();  
						String nutente = utente.getText().trim();
						if(ndoc.length()==0||nutente.length()==0) {
							JOptionPane.showMessageDialog(w,"Campi inseriti non validi.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}
						String risposta=null;
						try {
							risposta=client.NewUser(ndoc,nutente,username);
						}catch (SocketException e1) {
							SessioneScaduta(w);
							GestioneFinestra(f,nd);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(w,"Errore nell'invito di un utente.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}  
						if(risposta.equals("invitato"))
							JOptionPane.showMessageDialog(w,"Utente invitato con successo.\n\nNome Documento: "+ndoc+"\nNome Utente: "+nutente);
						else if(risposta.equals("nodoc"))
							JOptionPane.showMessageDialog(w,"Documento non esistente","Warning",JOptionPane.WARNING_MESSAGE);
						else if(risposta.equals("nouser"))
							JOptionPane.showMessageDialog(w,"Utente non esistente","Warning",JOptionPane.WARNING_MESSAGE);
						else if(risposta.equals("nodupl"))
							JOptionPane.showMessageDialog(w,"L'utente è stato già invitato.","Warning",JOptionPane.WARNING_MESSAGE);
						else if(risposta.equals("creator"))
							JOptionPane.showMessageDialog(w,"L'utente invitato è il creatore stesso.","Warning",JOptionPane.WARNING_MESSAGE);
						nd.setLayout(null);    
						GestioneFinestra(w,nd);
					} 
				});
			}
		});
		//SHOW SEZIONE
		bd5.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) { 
				JFrame nd=new JFrame("Visualizza Sezione");
				final JLabel label = new JLabel();            
				label.setBounds(20,150, 200,50);    
				JLabel l1=new JLabel("Creatore");    
				l1.setBounds(20,20, 150,30);    
				final JTextField creator = new JTextField();  
				creator.setBounds(130,20, 100,30);    
				JLabel l2=new JLabel("Nome Documento");    
				l2.setBounds(20,75, 150,30);   
				final JTextField nome = new JTextField();   
				nome.setBounds(130,75,100,30); 
				JLabel l3=new JLabel("Sezione");    
				l3.setBounds(20,130, 150,30);   
				final JTextField sez = new JTextField();   
				sez.setBounds(130,130,100,30); 
				JButton cd = new JButton("Apri");  
				cd.setBounds(130,180, 100,30);  
				nd.setBounds(220,120, 100,30); 
				nd.add(nome); nd.add(creator); nd.add(l1); nd.add(l3); nd.add(label); nd.add(l2); nd.add(cd); nd.add(sez);  
				nd.setSize(500,300);    
				nd.setLayout(null);    
				GestioneFinestra(nd,w);
				//GESTIONE CHIUSURA FINESTRA
				CloseWindow(nd,w);
				cd.addActionListener(new ActionListener() {  
					public void actionPerformed(ActionEvent e) { 
						String creatore = creator.getText().trim(); 
						String documento = nome.getText().trim();  
						String sezione = sez.getText().trim();
						if(documento.length()==0||sezione.length()==0||creatore.length()==0) {
							JOptionPane.showMessageDialog(w,"Campi inseriti non validi.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}
						String risposta=null;
						try {
							risposta=client.ShowSez(sezione,documento,username,creatore);
						}catch (SocketException e1) {
							SessioneScaduta(w);
							GestioneFinestra(f,nd);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(w,"Errore nella visualizzazione di una sezione.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}  
						if(risposta.equals("nodoc")) {
							JOptionPane.showMessageDialog(w,"Documento non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						else if(risposta.equals("nosez")) {
							JOptionPane.showMessageDialog(w,"Sezione non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						else if(risposta.equals("noperm")) {
							JOptionPane.showMessageDialog(w,"L'utente non può visualizzare il documento.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						else if(risposta.equals("errore")) {
							JOptionPane.showMessageDialog(w,"Parametro inserito non valido.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						String data2[]=risposta.split(" ");
						String contenuto="";
						int size=data2.length;
						for(int i=0;i<size-1;i++)
							contenuto+=data2[i]+" ";
						JFrame show=new JFrame("Sezione: "+sezione+" del documento: "+documento);
						show.setSize(610,450);   
						show.setLayout(null); 
						show.setVisible(true);
						//area per leggere il contenuto
						JPanel jp = new JPanel();  
						jp.setBounds(5, 5, 400, 300);
						JTextArea area=new JTextArea(contenuto); 
						area.setEditable(false);
						jp.add(area);
						JScrollPane scroll = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						scroll.setBounds(20,15,450,280);
						show.add(scroll);
						//area per info su chi sta editando
						JPanel jp2 = new JPanel(); 
						jp2.setBounds(5, 310, 450, 100);
						JLabel l1 = null;
						if(data2[size-1].equals("-"))
							l1=new JLabel("Sezione non editata in questo momento.");
						else 
							l1=new JLabel(data2[size-1]+" sta editando questa sezione.");
						jp2.add(l1);
						show.add(jp); show.add(jp2);
						nd.setLayout(null);    
						nd.setVisible(false);
						//GESTIONE CHIUSURA FINESTRA
						CloseWindow(show,w);
					} 
				});  
			} 
		});
		//SHOW DOCUMENTO
		bd7.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) { 
				JFrame nd=new JFrame("Visualizza Documento");
				final JLabel label = new JLabel();            
				label.setBounds(20,150, 200,50);    
				JLabel l1=new JLabel("Creatore");    
				l1.setBounds(20,20, 150,30);    
				final JTextField creator = new JTextField();  
				creator.setBounds(130,20, 100,30);    
				JLabel l2=new JLabel("Nome Documento");    
				l2.setBounds(20,75, 150,30);   
				final JTextField nome = new JTextField();   
				nome.setBounds(130,75,100,30); 
				JButton cd = new JButton("Apri");  
				cd.setBounds(130,130, 100,30);  
				nd.setBounds(220,120, 100,30); 
				nd.add(nome); nd.add(creator); nd.add(l1); nd.add(label); nd.add(l2); nd.add(cd);
				nd.setSize(500,300);    
				nd.setLayout(null);    
				GestioneFinestra(nd,w);
				CloseWindow(nd,w); //GESTIONE CHIUSURA FINESTRA
				cd.addActionListener(new ActionListener() {  
					public void actionPerformed(ActionEvent e) { 
						String creatore = creator.getText().trim(); 
						String documento = nome.getText().trim();  
						String risposta=null;
						if(creatore.length()==0||documento.length()==0) {
							JOptionPane.showMessageDialog(w,"Campi inseriti non validi.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}
						try {
							risposta=client.ShowDoc(documento,username,creatore);
						}catch (SocketException e1) {
							SessioneScaduta(w);
							GestioneFinestra(f,nd);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(w,"Errore nella visualizzazione di un documento.","Warning",JOptionPane.WARNING_MESSAGE);
							GestioneFinestra(w,nd);
							return;
						}  
						if(risposta.equals("nodoc")) {
							JOptionPane.showMessageDialog(w,"Documento non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						else if(risposta.equals("noperm")) {
							JOptionPane.showMessageDialog(w,"L'utente non può visualizzare il documento.","Warning",JOptionPane.WARNING_MESSAGE);
							nd.setLayout(null);   
							GestioneFinestra(w,nd);
							return;
						}
						String data2[]=risposta.split("-");
						String cont[]=data2[0].split(" ");
						String contenuto="";
						int size=cont.length;
						for(int i=0;i<size;i++)
							contenuto+=cont[i]+" ";
						JFrame show=new JFrame("Documento: "+documento+" creato da: "+creatore);  
						show.setSize(610,450);   
						show.setLayout(null); 
						show.setVisible(true);
						//area per leggere il contenuto
						JPanel jp = new JPanel();  
						jp.setBounds(5, 5, 400, 300);
						JTextArea area=new JTextArea(contenuto); 
						area.setEditable(false);
						jp.add(area);
						JScrollPane scroll = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						scroll.setBounds(20,15,450,280);
						show.add(scroll);
						//area per info sulle sezioni
						JPanel jp2 = new JPanel(); 
						jp2.setBounds(5, 310, 450, 100);
						JLabel l1 = null;
						if(!data2[1].equals(" "))
							l1=new JLabel("Sezioni in fase di editing: "+data2[1]);
						else
							l1=new JLabel("Nessuna sezione è in fase di editing.");
						jp2.add(l1);
						show.add(jp); show.add(jp2);
						nd.setLayout(null);    
						nd.setVisible(false);
						//GESTIONE CHIUSURA FINESTRA
						CloseWindow(show,w);
					} 
				});  
			} 
		});
		//LISTA DOCUMENTI
		bd6.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				String risposta[]=null;
				try {
					risposta=client.ListaDoc(username);
				}catch (SocketException e1) {
					SessioneScaduta(w);
					GestioneFinestra(f,w);
					return;
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(w,"Errore nella visualizzazione della lista di documenti.","Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				JFrame ld=new JFrame("Lista Documenti di: "+username);
				if(risposta[0].length()==0) {
					JOptionPane.showMessageDialog(w,"Non collabori ad alcun documento.","Warning",JOptionPane.WARNING_MESSAGE);
					ld.setLayout(null);    
					ld.setVisible(false);
					return;
				}
				final JLabel l1 = new JLabel("Lista dei documenti a cui collabori: ");            
				l1.setBounds(10,1, 200,50);   
				for(int i=0, j=15;i<risposta.length;i++,j+=15) {
					String doc[] = risposta[i].split("-");
					String riga = "- "+doc[0] + " Creato da: "+doc[1];
					if(doc.length>2) {
						riga+=" Collaboratori: ";
						for(int k=2;k<doc.length;k++)
							riga+=doc[k].toString()+", ";
						//elimino gli ultimi due caratteri
						riga = riga.substring(0, riga.length()-2);
					}
					final JLabel l = new JLabel(riga);            
					l.setBounds(10,j, 550,80); 
					ld.add(l);
				}
				ld.add(l1); 
				ld.setSize(500,300);    
				ld.setLayout(null);    
				GestioneFinestra(ld,w);
				//GESTIONE CHIUSURA FINESTRA
				CloseWindow(ld,w);
			}
		});
	}

	public void Principale() {
		JFrame f=new JFrame("Turing");    
		final JLabel label = new JLabel();            
		label.setBounds(20,150, 200,50);  
		final JPasswordField value = new JPasswordField();   
		value.setBounds(100,75,100,30);   
		JLabel l1=new JLabel("Username:");    
		l1.setBounds(20,20, 80,30);    
		JLabel l2=new JLabel("Password:");    
		l2.setBounds(20,75, 80,30);    
		JButton b = new JButton("Registra");  
		b.setBounds(100,120, 100,30); 
		JButton login = new JButton("Login");  
		b.setBounds(100,120, 100,30); 
		login.setBounds(220,120, 100,30); 
		final JTextField text = new JTextField();  
		text.setBounds(100,20, 100,30);    
		f.add(value); f.add(l1); f.add(label); f.add(l2); f.add(b); f.add(login); f.add(text);  
		f.setSize(500,300);    
		f.setLayout(null);    
		f.setVisible(true);
		//CHIUSURA FINESTRA
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//controllo se è stato fatto il login
				if(client!=null)
					client.Close();
				System.exit(0);
			}
		});
		//REGISTRAZIONE
		b.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				String username = text.getText().trim(); 
				String password = new String(value.getPassword());
				if(username.length()==0 || password.length()==0) {
					JOptionPane.showMessageDialog(f,"Username o password non validi.","Warning",JOptionPane.WARNING_MESSAGE);
				}
				else {
					boolean flag = true;
					try {
						flag=serverObject.Registra(username,password);
					} catch (RemoteException e1) {
						JOptionPane.showMessageDialog(f,"Errore in fase di registrazione.","Warning",JOptionPane.WARNING_MESSAGE);
						return;
					} 
					if(flag) {
						JOptionPane.showMessageDialog(f,"Utente registrato con successo.\n\nUsername: "+username+"\nPassword: "+password);
						text.setText("");
						value.setText("");
					}
					else
						JOptionPane.showMessageDialog(f,"Username già esistente.","Warning",JOptionPane.WARNING_MESSAGE);
				}
			}  
		});   
		//LOGIN
		login.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {
				try {
					client = new Client ();
				}catch (Exception e2) {
					JOptionPane.showMessageDialog(f,"Non è possibile connettersi al Server.","Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				String username = text.getText().trim(); 
				String password = new String(value.getPassword()).trim();;
				String risposta=null;
				System.out.println("\r\nConnected to Server: " + client.socket.getInetAddress());
				if(username.length()==0 || password.length()==0) {
					JOptionPane.showMessageDialog(f,"Username o password non validi.","Warning",JOptionPane.WARNING_MESSAGE);
				}
				else {
					try {
						risposta=client.Login(username,password);
					}catch (SocketException e1) {
						SessioneScaduta(f);
						return;
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(f,"Errore in fase di Login.","Warning",JOptionPane.WARNING_MESSAGE);
						return;
					}  
					if(risposta.equals("true")) {
						JOptionPane.showMessageDialog(f,"Utente loggato con successo.");
						f.setVisible(false); //you can't see me!
						String risp[]=null;
						try {
							risp=client.Inviti(username);
						} catch (SocketException e1) {
							SessioneScaduta(f);
							f.setVisible(true);
							return;
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(f,"Errore nella visualizzazione degli inviti.","Warning",JOptionPane.WARNING_MESSAGE);
							return;
						}
						if(!risp[0].equals("")) {
							risposta="";
							for(int i=0;i<risp.length;i++)
								risposta+="- "+risp[i]+"\n";
							JOptionPane.showMessageDialog(f,"Sei stato invitato a collaborare ai seguenti documenti:	\n"+risposta);
						}
						text.setText("");
						value.setText("");
						Documento(f,username);
					}
					else if(risposta.equals("wrong"))
						JOptionPane.showMessageDialog(f,"Username o password errati.","Warning",JOptionPane.WARNING_MESSAGE);
					else if(risposta.equals("loggato")) {
						JOptionPane.showMessageDialog(f,"Utente già loggato.","Warning",JOptionPane.WARNING_MESSAGE);
						client.Close();
					}
					else {
						JOptionPane.showMessageDialog(f,"Utente non esistente.","Warning",JOptionPane.WARNING_MESSAGE);
						client.Close();
					}
				}
			}  
		});   
	}
}