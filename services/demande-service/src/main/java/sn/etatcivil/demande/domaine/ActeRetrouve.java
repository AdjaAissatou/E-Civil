package sn.etatcivil.demande.domaine;

import java.util.UUID;

/** Acte du registre national rattaché à une demande. */
public record ActeRetrouve(UUID id, String numero, TypeActe typeActe, boolean valide, java.util.Set<String> mentions) {

    public ActeRetrouve(UUID id, String numero, TypeActe typeActe, boolean valide) {
        this(id, numero, typeActe, valide, java.util.Set.of());
    }

    /** Une mention de ce type (DIVORCE, LEGITIMATION…) est-elle portée en marge de l'acte ? */
    public boolean porteMention(String type) {
        return mentions != null && mentions.contains(type);
    }
}
