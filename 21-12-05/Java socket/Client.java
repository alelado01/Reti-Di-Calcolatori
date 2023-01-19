import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Client {

	public static void main(String[] args) throws IOException {
		InetAddress addr1 = null;
		int port1 = -1;
		InetAddress addr2 = null;
		int port2 = -1;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		try { // check args
			if (args.length == 4) {
				addr1 = InetAddress.getByName(args[0]);
				port1 = Integer.parseInt(args[1]);
				addr2 = InetAddress.getByName(args[2]);
				port2 = Integer.parseInt(args[3]);
				if (port1 < 1024 || port1 > 65535) {
					System.out.println(
							"Usage: java Client serverAddr1 serverPort1 serverAddr2 serverPort2");
					System.exit(1);
				}
				if (port2 < 1024 || port2 > 65535) {
					System.out.println(
							"Usage: java Client serverAddr1 serverPort1 serverAddr2 serverPort2");
					System.exit(1);
				}
			} else {
				System.out.println("Usage: java Client serverAddr1 serverPort1 serverAddr2 serverPort2");
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java Client serverAddr1 serverPort1 serverAddr2 serverPort2");
			System.exit(2);
		}
		Socket socket = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;

		// creazione datagram di input/output
		DatagramSocket datagramSocket = null;
		DatagramPacket datagramPacket = null;
		byte[] buf = new byte[256];
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(30000);
			datagramPacket = new DatagramPacket(buf, buf.length, addr2, port2);
			System.out.println("\nSwapClient: avviato");
			System.out.println("Creata la socket: " + datagramSocket);
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("SwapClient: interrompo...");
			System.exit(1);
		}

		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		byte[] data = null;

		String operazione;
		int num_stanze;
		try {
			System.out.println("Scegliere operazione (V=Visualizza stanze, S=Sospendi stanza):\n");
			while ((operazione = stdIn.readLine()) != null) {
				if (operazione.equals("V")) {
					try {
						socket = new Socket(addr1, port1);
						socket.setSoTimeout(30000);
						System.out.println("Creata la socket: " + socket);
						inSock = new DataInputStream(socket.getInputStream());
						outSock = new DataOutputStream(socket.getOutputStream());
					} catch (IOException ioe) {
						System.out.println("Problemi nella creazione degli stream su socket: ");
						ioe.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
						System.exit(1);
					}
					outSock.writeUTF(operazione);
					try {
						// attendo e ricevo l'esito
						System.out.println("Ricevo numero di stanze totali\n");
						num_stanze = inSock.readInt();
						if (num_stanze == 0) {
							System.out.println("Nessuna stanza esistente!\n");
						}
						String stanzaString = null;
						System.out.println("Ricevo le stanze:\n");
						for (int i = 0; i < num_stanze; i++) {
							stanzaString = inSock.readUTF();
							System.out.println("\n" + stanzaString);
						}
					} catch (SocketException e) {
						System.out.println("Problemi, i seguenti: ");
						e.printStackTrace();
						System.exit(2);
					}
				} else if (operazione.equals("S")) {
					String stanzaString = null;
					try {
						int esito;
						System.out.println("Inserisci nome stanza da sospendere: "
								+ " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
						stanzaString = stdIn.readLine();
						System.out.println(stanzaString);
						// Send request
						try {
							doStream.writeUTF(stanzaString);
							data = boStream.toByteArray();
							datagramPacket.setData(data);
							datagramSocket.send(datagramPacket);
							System.out.println("Richiesta inviata a " + addr2);
						} catch (IOException e) {
							System.out.println("Problemi nell'invio della richiesta: ");
							e.printStackTrace();
							System.exit(3);
						}

						// set buffer and receive answer
						try {
							datagramPacket.setData(buf);
							datagramSocket.receive(datagramPacket);
							// sospensiva solo per i millisecondi indicati, dopo solleva una SocketException
						} catch (IOException e) {
							System.out.println("Problemi nella ricezione del datagramma: ");
							e.printStackTrace();
							System.exit(3);
						}
						try {
							biStream = new ByteArrayInputStream(datagramPacket.getData(), 0,
									datagramPacket.getLength());
							diStream = new DataInputStream(biStream);
							esito = diStream.readInt();
							System.out.println("Esito sospensione: " + esito);

						} catch (IOException e) {
							System.out.println("Problemi nella lettura della risposta: ");
							e.printStackTrace();
							System.exit(3);
						}
					} catch (Exception e) {
						System.out.println("Problemi, i seguenti: ");
						e.printStackTrace();
						System.exit(2);
					}
				}
				System.out.println("Scegliere operazione (V=Visualizza stanze, S=Sospendi stanza):\n");
			}
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java Client serverAddr1 serverPort1 serverAddr2 serverPort2");
			System.exit(2);
		}
		socket.close();
	}
}