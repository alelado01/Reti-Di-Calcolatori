/************************************
*        Ladomorzi Alessio          *
*           0000988967              *
*                                   *
************************************/

const MAX_NUM_FILE=6;
const MAX_DIR=100;


struct ListaStringhe{
    char nomefile[MAX_DIR];
};

struct Risultato {
    ListaStringhe ls[MAX_NUM_FILE];
    int dim;
};



program OPERATION {
    version OPERATIONVERS {
        int elimina_occorrenze(string nomeFile<100>) = 1;
        Risultato lista_filetesto(string direttorio<100>) = 2;
    } = 1;
} = 0x20000013;
