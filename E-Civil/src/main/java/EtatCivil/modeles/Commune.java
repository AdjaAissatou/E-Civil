/***********************************************************************
 * Module:  Commune.java
 * Author:  Lenovo
 * Purpose: Defines the Class Commune
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 7a3de8b5-1939-44c4-8350-86f52e4b1415 */
public class Commune {
   /** @pdOid b4393088-966e-474e-818e-b5a4fc9b66a7 */
   private int idCommune;
   /** @pdOid 6ea1e79b-4f66-41e1-8363-e06027334018 */
   private java.lang.String nom;
   /** @pdOid 348db0d3-2e29-458a-a87f-c9f3826e0c52 */
   private java.lang.String ville;
   
   /** @pdOid dd324d7a-fbc2-426a-845b-4190e55c4c3a */
   public int getIdCommune() {
      return idCommune;
   }
   
   public java.lang.String getNom() {
	return nom;
}

public void setNom(java.lang.String nom) {
	this.nom = nom;
}

public java.lang.String getVille() {
	return ville;
}

public void setVille(java.lang.String ville) {
	this.ville = ville;
}

/** @param newIdCommune
    * @pdOid 2bdadf36-9805-4ed5-89b8-afec9cfb8685 */
   public void setIdCommune(int newIdCommune) {
      idCommune = newIdCommune;
   }

}