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
            RMI_interfaceFile serverRMI = (RMI_interfaceFile) Naming.lookup(completeName);
            System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

            System.out.println("\nRichieste di servizio fino a fine file");

            String service;
            System.out.print(
                    "\nServizio ? \n1) L=Lista Files\n2) C=Conta occorrenze interpunzione\n(ctrl+d) per terminare: \n");

            while ((service = stdIn.readLine()) != null) {
                // Richiamo i metodi remoti e controllo che tutto vada bene
                if (service.equals("L")) {
                    // LOGICA APPLICATIVA

                    // Eseguo il metodo richiesto
                    try {
                        System.out.print("\nInserire primo carattere: ");
                        char c1 = stdIn.readLine().charAt(0);
                        System.out.print("\nInserire secondo carattere: ");
                        char c2 = stdIn.readLine().charAt(0);
                        String[] listafile = serverRMI.lista_file_caratteri(c1, c2);

                        for (int i = 0; i < listafile.length; i++) {
                            System.out.println(listafile[i] + "\n");
                        }
                    } catch (RemoteException re) {
                        System.out.println("Errore Remoto: " + re.toString());
                    }
                } // Fine 1

                else if (service.equals("C")) {
                    // LOGICA APPLICATIVA

                    // Eseguo il metodo richiesto
                    try {
                        System.out.print("\nInserire nome file: ");
                        String nome = stdIn.readLine();
                        int esito = serverRMI.conta_occorrenze_interpunzione(nome);
                        if (esito == -1) {
                            System.out.println("Error File Not Found\n");
                        }
                        if (esito == 0) {
                            System.out.println("Nessun segno di interpunzione\n");
                        } else
                            System.out.println("Numero segni di interpunzione in " + nome + ": " + esito);
                    } catch (RemoteException re) {
                        System.out.println("Errore Remoto: " + re.toString());
                    }
                }

                else {
                    System.out.println("Servizio non disponibile");
                }

                System.out.print(
                        "\nServizio ? \n1) L=Lista Files\n2) C=Conta occorrenze interpunzione\n(ctrl+d) per terminare: \n");
            }

        } catch (Exception e) {
            System.err.println("ClientRMI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}