/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "RPCxFile.h"

bool_t
xdr_Utente (XDR *xdrs, Utente *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome, MAX_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Stanza (XDR *xdrs, Stanza *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome, MAX_STANZA_LENGTH,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_char (xdrs, &objp->tipo))
		 return FALSE;
	 if (!xdr_vector (xdrs, (char *)objp->utenti, MAX_UTENTI,
		sizeof (Utente), (xdrproc_t) xdr_Utente))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Richiesta (XDR *xdrs, Richiesta *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome, MAX_STANZA_LENGTH,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_char (xdrs, &objp->tipo))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Risultato (XDR *xdrs, Risultato *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->num, MAX_NUM,
		sizeof (Stanza), (xdrproc_t) xdr_Stanza))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->esito))
		 return FALSE;
	return TRUE;
}
