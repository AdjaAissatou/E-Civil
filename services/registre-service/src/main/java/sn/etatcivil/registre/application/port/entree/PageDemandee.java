package sn.etatcivil.registre.application.port.entree;

public record PageDemandee(int numero, int taille) {
    public PageDemandee {
        if (numero < 0) numero = 0;
        if (taille < 1 || taille > 100) taille = 20;
    }
}
