public class Calza {
    private String id;
    private String tipo;
    private char carbone;
    private String citta;
    private String via;
    private String messaggio;

    public Calza() {
        this.id = "L";
        this.tipo = "L";
        this.carbone = 'L';
        this.citta = "L";
        this.via = "L";
        this.messaggio = "L";

    }

    public Calza(String id, String tipo, char carbone, String citta, String via, String messaggio) {
        this.id = id;
        this.tipo = tipo;
        this.carbone = carbone;
        this.citta = citta;
        this.via = via;
        this.messaggio = messaggio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public char getCarbone() {
        return carbone;
    }

    public void setCarbone(char carbone) {
        this.carbone = carbone;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
}
