import java.io.IOException;
import java.net.*;

public class Server {
	public static final int PORT = 1050; // default port
	private static final int N = 10;
	static Canzone c[] = null;

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

		// inizializzazione struttura dati
		c = new Canzone[N];
		for (int i = 0; i < N; i++) {
			c[i] = new Canzone();
		}
		c[0].setCantante("Bocelli");
		c[0].setTitolo("Partiro");
		c[0].setNomeFile("Partiro.txt");
		c[0].setVoti(40);

		c[1].setCantante("Ligabue");
		c[1].setTitolo("Certe notti");
		c[1].setNomeFile("Certe notti.txt");
		c[1].setVoti(20);

		c[2].setCantante("MGK");
		c[2].setTitolo("gfy");
		c[2].setNomeFile("gfy.txt");
		c[2].setVoti(65);

		c[3].setCantante("Blackbear");
		c[3].setTitolo("hot girl");
		c[3].setNomeFile("hot girl.txt");
		c[3].setVoti(43);

		c[4].setCantante("Arizona Zervas");
		c[4].setTitolo("FML");
		c[4].setNomeFile("FML.txt");
		c[4].setVoti(91);

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
					new ServerThread(clientSocket, c).start();
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