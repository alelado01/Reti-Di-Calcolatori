import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_Interface extends Remote {

    int inserisci_nuovo_libro(String titolo, int copie_tot) throws RemoteException;

    int richiedi_prestito_libro(String titolo, String giorno) throws RemoteException;

}