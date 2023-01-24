#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>

/******** #DEFINE *******/
#define MAX_FILE_LENGTH 20
#define LENGTH 6

/*** STRUTTURA DA INVIARE ATTRAVERO LA SOCKET ***/
typedef struct
{
	char filename[MAX_FILE_LENGTH];
	char c1;
	char c2;
} Richiesta;

int main(int argc, char **argv)
{
	struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int port, nread, sd, ris, len=0, num;
	int nbyte_recv, nbyte_send;

	// VARIABILI
	Richiesta req;
	char char1[LENGTH], char2[LENGTH], c1, c2, filename[MAX_FILE_LENGTH];
	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if (argc != 3)
	{
		printf("Error:%s serverAddress serverPort\n", argv[0]);
		exit(1);
	}

	/* INIZIALIZZAZIONE INDIRIZZO CLIENT -------------------------- */
	memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr == INADDR_ANY;
	clientaddr.sin_port = 0;

	/* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname(argv[1]);

	/* CONTROLLO PARAMETRI SERVER -------------------------- */
	nread = 0;
	while (argv[2][nread] != '\0')
	{
		if ((argv[2][nread] < '0') || (argv[2][nread] > '9'))
		{
			printf("Secondo argomento non intero\n");
			printf("Error:%s serverAddress serverPort\n", argv[0]);
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[2]);
	if (port < 1024 || port > 65535)
	{
		printf("Port scorretta...");
		exit(2);
	}
	// controllo su host
	if (host == NULL)
	{
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}
	else
	{
		servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
		servaddr.sin_port = htons(port);
	}

	/* CREAZIONE SOCKET ---------------------------------- */
	sd = socket(AF_INET, SOCK_DGRAM, 0);
	if (sd < 0)
	{
		perror("apertura socket");
		exit(1);
	}
	printf("Client: creata la socket sd=%d\n", sd);

	/* BIND SOCKET, a una porta a caso ------------------- */
	if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0)
	{
		perror("bind socket ");
		exit(1);
	}
	printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

	/* CORPO DEL CLIENT:
	 ciclo di accettazione di richieste da utente ------- */

	printf("\n************************************\n");
	printf("\nInserisci nome file, (ctrl+d per terminare): \n");
	while (gets(filename))
	{
		strcpy(req.filename, filename);
		printf("\nInserito %s", req.filename);
		printf("\nInserisci carattere da rimuovere: ");
		gets(char1);
		c1 = char1[0];
		req.c1 = c1;
		printf("\nInserito %c", req.c1);

		printf("\nInserisci carattere da aggiungere: ");
		gets(char2);
		c2 = char2[0];
		req.c2 = c2;
		printf("\nInserito %c\n", req.c2);
		len=sizeof(servaddr); //!!!!!!!!!!!!!!!
		if (sendto(sd, &req, sizeof(Richiesta), 0, (struct sockaddr *)&servaddr, len) < 0)
		{
			perror("scrittura socket");
			printf("Inserisci nome file: \n");
			continue; // se questo invio fallisce il client torna all'inizio del ciclo
		}
		printf("Attesa del risultato...\n");
		if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&servaddr, &len) < 0)
		{
			perror("recvfrom");
			printf("Inserisci nome file: ");
			continue; // se questa ricezione fallisce il client torna all'inzio del ciclo
		}
		ris = ntohl(ris);
		if (ris >= 0)
		{
			printf("Lettere rimosse con successo: %d\n", ris);
		}
		else
			printf("Errore nella modifica del file\n");
		printf("Inserisci file: \n");
	} // while

	printf("\nClient: termino...\n");
	close(sd);
	exit(0);
} // main