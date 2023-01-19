import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
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
							"Usage: java RentACarClient serverAddr serverPort");
					System.exit(1);
				}
			} else {
				System.out.println("Usage: java Client serverAddr serverPort");
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java RentACarClient serverAddr serverPort");
			System.exit(2);
		}
		Socket socket = null;
		FileInputStream inFile = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		String nomeFile = null;
		try {
			socket = new Socket(addr, port);
			socket.setSoTimeout(30000);
			System.out.println("Creata la socket: " + socket);
		} catch (Exception e) {
			System.out.println("Erorre nella creazione della socket\n");
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
		int num_file = 0, nread;
		String operazione, targa, filename;
		boolean esito = false;
		long length;
		FileOutputStream outFile = null;
		try {
			System.out.println("Scegliere operazione (E=Elimina prenotazione, D=Download immagini):\n");
			while ((operazione = stdIn.readLine()) != null) {
				outSock.writeUTF(operazione);
				if (operazione.equals("E")) {
					System.out.println("Inserire targa:\n");
					targa = stdIn.readLine();

					outSock.writeUTF(targa);

					// attendo e ricevo l'esito
					esito = inSock.readBoolean();
					if (esito) {
						System.out.println("Eliminazione avvenuta con successo\n");
					} else
						System.out.println("Errore nell'eliminazione della prenotazione\n");
				}
				if (operazione.equals("D")) {
					System.out.println("Inserire targa:\n");
					targa = stdIn.readLine();

					outSock.writeUTF(targa);

					// attendo e ricevo esito
					esito = inSock.readBoolean();
					if (esito) {
						num_file = inSock.readInt();
						System.out.println("Numero file nella directory: " + num_file + "\n");
						for (int i = 0; i < num_file; i++) {

							// ricevo nome file
							filename = inSock.readUTF();
							System.out.println("Nome file: " + filename + "\n");

							// ricevo lunghezza file
							length = inSock.readLong();
							System.out.println("Lunghezza " + filename + ": " + length + " byte\n");

							while (length > 0) {
								nread = inSock.read(buffer, 0, length > buffer_size ? buffer_size : (int) length);
								outFile.write(buffer, 0, nread);

								length -= nread;
							}
							outFile.close();
						}
					} else {
						System.out.println("Errore!, Cartella immagini non presente sul server");
					}

				}
				System.out.print("Inserire (E=EliminaPrenotazione,D=DownloadImmagini) : ");
			}

		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java PutFileClient serverAddr serverPort");
			System.exit(2);
		}
		socket.close();
	}
}