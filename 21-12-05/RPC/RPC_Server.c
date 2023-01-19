#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static Stanza stanze[MAX_NUM];
static int inizializzato = 0;

void inizializza()
{
    if (inizializzato == 1)
        return;

    inizializzato = 1;

    for (int i = 0; i < MAX_NUM; i++)
    {
        strcpy(stanze[i].nome, "L");
        stanze[i].tipo = 'L';
        for (int j = 0; j < MAX_UTENTI; j++)
        {
            strcpy(stanze[i].utenti[j].nome, "L");
        }
    }

    strcpy(stanze[0].nome, "prima");
    stanze[0].tipo = 'P';
    strcpy(stanze[0].utenti[0].nome, "Pippo");
    strcpy(stanze[0].utenti[1].nome, "Pluto");

    strcpy(stanze[1].nome, "seconda");
    stanze[1].tipo = 'M';
    strcpy(stanze[1].utenti[0].nome, "Minnie");
    strcpy(stanze[1].utenti[1].nome, "Quo");
}

int *aggiungi_stanza_1_svc(Richiesta *richiesta, struct svc_req *reqstp)
{
    static int ris = -1;
    int empty_spot = -1;
    printf("Ricevuta richiesta aggiunta stanza %s, tipo %c\n", richiesta->nome,
           richiesta->tipo);

    if (inizializzato == 0)
    {
        inizializza();
    }

    for (int i = 0; i < MAX_NUM; i++)
    {
        if (strcmp(stanze[i].nome, richiesta->nome) == 0)
        {
            // nome già esistente
            printf("Stanza con nome già esistente\n");
            break;
        }
        if (strcmp(stanze[i].nome, "L") == 0)
        {
            printf("Posto libero trovato. Creazione stanza...\n");
            empty_spot = i;
        }
        else
            printf("Struttura stanze piena\n");
    }
    if (empty_spot == -1)
    {
        ris = -1;
    }
    else
    {
        strcpy(stanze[empty_spot].nome, richiesta->nome);
        stanze[empty_spot].tipo = richiesta->tipo;
        ris = 0;
    }
    printf("%d\n", ris);
    return &ris;
}

Risultato *elimina_utente_1_svc(char **nomeUtente, struct svc_req *reqstp)
{
    static Risultato res;
    int k = 0;
    res.esito = -1;
    printf("Richiesta eliminazione utente %s\n", *nomeUtente);

    if (inizializzato == 0)
    {
        inizializza();
    }

    for (int i = 0; i < MAX_NUM; i++)
    {
        for (int j = 0; j < MAX_UTENTI; j++)
        {
            if (strcmp(stanze[i].utenti[j].nome, *nomeUtente) == 0)
            {
                res.esito = 0;
                printf("Utente trovato, procedo all'eliminazione...\n");
                strcpy(stanze[i].utenti[j].nome, "L");
                res.num[k++] = stanze[i];
            }
        }
    }
    return (&res);
}