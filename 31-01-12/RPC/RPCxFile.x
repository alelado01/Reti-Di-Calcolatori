const MAX_GIORNO = 10;
const MAX_PREVISIONE = 10;
const MAX_GIORNI = 8;
const MAX_AUDIO = 256;
const MAX_BUFF=256;

struct PrevedoBrutto {
    char giorno[MAX_GIORNO];
    char previsione[MAX_PREVISIONE];
    int max;
    int min;
    char fileAudio[MAX_AUDIO];  
};

struct Giorno{
    char giorno[MAX_GIORNO];
    int max;
    int min;
};

struct Output {
    Giorno giorni[MAX_GIORNI];
    int dim;
};

program OPERATION {
    version OPERATIONVERS {
        Output visualizza_dati(string previsione<10>) = 1;
        int aggiorna_temperature(Giorno g) = 2;
    } = 1;
} = 0x20000013;
