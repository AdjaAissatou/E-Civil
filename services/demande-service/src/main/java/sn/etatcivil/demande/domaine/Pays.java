package sn.etatcivil.demande.domaine;

/** Pays du référentiel de la diaspora, rattaché à une zone de compétence. */
public record Pays(Long id, String nom, String codeIso, String continent, String capitale, String zone) {}
