package sn.etatcivil.registre.domaine;

public enum ActionJournal {
    TRANSCRIPTION("Transcription"), CORRECTION("Correction"), VALIDATION("Validation"), MENTION("Mention marginale");

    private final String libelle;

    ActionJournal(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
