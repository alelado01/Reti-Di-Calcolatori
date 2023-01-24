
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ServerThread extends Thread {

    private Socket clientSocket = null;
    private int buffer_size = 4096;
    private Meteo[] m;
    private static final int N = 5;

    /**
     * Constructor
     * 
     * @param clientSocket
     */
    public ServerThread(Socket clientSocket, Meteo[] m) {
        this.clientSocket = clientSocket;
        this.m = m;
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

        for (int i = 0; i < N; i++)
            System.out.println(m[i].toString());

        try {
            try {
                FileInputStream inFile = null;
                FileOutputStream outFile = null;
                String operazione = null, filename = null, giorno=null;
                int esito = -1, files_count = 0;
                long bytes;
                int nread;
                File f;
                byte[] buffer = new byte[buffer_size];
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("U")) {
                        giorno=inSock.readUTF();
                        System.out.println("\nGiorno inviato: "+giorno);
                        filename = inSock.readUTF();
                        
                        outFile = new FileOutputStream(new File(filename));
                        System.out.println("\nNome file: " + filename);
                        // Ricevo lunghezza file
                        bytes = inSock.readLong();
                        System.out.println("\nNum byte: " + bytes);
                        // Ricevo il file dal client
                        while (bytes > 0) {
                            nread = inSock.read(buffer, 0, bytes > buffer_size ? buffer_size : (int) bytes);
                            outFile.write(buffer, 0, nread);

                            bytes -= nread;
                        }
                        outFile.close();
                        esito = 0;
                        outSock.writeInt(esito);
                        continue;
                    } else if (operazione.equals("D")) {
                        int soglia;
                        long length = 0l;
                        soglia = inSock.readInt();
                        System.out.println("\nSoglia arrivata: " + soglia);
                        for (int i = 0; i < N; i++) {
                            if (m[i].getMax() - m[i].getMin() > soglia) {
                                files_count++;
                            }
                        }
                        outSock.writeInt(files_count);

                        for (int i = 0; i < N; i++) {
                            if (m[i].getMax() - m[i].getMin() > soglia) {
                                f = new File(m[i].getFileAudio());
                                System.out.println(f.getName());
                                if (f.exists()) {
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
                                }
                                else{
                                    System.out.println("File inesistente\n");
                                    System.exit(3);
                                }
                            }
                        }
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
