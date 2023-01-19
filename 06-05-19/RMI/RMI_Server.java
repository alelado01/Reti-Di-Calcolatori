
/************************************
*     	  Ladomorzi Alessio       	*
*           RMI_Server.java         *
*                                   *
************************************/

// Implementazione del Server RMI

/*----- import ---- */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {
	private static final long serialVersionUID = 1L;

	// qui eventuali variabili e strutture dati

	// Costruttore
	public RMI_Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	// Eventuali metodi legati alla struttura dati

	/*
	 * public static void stampa() {
	 * System.out.println("Targa\tPatente\tTipo\tImages\n");
	 * 
	 * for (int i = 0; i < dim; i++) {
	 * System.out.println(prenotazioni[i].getTarga() + "\t" +
	 * prenotazioni[i].getPatente() + "/" + prenotazioni[i].getType() + "/" +
	 * prenotazioni[i].getImages()
	 * + "\t");
	 * }
	 * }
	 */

	// Implementazione dei metodi

	@Override
	public synchronized String[] lista_file(String direttorio) throws RemoteException {
		File dir = new File(direttorio);
		int j = 0;
		String result[];
		if (dir.exists() && dir.isDirectory() && dir.canRead()) {
			if (dir.list().length == 0) {
				throw new RemoteException("Nessun file presente\n");
			}
		} else
			throw new RemoteException("Direttorio inesistente\n");
		String[] tmp = new String[dir.listFiles().length];
		for (String s : dir.list()) {
			int num_consonanti = 0;
			s = s.toLowerCase();
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) != 'a' && s.charAt(i) != 'e' && s.charAt(i) != 'i' && s.charAt(i) != 'o'
						&& s.charAt(i) != 'u') {
					System.out.println(
							"Letta lettera " + s.charAt(i) + " = consonante, num consonanti=" + num_consonanti + "\n");
					num_consonanti++;
				}
				if (num_consonanti >= 3) {
					tmp[j++] = s;
					break;
				}
			}
		}
		result = new String[j];
		for (int i = 0; i < result.length; i++) {
			result[i] = tmp[i];
		}
		return result;
	}

	@Override
	public synchronized int numerazione_righe(String file) throws RemoteException {
		File f = new File(file);
		int num_linea = 1, result = 0;
		String line = null;
		if (!f.exists() || !f.isFile() || !f.canRead()) {
			return -1;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				if (num_linea % 2 != 0) {
					result++;
					line = result + ") " + line;
				}
				num_linea++;
				System.out.println(line);
			}
			br.close();
		} catch (IOException e) {
			System.err.println("Errore nella lettura o scrittura del file");
		}
		return result;
	}

	// Avvio del Server RMI
	public static void main(String[] args) {
		int registryPort = 1099;
		String registryHost = "localhost";
		String serviceName = "ServerRMI";

		// Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerRMI [REGISTRYPORT]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				registryPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Sintassi: ServerRMI [REGISTRYPORT], REGISTRYPORT intero");
				System.exit(2);
			}
		}

		// Qui inizializzo eventuali strutture dati

		// Registrazione del servizio
		String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;

		try {
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}