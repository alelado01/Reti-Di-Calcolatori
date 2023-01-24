import java.io.Serializable;
import java.util.Arrays;

public class PrenotaLibri implements Serializable {

    private String titolo;
    private int M;
    private String[] copie;
    private int copieDisponibili;

    public PrenotaLibri() {
        this.titolo = "L";
        this.M = -1;
        this.copie = null;
        this.copieDisponibili = -1;
    }

    public PrenotaLibri(String titolo, int m) {
        this.titolo = titolo;
        this.M = m;
        this.copieDisponibili = m;
        this.copie = new String[M];
        for (int i = 0; i < M; i++) {
            this.copie[i] = "-1/-1/-1";
        }
    }

    public PrenotaLibri(String titolo, int m, String[] copie) {
        this.titolo = titolo;
        this.M = m;
        for (int i = 0; i < copie.length; i++) {
            if (copie[i].equals("L"))
                this.copieDisponibili++;
        }
        this.copie = copie;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public int getM() {
        return M;
    }

    public String[] getCopie() {
        return copie;
    }

    public int getCopieDisponibili() {
        return copieDisponibili;
    }

    public void decreaseCopie() {
        this.copieDisponibili--;
    }

    @Override
    public String toString() {
        return "PrenotaLibri [titolo=" + titolo + ", M=" + M + ", copie=" + Arrays.toString(copie)
                + ", copieDisponibili=" + copieDisponibili + "]";
    }

}
