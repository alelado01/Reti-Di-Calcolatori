#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

//Verifica che sia un numero
int isNumber(char *text) {
    int j;
    j = strlen(text);
    while (j--) {
        if (text[j] >= '0' && text[j] <= '9') continue;

        return 0;
    }
    return 1;
}

int main(int argc, char *argv[]) {
    char   *host; // nome host
    CLIENT *cl;   // gestore del trasporto

    char ok[2], nl[2], *previsione, buff[MAX_BUFF];
    Giorno input;
    Output *giorni;
    int max, min, *ris;

    previsione=(char*)malloc(10);

    // Controllo degli argomenti
    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    // Creazione gestore del trasporto
    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    // Interazione con l'utente
    printf("Inserire:\n1\tVisualizza Dati\n2\tAggiorna Temperature\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1) {
	// Consuma fine linea
	    gets(&nl);

        /********* 1 - Visualizza Dati *********/
        if (strcmp(ok, "1") == 0) {

            printf("\nInserisci previsione: ");
            gets(previsione);

            // Invocazione remota
            giorni = visualizza_dati_1(&previsione, cl);
            // Controllo del risultato
            if (giorni == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            printf("\nPrevisioni con %s: %d\n", previsione, giorni->dim);

            for (int i = 0; i < giorni->dim; i++) {
                if ( strcmp(giorni->giorni[i].giorno,"L") != 0 ) {
                    printf("\n%s\t|\t%d\t|\t%d\n",giorni->giorni[i].giorno, giorni->giorni[i].max,giorni->giorni[i].min);
                }
            }

        }
        /********* 2 - Aggiorna Temperature *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci giorno: ");
            gets(input.giorno);

            printf("\nInserisci temperatura max: ");
            while (scanf("%i", &max) != 1)
            {
                while (getchar() != '\n')
                    ;

                printf("\nInserisci temperatura max: ");
            }
            gets(buff);
            printf("%i\n", max);
            input.max=max;

            printf("\nInserisci temperatura min: ");
            while (scanf("%i", &min) != 1)
            {
                while (getchar() != '\n')
                    ;

                printf("\nInserisci temperatura min: ");
            }
            gets(buff);
            printf("%i\n", min);
            input.min=min;

            // Invocazione remota
            ris = aggiorna_temperature_1(&input, cl);

            // Controllo del risultato
            if (*ris == -1) {
                printf("Problemi nell'aggiornamento temperatura\n");
            } else if (*ris == 0) {
                printf("Temperatura aggiornata con successo\n");
            }
        } else {
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tVisualizza Dati\n2\tAggiorna Temperature\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    free(previsione);
    clnt_destroy(cl);
    exit(0);
} // main
