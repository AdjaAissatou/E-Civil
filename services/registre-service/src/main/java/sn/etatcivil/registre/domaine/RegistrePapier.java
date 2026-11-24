package sn.etatcivil.registre.domaine;

/**
 * Registre papier d'un centre : un par type d'acte et par année, en deux exemplaires. Il indique où le trouver,
 * son état matériel et s'il a été numérisé, pour orienter les recherches du chargé de renseignement.
 */
public record RegistrePapier(Long id, Long centreId, String nomCentre, TypeActe typeActe, int annee, Exemplaire exemplaire,
                             String volume, String lieuConservation, EtatRegistre etat, boolean numerise,
                             String observations) {

    public static RegistrePapier nouveau(Long id, Long centreId, TypeActe typeActe, Integer annee, Exemplaire exemplaire,
                                         String volume, String lieuConservation, EtatRegistre etat, boolean numerise,
                                         String observations, int anneeCourante) {
        if (centreId == null) throw new RegleMetierException("Choisissez le centre qui tient le registre");
        if (typeActe == null) throw new RegleMetierException("Indiquez le type d'actes du registre");
        if (annee == null || annee < 1900 || annee > anneeCourante) {
            throw new RegleMetierException("L'année du registre doit être comprise entre 1900 et " + anneeCourante);
        }
        if (exemplaire == null) throw new RegleMetierException("Indiquez l'exemplaire (centre ou greffe)");
        String v = CentreEtatCivil.net(volume);
        if (v != null && v.length() > 30) throw new RegleMetierException("Le volume ne doit pas dépasser 30 caractères");
        String l = CentreEtatCivil.net(lieuConservation);
        if (l != null && l.length() > 200) throw new RegleMetierException("Le lieu de conservation ne doit pas dépasser 200 caractères");
        String o = CentreEtatCivil.net(observations);
        if (o != null && o.length() > 1000) throw new RegleMetierException("Les observations ne doivent pas dépasser 1000 caractères");
        return new RegistrePapier(id, centreId, null, typeActe, annee, exemplaire, v, l, etat == null ? EtatRegistre.BON : etat,
                numerise, o);
    }

    /** Libellé court : « Naissances 1998, Centre principal de Kaolack, exemplaire du centre, vol. 2 ». */
    public String libelle() {
        String t = switch (typeActe) {
            case NAISSANCE -> "Naissances";
            case MARIAGE -> "Mariages";
            case DIVORCE -> "Divorces";
            case DECES -> "Décès";
            case RECONNAISSANCE -> "Reconnaissances";
        };
        return t + " " + annee + ", " + nomCentre + ", " + exemplaire.libelle().toLowerCase(java.util.Locale.ROOT)
                + (volume == null ? "" : ", vol. " + volume);
    }
}
