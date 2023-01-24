#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <unistd.h>

int *elimina_occorrenze_1_svc(char **nomeFile, struct svc_req *reqstp)
{
    DIR *dir1;
    char buff[MAX_DIR], chr, tmp_file[MAX_DIR];
    struct dirent *dd1;
    static int count_linea = 0;
    int fd, tmp_fd, nread, i = 0;
    int static ris = 0;
    printf("Ricevuta richiesta elimina occorrenze file %s\n", *nomeFile);

    printf("Apertura file %s\n", *nomeFile);
    fd = open(*nomeFile, O_RDONLY);
    strcpy(tmp_file, *nomeFile);
    strcat(tmp_file, "__TMP_.tmp");
    tmp_fd = open(tmp_file, O_WRONLY | O_CREAT, 0777);

    if (fd < 0 || tmp_fd < 0)
    {
        perror("open");
        ris=-1;
        return &ris;
    }

    ris = 0;

    // LEGGO E SCRIVO CARATTERE
    while ((nread = read(fd, &chr, 1)) > 0)
    {
        if (nread < 0)
        {
            perror("read");
            // INVIO RISULTATO AL CLIENT
            ris=-1;
            return &ris;
        }

        if (chr != '0' && chr != '1' && chr != '2' && chr != '3' && chr != '4' && chr != '5' && chr != '6' && chr != '7' && chr != '8' && chr != '9')
        {

            if (write(tmp_fd, &chr, sizeof(char)) < 0)
            {
                perror("write");
                // INVIO RISULTATO AL CLIENT
                ris=-1;
                return &ris;
            }
        }
        else
        {
            ris++;
        }
    }

    close(fd);
    close(tmp_fd);

    rename(tmp_file, *nomeFile);

    printf("%d\n", ris);
    return &ris;
}

Risultato *lista_file_carattere_1_svc(Richiesta *req, struct svc_req *reqstp)
{
    if (req == NULL)
    {
        return NULL;
    }
    DIR *dir1;
    struct dirent *dd1;
    static Risultato res;
    int k = 0, trovato = 0;
    printf("Arrivato direttorio %s, carattere da contare almeno %d volte: %c\n", req->nomeDir, req->occorrenze, req->chr);
    if ((dir1 = opendir(req->nomeDir)) != NULL)
    {
        while ((dd1 = readdir(dir1)) != NULL)
        {

            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0)
            {
                trovato=0;
                for (int i = 0; i < strlen(dd1->d_name); i++)
                {
                    if (dd1->d_name[i] == req->chr)
                    {
                        trovato++;
                    }
                    
                }
                if (trovato>req->occorrenze)
                {
                    printf("%s\n", dd1->d_name);
                    strcpy(res.ls[k++].nomefile, dd1->d_name);
                }
            }
        }
        res.dim = k;
        return (&res);
    }
    else
    {
        perror("Direttorio inesistente\n");
        return NULL;
    }
}
