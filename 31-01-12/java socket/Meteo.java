public class Meteo {
    private String giorno;
    private String previsione;
    private int max;
    private int min;
    private String fileAudio;


    public String getGiorno() {
        return giorno;
    }
    public void setGiorno(String giorno) {
        this.giorno = giorno;
    }
    public String getPrevisione() {
        return previsione;
    }
    public void setPrevisione(String previsione) {
        this.previsione = previsione;
    }
    public int getMax() {
        return max;
    }
    public void setMax(int max) {
        this.max = max;
    }
    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min = min;
    }
    public String getFileAudio() {
        return fileAudio;
    }
    public void setFileAudio(String fileAudio) {
        this.fileAudio = fileAudio;
    }
    public Meteo(String giorno, String previsione, int max, int min, String fileAudio) {
        this.giorno = giorno;
        this.previsione = previsione;
        this.max = max;
        this.min = min;
        this.fileAudio = fileAudio;
    }
    public Meteo() {
        this.giorno = "L";
        this.previsione = "L";
        this.max = -273;
        this.min = -273;
        this.fileAudio = "L";
    }
    @Override
    public String toString() {
        return "Meteo [giorno=" + giorno + ", previsione=" + previsione + ", max=" + max + ", min=" + min
                + ", fileAudio=" + fileAudio + "]";
    }

    
    
}
