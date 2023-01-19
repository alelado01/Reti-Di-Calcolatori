import java.util.Arrays;

public class Stanza {
    private final static int MAX_UTENTI = 4;
    private String name;
    private String tipo;
    private String[] utenti;

    public Stanza() {

        this.name = "L";
        this.tipo = "L";
        this.utenti = new String[MAX_UTENTI];
        for (int i = 0; i < MAX_UTENTI; i++) {
            utenti[i] = "L";
        }
    }

    public Stanza(String name, String tipo, String[] utenti) {
        this.name = name;
        this.tipo = tipo;
        this.utenti = utenti;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String[] getUtenti() {
        return utenti;
    }

    public void setUtenti(String[] utenti) {
        this.utenti = utenti;
    }

    @Override
    public String toString() {
        return "Stanza [name=" + name + ", tipo=" + tipo + ", utenti=" + Arrays.toString(utenti) + "]";
    }


    
}
