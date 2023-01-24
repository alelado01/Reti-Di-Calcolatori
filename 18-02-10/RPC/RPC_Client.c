
#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    char *host; // nome host
    CLIENT *cl; // gestore del trasporto

    char ok[2], nl[2], buff1[MAX_LENGTH], buff2[MAX_LENGTH], buffer[MAX_LENGTH], param[MAX_LENGTH];
    Risultato *ris;
    Richiesta req;
    int *esito, voti;
    char c;
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
    printf("Inserire:\n1\tVisualizza canzoni con meno di V voti\n2\tEsprimi voto\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1)
    {
        // Consuma fine linea
        gets(&nl);

        /********* 1 - Visualizza canzoni con meno di V voti *********/
        if (strcmp(ok, "1") == 0)
        {

            printf("\nInserisci numero voti: ");
            while (scanf("%i", &voti) != 1)
            {
                while (getchar() != '\n')
                    ;

                printf("\nInserisci numero voti: ");
            }
            gets(buffer);
            printf("%i\n", voti);
            // Invocazione remota
            ris = visualizza_canzoni_conmenodi_v_voti_1(&voti, cl);
            printf("%d\n", ris->dim);
            for (int i = 0; i < ris->dim; i++)
                {
                    printf("%s\n", ris->canzoni[i].titolo);
                }
            // Controllo del risultato
            if (ris == (Risultato *)NULL)
            {
                // Errore di RPC
                printf("Risultato nullo\n");
                clnt_perror(cl, host);
                exit(1);
            }
            if (ris->dim == 0)
            {
                printf("Nessuna canzone con meno di %d voti\n", voti);
            }
            else
                for (int i = 0; i < ris->dim; i++)
                {
                    printf("%s\n", ris->canzoni[i].titolo);
                }
        }
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci T)Titolo\tC)Cantante: ");
            gets(buff2);
            c = buff2[0];
            printf("inserito carattere %c\n", c);
            if (c == 'T')
            {
                printf("\nInserisci titolo: ");
                gets(req.parametro);
                req.Titolo_Cantante = 0;
            }
            else if (c == 'C')
            {
                printf("\nInserisci cantante: ");
                gets(req.parametro);
                req.Titolo_Cantante = 1;
            }
            else
            {
                printf("Carattere invalido\n");
                continue;
            }
            esito = esprimi_voto_1(&req, cl);

            // Controllo del risultato
            if (*esito == -1)
            {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }
            else if (*esito == 0)
            {
                printf("Voto inserito correttamente\n");
            }
        }
        else
        {
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tVisualizza canzoni con meno di V voti\n2\tEsprimi voto\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    clnt_destroy(cl);
    exit(0);
} // main