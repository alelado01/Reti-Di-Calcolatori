import java.util.Arrays;

public class Libro{
    private String titolo;
    private int copie_tot;
    private int copie_disp;
    private int copie_prestito;
    private String[] copie_prestate;


    public String getTitolo() {
        return titolo;
    }
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
    public int getCopie_tot() {
        return copie_tot;
    }
    public void setCopie_tot(int copie_tot) {
        this.copie_tot = copie_tot;
    }
    public int getCopie_disp() {
        return copie_disp;
    }
    public void setCopie_disp(int copie_disp) {
        this.copie_disp = copie_disp;
    }
    public int getCopie_prestito() {
        return copie_prestito;
    }
    public void setCopie_prestito(int copie_prestito) {
        this.copie_prestito = copie_prestito;
    }
    public String[] getCopie_prestate() {
        return copie_prestate;
    }
    public void setCopie_prestate(String[] copie_prestate) {
        this.copie_prestate = copie_prestate;
    }

    public void aggiungi_prestito(String data, int pos)
    {
        this.copie_prestate[pos]=data;
    }
    public Libro() {
        this.titolo="L";
        this.copie_tot=-1;
        this.copie_disp=-1;
        this.copie_prestito=-1;
        this.copie_prestate=new String[0];
    }
    public Libro(String titolo, int copie_tot) {
        this.titolo = titolo;
        this.copie_tot = copie_tot;
    }
    public Libro(String titolo, int copie_tot, int copie_disp, int copie_prestito, String[] copie_prestate) {
        this.titolo = titolo;
        this.copie_tot = copie_tot;
        this.copie_disp = copie_disp;
        this.copie_prestito = copie_prestito;
        this.copie_prestate = new String[copie_prestito];
    }
    @Override
    public String toString() {
        return "Libro [titolo=" + titolo + ", copie_tot=" + copie_tot + ", copie_disp=" + copie_disp
                + ", copie_prestito=" + copie_prestito + ", copie_prestate=" + Arrays.toString(copie_prestate) + "]";
    }


    

    

}