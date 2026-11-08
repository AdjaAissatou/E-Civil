/***********************************************************************
 * Module:  Role.java
 * Author:  Lenovo
 * Purpose: Defines the Class Role
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 968c7db6-f80f-4d37-9d6d-e608754470ae */
public class Role {
   /** @pdOid 9f9a37f4-88f0-404c-a857-be65cf8a2d0c */
   private int idrole;
   /** @pdOid 408c4938-3ee5-4050-b359-a3de3f175529 */
   private java.lang.String nom;
   /** @pdOid 8323b8c2-b96d-4b08-b2c0-93665fb7db85 */
   private java.lang.String description;
   
   /** @pdOid 9929646b-638f-486f-a49f-8d5bb515f8fd */
   public int getIdrole() {
      return idrole;
   }
   
   
   public java.lang.String getNom() {
	return nom;
}


public void setNom(java.lang.String nom) {
	this.nom = nom;
}


public java.lang.String getDescription() {
	return description;
}


public void setDescription(java.lang.String description) {
	this.description = description;
}


/** @param newIdrole
    * @pdOid 0316b5fa-7fef-4e72-8c82-85577306244d */
   public void setIdrole(int newIdrole) {
      idrole = newIdrole;
   }

}