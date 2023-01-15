/************************************
 *     Ricatti Luigi Francesco       *
 *     Select_Server.c               *
 *           		            *
 ************************************/

/******** #INCLUDE ********/
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
#define DIR_LENGTH 13
#define MAX_LENGTH_FILENAME 256
#define max(a, b) ((a) > (b) ? (a) : (b))

/*** STRUTTURA DA INVIARE ATTRAVERO LA SOCKET **/
// struttura di esempi del Client Datagram
typedef struct
{
	char targa[LENGTH];
	char patente[PATENTE_LENGTH];
} Request;

typedef struct
{
	char targa[LENGTH];
	char patente[PATENTE_LENGTH];
	char tipo[7];
	char images[LENGTH + 5];
} Prenotazione;

/********************************************************/
// Eventuale struttura dati del server
/*

#define LENGTH 25
#define N 5

typedef struct
{
	char cantante[LENGTH];
	char titolo[LENGTH];
	int voti;
	char nomeFile[LENGTH];
}
Canzone;

// Funzione per la stampa della struttura dati server
void stampa(Canzone tabella[])
{
	int i;
	printf("Cantan\t\tTitolo\t\tVoti\t\tNomeFile\n");

	for( i=0; i<N; i++)
	{
		printf("%s\t\t",tabella[i].cantante);
		printf("%s\t\t",tabella[i].titolo);
		printf("%d\t\t",tabella[i].voti);
		printf("%s\n",tabella[i].nomeFile);
	}
}

*/

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
	int listenfd, conn_sd, udpfd, nready, nread, maxfdp1, fd;
	char buff[DIM_BUFF], patente[PATENTE_LENGTH], targa[LENGTH], dir[DIR_LENGTH], path[1000]; // tipi di risposte
	int qualcosa;																			  // tipi di wrapping per le risposte
	const int on = 1;																		  //--> usata in setsockopt
	int len, nwrite, num, esito, port, trovato = 0, size;
	DIR *dir1;
	FILE *picture;
	struct dirent *dd1;
	struct sockaddr_in clientaddr, servaddr;
	struct hostent *hostTcp, *hostUdp;
	fd_set rset;
	Request req;
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

	// Controllo che la porta sia nel range
	port = atoi(argv[1]);
	if (port < 1024 || port > 65535)
	{
		printf("Error: porta non valida!\n");
		printf("1024 <= port <= 65535\n");
		exit(2);
	}
	printf("Server avviato\n");

	Prenotazione prenotazioni[4];
	for (int i = 0; i < 4; i++)
	{
		Prenotazione p;
		strcpy(p.targa, "L");
		strcpy(p.patente, "L");
		strcpy(p.tipo, "L");
		strcpy(p.images, "L");
		prenotazioni[i] = p;
	}

	strcpy(prenotazioni[0].targa, "AA333AA");
	strcpy(prenotazioni[0].patente, "aaaaa");
	strcpy(prenotazioni[0].tipo, "auto");
	strcpy(prenotazioni[0].images, "AA333AA");
	

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
			if (recvfrom(udpfd, &req, sizeof(Request), 0, (struct sockaddr *)&clientaddr, &len) < 0)
			{
				perror("Recvfrom");
				continue;
			}
			printf("Richiesto dal client...\n");
			esito = -1;
			strcpy(targa, req.targa);
			strcpy(patente, req.patente);
			for (int i = 0; i < 4; i++)
			{
				if (strcmp(prenotazioni[i].targa, targa) == 0)
				{
					strcpy(prenotazioni[i].patente, patente);
					printf("patente aggiornata: %s\n", prenotazioni[i].patente);
					esito = 1;
				}
			}
			printf("Esito: %d\n", esito);
			esito = ntohl(esito);

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
				while ((nread = read(conn_sd, buff, sizeof(buff))) > 0)
				{
					printf("Targa richiesta: %s\n", buff);
					strcpy(targa, buff);
					Prenotazione p;
					for (int i = 0; i < 4; i++)
					{
						if (strcmp(prenotazioni[i].targa, targa) == 0)
						{
							trovato = 1;
							p = prenotazioni[i];
							break;
						}
					}
					if (trovato)
					{
						printf("Targa trovata!\n");
						strcpy(dir, p.images);
					}
					char risp;
					if ((dir1 = opendir(dir)) != NULL)
					{
						risp = 'S';
						int file_count = 0;
						printf("Invio risposta affermativa al client\n");
						write(conn_sd, &risp, sizeof(char));
						while ((dd1 = readdir(dir1)) != NULL)
						{
							if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0)
							{
								// path = directory/filename
								path[0] = '\0'; // path reset
								sprintf(path, "%s/%s", dir, dd1->d_name);
								fd = open(path, O_RDONLY);
								size = lseek(fd, 0, SEEK_END);
								lseek(fd, 0, SEEK_SET);

								// Invio nome file
								printf("Invio nome file: %s\n", dd1->d_name);
								write(conn_sd, &dd1->d_name, MAX_LENGTH_FILENAME);

								// Invio dimensione file
								printf("Invio dimensione file: %d\n", size);
								write(conn_sd, &size, sizeof(int));

								char send_buff[size];

								if (nread = read(fd, send_buff, sizeof(send_buff)) < 0)
								{
									perror("read file");
									break;
								}
								write(conn_sd, send_buff, sizeof(send_buff));
								bzero(send_buff, sizeof(send_buff));
								close(fd);
							}
						}
					}
				}
				//} //for

				printf("Figlio %i: chiudo connessione e termino\n", getpid());
				close(conn_sd);
				exit(0);
			} // fine figlio

			close(conn_sd);
		} // if listen

	} /* ciclo for della select */

	/* NEVER ARRIVES HERE */
	exit(0);
} // main
