import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerDatagram {

    private static int PORT = -1;
    private static final int MAX = 10;
    private final static int MAX_UTENTI = 4;

    public static void main(String[] args) {

        if ( args.length != 1) {
			System.out.println("Usage: java ServerDgram port"); 
			System.exit(1);
		}
		PORT = Integer.parseInt(args[0]);
		if (PORT <= 1024 || PORT > 65535) {
			System.out.println("The server port is not valid: " + args[0]);
			System.exit(2);
		}

        DatagramSocket socket = null;
        DatagramPacket packet = null;
        byte[] buf = new byte[256];

        try {
            // apertura Socket
            socket = new DatagramSocket(PORT);
            packet = new DatagramPacket(buf, buf.length);
            System.out.println("ServerSeq per file avviato con socket port: " + socket.getLocalPort());
        } catch (SocketException e) {
            System.out.println("Problemi nella creazione della socket: ");
            e.printStackTrace();
            System.exit(1);
        }
        Stanza []stanze=new Stanza[MAX];

        for (int i = 0; i < MAX; i++) {
            stanze[i] = new Stanza();
        }
        String[] utenti = new String[MAX_UTENTI];
        utenti[0] = "Pippo";
        utenti[1] = "Minnie";
        utenti[2] = "L";
        utenti[3] = "L";
        stanze[0] = new Stanza("aaaaa", "M", utenti);

        try {
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            byte[] data = null;
            String stanza = null;
            int esito=-1;

            while (true) {
                System.out.println("\nServerDatagram in attesa di richieste...");

                // ricezione del datagramma
                try {
                    packet.setData(buf);
                    socket.receive(packet);

                } catch (IOException e) {
                    System.err.println("Problemi nella ricezione del datagramma: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo
                }

                try {
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);
                    stanza = diStream.readUTF();
                    System.out.println("Stanza scelta: " + stanza);
                } catch (IOException e) {
                    System.out.println("Problemi nella lettura della risposta: ");
                    e.printStackTrace();
                }

                // sospensione e invio dell'esito
                try {
                    // Sospensione

                    for (int i = 0; i < MAX; i++) {
                        if (stanze[i].getName().equals(stanza)) {
                            if (stanze[i].getTipo().equals("P")) {
                                stanze[i].setTipo("SP");
                                esito=0;
                            }
                            if (stanze[i].getTipo().equals("M")) {
                                stanze[i].setTipo("SM");
                                esito=0;
                            }
                            else System.out.println("stato già sospeso\n");
                        }
                    }
                    // Preparo l'esito
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    doStream.writeInt(esito);
                    data = boStream.toByteArray();
                    packet.setData(data, 0, data.length);
                    socket.send(packet);
                } catch (IOException e) {
                    System.err.println("Problemi, i seguenti: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo
                }

            } // while

        }
        // qui catturo le eccezioni non catturate all'interno del while
        // in seguito alle quali il server termina l'esecuzione
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ServerDatagram: termino...");
        socket.close();
    }
}