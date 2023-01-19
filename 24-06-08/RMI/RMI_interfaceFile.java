/************************************
*     Ricatti Luigi Francesco       *
*      RMI_interfaceFile.java       *
*                                   *
************************************/
	

// Interfaccia remota

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMI_interfaceFile extends Remote 
{

  // Insieme dei metodi che devono essere implementati
  // Ognuno deve lanciare una RemoteException in caso d'errore
  // Se ci sono classi accessorie esse devono implementare Serializable e bisogna
  //fare attenzione ai metodi da implementare synchronized

/*Quabdo un metodo del server modifica lo stato ed è accessibile contemporaneamente da più cliente (tramite l'invocazione remota)
Quindi qualsiasi metodo remoto che modifichi lo stato del server
Con stato intendo la struttura dati che gestisce il server*/
	
	// Esempio di metodi da implementare
	String[] lista_nomi_file_contenenti_parola_in_linea(String dir, String parola) throws RemoteException;
	int conta_numero_linee(String file, String parola)	throws RemoteException;
}
