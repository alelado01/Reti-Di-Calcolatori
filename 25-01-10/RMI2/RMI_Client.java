import java.rmi.*;
import java.io.*;

public class RMI_Client {

    public static void main(String[] args) {
        int registryPort = 1099;
        String registryHost = null;
        String serviceName = "ServerRMI";
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // Controllo parametri
        if (args.length != 1 && args.length != 2) {
            System.out.println("Sintassi: ClientFile RegistryHost [registryPort]");
            System.exit(1);
        } else {
            registryHost = args[0];
            if (args.length == 2) {
                try {
                    registryPort = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out
                            .println("Sintassi: ClientFile NomeHost [registryPort], registryPort intero");
                    System.exit(1);
                }
            }
        }

        // Connessione al servizio RMI remoto
        try {
            String completeName = "//" + registryHost + ":" + registryPort + "/"
                    + serviceName;
            RMI_interfaceFile serverRMI = (RMI_interfaceFile) Naming.lookup(completeName);
            System.out.println("Client RMI: Servizio \"" + serviceName
                    + "\" connesso");

            String service;
            System.out.println("Servizio (I=Inserisci, R=Richiedi): ");
            while ((service = stdIn.readLine()) != null) {
                if (service.equals("I")) {
                    String titolo,nCopieString;
                    System.out.println("Inserisci il titolo: ");
                    titolo = stdIn.readLine();
                    System.out.println("Inserisci il numero di copie totali: ");
                    nCopieString = stdIn.readLine();
                    int copie=-1;
                    try {
                        copie = Integer.parseInt(nCopieString);
                    } catch (NumberFormatException e) {
                        System.out.println("Numero non valido");
                        continue;
                    }
                    int ris = serverRMI.inserisci_nuovo_libro(titolo, copie);
                    if(ris<0){
                        System.out.println("Operazione Fallita");
                    }else{
                        System.out.println("Operazione Riuscita");
                    }
                } else if (service.equals("R")) {
                    String titolo,giorno, mese, anno;
                    System.out.println("Inserisci il titolo: ");
                    titolo = stdIn.readLine();
                    System.out.println("Inserisci il giorno: ");
                    giorno = stdIn.readLine();
                    System.out.println("Inserisci il mese: ");
                    mese = stdIn.readLine();
                    System.out.println("Inserisci l'anno: ");
                    anno = stdIn.readLine();
                    int ris = serverRMI.richiedi_prestito_libro(titolo, giorno, mese, anno);

                    if(ris<0){
                        if(ris == -1){
                            System.out.println("Operazione Riuscita");
                        }else{
                            System.out.println("Copie finite");
                        }
                    }else{
                        System.out.println("Operazione Riuscita");
                    }
                } else {
                    System.out.println("Operazione non possibile!");
                }
                System.out.println("Servizio (I=Inserisci, R=Richiedi): ");
            } // !EOF

        } catch (Exception e) {
            System.err.println("ClientRMI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
