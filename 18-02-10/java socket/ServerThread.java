import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ServerThread extends Thread {

    private Socket clientSocket = null;
    private int buffer_size = 4096;

    private Canzone[] canzoni;

    /**
     * Constructor
     * 
     * @param clientSocket
     */
    public ServerThread(Socket clientSocket, Canzone[] canzoni) {
        this.clientSocket = clientSocket;
        this.canzoni = canzoni;
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
                FileInputStream inFile = null;
                String operazione = null, song = null;
                int nread;
                long length = 0l;
                File dirE;
                byte[] buffer = new byte[buffer_size];
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("R")) {
                        song = inSock.readUTF();
                        System.out.println("\nCanzone da reperire: " + song);
                        dirE = new File("testi/");
                        if (dirE.exists() && dirE.isDirectory() && dirE.canRead()) {
                            for (File f : dirE.listFiles()) {
                                // Controllo che il file si chiami come la canzone evitando il .txt
                                if (f.getName().substring(0, f.getName().length() - 4).equals(song)) {
                                    inFile = new FileInputStream(f);
                                    outSock.writeUTF(f.getName());
                                    length=f.length();
                                    outSock.writeLong(length);
                                    // leggo buffer da file finchè length >0, sottraggo i byte letti a length
                                    while (length > 0) {
                                        nread = inFile.read(buffer);
                                        outSock.write(buffer, 0, nread);

                                        length -= nread;
                                    }
                                    inFile.close();
                                }
                            }
                        }
                        continue;
                    } else if (operazione.equals("V")) {
                        Canzone[] max = new Canzone[2];
                        for (int i = 0; i < 2; i++) {
                            max[i] = new Canzone();
                        }
                        max[0].setVoti(canzoni[0].getVoti());
                        max[1].setVoti(canzoni[1].getVoti());
                        for (int i = 0; i < canzoni.length; i++) {
                            if (canzoni[i].getVoti() > max[0].getVoti()) {
                                max[1] = max[0];
                                max[0]=canzoni[i];
                                continue;
                            }
                            if (canzoni[i].getVoti() > max[1].getVoti()) {
                                max[1]=canzoni[i];
                            }
                        }

                        for (int i = 0; i < 2; i++) {
                            outSock.writeUTF(max[i].getCantante());
                            outSock.writeUTF(max[i].getTitolo());
                            outSock.writeInt(max[i].getVoti());
                        }
                        continue;
                    }
                }
            } catch (EOFException eof) {
                System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
                clientSocket.close();
                System.out.println("Server: termino...");
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
        } catch (

        IOException ioe) {
            System.out.println("Problemi nella chiusura della socket: ");
            ioe.printStackTrace();
            System.out.println("Chiudo ed esco...");
            System.exit(3);
        }
    }
}
// run
