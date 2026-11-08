package sn.etatcivil.document.application.port.sortie;

import sn.etatcivil.document.domaine.DocumentDelivre;

public interface RenduPdf {
    byte[] rendre(DocumentDelivre document);
}
