package sn.etatcivil.habilitation.domaine;

import java.time.Instant;

/** Ligne du journal : qui a fait quoi, sur quoi, quand. Le journal ne se modifie jamais. */
public record Operation(Instant date, String auteur, TypeOperation type, String cible, String detail) {}
