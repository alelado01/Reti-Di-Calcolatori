public class Canzone {
    private String cantante;
    private String titolo;
    private String nomeFile;
    private int voti;
    public String getCantante() {
        return cantante;
    }
    public void setCantante(String cantante) {
        this.cantante = cantante;
    }
    public String getTitolo() {
        return titolo;
    }
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
    public String getNomeFile() {
        return nomeFile;
    }
    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }
    public int getVoti() {
        return voti;
    }
    public void setVoti(int voti) {
        this.voti = voti;
    }
    public Canzone() {
        this.cantante="L";
        this.titolo="L";
        this.nomeFile="L";
        this.voti=-1;
    }
    public Canzone(String cantante, String titolo, String nomeFile, int voti) {
        this.cantante = cantante;
        this.titolo = titolo;
        this.nomeFile = nomeFile;
        this.voti = voti;
    }

    
}
