
const MAX_LENGTH_FILE= 40;
const MAX_LENGTH =256;
const MAX_SONGS=6;
const N=12;


struct Canzone{
    char cantante[MAX_LENGTH];
    char titolo[MAX_LENGTH];
    char nomeFile[MAX_LENGTH_FILE];
    int voti;
};

struct Richiesta {
    char parametro[MAX_LENGTH];
    int Titolo_Cantante;   
};

struct Risultato {
    Canzone canzoni[MAX_SONGS];
    int dim;
};



program OPERATION {
    version OPERATIONVERS {
        Risultato visualizza_canzoni_conmenodi_v_voti(int num_voti) = 1;
        int esprimi_voto(Richiesta) = 2;
    } = 1;
} = 0x20000013;