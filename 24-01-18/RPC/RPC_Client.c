
#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    char *host; // nome host
    CLIENT *cl; // gestore del trasporto

    char ok[2], nl[2], *linea, dir[MAX_DIR], prefix[MAX_PREFIX];
    Risultato *ris;
    Richiesta req;
    int *esito;

    linea = (char *)malloc(256);

    // Controllo degli argomenti
    if (argc != 2)
    {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    // Creazione gestore del trasporto
    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL)
    {
        clnt_pcreateerror(host);
        exit(1);
    }

    // Interazione con l'utente
    printf("Inserire:\n1\tConta occorrenze linea in file\n2\tLista file con prefisso\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1)
    {
        // Consuma fine linea
        gets(&nl);

        /********* 1 - Conta occorrenze linea *********/
        if (strcmp(ok, "1") == 0)
        {

            printf("\nInserisci linea: ");
            gets(linea);
            // Invocazione remota
            esito = conta_occorrenze_linea_1(&linea, cl);
            // Controllo del risultato
            printf("Esito=%d\n", *esito);
            if (esito == (int *)NULL)
            {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }
            if (*esito == -1)
            {
                printf("Nessuna occorrenza della linea trovata\n");
            }
            else
                printf("Occorrenze trovate: %d\n", *esito);
        }
        /********* 2 - lista file con prefisso *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci direttorio: ");
            gets(dir);
            strcpy(req.nome, dir);
            printf("\nInserisci prefisso: ");
            gets(prefix);
            strcpy(req.prefix, prefix);
            // Invocazione remota
            ris = lista_file_prefisso_1(&req, cl);

            // Controllo del risultato
            if (ris == (Risultato *)NULL)
            {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }
            else
            {
                printf("File trovati con successo\n");
                for (int i = 0; i < ris->dim; i++)
                {
                    printf("%s\n", ris->ls[i].nomefile);
                }
            }
        }
        else
        {
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tConta occorrenze linea in file\n2\tLista file con prefisso\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    free(linea);
    clnt_destroy(cl);
    exit(0);
} // main