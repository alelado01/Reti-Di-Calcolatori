
const MAX_DIR= 20;
const MAX_LINEA =256;
const MAX_NUM_FILE=6;
const MAX_PREFIX=11;


struct ListaStringhe{
    char nomefile[MAX_DIR];
};

struct Richiesta {
    char nome[MAX_DIR];
    char prefix[MAX_PREFIX];   
};

struct Risultato {
    ListaStringhe ls[MAX_NUM_FILE];
    int dim;
};



program OPERATION {
    version OPERATIONVERS {
        int conta_occorrenze_linea(string linea<MAX_LINEA>) = 1;
        Risultato lista_file_prefisso(Richiesta req) = 2;
    } = 1;
} = 0x20000013;