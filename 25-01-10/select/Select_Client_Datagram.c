
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
#define MAX_PREN 10
#define M 6
#define STRING_LENGTH 255

#define MAX_TITOLO 21
#define MAX_COPIA 11

typedef struct
{
	char titolo[MAX_TITOLO];
	int copie[M][3];
	int copieTotali;
	int copieDisp;
} Prenotazione;

typedef struct
{
	char titolo[MAX_TITOLO];
	int gg;
	int mm;
	int aa;
} UDPReq;


int main(int argc, char **argv) {
    struct hostent    *host;
    struct sockaddr_in clientaddr, servaddr;
    int                port, nread, sd, ris, len = 0;
    
    char buffer[50];
    UDPReq req;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
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
    host                = gethostbyname(argv[1]);

    /* CONTROLLO PARAMETRI SERVER -------------------------- */
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Secondo argomento non intero\n");
            printf("Error:%s serverAddress serverPort\n", argv[0]);
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Port scorretta...");
        exit(2);
    }
    // controllo su host
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    } else {
        servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
        servaddr.sin_port        = htons(port);
    }

    /* CREAZIONE SOCKET ---------------------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(1);
    }
    printf("Client: creata la socket sd=%d\n", sd);

    /* BIND SOCKET, a una porta a caso ------------------- */
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

    /* CORPO DEL CLIENT:
     ciclo di accettazione di richieste da utente ------- */

    printf("Dammi l'id della prenotazione, EOF per terminare: ");

    while (gets(req.titolo)) {

        printf("\nInserisci giorno: ");
        while(scanf("%i",&req.gg ) != 1) {
            while ( getchar() != '\n' );
            printf("\nInserisci giorno: ");
        }
        gets(buffer);

        printf("\nInserisci mese: ");
        while(scanf("%i",&req.mm ) != 1) {
            while ( getchar() != '\n' );
            printf("\nInserisci mese: ");
        }
        gets(buffer);

        printf("\nInserisci anno: ");
        while(scanf("%i",&req.aa ) != 1) {
            while ( getchar() != '\n' );
            printf("\nInserisci anno: ");
        }
        gets(buffer);

        // INVIA REQUEST UDP
        len = sizeof(servaddr);
        if (sendto(sd, &req, sizeof(req), 0, (struct sockaddr *)&servaddr, len) < 0) {
            perror("sendto");
            
            printf("Dammi l'id della prenotazione, EOF per terminare: ");
            continue;
        }

        // RICEVI RISULTATO
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&servaddr, &len) < 0) {
            perror("recvfrom");
            
            printf("Dammi l'id della prenotazione, EOF per terminare: ");
            continue;
        }

        ris = ntohl(ris);

        if (ris < 0) {
            printf("Errore:\n");
        } else {
            printf("Risultato ottenuto: %d\n", ris);
        }
        printf("Dammi l'id della prenotazione, EOF per terminare: ");
    } // while
} // main
