#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <unistd.h>

static Canzone table[N];
static int inizializzato = 0;

void inizializza()
{
    if (inizializzato == 1)
        return;

    inizializzato = 1;

    for (int i = 0; i < N; i++)
    {
        strcpy(table[i].cantante, "L");
        strcpy(table[i].titolo, "L");
        strcpy(table[i].nomeFile, "L");
        table[i].voti = -1;
    }

    strcpy(table[0].cantante, "Ranieri");
    strcpy(table[0].titolo, "Rose rosse");
    strcpy(table[0].nomeFile, "rose.txt");
    table[0].voti = 25;

    strcpy(table[1].cantante, "Ligabue");
    strcpy(table[1].titolo, "Certe notti");
    strcpy(table[1].nomeFile, "certenotti.txt");
    table[1].voti = 34;

    strcpy(table[2].cantante, "Mengoni");
    strcpy(table[2].titolo, "Essenziale");
    strcpy(table[2].nomeFile, "essenziale.txt");
    table[2].voti = 45;

    strcpy(table[3].cantante, "Maneskin");
    strcpy(table[3].titolo, "Fuori di testa");
    strcpy(table[3].nomeFile, "fuoriditesta.txt");
    table[3].voti = 17;
}

Risultato *visualizza_canzoni_conmenodi_v_voti_1_svc(int *voti, struct svc_req *reqstp)
{
    if (inizializzato == 0)
        inizializza();
    int k = 0;
    static Risultato ris;

    for (int i = 0; i < N; i++)
    {
        if (table[i].voti < *voti && table[i].voti > -1)
        {
            strcpy(ris.canzoni[k].cantante, table[i].cantante);
            strcpy(ris.canzoni[k].titolo, table[i].titolo);
            strcpy(ris.canzoni[k].nomeFile, table[i].nomeFile);
            ris.canzoni[k].voti = table[i].voti;
            k++;
        }
        if (k >= 6)
        {
            break;
        }
    }
    ris.dim = k;
    for (int i = 0; i < ris.dim; i++)
    {
        printf("%s, %s, %s, %d\n", ris.canzoni[i].titolo, ris.canzoni[i].cantante, ris.canzoni[i].nomeFile, ris.canzoni[i].voti);
    }

    return (&ris);
}

int *esprimi_voto_1_svc(Richiesta *req, struct svc_req *reqstp)
{
    if (inizializzato == 0)
        inizializza();
    static int esito = -1;
    // Nel caso in cui è stato inserito il titolo
    if (!req->Titolo_Cantante)
    {
        for (int i = 0; i < N; i++)
        {
            if (strcmp(req->parametro, table[i].titolo) == 0)
            {
                table[i].voti++;
                esito = 0;
            }
        }
    }
    // Nel caso in cui è stato inserito il cantante
    if (req->Titolo_Cantante)
    {
        for (int i = 0; i < N; i++)
        {
            if (strcmp(req->parametro, table[i].cantante) == 0)
            {
                table[i].voti++;
                esito = 0;
            }
        }
    }
    return (&esito);
}
