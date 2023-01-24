/************************************
 *        Ladomorzi Alessio          *
 *           0000988967              *
 *                                   *
 ************************************/
#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    char *host; // nome host
    CLIENT *cl; // gestore del trasporto

    char ok[2], nl[2], *nomeFile, *dir;
    Risultato *ris;
    int *esito;

    nomeFile = (char *)malloc(100);
    dir = (char *)malloc(100);

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
    printf("Inserire:\n1\tElimina occorrenze in file\n2\tLista file testo\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1)
    {
        // Consuma fine linea
        gets(&nl);

        /********* 1 - Conta occorrenze in file *********/
        if (strcmp(ok, "1") == 0)
        {

            printf("\nInserisci nome file: ");
            gets(nomeFile);
            // Invocazione remota
            esito = elimina_occorrenze_1(&nomeFile, cl);
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
        /********* 2 - lista file testo *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci direttorio: ");
            gets(dir);
            // Invocazione remota
            printf("direttorio %s\n", dir);
            ris = lista_filetesto_1(&dir, cl);

            // Controllo del risultato
            if (ris == (Risultato *)NULL)
            {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }
            else
            {
                printf("File txt trovati con successo\n");
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

        printf("\nInserire:\n1\tElimina occorrenze in file\n2\tLista file testo\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    free(nomeFile);
    free(dir);
    clnt_destroy(cl);
    exit(0);
} // main
