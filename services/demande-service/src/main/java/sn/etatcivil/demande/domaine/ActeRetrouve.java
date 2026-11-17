package sn.etatcivil.demande.domaine;

import java.util.UUID;

/** Acte du registre national rattaché à une demande. */
public record ActeRetrouve(UUID id, String numero, TypeActe typeActe, boolean valide) {
}
