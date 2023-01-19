
const MAX_STANZA_LENGTH = 10;
const MAX_UTENTI =4;
const MAX_NUM=3;
const MAX_NOME=8;

struct Utente {
    char nome[MAX_NOME];
};

struct Stanza {
    char nome[MAX_STANZA_LENGTH];
    char tipo;
    Utente utenti[MAX_UTENTI];
};

struct Richiesta {
    char nome[MAX_STANZA_LENGTH];
    char tipo;   
};

struct Risultato {
    Stanza num[MAX_NUM];
    int esito;
};



program OPERATION {
    version OPERATIONVERS {
        int aggiungi_stanza(Richiesta) = 1;
        Risultato elimina_utente(string utente<8>) = 2;
    } = 1;
} = 0x20000013;