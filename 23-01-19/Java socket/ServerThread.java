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
    private static Prenotazione[] prenotazioni;
    private static final int MAX = 10;
    private int buffer_size = 4096;

    /**
     * Constructor
     * 
     * @param clientSocket
     */
    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        if (prenotazioni == null) {
            prenotazioni = new Prenotazione[MAX];
        }
        for (int i = 0; i < MAX; i++) {
            prenotazioni[i] = new Prenotazione("L", "L", "L", "L");
        }
        prenotazioni[0] = new Prenotazione("AA111AA", "12345", "auto", "AA111AA_img/");
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
                String operazione, targa, downloadFolder=null;
                boolean esito = false;
                long numByte;
                int nread;
                File dirImages;
                byte[] buffer = new byte[buffer_size];
                while ((operazione = inSock.readUTF()) != null) {
                    if (operazione.equals("E")) {
                        targa = inSock.readUTF();
                        for (int i = 0; i < MAX; i++) {
                            if (prenotazioni[i].getTarga().equals(targa)) {
                                dirImages = new File(prenotazioni[i].getImages());
                                prenotazioni[i] = new Prenotazione("L", "L", "L", "L");
                                esito = true;
                                if (dirImages.exists() && dirImages.isDirectory()) {
                                    for (File f : dirImages.listFiles()) {
                                        f.delete();
                                    }
                                }
                                dirImages.delete();
                                break;
                            }
                        }
                        outSock.writeBoolean(esito);
                        continue;
                    } else if (operazione.equals("D")) {
                        targa = inSock.readUTF();
                        System.out.println("Targa arrivata: " + targa);

                        for (int i = 0; i < MAX; i++) {
                            if (prenotazioni[i].getTarga().equals(targa)) {
                                downloadFolder = prenotazioni[i].getImages();
                                break;
                            }
                        }
                        System.out.println("download Folder: "+ downloadFolder+ "\n");
                        dirImages=new File(downloadFolder);
                        if (dirImages.exists() && dirImages.isDirectory() && dirImages.canRead()) {
                            esito = true;
                            outSock.writeBoolean(esito);
                            System.out.println("Directory presente\n");

                            // num_file
                            outSock.writeInt(dirImages.listFiles().length);
                            for (File f : dirImages.listFiles()) {
                                outSock.writeUTF(f.getName());
                                inFile = new FileInputStream(f);
                                numByte = f.length();
                                outSock.writeLong(numByte);
                                // leggo buffer da file finchè il numByte >0, sottraggo i byte letti a numByte
                                while (numByte > 0) {
                                    nread = inFile.read(buffer);
                                    outSock.write(buffer, 0, nread);

                                    numByte -= nread;
                                }
                                inFile.close();
                            }
                        } else
                            outSock.writeBoolean(esito);
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
