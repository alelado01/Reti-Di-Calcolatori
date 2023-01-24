#include "RPCxFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <unistd.h>

int *conta_occorrenze_linea_1_svc(char **linea, struct svc_req *reqstp)
{
    DIR *dir1;
    char buff[MAX_LINEA], chr, empty[MAX_LINEA];
    struct dirent *dd1;
    static int count_linea = 0;
    int fd, nread, i = 0;
    printf("Ricevuta richiesta conta occorrenza linea %s\n", *linea);

    if ((dir1 = opendir("./")) != NULL)
    {
        while ((dd1 = readdir(dir1)) != NULL)
        {
            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0)
            {
                printf("Apertura file %s\n", dd1->d_name);
                fd = open(dd1->d_name, O_RDONLY);
                if (fd > 0)
                {
                    while ((nread = read(fd, &chr, 1)) > 0)
                    {
                        buff[i] = chr;
                        i++;
                        if (chr == '\n')
                        {
                            buff[i] = '\0';
                            i = 0;
                            if (strstr(buff, *linea) != NULL)
                            {
                                count_linea++;
                            }
                            strcpy(buff, empty);
                        }
                    }
                }
                else
                {
                    perror("Apertura file fallita\n");
                    count_linea = -1;
                }
                close(fd);
            }
        }
    }
    closedir(dir1);

    printf("%d\n", count_linea);
    return &count_linea;
}

Risultato *lista_file_prefisso_1_svc(Richiesta *req, struct svc_req *reqstp)
{
    if (req == NULL)
    {
        return NULL;
    }
    DIR *dir1;
    struct dirent *dd1;
    static Risultato res;
    int k = 0, trovato = 0;
    printf("Arrivato direttorio %s, prefisso da trovare: %s\n", req->nome, req->prefix);
    if ((dir1 = opendir(req->nome)) != NULL)
    {
        while ((dd1 = readdir(dir1)) != NULL)
        {
            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0)
            {
                for (int i = 0; i < strlen(req->prefix); i++)
                {
                    if (dd1->d_name[i] == req->prefix[i])
                    {
                        trovato = 1;
                    }
                    else{
                        trovato = 0;
                        break;
                    }
                }
                if (trovato)
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
