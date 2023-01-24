import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ServerThread extends Thread {

    private Socket clientSocket = null;
    private int buffer_size = 4096;

    /**
     * Constructor
     * 
     * @param clientSocket
     */
    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
                String operazione = null, line = null, directory = null;
                int esito = 0, occorrenze, count_occorrenze;
                char chr;
                int nread;
                File dirE, dirT;
                BufferedReader br;
                byte[] buffer = new byte[buffer_size];
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("C")) {
                        chr = inSock.readChar();
                        System.out.println("carattere da contare: " + chr);
                        occorrenze = inSock.readInt();
                        System.out.println("\nOccorrenze minime: " + occorrenze);
                        dirE = new File(".");
                        if (dirE.exists() && dirE.isDirectory() && dirE.canRead()) {
                            for (File f : dirE.listFiles()) {
                                if (f.isFile()) {
                                    br = new BufferedReader(new FileReader(f));
                                    while ((line = br.readLine()) != null) {
                                        count_occorrenze = 0;
                                        for (int i = 0; i < line.length(); i++) {
                                            if (line.charAt(i) == chr) {
                                                count_occorrenze++;
                                            }
                                        }
                                        if (count_occorrenze >= occorrenze) {
                                            esito++;
                                        }
                                    }
                                    br.close();
                                }
                            }
                        } else
                            esito = -1;

                        outSock.writeInt(esito);
                        continue;
                    } else if (operazione.equals("T")) {
                        int files_count = 0;
                        long length = 0l;
                        directory = inSock.readUTF();
                        System.out.println("\nDirettorio arrivato: " + directory);

                        dirT = new File(directory);
                        if (dirT.exists() && dirT.isDirectory() && dirT.canRead()) {
                            System.out.println("\nDirectory presente\n");
                            // num_file
                            for (File f : dirT.listFiles()) {
                                length = f.length();
                                if (!f.getName().endsWith(".txt")) {
                                    files_count++;
                                }
                            }
                            outSock.writeInt(files_count);
                            for (File f : dirT.listFiles()) {
                                length = f.length();
                                if (!f.getName().endsWith(".txt")) {
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
                            }
                        }
                    }
                }
            } catch (EOFException eof) {
                System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
                clientSocket.close();
                System.out.println("Server: termino...");
                return;
            } catch (SocketTimeoutException ste) {
                System.out.println("Timeout scattato: ");
                ste.printStackTrace();
                clientSocket.close();
                return;
            } catch (Exception e) {
                System.out.println("Problemi, i seguenti : ");
                e.printStackTrace();
                System.out.println("Chiudo ed esco...");
                clientSocket.close();
                return;
            }
        } catch (

        IOException ioe) {
            System.out.println("Problemi nella chiusura della socket: ");
            ioe.printStackTrace();
            System.out.println("Chiudo ed esco...");
            return;
        }
    }
}
// run
