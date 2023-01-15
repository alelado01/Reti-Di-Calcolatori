/************************************
*     	  Ladomorzi Alessio       	*
*           RMI_Server.java         *
*                                   *
************************************/

// Implementazione del Server RMI

/*----- import ---- */
import java.rmi.*;
import java.rmi.server.*;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {
	private static final long serialVersionUID = 1L;

	// qui eventuali variabili e strutture dati
	private static Prenotazione prenotazioni[];
	private static final int dim = 5;

	// Costruttore
	public RMI_Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	// Eventuali metodi legati alla struttura dati

	public static void stampa() {
		System.out.println("Targa\tPatente\tTipo\tImages\n");

		for (int i = 0; i < dim; i++) {
			System.out.println(prenotazioni[i].getTarga() + "\t" +
					prenotazioni[i].getPatente() + "/" + prenotazioni[i].getType() + "/" + prenotazioni[i].getImages()
					+ "\t");
		}
	}

	// Implementazione dei metodi

	@Override
	public synchronized int eliminaPrenotazione(String targa) throws RemoteException {
		Prenotazione[] newArray = new Prenotazione[prenotazioni.length - 1];
		int ok = 0;
		for (int i = 0, k = 0; i < prenotazioni.length; i++) {
			if (prenotazioni[i].getTarga().equals(targa)) {
				ok = 1;
				continue;
			}
			newArray[k++] = prenotazioni[i];
		}
		prenotazioni = newArray;

		return ok;
	}

	@Override
	public synchronized Prenotazione[] visualizza_prenotazioni(String tipo) throws RemoteException {
		Prenotazione[] buff=new Prenotazione[prenotazioni.length];
		int k=0;
		for (int i = 0; i < prenotazioni.length; i++) {
			if (prenotazioni[i].getType().equals(tipo)) {
				if (prenotazioni[i].getTarga().charAt(0)>'E' && prenotazioni[i].getTarga().charAt(0)>'D') {
					buff[k] = prenotazioni[i];
					k++;
				}
			}
		}
		Prenotazione[] result=new Prenotazione[k];
		for(int i=0; i<k; i++)
		{
			result[i]=buff[i];
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
		prenotazioni = new Prenotazione[dim];
		prenotazioni[0] = new Prenotazione("AA333AA", "aaaaa", "auto", "AA333AA_img/");
		prenotazioni[1] = new Prenotazione("BB333AA", "bbbbb", "camion", "BB333AA_img/");
		prenotazioni[2] = new Prenotazione("CC333AA", "ccccc", "auto", "CC333AA_img/");
		prenotazioni[3] = new Prenotazione("EE333AA", "eeeee", "camion", "EE333AA_img/");
		prenotazioni[4] = new Prenotazione("FF333AA", "fffff", "auto", "FF333AA_img/");
		stampa();

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
