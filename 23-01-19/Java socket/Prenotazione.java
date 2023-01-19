import java.io.*;

public class Prenotazione implements Serializable{
    private String targa;
    private String patente;
    private String type;
    private String images;

    public String getTarga() {
        return targa;
    }
    public void setTarga(String targa) {
        this.targa = targa;
    }
    public String getPatente() {
        return patente;
    }
    public void setPatente(String patente) {
        this.patente = patente;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getImages() {
        return images;
    }
    public void setImages(String images) {
        this.images = images;
    }
    public Prenotazione(String targa, String patente, String type, String images) {
        this.targa = targa;
        this.patente = patente;
        this.type = type;
        this.images = images;
    }
    
    
}
