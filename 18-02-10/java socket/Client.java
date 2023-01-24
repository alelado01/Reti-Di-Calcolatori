import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	public static void main(String[] args) throws IOException {
		InetAddress addr = null;
		int port = -1;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		try { // check args
			if (args.length == 2) {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				if (port < 1024 || port > 65535) {
					System.out.println(
							"Usage: java Client serverAddr serverPort");
					System.exit(1);
				}
			} else {
				System.out.println("Usage: java Client serverAddr serverPort");
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java Client serverAddr serverPort");
			System.exit(2);
		}
		Socket socket = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		try {
			socket = new Socket(addr, port);
			socket.setSoTimeout(30000);
			System.out.println("Creata la socket: " + socket);
		} catch (Exception e) {
			System.out.println("Errore nella creazione della socket\n");
			System.exit(1);
		}
		// creazione stream di input/output su socket
		try {
			inSock = new DataInputStream(socket.getInputStream());
			outSock = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Errore nella creazione degli stream");
			System.exit(2);
		}

		int buffer_size = 4096;
		byte[] buffer = new byte[buffer_size];
		int nread;
		String operazione, filename, song;
		Canzone[] res = new Canzone[2];
		long bytes;
		FileOutputStream outFile = null;
		try {
			System.out.println("Scegliere operazione (R=Reperisci testo canzone, V=Visualizza canzoni più votate):\n");
			while ((operazione = stdIn.readLine()) != null) {
				outSock.writeUTF(operazione);
				// prima scelta
				if (operazione.equals("R")) {
					// CORPO CLIENT PRIMA SCELTA
					System.out.println("\nInserisci titolo canzone: ");
					song = stdIn.readLine();
					outSock.writeUTF(song);

					// attendo e ricevo l'esito
					filename = inSock.readUTF();
					outFile = new FileOutputStream(new File(filename));
					System.out.println("\nNome file: " + filename);
					// Ricevo lunghezza file
					bytes = inSock.readLong();
					System.out.println("\nNum byte: " + bytes);
					// Ricevo il file dal server
					while (bytes > 0) {
						nread = inSock.read(buffer, 0, bytes > buffer_size ? buffer_size : (int) bytes);
						outFile.write(buffer, 0, nread);

						bytes -= nread;
					}
					outFile.close();
					System.out.println("Operazione terminata\n");
				}
				// seconda scelta
				if (operazione.equals("V")) {
					// CORPO CLIENT SECONDA SCELTA
					System.out.println("\nInvio richiesta di visualizzazione canzoni più votate");

					// attendo e ricevo esito
					for (int i = 0; i < 2; i++) {
						
						System.out.println("\nCantante: " + inSock.readUTF() + " Canzone: " + inSock.readUTF()
								+ " Voti: " + inSock.readInt());
					}
				}
				System.out
						.print("Scegliere operazione (R=Reperisci testo canzone, V=Visualizza canzoni più votate):\n");
			}

		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java Client serverAddr serverPort");
			System.exit(2);
		}
		socket.close();
	}
}
