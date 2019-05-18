import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;;

final class Strutture{
	public static ConcurrentHashMap<String, RegisteredUser> registrati = new ConcurrentHashMap<String, RegisteredUser>();
	public static ConcurrentHashMap<String, Documento> documenti = new ConcurrentHashMap<String, Documento>();
	public static ConcurrentHashMap<String, Gruppo> gruppi = new ConcurrentHashMap<String, Gruppo>();
	public static List<String> indirizzi = Collections.synchronizedList(new ArrayList<String>());
	public static ConcurrentHashMap<String, OnlineUser> online = new ConcurrentHashMap<String, OnlineUser>();
	public final static int maxgroup = 254;
	public final static String path = "C:/";
	public final static String estensione = ".txt";
}