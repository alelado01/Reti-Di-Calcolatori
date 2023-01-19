
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
#define MAX_FILES 255
#define MAX_LENGTH_FILENAME 256
#define max(a, b) ((a) > (b) ? (a) : (b))

/*** STRUTTURA DA INVIARE ATTRAVERO LA SOCKET **/
// struttura di esempi del Client Datagram

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
	char buff[DIM_BUFF], files[MAX_FILES][MAX_LENGTH_FILENAME], patente[PATENTE_LENGTH], dir[MAX_LENGTH_FILENAME], tmp_file[MAX_LENGTH_FILENAME], filename[MAX_LENGTH_FILENAME], path[1000], risp; // tipi di risposte
	int qualcosa;
	char chr;		  // tipi di wrapping per le risposte
	const int on = 1; //--> usata in setsockopt
	int len, nwrite, num, esito, port, trovato = 0, size, file_name_length, tmp_fd;
	DIR *dir1;
	FILE *picture;
	struct dirent *dd1;
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
			if (recvfrom(udpfd, &filename, sizeof(filename), 0, (struct sockaddr *)&clientaddr, &len) < 0)
			{
				perror("Recvfrom");
				continue;
			}
			printf("Richiesto dal client questo file: %s\n", filename);
			esito = -1;
			fd = open(filename, O_RDONLY);
			strcpy(tmp_file, filename);
			strcat(tmp_file, "_tmp.tmp");
			printf("File %s creato\n", tmp_file);
			tmp_fd = open(tmp_file, O_WRONLY | O_CREAT, 0777);
			if (fd < 0 || tmp_fd < 0)
			{
				perror("open");
				// INVIO RISULTATO AL CLIENT
				esito = -1;
				esito = htonl(esito);
				if (sendto(udpfd, &esito, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
				{
					perror("sendto ");
					continue;
				}

				continue;
			}
			while ((nread = read(fd, &chr, 1) > 0))
			{
				if (nread < 0)
				{
					perror("read");
					// INVIO RISULTATO AL CLIENT
					esito = -1;
					esito = htonl(esito);
					if (sendto(udpfd, &esito, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
					{
						perror("sendto ");
						continue;
					}

					continue;
				}
				if (chr != 'a' && chr != 'A' && chr != 'e' && chr != 'E' && chr != 'i' && chr != 'I' && chr != 'o' && chr != 'O' && chr != 'u' && chr != 'U')
				{
					printf("%c", chr);
					if (nwrite = write(tmp_fd, &chr, sizeof(char)) < 0)
					{
						perror("write");
						// INVIO RISULTATO AL CLIENT
						esito = -1;
						esito = htonl(esito);
						if (sendto(udpfd, &esito, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
						{
							perror("sendto ");
							continue;
						}

						continue;
					}
				}
				else
					esito++;
			}
			close(fd);
			close(tmp_fd);
			rename(tmp_file, filename);
			printf("Esito: %d\n", esito);
			esito=htonl(esito);

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
					printf("direttorio richiesto: %s\n", buff);
					strcpy(dir, buff);
					if ((dir1 = opendir(dir)) != NULL)
					{
						risp = 'S';
						int file_count = 0;
						printf("Invio risposta affermativa al client\n");
						write(conn_sd, &risp, sizeof(char));

						// Invio numero file nel direttorio
						file_count = 0;
						dir1 = opendir(dir);
						int i, vocale = 0, consonante = 0, j;
						while ((dd1 = readdir(dir1)) != NULL)
						{
							printf("%s\n", dd1->d_name);
							j = 0;
							// aggiungo 32 per avere tutte minuscole
							while (dd1->d_name[j] != '\0')
							{
								if (dd1->d_name[j] > 64 && dd1->d_name[j] < 91)
								{
									filename[j] = dd1->d_name[j] + 32;
								}
								j++;
							}
							printf("%s\n", filename);
							i=0;
							while (filename[i] != '\0')
							{
								printf("%c", filename[i]);
								if ((filename[i] >= 97 && filename[i] <= 122) &&
									(filename[i] == 'a' || filename[i] == 'e' || filename[i] == 'i' || filename[i] == 'o' || filename[i] == 'u'))
								{
									vocale++;
								}
								if ((filename[i] >= 97 && filename[i] <= 122) &&
									(filename[i] != 'a' || filename[i] != 'e' || filename[i] != 'i' || filename[i] != 'o' || filename[i] != 'u'))
								{
									consonante++;
								}
								if (consonante && vocale)
								{
									consonante = 0;
									vocale = 0;
									file_count;
									strcpy(files[file_count++], dd1->d_name);
									break;
								}
								i++;
							}
						}
						file_count = htonl(file_count);
						printf("Invio numero dei file con una vocale e una consonante: %d\n", file_count);
						if (write(conn_sd, &file_count, sizeof(file_count)) < 0)
						{
							perror("write");
							close(conn_sd);
							exit(6);
						}

						file_count = ntohl(file_count);
						for (int i = 0; i < file_count; i++)
						{
							strcpy(filename, files[i]);

							// Invio lunghezza nome file
							file_name_length = htonl(strlen(filename) + 1);
							if (write(conn_sd, &file_name_length, sizeof(int)) < 0)
							{
								perror("Errore nell'invio del nome file\n");
								close(conn_sd);
								exit(6);
							}
							// Invio nome file
							if (write(conn_sd, filename, (strlen(filename) + 1)) < 0)
							{
								perror("Errore nell'invio del nome file\n");
								close(conn_sd);
								exit(6);
							}

							// Invio dimensione file
							path[0] = '\0'; // path reset
							sprintf(path, "%s/%s", dir, filename);
							fd = open(path, O_RDONLY);
							size = lseek(fd, 0, SEEK_END);
							lseek(fd, 0, SEEK_SET);
							if (write(conn_sd, &size, sizeof(int)) < 0)
							{
								perror("Errore nell'invio della lunghezza file\n");
								close(conn_sd);
								exit(6);
							}

							// Invio file
							while ((nread = read(fd, buff, sizeof(buff))) > 0)
							{
								if (nread < 0)
								{
									perror("read");
									close(fd);
									close(conn_sd);
									exit(6);
								}
								if (write(conn_sd, buff, nread) < 0)
								{
									perror("write");
									close(fd);
									close(conn_sd);
									exit(6);
								}
							}
							close(fd);
						}
					}
					else
					{ // err apertura dir
						risp = 'N';
						printf("Invio risposta negativa al client\n");
						write(conn_sd, &risp, sizeof(char));
					}

					// Libero risorse
					printf("Figlio TCP terminato, libero risorse e chiudo. \n");
					close(conn_sd);
					exit(0);
				}
				close(conn_sd);
			}
		}
	}
}