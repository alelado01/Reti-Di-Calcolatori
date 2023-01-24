
/*----- import ---- */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
	 *
	 * }
	 */

	// Implementazione dei metodi

	@Override
	// synchronized
	public String[] lista_file_caratteri(char c1, char c2) throws RemoteException {
		File dir = new File("files/");
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
			s = s.toLowerCase();
			if (s.indexOf(c1) >= 0 && s.indexOf(c2) >= 0) {
				System.out.println("Lette entrambe le lettere\n");
				tmp[j] = s;
				j++;
			}
		}
		result = new String[j];
		for (int i = 0; i < result.length; i++) {
			result[i] = tmp[i];
		}
		return result;
	}

	@Override
	public int conta_occorrenze_interpunzione(String file) throws RemoteException {
		File f = new File(file);
		int result = 0;
		String line = null;
		if (!f.exists() || !f.isFile() || !f.canRead()) {
			return -1;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(i) == '.' || line.charAt(i) == ';' || line.charAt(i) == '!') {
						result++;
					}
				}
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