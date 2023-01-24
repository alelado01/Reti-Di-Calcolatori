
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#define DIM_BUFF 100
#define LENGTH_FILE_NAME 20
#define LENGTH 8
#define MAX_LENGTH_FILENAME 256

/********************************************************
typedef struct
{
    struttura dati
}
Request;
/********************************************************/

int main(int argc, char *argv[])
{
  int sd, nread, port, size, file_count, file_name_length, len, fd;
  char c, ok, dir[MAX_LENGTH_FILENAME], prefix[LENGTH], buff[DIM_BUFF];
  struct hostent *host;
  struct sockaddr_in serverAddress;

  /* CONTROLLO ARGOMENTI ---------------------------------- */
  if (argc != 3)
  {
    printf("Error:%s serverAddress serverPort\n", argv[0]);
    exit(1);
  }

  printf("Client avviato\n");

  /* PREPARAZIONE INDIRIZZO SERVER ----------------------------- */
  memset((char *)&serverAddress, 0, sizeof(struct sockaddr_in));
  serverAddress.sin_family = AF_INET;

  host = gethostbyname(argv[1]);
  if (host == NULL)
  {
    printf("%s not found in /etc/hosts\n", argv[1]);
    exit(2);
  }

  // Controllo secondo argomento --> numero di porta intero
  nread = 0;
  while (argv[2][nread] != '\0')
  {
    if (argv[2][nread] < '0' || argv[2][nread] > '9')
    {
      printf("Secondo argomento non intero\n");
      exit(2);
    }
    nread++;
  }
  port = atoi(argv[2]);
  if (port < 1024 || port > 65535)
  {
    printf("Porta non corretta, range numerico sbagliato\n");
    exit(3);
  }

  serverAddress.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
  serverAddress.sin_port = htons(atoi(argv[2]));

  /* CREAZIONE E CONNESSIONE SOCKET (BIND IMPLICITA) ----------------- */
  sd = socket(AF_INET, SOCK_STREAM, 0);
  if (sd < 0)
  {
    perror("apertura socket ");
    exit(3);
  }
  printf("Creata la socket sd=%d\n", sd);

  if (connect(sd, (struct sockaddr *)&serverAddress, sizeof(struct sockaddr)) < 0)
  {
    perror("Errore in connect");
    exit(4);
  }
  printf("Connect ok\n");

  /* CORPO DEL CLIENT: */
  /* ciclo di accettazione di richieste di file ------- */

  printf("Richieste di file fino alla fine del file di input\n");
  printf("Qualsiasi tasto per procedere, EOF per terminare\n\n");

  printf("Inserisci direttorio:\n");

  while (gets(dir))
  {
    printf("\nInvio la dim del direttorio");
    len = strlen(dir) + 1;
    if (write(sd, &len, sizeof(int)) < 0)
    {
      perror("write");
      break;
    }

    if (write(sd, dir, strlen(dir) + 1) < 0)
    {
      perror("write");
      break;
    }
    printf("\nRichiesta del direttorio %s inviata... \n", dir);

    printf("Inserisci prefisso: ");
    gets(prefix);
    printf("prefisso: %s\n", prefix);
    printf("\nInvio dimensione prefisso...");
    len = strlen(prefix);
    if (write(sd, &len, sizeof(int)) < 0)
    {
      perror("write");
      break;
    }

    if (write(sd, prefix, (strlen(prefix) + 1)) < 0)
    {
      perror("write");
      break;
    }

    if (read(sd, &ok, 1) < 0)
    {
      perror("read");
      break;
    }

    if (ok == 'S')
    {
      printf("Ricevo il numero di file:\n");
      if (read(sd, &file_count, sizeof(file_count)) < 0)
      {
        perror("num file");
        break;
      }
      file_count = ntohl(file_count);
      printf("Numero di file ricevuto: %d\n", file_count);
      for (int i = 0; i < file_count; i++)
      {
        printf("Ricevo dimensione nome file\n");
        if (read(sd, &file_name_length, sizeof(int)) < 0)
        {
          perror("read");
          break;
        }
        file_name_length = ntohl(file_name_length);
        printf("Lunghezza nome file ricevuta: %d\n", file_name_length);
        char filename[file_name_length];

        printf("Ricevo nome file\n");
        if (read(sd, filename, file_name_length) < 0)
        {
          perror("read");
          break;
        }

        printf("Nome file ricevuto: %s\n", filename);

        fd = open(filename, O_WRONLY | O_CREAT, 0777);

        if (fd < 0)
        {
          perror("open");
          close(fd);
          printf("Inserire directory da scaricare, EOF per terminare: ");
          continue;
        }

        printf("Ricevo la dimensione del file\n");
        if (read(sd, &size, sizeof(int)) < 0)
        {
          perror("read");
          break;
        }

        size = ntohl(size);
        printf("Dimensione del file ricevuta: %d byte\n", size);
        char receive_buff[size];

        while (size > 0)
        {
          if (sizeof(buff) < size)
          {
            nread = read(sd, buff, sizeof(buff));
          }
          else
          {
            nread = read(sd, buff, size);
          }

          if (nread <= 0)
          {
            perror("read");
            close(fd);
            printf("Inserire directory da scaricare, EOF per terminare: ");
            continue;
          }

          if (write(fd, buff, nread) < 0)
          {
            perror("read");
            close(fd);
            printf("Inserire directory da scaricare, EOF per terminare: ");
            continue;
          }

          size -= nread;
        }

        close(fd);

        printf("Ricevo il file:\n");
        if (read(sd, &receive_buff, sizeof(receive_buff)) < 0)
        {
          perror("read");
          break;
        }
        printf("file %s ricevuto con successo!\n\n", filename);
      }
    }
    else if (ok == 'N')
      printf("File inesistente\n");

    printf("Inserisci direttorio:\n");

  } // while

  printf("\nClient Stream: termino...\n");

  // Chiusura socket
  close(sd);

  exit(0);
}
