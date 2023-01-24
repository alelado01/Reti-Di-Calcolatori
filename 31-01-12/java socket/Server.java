import java.io.IOException;
import java.net.*;

public class Server {
	public static final int PORT = 1050; // default port
	/*Eventuali strutture da dichiarare*/
	private static Meteo[] m;
	private static final int N=5;

	public static void main(String[] args) throws IOException {

		int port = -1;

		/* controllo argomenti */
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java Server [serverPort>1024]");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out
						.println("Usage: java Server or java Server port");
				System.exit(1);
			}
		} // try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
					.println("Usage: java Server or java Server port");
			System.exit(2);
		}
		//Inizializzazione struttura dati
		m=new Meteo[N];
		//
		for(int i=0; i<N; i++)
		{
			m[i]=new Meteo();
		}
		m[0]=new Meteo("Lunedi", "nuvoloso", 13, 7, "30gen12.mp3");
		m[1]=new Meteo("Martedi", "neve", 0, -4, "31gen12.mp3");
		m[2]=new Meteo("Sabato", "sereno", 13, 3, "15feb.mp3");
		
		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("Server: avviato ");
			System.out.println("Server: creata la server socket: " + serverSocket);
		} catch (Exception e) {
			System.err
					.println("Server: problemi nella creazione della server socket: "
							+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		try {

			while (true) {
				System.out.println("Server: in attesa di richieste...\n");

				try {
					// bloccante fino ad una pervenuta connessione
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(30000);
					System.out.println("Server: connessione accettata: " + clientSocket);
				} catch (Exception e) {
					System.err
							.println("Server: problemi nella accettazione della connessione: "
									+ e.getMessage());
					e.printStackTrace();
					continue;
				}

				// serizio delegato ad un nuovo thread
				try {
					new ServerThread(clientSocket, m).start();
				} catch (Exception e) {
					System.err.println("Server: problemi nel server thread: "
							+ e.getMessage());
					e.printStackTrace();
					continue;
				}

			} // while
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Server: termino...");
			System.exit(2);
		}
	}
}
