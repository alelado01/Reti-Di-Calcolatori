import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RMI_Client {
	/* --- AVVIO DEL CLIENT RMI ---- */
	public static void main(String[] args) {
		int registryPort = 1099;
		String registryHost = null;
		String serviceName = "ServerRMI";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo degli argomenti
		if (args.length != 1 && args.length != 2) {
			System.out.println("Sintassi: RMI_Client NomeHost [registryPort]");
			System.exit(1);
		} else {
			registryHost = args[0];
			if (args.length == 2) {
				try {
					registryPort = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out.println("Sintassi: RMI_Client NomeHost [registryPort], registryPort intero");
					System.exit(2);
				}
			}
		}

		// Connessione al servizio RMI remoto
		try {
			String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
			RMI_Interface serverRMI = (RMI_Interface) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print(
					"\nServizio ? \n1) I=Inserisci nuovo libro\n2) R=Richiedi prestito libro\n(ctrl+d) per terminare: \n");

			while ((service = stdIn.readLine()) != null) {
				// Richiamo i metodi remoti e controllo che tutto vada bene
				if (service.equals("I")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {
						// Leggo da input
						System.out.println("\nInserisci titolo: ");
						String titolo = stdIn.readLine();
						System.out.println("\nInserisci copie disponibili: ");
						int copie = Integer.parseInt(stdIn.readLine());
						int esito = serverRMI.inserisci_nuovo_libro(titolo, copie);
						// Gestione esito
						if (esito == 0) {
							System.out.println("\nLibro aggiunto con successo!\n");
						} else {
							System.out.println("\nImpossibile aggiungere il libro\n");
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				} // Fine 1

				else if (service.equals("R")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {
						System.out.print("\nInserisci titolo libro: ");
						String titolo = stdIn.readLine();
						System.out.print("Inserisci data di riconsegna: ");
						String data = stdIn.readLine();
						int esito = serverRMI.richiedi_prestito_libro(titolo, data);
						// gestione esito
						if (esito == -1) {
							System.out.println("Titolo non presente\n");
						}
						if (esito == -2) {
							System.out.println("Nessuna copia disponibile\n");
						} else if (esito == 0) {
							System.out.println("Libro preso con successo\n");
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				}

				else {
					System.out.println("Servizio non disponibile");
				}

				System.out.print(
						"\nServizio ? \n1) I=Inserisci nuovo libro\n2) R=Richiedi prestito libro\n(ctrl+d) per terminare: \n");
			}

		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}