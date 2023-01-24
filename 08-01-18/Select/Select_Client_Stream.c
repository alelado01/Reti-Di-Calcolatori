
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
#define LENGTH_NAME 20
#define LENGTH 8
#define MAX_LENGTH 256

typedef struct
{
  char id[MAX_LENGTH];
  char tipo[LENGTH];
  char carbone;
  char citta[MAX_LENGTH];
  char via[MAX_LENGTH];
  char messaggio[MAX_LENGTH];
} Calza;

int main(int argc, char *argv[])
{
  int sd, nread, port, dim, length_tipo, length_next, error;
  char c, ok, tipo[LENGTH], buff[LENGTH], buff_next[MAX_LENGTH];
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

  printf("Inserisci tipo della calza (Celiaco o Normale):\n");

  while (gets(tipo))
  {

    printf("Inserire carbone ('S', 'N'): ");
    gets(buff);
    while (buff == NULL || strlen(buff) != 1 || (strcmp(buff, "S") != 0 && strcmp(buff, "N") != 0))
    {
      printf("Errore inserimento, riprovare ('S', 'N'): ");
      gets(buff);
    }
    c = buff[0];

    length_tipo = strlen(tipo) + 1;
    length_tipo = htonl(length_tipo);

    // Invio lunghezza tipo
    if (write(sd, &length_tipo, sizeof(length_tipo)) < 0)
    {
      perror("Errore write ");
      printf("\nInserire tipo calza: (ctrl+d per terminare): ");
      continue;
    }

    // Invio tipo
    if (write(sd, tipo, sizeof(char) * ntohl(length_tipo)) < 0)
    {
      perror("Errore write ");
      printf("\nInserire tipo calza: (ctrl+d per terminare): ");
      continue;
    }
    printf("Richiesta della calza di tipo %s inviata... \n", tipo);

    // Invio presenza carbone
    if (write(sd, &c, sizeof(c)) < 0)
    {
      perror("Errore write ");
      printf("\nInserire tipo calza: (ctrl+d per terminare): ");
      continue;
    }

    printf("\nRichiesta inviata\n");

    printf("Ricevo la dimensione:\n");
    if (read(sd, &dim, sizeof(dim)) < 0)
    {
      perror("num file");
      break;
    }
    dim = ntohl(dim);
    if (dim == 0)
    { // Nessuna calza trovata
      printf("\nNessuna calza trovata\n");
    }
    else
    {
      printf("\nNumero calze trovate: %d\n", dim);

      error = 0;
      for (int i = 0; i < dim && !error; i++)
      {

        if (read(sd, &length_next, sizeof(length_next)) < 0)
        {
          perror("Errore read ");
          error = 1;
          continue;
        }
        length_next = ntohl(length_next);

        if (read(sd, buff_next, sizeof(char) * length_next) < 0)
        {
          perror("Errore read ");
          error = 1;
          continue;
        }
        printf("%s\n", buff_next);
      }
    }
    printf("Inserisci tipo calza:\n");

  } // while

  printf("\nClient Stream: termino...\n");

  // Chiusura socket
  close(sd);

  exit(0);
}
