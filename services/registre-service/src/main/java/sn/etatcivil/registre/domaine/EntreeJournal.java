package sn.etatcivil.registre.domaine;

import java.time.Instant;
import java.util.UUID;

/** Trace d'une opération sur un acte : qui, quand, quoi. Le journal du registre ne se modifie jamais. */
public record EntreeJournal(UUID acteId, Instant date, String auteur, ActionJournal action, String detail) {}
