
const MAX_DIR= 20;
const MAX_LENGTH =256;
const LENGTH=8;
const MAX_NUM_FILE=6;


struct ListaStringhe{
    char nomefile[MAX_DIR];
};

struct Richiesta {
    char nomeDir[MAX_DIR];
    char chr;
    int occorrenze;   
};

struct Risultato {
    ListaStringhe ls[MAX_NUM_FILE];
    int dim;
};



program OPERATION {
    version OPERATIONVERS {
        int elimina_occorrenze(string nomeFile<MAX_LENGTH>) = 1;
        Risultato lista_file_carattere(Richiesta req) = 2;
    } = 1;
} = 0x20000013;