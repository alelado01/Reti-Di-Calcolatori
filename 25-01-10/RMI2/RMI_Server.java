
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {

	private static final int K = 3;
	private static PrenotaLibri[] libri;

	protected RMI_Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	void debug(){
		for(int i = 0;i<K;i++){
			System.out.println(libri[i].toString());
		}
	}
	public static void main(String[] args) {

		libri = new PrenotaLibri[K];
		for (int i = 0; i < K; i++) {
			libri[i] = new PrenotaLibri();
		}
		
		String[] copie = new String[3];
		copie[0] = "18/12/2022";
		copie[1] = "12/10/2021";
		copie[2] = "L";

		libri[0] = new PrenotaLibri("AAA", 3, copie);

		int registryPort = 1099;
		String registryHost = "localhost";
		String serviceName = "ServerRMI";

		// Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerImpl [registryPort]");
			System.exit(1);
		}

		if (args.length == 1) {
			try {
				// Aggiungere anche controllo porta valida, nel range di quelle usabili
				registryPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Sintassi: ServerImpl [registryPort], registryPort");
				System.exit(2);
			}
		}
		// Registrazione del servizio RMI
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
		// metodi synchronized!!
	}

	@Override
	public synchronized int inserisci_nuovo_libro(String titolo, int totCopie) throws RemoteException {
		if (titolo == null || totCopie < 0 || titolo.equals("L")) {
			return -1;
		}
		int index = -1;
		for (int i = 0; i < K; i++) {
			if (libri[i].getTitolo().equals(titolo)) {
				break;
			} else if (libri[i].getTitolo().equals("L")) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			return index;
		}
		libri[index] = new PrenotaLibri(titolo, totCopie);
		debug();
		return 0;
	}

	@Override
	public synchronized int richiedi_prestito_libro(String titolo, String giorno, String mese, String anno)
			throws RemoteException {
		if (titolo == null || titolo.equals("L") || giorno == null || mese == null || anno == null) {
			return -1;
		}
		int index1 = -1;
		int index2 = -1;
		for (int i = 0; i < K; i++) {
			if (libri[i].getTitolo().equals(titolo)) {
				if (libri[i].getCopieDisponibili() == 0) {
					index1 = -2;
					break;
				} else {
					index1 = i;
					for (int j = 0; j < libri[i].getM(); j++) {
						if (libri[i].getCopie()[j].equals("L")) {
							index2 = j;
							break;
						}
					}
				}
			}
		}
		if (index1 < 0 || index2 < 0) {
			return index1;
		}
		libri[index1].getCopie()[index2] = giorno + "/" + mese + "/" + anno + "/";
		libri[index1].decreaseCopie();
		debug();
		return index1;
	}
}
