package sn.etatcivil.central.numerotation;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "numerotation")
@IdClass(Numerotation.Cle.class)
public class Numerotation {

    @Id
    @Column(name = "type_acte")
    private String typeActe;

    @Id
    private Integer annee;

    @Column(name = "dernier_numero", nullable = false)
    private Long dernierNumero;

    protected Numerotation() {}

    public Numerotation(String typeActe, Integer annee) {
        this.typeActe = typeActe;
        this.annee = annee;
        this.dernierNumero = 0L;
    }

    public long suivant() {
        dernierNumero = dernierNumero + 1;
        return dernierNumero;
    }

    public static class Cle implements Serializable {
        private String typeActe;
        private Integer annee;

        public Cle() {}

        public Cle(String typeActe, Integer annee) {
            this.typeActe = typeActe;
            this.annee = annee;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Cle c && Objects.equals(typeActe, c.typeActe) && Objects.equals(annee, c.annee);
        }

        @Override
        public int hashCode() { return Objects.hash(typeActe, annee); }
    }
}
