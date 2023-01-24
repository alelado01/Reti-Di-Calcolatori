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
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
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
		int nread;
		String operazione, parola, filename, dir;
		int esito = -1, files_count;
		long num_byte, bytes;
		FileOutputStream outFile = null;
		try {
			System.out.println("Scegliere operazione (E=Elimina occorrenze parola, T=Trasferimento file):\n");
			while ((operazione = stdIn.readLine()) != null) {
				outSock.writeUTF(operazione);
				if (operazione.equals("E")) {
					System.out.println("Inserire nome file:\n");
					filename = stdIn.readLine();

					outSock.writeUTF(filename);
					System.out.println("Inserire parola da rimuovere\n");
					parola = stdIn.readLine();

					outSock.writeUTF(parola);

					// attendo e ricevo l'esito
					esito = inSock.readInt();
					if (esito >= 0) {
						System.out.println("\nEliminazione avvenuta con successo di " + esito + " parole\n");
					} else
						System.out.println("\nErrore nell'eliminazione della parola\n");
				}
				if (operazione.equals("T")) {
					System.out.println("Inserire direttorio:\n");
					dir = stdIn.readLine();

					outSock.writeUTF(dir);

					System.out.println("Inserire soglia byte:\n");
					num_byte = Integer.parseInt(stdIn.readLine());

					outSock.writeLong(num_byte);

					// attendo e ricevo esito
					files_count = inSock.readInt();
					System.out.println("File con byte maggiori della soglia: " + files_count);
					for (int i = 0; i < files_count; i++) {
						filename = inSock.readUTF();
						outFile = new FileOutputStream(new File(filename));
						System.out.println("\nNome file: " + filename);
						bytes = inSock.readLong();
						System.out.println("\nNum byte: " + bytes);
						while (bytes > 0) {
							nread = inSock.read(buffer, 0, bytes > buffer_size ? buffer_size : (int) bytes);
							outFile.write(buffer, 0, nread);

							bytes -= nread;
						}
						outFile.close();
					}
				}
				System.out.print("Scegliere operazione (E=Elimina occorrenze parola, T=Trasferimento file):\n");
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