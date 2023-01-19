
#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    char *host; // nome host
    CLIENT *cl; // gestore del trasporto

    char ok[2], nl[2], *nomeUtente, tipo;
    Risultato *ris;
    Richiesta req;
    int *esito;

    nomeUtente = (char *)malloc(8);

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
    printf("Inserire:\n1\tAggiungi stanza\n2\tElimina utente\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1)
    {
        // Consuma fine linea
        gets(&nl);

        /********* 1 - Aggiungi stanza *********/
        if (strcmp(ok, "1") == 0)
        {

            printf("\nInserisci nome stanza: ");
            gets(nomeUtente);
            printf("\nInserisci tipo di comunicazione: (M)ulticast o (P)unto-a-punto: \n");
            tipo = getchar();
            if (tipo == EOF)
            {
                printf("Richiesta terminazione, esco...");
                clnt_destroy(cl);
                free(nomeUtente);
                exit(0);
            }
            strcpy(req.nome, nomeUtente);
            req.tipo = tipo;
            // Invocazione remota
            esito = aggiungi_stanza_1(&req, cl);
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
                printf("Errore, stanza non aggiunta\n");
            }
            else
                printf("Stanza aggiunta con successo!\n");
        }
        /********* 2 - Elimina utente *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci nome utente da eliminare: ");
            gets(nomeUtente);

            // Invocazione remota
            ris = elimina_utente_1(&nomeUtente, cl);

            // Controllo del risultato
            if (ris == (Risultato *)NULL)
            {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if (ris->esito == -1)
            {
                printf("Problemi nell'eliminazione\n");
            }
            else if (ris->esito == 0)
            {
                printf("Utente eliminato con successo\n");
            }
        }
        else
        {
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tAggiungi stanza\n2\tElimina utente\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    clnt_destroy(cl);
    exit(0);
} // main