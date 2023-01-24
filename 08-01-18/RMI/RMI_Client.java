
import java.rmi.*;
import java.io.*;

public class RMI_Client {
	/* --- AVVIO DEL CLIENT RMI ---- */
	public static void main(String[] args) {
		int registryPort = 1099;
		String registryHost = null;
		String serviceName = "ServerRMI";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo degli argomenti
		if (args.length != 1 && args.length != 2) {
			System.out.println("Sintassi: ClientFile NomeHost [registryPort]");
			System.exit(1);
		} else {
			registryHost = args[0];
			if (args.length == 2) {
				try {
					registryPort = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out.println("Sintassi: ClientFile NomeHost [registryPort], registryPort intero");
					System.exit(2);
				}
			}
		}

		// Connessione al servizio RMI remoto
		try {
			String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
			RMI_interfaceFile serverRMI = (RMI_interfaceFile) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print(
					"\nServizio ? \n1) L=Visualizza Lista\n2) N=Visualizza Numero\n(ctrl+d) per terminare: \n");

			while ((service = stdIn.readLine()) != null) {
				// Richiamo i metodi remoti e controllo che tutto vada bene
				if (service.equals("L")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {

						String[] stringhe = serverRMI.visualizza_lista();

						if (stringhe.length == 0) {
							System.out.println("Nessuna città");
						} else {
							for(int i=0; i<stringhe.length; i++)
							{
								System.out.println(stringhe[i]+"\n");
							}
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				} // Fine 1

				else if (service.equals("N")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {
						System.out.print("Inserisci città : ");
						String citta = stdIn.readLine();
						System.out.print("Inserisci via : ");
						String via = stdIn.readLine();
						int num = serverRMI.visualizza_numero(citta, via);
						if (num == -1) {
							System.out.println("Nessuna calza inviata a quell'indirizzo");
						} else {
							System.out.println("Calze inviate a '" + citta + "' in via " + via + ": " + num + "\n");
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				}

				else {
					System.out.println("Servizio non disponibile");
				}

				System.out.print(
						"\nServizio ? \n1) L=Visualizza Lista\n2) N=Visualizza Numero\n(ctrl+d) per terminare: \n");
			}

		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
