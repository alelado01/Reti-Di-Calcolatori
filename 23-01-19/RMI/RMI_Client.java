
/************************************
*     	   Ladomorzi Alessio        *
*           RMI_Client.java         *
*                                   *
************************************/

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
			System.out.print("\nServizio ? \n1) E=Elimina\n2) V=Visualizza\n(ctrl+d) per terminare: \n");

			while ((service = stdIn.readLine()) != null) {
				// Richiamo i metodi remoti e controllo che tutto vada bene
				if (service.equals("E")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {
						System.out.print("Inserisci la targa : ");
						String targa = stdIn.readLine();
						int esito = serverRMI.eliminaPrenotazione(targa);

						if (esito == 1) {
							System.out.println("Eliminazione completata");
						} else if (esito < 0) {
							System.out.println("Errore eliminazione targa");
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				} // Fine 1

				else if (service.equals("V")) {
					// LOGICA APPLICATIVA

					// Eseguo il metodo richiesto
					try {
						System.out.print("Inserisci il tipo : ");
						String tipo = stdIn.readLine();
						Prenotazione[] p = serverRMI.visualizza_prenotazioni(tipo);
						if (p == null) {
							System.out.println("Errore Visualizza");
						} else {
							System.out.println("Targa\tPatente\tTipo\tImages\n");

							for (int i = 0; i < p.length; i++) {
								System.out.println(p[i].getTarga() + "\t" +
										p[i].getPatente() + "/" + p[i].getType() + "/"
										+ p[i].getImages()
										+ "\t");
							}
						}
					} catch (RemoteException re) {
						System.out.println("Errore Remoto: " + re.toString());
					}
				}

				else {
					System.out.println("Servizio non disponibile");
				}

				System.out.print("\nServizio ? \n1) E=Elimina\n2) V=Visualizza\n(ctrl+d) per terminare: \n");
			}

		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
