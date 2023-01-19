import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ServerStreamThread extends Thread {

    private Socket clientSocket = null;
    private static Stanza[] stanze;
    private static final int MAX = 10;
    private final static int MAX_UTENTI = 4;

    /**
     * Constructor
     * 
     * @param clientSocket
     */
    public ServerStreamThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        if (stanze == null) {
            stanze = new Stanza[MAX];
        }
        for (int i = 0; i < MAX; i++) {
            stanze[i] = new Stanza();
        }
        String[] utenti=new String[MAX_UTENTI];
        utenti[0]="Pippo";
        utenti[1]="Minnie";
        utenti[2]="L";
        utenti[3]="L";
        stanze[0] = new Stanza("aaaaa", "M", utenti);
    }

    public void run() {
        DataInputStream inSock;
        DataOutputStream outSock;
        try {
            // creazione stream di input e out da socket
            inSock = new DataInputStream(clientSocket.getInputStream());
            outSock = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out
                    .println("Problemi nella creazione degli stream di input/output "
                            + "su socket: ");
            ioe.printStackTrace();
            // il server continua l'esecuzione riprendendo dall'inizio del ciclo
            return;
        } catch (Exception e) {
            System.out
                    .println("Problemi nella creazione degli stream di input/output "
                            + "su socket: ");
            e.printStackTrace();
            return;
        }

        try {
            try {
                int num_stanze=0;
                String operazione;
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("V")) {
                        for (int i = 0; i < MAX; i++) {
                            num_stanze++;
                        }
                        outSock.writeInt(num_stanze);
                        for(int i=0; i<num_stanze; i++)
                        {
                            outSock.writeUTF(stanze[i].toString());
                        }
                    }
                }
            } catch (EOFException eof) {
                System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
                clientSocket.close();
                System.out.println("ServerStream: termino...");
                System.exit(0);
            } catch (SocketTimeoutException ste) {
                System.out.println("Timeout scattato: ");
                ste.printStackTrace();
                clientSocket.close();
                System.exit(1);
            } catch (Exception e) {
                System.out.println("Problemi, i seguenti : ");
                e.printStackTrace();
                System.out.println("Chiudo ed esco...");
                clientSocket.close();
                System.exit(2);
            }
        } catch (IOException ioe) {
            System.out.println("Problemi nella chiusura della socket: ");
            ioe.printStackTrace();
            System.out.println("Chiudo ed esco...");
            System.exit(3);
        }
    }
}
// run
