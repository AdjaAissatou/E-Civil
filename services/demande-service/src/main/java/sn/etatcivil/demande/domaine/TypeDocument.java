package sn.etatcivil.demande.domaine;

/** Document délivrable du catalogue (tarif en francs CFA). */
public class TypeDocument {

    /** Montant minimal accepté par PayDunya. */
    public static final int TARIF_MINIMAL = 200;

    private final Long id;
    private final String code;
    private final String libelle;
    private final String description;
    private final TypeActe typeActe;
    private final boolean copieIntegrale;
    private int tarif;
    private int delaiJours;
    private boolean actif;

    public TypeDocument(Long id, String code, String libelle, String description, TypeActe typeActe,
                        boolean copieIntegrale, int tarif, int delaiJours, boolean actif) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.typeActe = typeActe;
        this.copieIntegrale = copieIntegrale;
        this.tarif = tarif;
        this.delaiJours = delaiJours;
        this.actif = actif;
    }

    /** Un changement de tarif ne s'applique qu'aux nouvelles demandes : le montant est figé au dépôt. */
    public void modifier(int tarif, int delaiJours, boolean actif) {
        if (tarif < TARIF_MINIMAL) throw new RegleMetierException("Le tarif doit être d'au moins " + TARIF_MINIMAL + " FCFA");
        if (delaiJours < 1 || delaiJours > 60) throw new RegleMetierException("Le délai doit être compris entre 1 et 60 jours");
        this.tarif = tarif;
        this.delaiJours = delaiJours;
        this.actif = actif;
    }

    public Long id() { return id; }
    public String code() { return code; }
    public String libelle() { return libelle; }
    public String description() { return description; }
    public TypeActe typeActe() { return typeActe; }
    public boolean copieIntegrale() { return copieIntegrale; }
    public int tarif() { return tarif; }
    public int delaiJours() { return delaiJours; }
    public boolean actif() { return actif; }
}
