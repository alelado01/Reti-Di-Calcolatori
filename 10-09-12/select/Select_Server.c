
#include <stdio.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <regex.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define DIM_BUFF 100
#define LENGTH 8
#define LENGTH_FILE_NAME 20
#define MAX_FILES 255
#define MAX_LENGTH_FILENAME 256
#define max(a, b) ((a) > (b) ? (a) : (b))

/********************************************************/
void gestore(int signo)
{
	int stato;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}

typedef struct
{
	char filename[LENGTH_FILE_NAME];
	char c1;
	char c2;
} Richiesta;

int main(int argc, char **argv)
{
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *hostTcp, *hostUdp;
	int port, listen_sd, conn_sd, udp_sd, nread, maxfdp1, len, nready;
	const int on = 1;
	fd_set rset;

	// TCP variabili
	int dir_dim, prefix_dim, fd;
	char buff[DIM_BUFF], tmp_file[MAX_LENGTH_FILENAME], filename[MAX_LENGTH_FILENAME], path[1000], risp, files[MAX_FILES][LENGTH_FILE_NAME]; // tipi di risposte
	int qualcosa;
	char chr, c1, c2; // tipi di wrapping per le risposte
	int length, nwrite, num, esito=0, trovato = 0, size, file_name_length, tmp_fd;
	DIR *dir1;
	struct dirent *dd1;

	int found, image_fd, file_length;

	// UDP variabili
	Richiesta req;

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if (argc != 2)
	{
		printf("Error: %s port \n", argv[0]);
		exit(1);
	}
	else
	{
		nread = 0;
		while (argv[1][nread] != '\0')
		{
			if ((argv[1][nread] < '0') || (argv[1][nread] > '9'))
			{
				printf("Secondo argomento non intero\n");
				exit(2);
			}
			nread++;
		}
		port = atoi(argv[1]);
		if (port < 1024 || port > 65535)
		{
			printf("Porta scorretta...");
			exit(2);
		}
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
	printf("Server avviato\n");

	//**  CREAZIONE SOCKET TCP **/
	listen_sd = socket(AF_INET, SOCK_STREAM, 0);
	if (listen_sd < 0)
	{
		perror("apertura socket TCP ");
		exit(1);
	}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", listen_sd);

	// **  INIZIALIZZAZIONE INDIRIZZO SERVER E BIND **/
	memset((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
	{
		perror("set opzioni socket TCP");
		exit(2);
	}
	printf("Set opzioni socket TCP ok\n");
	if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
	{
		perror("bind socket TCP");
		exit(3);
	}
	printf("Bind socket TCP ok\n");

	if (listen(listen_sd, 5) < 0)
	{
		perror("listen");
		exit(4);
	}
	printf("Listen ok\n");

	// CREAZIONE SOCKET UDP
	udp_sd = socket(AF_INET, SOCK_DGRAM, 0);
	if (udp_sd < 0)
	{
		perror("apertura socket UDP");
		exit(5);
	}
	printf("Creata la socket UDP, fd=%d\n", udp_sd);

	// INIZIALIZZAZIONE INDIRIZZO SERVER E BIND
	memset((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if (setsockopt(udp_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
	{
		perror("set opzioni socket UDP");
		exit(6);
	}
	printf("Set opzioni socket UDP ok\n");

	if (bind(udp_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
	{
		perror("bind socket UDP");
		exit(7);
	}
	printf("Bind socket UDP ok\n");

	// AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE
	signal(SIGCHLD, gestore);

	// PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR
	FD_ZERO(&rset);
	maxfdp1 = max(listen_sd, udp_sd) + 1;

	// CICLO DI RICEZIONE EVENTI DALLA SELECT
	for (;;)
	{
		FD_SET(listen_sd, &rset);
		FD_SET(udp_sd, &rset);

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
		/* GESTIONE RICHIESTE UDP  ----------------------------- */
		if (FD_ISSET(udp_sd, &rset))
		{
			printf("Ricevuta richiesta di UDP\n");
			len = sizeof(struct sockaddr_in);

			// RICEVO REQUEST UDP
			if (recvfrom(udp_sd, &req, sizeof(Richiesta), 0, (struct sockaddr *)&cliaddr, &len) < 0)
			{
				perror("Recvfrom");
				continue;
			}
			printf("Richiesto dal client questo file: %s con c1=%c e c2=%c", req.filename, req.c1, req.c2);
			c1 = req.c1;
			c2 = req.c2;
			fd = open(req.filename, O_RDONLY);
			strcpy(tmp_file, req.filename);
			strcat(tmp_file, "_tmp.tmp");
			printf("\nFile %s creato\n", tmp_file);
			tmp_fd = open(tmp_file, O_WRONLY | O_CREAT, 0777);
			if (fd < 0 || tmp_fd < 0)
			{
				perror("open");
				// INVIO RISULTATO AL CLIENT
				esito = -1;
				esito = htonl(esito);
				if (sendto(udp_sd, &esito, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0)
				{
					perror("sendto ");
					continue;
				}

				continue;
			}
			while ((nread = read(fd, &chr, 1) > 0))
			{
				printf("\nCarattere letto: %c", chr);
				if (nread < 0)
				{
					perror("read");
					// INVIO RISULTATO AL CLIENT
					esito = -1;
					esito = htonl(esito);
					if (sendto(udp_sd, &esito, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0)
					{
						perror("sendto ");
						continue;
					}

					continue;
				}
				if (chr == c1)
				{
					printf("\nSi chr =%c\n", chr);
					if (nwrite = write(tmp_fd, &c2, sizeof(char)) < 0)
					{
						perror("write");
						// INVIO RISULTATO AL CLIENT
						esito = -1;
						esito = htonl(esito);
						if (sendto(udp_sd, &esito, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0)
						{
							perror("sendto ");
							continue;
						}
					}
					esito++;
					printf("\nScritto %c\n", c2);
				}
				else
				{
					printf("\nchr =%c\n", chr);
					if (nwrite = write(tmp_fd, &chr, sizeof(char)) < 0)
					{
						perror("write");
						// INVIO RISULTATO AL CLIENT
						esito = -1;
						esito = htonl(esito);
						if (sendto(udp_sd, &esito, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0)
						{
							perror("sendto ");
							continue;
						}
						continue;
					}
				}
			}
			close(fd);
			close(tmp_fd);
			rename(tmp_file, req.filename);
			printf("Esito: %d\n", esito);
			esito = htonl(esito);

			if (sendto(udp_sd, &esito, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0)
			{
				perror("Sendto");
				continue;
			}
			printf("Operazione conclusa.\n");
		}
		/* GESTIONE RICHIESTE TCP  ----------------------------- */
		if (FD_ISSET(listen_sd, &rset))
		{
			printf("Ricevuta richiesta TCP: richiesta di download dei file\n");
			length = sizeof(cliaddr);
			if ((conn_sd = accept(listen_sd, (struct sockaddr *)&cliaddr, &length)) < 0)
			{
				if (errno == EINTR)
				{
					perror("Forzo la continuazione della accept");
					continue;
				}
				else
					exit(6);
			}
			if (fork() == 0)
			{
				// GESTIONE FIGLIO TCP
				close(listen_sd);
				hostTcp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
				if (hostTcp == NULL)
				{
					printf("client host information not found\n");
					close(conn_sd);
					exit(6);
				}
				else
				{
					printf("Server (figlio): host client e' %s \n", hostTcp->h_name);
				}

				printf("Richiesta, la seguente..");
				while ((nread = read(conn_sd, &dir_dim, sizeof(int))) > 0)
				{
					printf("Dimensione direttorio ricevuta: %d\n", dir_dim);
					char dir[dir_dim];
					if (read(conn_sd, dir, strlen(dir) + 1) < 0)
					{
						perror("read");
						break;
					}
					printf("direttorio richiesto: %s\n", dir);

					printf("\nRicevo dimensione prefisso");

					if (read(conn_sd, &prefix_dim, sizeof(int)) < 0)
					{
						perror("read");
						break;
					}

					char prefix[prefix_dim];
					printf("\nRicevo il prefisso");
					if (read(conn_sd, prefix, strlen(prefix) + 1) < 0)
					{
						perror("read");
						break;
					}
					if ((dir1 = opendir(dir)) != NULL)
					{
						risp = 'S';
						int file_count = 0;
						printf("Invio risposta affermativa al client\n");
						write(conn_sd, &risp, sizeof(char));

						// Invio numero file nel direttorio
						file_count = 0;
						int trovato, j;
						while ((dd1 = readdir(dir1)) != NULL)
						{
							printf("%s\n", dd1->d_name);
							j = 0;
							trovato = 0;
							for (int i = 0; i < prefix_dim; i++)
							{
								if (prefix[i] == dd1->d_name[i])
								{
									trovato = 1;
								}
								else
								{
									trovato = 0;
									break;
								}
							}
							if (trovato == 1)
							{
								strcpy(files[file_count++], dd1->d_name);
							}
						}
						printf("files: %d", file_count);
						file_count = htonl(file_count);
						printf("\nInvio numero dei file con il prefisso %s\n", prefix);
						if (write(conn_sd, &file_count, sizeof(file_count)) < 0)
						{
							perror("write");
							close(conn_sd);
							exit(6);
						}

						file_count = ntohl(file_count);
						printf("Procedo con l'invio di %d files\n", file_count);
						for (int i = 0; i < file_count; i++)
						{
							strcpy(filename, files[i]);
							file_name_length = strlen(filename) + 1;
							printf("Invio lunghezza nome file: %d\n", file_name_length);
							// Invio lunghezza nome file
							file_name_length = htonl(file_name_length);
							if (write(conn_sd, &file_name_length, sizeof(int)) < 0)
							{
								perror("Errore nell'invio del nome file\n");
								close(conn_sd);
								exit(6);
							}
							printf("Invio nome file: %s\n", filename);
							// Invio nome file
							file_name_length = ntohl(file_name_length);
							if (write(conn_sd, filename, file_name_length) < 0)
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
							size = htonl(size);
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
					printf("Figlio TCP terminato, libero risorse e chiudo. \n");
					close(conn_sd);
					exit(0);
				}
			}
			close(conn_sd);
		}
	}
}
