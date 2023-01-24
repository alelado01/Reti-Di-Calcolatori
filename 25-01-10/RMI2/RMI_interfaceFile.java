/**
 * Interfaccia remota di servizio
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_interfaceFile extends Remote {

	int inserisci_nuovo_libro(String titolo, int totCopie) throws RemoteException;
	int richiedi_prestito_libro(String titolo, String giorno,String mese,String anno) throws RemoteException;
}