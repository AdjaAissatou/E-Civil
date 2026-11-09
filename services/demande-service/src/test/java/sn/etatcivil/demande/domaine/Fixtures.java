package sn.etatcivil.demande.domaine;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Fixtures {
    private Fixtures() {}

    public static final Instant MAINTENANT = Instant.parse("2026-11-08T10:00:00Z");
    public static final Demandeur FATOU = new Demandeur("u-fatou", "fatou@example.sn", "Fatou", "Diallo");
    public static final Acteur DELEGUE = new Acteur("u-del", "Awa Ndiaye", Role.DELEGUE);
    public static final Acteur AGENT = new Acteur("u-reg", "Ibrahima Fall", Role.AGENT_REGISTRE);
    public static final Acteur OFFICIER = new Acteur("u-off", "Mariama Sow", Role.OFFICIER);

    public static TypeDocument extraitNaissance() {
        return new TypeDocument(1L, "EXTRAIT_NAISSANCE", "Extrait d'acte de naissance", "", TypeActe.NAISSANCE,
                false, 1000, 2, true);
    }

    public static Motif motif() {
        return new Motif(1L, "Constitution d'un dossier administratif", 1);
    }

    public static PersonneConcernee fatou() {
        return new PersonneConcernee(" Diallo ", "Fatou", "F", LocalDate.of(1998, 3, 14), "Dakar",
                "Diallo", "Mamadou", "Sow", "Aminata", "ignoré", "ignoré", LocalDate.of(2000, 1, 1));
    }

    public static Demande demande() {
        return Demande.deposer("DEM-2026-000001", FATOU, extraitNaissance(), motif(), null, LienDemandeur.MOI_MEME,
                FormatDocument.NUMERIQUE, null, null, fatou(), MAINTENANT);
    }

    public static ActeRetrouve acte(boolean valide, TypeActe type) {
        return new ActeRetrouve(UUID.fromString("00000000-0000-0000-0000-000000000042"), "N-1998-000042", type, valide);
    }
}
