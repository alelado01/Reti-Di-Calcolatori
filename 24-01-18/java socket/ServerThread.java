import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
                String operazione = null, parola = null, nomefile = null, line = null, directory;
                int esito = 0;
                int nread;
                File file, temp, dir;
                BufferedReader br;
                BufferedWriter bw;
                byte[] buffer = new byte[buffer_size];
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("E")) {
                        nomefile = inSock.readUTF();
                        parola = inSock.readUTF();
                        if (nomefile != null && parola != null) {
                            file = new File(nomefile);
                            temp = new File("file.txt");
                            br = new BufferedReader(new FileReader(file));
                            bw = new BufferedWriter(new FileWriter(temp));
                            while ((line = br.readLine()) != null) {
                                String[] lineSplit = line.split(" ");
                                for (int i = 0; i < lineSplit.length; i++) {
                                    if (!lineSplit[i].equals(parola)) {
                                        System.out.println(lineSplit[i]);
                                        bw.write(lineSplit[i]+" ");
                                    } else
                                        esito++;
                                }
                                bw.write("\n");
                            }
                            br.close();
                            bw.close();
                            file.delete();
                            if(!temp.renameTo(file))
                            {
                                esito=-1;
                            }
                        }
                        outSock.writeInt(esito);
                        continue;
                    } else if (operazione.equals("T")) {
                        int files_count = 0;
                        long numByte, length = 0l;
                        directory = inSock.readUTF();
                        System.out.println("\nDirettorio arrivato: " + directory);

                        numByte = inSock.readLong();
                        System.out.println("\nSoglia byte arrivata: " + numByte);

                        dir = new File(directory);
                        if (dir.exists() && dir.isDirectory() && dir.canRead()) {
                            System.out.println("\nDirectory presente\n");
                            // num_file
                            for (File f : dir.listFiles()) {
                                length = f.length();
                                if (length > numByte) {
                                    files_count++;
                                }
                            }
                            outSock.writeInt(files_count);
                            for (File f : dir.listFiles()) {
                                length = f.length();
                                if (length > numByte) {
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
                System.out.println("RentACarServer: termino...");
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
