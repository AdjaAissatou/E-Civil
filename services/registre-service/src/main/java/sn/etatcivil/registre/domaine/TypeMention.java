package sn.etatcivil.registre.domaine;

import java.util.Set;

/** Nature d'une mention marginale et actes en marge desquels elle peut être portée. */
public enum TypeMention {
    MARIAGE("Mention de mariage", Set.of(TypeActe.NAISSANCE)),
    DIVORCE("Mention de divorce", Set.of(TypeActe.NAISSANCE, TypeActe.MARIAGE)),
    DECES("Mention de décès", Set.of(TypeActe.NAISSANCE, TypeActe.MARIAGE)),
    RECONNAISSANCE("Mention de reconnaissance", Set.of(TypeActe.NAISSANCE)),
    LEGITIMATION("Mention de légitimation", Set.of(TypeActe.NAISSANCE)),
    ADOPTION("Mention d'adoption", Set.of(TypeActe.NAISSANCE)),
    RECTIFICATION("Mention de rectification", Set.of(TypeActe.values())),
    AUTRE("Autre mention", Set.of(TypeActe.values()));

    private final String libelle;
    private final Set<TypeActe> actes;

    TypeMention(String libelle, Set<TypeActe> actes) {
        this.libelle = libelle;
        this.actes = actes;
    }

    public String libelle() { return libelle; }

    public boolean sApplique(TypeActe type) { return actes.contains(type); }
}
