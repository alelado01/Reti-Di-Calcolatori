import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {
	private static final long serialVersionUID = 1L;

	// qui eventuali variabili e strutture dati
	private static Calza calze[];
	private static final int dim = 5;

	// Costruttore
	public RMI_Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	// Eventuali metodi legati alla struttura dati

	public static void stampa() {

	}

	// Implementazione dei metodi

	@Override
	public synchronized String[] visualizza_lista()
			throws RemoteException {
				int dim=0;
		for(int i=0; i<calze.length; i++)
		{
			if(!calze[i].getCitta().equals("L"))
			{
				dim++;
			}
		}
		String[] cittaDup=new String[dim];
		int k=0;
		for(int i=0; i<calze.length; i++)
		{
			if(!calze[i].getCitta().equals("L"))
			{
				cittaDup[k++]=calze[i].getCitta();
			}
		}
		String []cittaBuff=new String[dim];
		for(int i=0; i<dim; i++)
		{
			cittaBuff[i]="L";
		}
		cittaBuff[0]=cittaDup[0];
		for(int i=1; i<dim; i++)
		{
			for(int j=0; j<dim; j++)
			{
				if(!cittaDup[i].equals(cittaBuff[j]) && !cittaBuff[j].equals("L"))
				{
					cittaBuff[j+1]=cittaDup[i];
					break;
				}
			}
		}
		int dim_res=0;
		for(int i=0; i<dim; i++)
		{
			if(!cittaBuff[i].equals("L"))
			{
				dim_res++;
			}
		}
		String[] res=new String[dim_res];
		k=0;
		for(int i=0; i<dim; i++)
		{
			if(!cittaBuff[i].equals("L"))
			{
				res[k]=cittaBuff[i];
				k++;
			}
		}
		return res;
	}

	@Override
	public synchronized int visualizza_numero(String citta, String via) throws RemoteException {
		int res = 0;
		for (int i = 0; i < calze.length; i++) {
			if (calze[i].getCitta().equals(citta) && calze[i].getVia().equals(via)) {
				res++;
			}
		}
		if (res == 0) {
			res = -1;
		}
		return res;
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
		calze = new Calza[dim];
		calze[0] = new Calza("Mario", "Celiaco", 'S', "Roma", "Galardi", "okokok");
		calze[1] = new Calza("Luigi", "Normale", 'S', "Roma", "Galardi", "bau");
		calze[2] = new Calza("Giuseppe", "Normale", 'S', "Roma", "Veneto", "miao");
		calze[3] = new Calza("Wario", "Normale", 'S', "Milano", "Capoiaccio", "woof");
		calze[4] = new Calza();

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
