package sn.etatcivil.demande.application.port.sortie;

/** Références lisibles et uniques : DEM-2026-000001, DEM-2026-000002… */
public interface GenerateurReference {
    String suivante(int annee);
}
