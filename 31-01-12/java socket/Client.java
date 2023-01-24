import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
		int nread, soglia;
		String operazione, filename;
		int esito = -1, files_count;
		long length, bytes;
		File f;
		FileInputStream inFile = null;
		FileOutputStream outFile = null;
		try {
			System.out.println("Scegliere operazione (U=Upload file audio, D=Download audio):\n");
			while ((operazione = stdIn.readLine()) != null) {
				outSock.writeUTF(operazione);
				// prima scelta
				if (operazione.equals("U")) {
					// CORPO CLIENT PRIMA SCELTA
					System.out.println("\nInserire giorno della settimana: ");
					String giorno = stdIn.readLine();
					outSock.writeUTF(giorno);

					System.out.println("Inserire nome file audio: ");
					String fileAudio = stdIn.readLine();

					f = new File(fileAudio);
					if (f.exists() && f.isFile() && f.canRead()) {
						length = f.length();
						inFile = new FileInputStream(f);
						outSock.writeUTF(f.getName());
						outSock.writeLong(length);
						// leggo buffer da file finchè length >0, sottraggo i byte letti a length
						while (length > 0) {
							nread = inFile.read(buffer);
							outSock.write(buffer, 0, nread);

							length -= nread;
						}
						inFile.close();
						esito = inSock.readInt();
						if (esito == 0) {
							System.out.println("\nUpload riuscito");
						} else
							System.out.println("\nUpload fallito");
					} else {
						System.out.println("\nErrore nel trovare il file audio\n");
						System.exit(1);
					}
				}
				// seconda scelta
				if (operazione.equals("D")) {
					// CORPO CLIENT SECONDA SCELTA
					System.out.println("\nInserire soglia di differenza tra max e min: ");
					soglia = Integer.parseInt(stdIn.readLine());

					outSock.writeInt(soglia);

					// attendo e ricevo esito
					files_count = inSock.readInt();
					System.out.println("Files: " + files_count);
					for (int i = 0; i < files_count; i++) {
						// Ricevo nome file
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
					}
				}
				System.out.print("\nScegliere operazione (U=Upload file audio, D=Download audio):\n");
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
