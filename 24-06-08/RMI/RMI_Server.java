
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

	public static void stampa() {

	}

	// Implementazione dei metodi

	@Override
	public synchronized String[] lista_nomi_file_contenenti_parola_in_linea(String dir, String parola)
			throws RemoteException {
		File f = new File(dir);
		int i = 0, count_files = 0;
		String line = null;
		if (!f.isDirectory() || !f.exists() || !f.canRead()) {
			System.out.println("Directory inesistente o illeggibile\n");
			System.exit(2);
		}
		// Conto i file per inizializzare l'array di stringhe
		for (File file : f.listFiles()) {
			System.out.println(file.getName());
			try {
				BufferedReader br = new BufferedReader(new FileReader(file.getName()));
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					if (line.contains(parola)) {
						count_files++;
						break;
					}
				}
				br.close();
				System.out.println(count_files);

			} catch (IOException e) {
				System.err.println("Errore nella lettura del file\n");
				System.exit(2);
			}
		}

		String[] files = new String[count_files];

		for (File file : f.listFiles()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file.getName()));
				while ((line = br.readLine()) != null) {
					if (line.contains(parola)) {
						files[i] = new String(file.getName());
						i++;
						break;
					}
				}

			} catch (IOException e) {
				System.err.println("Errore nella lettura del file\n");
				System.exit(2);
			}
		}
		return files;
	}

	@Override
	public synchronized int conta_numero_linee(String file, String parola) throws RemoteException {
		File f = new File(file);
		int count_lines = 0;
		String line = null;
		boolean trovata = false;
		BufferedReader br;
		if (!f.isFile() || !f.exists() || !f.canRead()) {
			System.out.println("Directory inesistente o illeggibile\n");
			System.exit(2);
		}

		try {
			br = new BufferedReader(new FileReader(f.getName()));
			while ((line = br.readLine()) != null) {
				count_lines++;
				if (line.contains(parola)) {
					trovata = true;
				}
			}

		} catch (IOException e) {
			System.err.println("Errore nella lettura del file\n");
			System.exit(2);
		}
		if (trovata) {
			return count_lines;
		} else
			return -1;
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
