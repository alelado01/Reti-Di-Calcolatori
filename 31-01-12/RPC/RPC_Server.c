#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static PrevedoBrutto table[MAX_GIORNI];
static int inizializzato = 0;

void inizializza()
{
    if (inizializzato == 1)
        return;

    inizializzato = 1;

    for (int i = 0; i < MAX_GIORNI; i++)
    {
        strcpy(table[i].giorno, "L");
        strcpy(table[i].previsione, "L");
        table[i].max = -273;
        table[i].min = -273;
        strcpy(table[i].fileAudio, "L");
    }

    strcpy(table[0].giorno, "lunedi");
    strcpy(table[0].previsione, "nuvoloso");
    table[0].max = 10;
    table[0].min = -1;
    strcpy(table[0].fileAudio, "ok");

    strcpy(table[1].giorno, "giovedi");
    strcpy(table[1].previsione, "neve");
    table[1].max = -1;
    table[1].min = -6;
    strcpy(table[1].fileAudio, "bau");

    strcpy(table[2].giorno, "giovedi");
    strcpy(table[2].previsione, "sereno");
    table[2].max = 15;
    table[2].min = 6;
    strcpy(table[2].fileAudio, "liu");
}

Output *visualizza_dati_1_svc(char **previsione, struct svc_req *reqstp)
{
    static Output res;
    int days_count = 0;
    printf("\nPrevisione ricevuta: %s\n", *previsione);
    if (inizializzato == 0)
    {
        inizializza();
    }

    for (int i = 0; i < MAX_GIORNI; i++)
    {
        strcpy(res.giorni[i].giorno, "L");
        res.giorni[i].max = -273;
        res.giorni[i].min = -273;
    }

    for (int i = 0; i < MAX_GIORNI; i++)
    {
        if (strcmp(table[i].previsione, *previsione) == 0)
        {
            printf("Trovato giorno con %s: %s\n", *previsione, table[i].giorno);
            strcpy(res.giorni[days_count].giorno, table[i].giorno);
            res.giorni[days_count].max = table[i].max;
            res.giorni[days_count].min = table[i].min;
            printf("%d, %d, totale: %d\n", res.giorni[days_count].max, res.giorni[days_count].min, days_count);
            days_count++;
        }
    }
    res.dim = days_count;

    return (&res);
}

int *aggiorna_temperature_1_svc(Giorno *input, struct svc_req *reqstp)
{
    static int res = -1;

    if (inizializzato == 0)
    {
        inizializza();
    }

    for (int i = 0; i < MAX_GIORNI; i++)
    {
        if (strcmp(table[i].giorno, input->giorno) == 0)
        {
            table[i].max = input->max;
            table[i].min = input->min;
            res = 0;
        }
    }

    return (&res);
}
