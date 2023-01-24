import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_Interface {
    private static Libro[] libri;
    private static final int MAX = 5;

    private RMI_Server() throws RemoteException {
        super();
    }

    @Override
    public synchronized int inserisci_nuovo_libro(String titolo, int copie) throws RemoteException {
        // Corpo prima funzione Server
        if (titolo == null)
            // Lancio eccezione se argomenti nulli
            throw new RemoteException();
        int esito = -1;
        Libro l = new Libro(titolo, copie);
        for (int i = 0; i < MAX; i++) {
            if (libri[i].getTitolo().equals(titolo)) {
                return -1;
            }
        }
        for(int i=0; i<MAX; i++)
        {
            if(libri[i].getTitolo().equals("L"))
            {
                libri[i]=l;
                esito=0;
                break;
            }
        }
        return esito;
    }

    @Override
    public synchronized int richiedi_prestito_libro(String titolo, String data) throws RemoteException {
        int esito=-1;
        if (titolo == null || data == null)
            // Lancio eccezione se argomenti nulli
            throw new RemoteException();
        for(int i=0; i<MAX; i++)
        {
            if(!libri[i].getTitolo().equals(titolo))
            {
                esito=-1;
            }
            else if(libri[i].getTitolo().equals(titolo))
            {
                if(libri[i].getCopie_disp()<=0)
                {
                    return -2;
                }
                else{
                    libri[i].setCopie_disp(libri[i].getCopie_disp()-1);
                    for(int j=0; j<libri[i].getCopie_prestito(); j++)
                    {
                        if(libri[i].getCopie_prestate()[j].equals("L"))
                        {
                            libri[i].aggiungi_prestito(data, j);
                            esito=0;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return esito;
    }

    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
        String serviceName = "Server";
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;

        /* Inizializzo struttura dati */
        libri = new Libro[MAX];
        for (int i = 0; i < MAX; i++)
            libri[i] = new Libro();

        /* Riempio la struttura dati */
        String prestate[]=null;
        prestate=new String[1];
        prestate[0]="27/10/10";
        libri[0]=new Libro("Gli spiriti non dimenticano", 3, 2, 1, prestate);

        prestate=new String[2];
        prestate[0]="27/10/10";
        prestate[0]="30/10/10";
        libri[1]=new Libro("Piccolo principe", 4, 2, 2, prestate);

        /* stampa strutture dati */
        for (int i = 0; i < MAX; i++)
            System.out.println(libri[i].toString());

        try {
            /* Registration */
            RMI_Server serverRMI = new RMI_Server();
            Naming.rebind(completeName, serverRMI);
        } catch (RemoteException | MalformedURLException ex) {
            if (ex instanceof RemoteException) {
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            } else if (ex instanceof MalformedURLException) {
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
        }
    }
}
