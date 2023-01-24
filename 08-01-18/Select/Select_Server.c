
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

/*** #DEFINE **/
#define DIM_BUFF 100
#define LENGTH 8
#define PATENTE_LENGTH 6
#define calza_LENGTH 13
#define MAX_FILES 255
#define MAX_LENGTH 256
#define max(a, b) ((a) > (b) ? (a) : (b))

/*** STRUTTURA DA INVIARE ATTRAVERO LA SOCKET **/
// struttura di esempi del Client Datagram

/********************************************************/
// Eventuale struttura dati del server
#define N 5

typedef struct
{
	char id[MAX_LENGTH];
	char tipo[LENGTH];
	char carbone;
	char citta[MAX_LENGTH];
	char via[MAX_LENGTH];
	char messaggio[MAX_LENGTH];
} Calza;

// Funzione per la stampa della struttura dati server
void stampa(Calza tabella[])
{
	int i;
	printf("Tipo\tCarbone\tMessaggio\n");

	for (i = 0; i < N; i++)
	{
		printf("%s\t\t", tabella[i].tipo);
		printf("%c\t\t\t", tabella[i].carbone);
		printf("%s\t\t", tabella[i].messaggio);
	}
}

/********************************************************/

void gestore(int signo)
{
	int stato;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}

/********************************************************/

int main(int argc, char **argv)
{
	int listenfd, conn_sd, udpfd, nready, nread, maxfdp1, fd, num, port, len, esito, length, length_next, error;
	char calza[MAX_LENGTH], risp, buff[DIM_BUFF], buff_next[DIM_BUFF]; // tipi di risposte
	int qualcosa;
	char c;			  // tipi di wrapping per le risposte
	const int on = 1; //--> usata in setsockopt
	int dim;
	Calza req;
	FILE *picture;
	struct calzaent *dd1;
	struct sockaddr_in clientaddr, servaddr;
	struct hostent *hostTcp, *hostUdp;
	fd_set rset;
	/*** ----  Controllo argomenti ---- **/
	if (argc != 2)
	{
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}
	// Controllo che la porta sia un intero
	num = 0;
	while (argv[1][num] != '\0')
	{
		if ((argv[1][num] < '0') || (argv[1][num] > '9'))
		{
			printf("Argomento non intero\n");
			exit(2);
		}
		num++;
	}
	Calza calze[N];
	for (int i = 0; i < N; i++)
	{
		Calza c;
		strcpy(c.id, "L");
		strcpy(c.tipo, "L");
		c.carbone = 'L';
		strcpy(c.citta, "L");
		strcpy(c.via, "L");
		strcpy(c.messaggio, "L");
		calze[i] = c;
	}

	strcpy(calze[0].id, "Mario");
	strcpy(calze[0].tipo, "Celiaco");
	calze[0].carbone = 'N';
	strcpy(calze[0].citta, "Campobasso");
	strcpy(calze[0].via, "Brombeis");
	strcpy(calze[0].messaggio, "oleee");

	strcpy(calze[1].id, "Luigi");
	strcpy(calze[1].tipo, "Normale");
	calze[1].carbone = 'S';
	strcpy(calze[1].citta, "Napoli");
	strcpy(calze[1].via, "Vesuvio");
	strcpy(calze[1].messaggio, "erutta");

	// Controllo che la porta sia nel range
	port = atoi(argv[1]);
	if (port < 1024 || port > 65535)
	{
		printf("Error: porta non valida!\n");
		printf("1024 <= port <= 65535\n");
		exit(2);
	}
	printf("Server avviato\n");

	//**  CREAZIONE SOCKET TCP **/
	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd < 0)
	{
		perror("apertura socket TCP ");
		exit(1);
	}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);

	// **  INIZIALIZZAZIONE INDIRIZZO SERVER E BIND **/
	memset((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
	{
		perror("set opzioni socket TCP");
		exit(2);
	}
	printf("Set opzioni socket TCP ok\n");
	if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
	{
		perror("bind socket TCP");
		exit(3);
	}
	printf("Bind socket TCP ok\n");

	if (listen(listenfd, 5) < 0)
	{
		perror("listen");
		exit(4);
	}
	printf("Listen ok\n");

	// CREAZIONE SOCKET UDP
	udpfd = socket(AF_INET, SOCK_DGRAM, 0);
	if (udpfd < 0)
	{
		perror("apertura socket UDP");
		exit(5);
	}
	printf("Creata la socket UDP, fd=%d\n", udpfd);

	// INIZIALIZZAZIONE INDIRIZZO SERVER E BIND
	memset((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
	{
		perror("set opzioni socket UDP");
		exit(6);
	}
	printf("Set opzioni socket UDP ok\n");

	if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
	{
		perror("bind socket UDP");
		exit(7);
	}
	printf("Bind socket UDP ok\n");

	// AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE
	signal(SIGCHLD, gestore);

	// PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR
	FD_ZERO(&rset);
	maxfdp1 = max(listenfd, udpfd) + 1;

	// CICLO DI RICEZIONE EVENTI DALLA SELECT
	for (;;)
	{
		FD_SET(listenfd, &rset);
		FD_SET(udpfd, &rset);

		if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0)
		{
			if (errno == EINTR)
				continue;
			else
			{
				perror("select");
				exit(8);
			}
		}

		// CASO 1: GESTIONE RICHIESTE DA SOCKET DATAGRAM
		if (FD_ISSET(udpfd, &rset))
		{
			printf("Server: ricevuta richiesta UDP da client: \n");
			hostUdp = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
			if (hostUdp == NULL)
			{
				printf("client host information not found\n");
			}
			else
			{
				printf("Operazione richiesta da: %s %i\n", hostUdp->h_name, (unsigned)ntohs(clientaddr.sin_port));
			}

			len = sizeof(struct sockaddr_in);
			if (recvfrom(udpfd, &req, sizeof(Calza), 0, (struct sockaddr *)&clientaddr, &len) < 0)
			{
				perror("Recvfrom");
				continue;
			}
			printf("Richiesto dal client l'inserimento di questa calza: %s\n", req.id);
			esito = -1;
			dim = 0;
			for (int i = 0; i < N; i++)
			{
				if (strcmp(calze[i].id, "L") != 0 && strcmp(calze[i].tipo, "L") != 0 && calze[i].carbone != 'L' && strcmp(calze[i].citta, "L") != 0 && strcmp(calze[i].via, "L") != 0 && strcmp(calze[i].messaggio, "L") != 0)
				{
					dim++;
				}
			}
			if (dim < N)
			{
				calze[dim + 1] = req;
				esito = 0;
			}
			printf("Calza di %s, tipo %s, carbone %c, citta %s, via %s, messaggio %s\n", req.id, req.tipo, req.carbone, req.citta, req.via, req.messaggio);
			printf("Esito: %d\n", esito);
			esito = htonl(esito);
			
			if (sendto(udpfd, &esito, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
			{
				perror("Sendto");
				continue;
			}
			printf("Operazione conclusa.\n");
		}

		// CASO 2: GESTIONE RICHIESTE DA SOCKET STREAM
		if (FD_ISSET(listenfd, &rset))
		{
			len = sizeof(struct sockaddr_in);
			if ((conn_sd = accept(listenfd, (struct sockaddr *)&clientaddr, &len)) < 0)
			{
				if (errno == EINTR)
					continue;
				else
				{
					perror("accept");
					exit(9);
				}
			}
			// figlio
			if (fork() == 0)
			{
				close(listenfd);
				// chi mi fa richiesta
				hostTcp = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
				if (hostTcp == NULL)
				{
					printf("client host information not found\n");
					close(conn_sd);
					exit(6);
				}
				else
					printf("Server (figlio): host client e' %s \n", hostTcp->h_name);

				printf("Richiesta, la seguente..");
				// Lettura lunghezza tipo
				while ((nread = read(conn_sd, &length, sizeof(length))) > 0)
				{
					length = ntohl(length);
					// Lettura tipo
					if (read(conn_sd, buff, sizeof(char) * length) < 0)
					{
						perror("Errore read ");
						break;
					}
					printf("Ricevo tipologia calza: %s\n", buff);
					strcpy(calza, buff);

					//Lettura carbone
					if (read(conn_sd, &c, sizeof(c)) < 0)
					{
						perror("Errore read\n");
						exit(2);
					}
					printf("Ricevo carbone %c\n", c);
					
					// Invio numero calze selezionate
					for (int i = 0; i < N; i++)
					{
						if (strcmp(calze[i].tipo, calza) == 0 && calze[i].carbone == c)
						{
							dim++;
						}
					}
					printf("Calze corrispondenti trovate: %d\n", dim);
					dim = htonl(dim);
					if(write(conn_sd, &dim, sizeof(dim))<0)
					{
						perror("Write");
						continue;
					}
					// Invio calze selezionate
					for(int i = 0; i < N && !error; i++) {
						if(strcmp(calze[i].id, "L") != 0 && strcmp(calze[i].tipo, calza) == 0 &&
						calze[i].carbone == c) {
						
							length_next = strlen(calze[i].id) + 1;
							printf("Length %d\n", length_next);
							length_next = htonl(length_next);
							if(write(conn_sd, &length_next, sizeof(length_next)) < 0) {
								perror("Errore write ");
								error = 1;
								continue;
							}

							strcpy(buff_next, calze[i].id);
							if(write(conn_sd, buff_next, sizeof(char) * ntohl(length_next)) < 0) {
								perror("Errore write ");
								error = 1;
								continue;
							}
						}	
					}
				}
			}

			// Libero risorse
			printf("Figlio TCP terminato, libero risorse e chiudo. \n");
			close(conn_sd);
			exit(0);
		}
		close(conn_sd);
	}
}