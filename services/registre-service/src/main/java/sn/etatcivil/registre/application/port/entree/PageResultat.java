package sn.etatcivil.registre.application.port.entree;

import java.util.List;
import java.util.function.Function;

public record PageResultat<T>(List<T> contenu, int numero, int taille, long total) {
    public int nombrePages() { return taille == 0 ? 0 : (int) Math.ceil((double) total / taille); }

    public <R> PageResultat<R> map(Function<T, R> f) {
        return new PageResultat<>(contenu.stream().map(f).toList(), numero, taille, total);
    }
}
