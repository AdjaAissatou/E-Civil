/***********************************************************************
 * Module:  Ville.java
 * Author:  Lenovo
 * Purpose: Defines the Class Ville
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 2b31ff21-98e1-432d-88b1-66e272805f9a */
public class Ville {
   /** @pdOid 6532fd31-f79d-48fa-ab9a-be35d8ad5f17 */
   private int idVille;
   /** @pdOid 2778f0c6-b466-49f1-bcba-fe3b8a35a943 */
   private java.lang.String nom;
   /** @pdOid beef0fbd-64eb-4c12-87bb-cd6bdd33bd97 */
   private java.lang.String codePostal;
   
   /** @pdRoleInfo migr=no name=Pays assc=association21 mult=1..1 */
   public Pays pays;
   
   /** @pdOid 75eaa4b2-855a-4388-838c-2d5abd701654 */
   public int getIdVille() {
      return idVille;
   }
   
   /** @param newIdVille
    * @pdOid 6e7833fc-92d3-4081-8df0-1b30572a5bab */
   public void setIdVille(int newIdVille) {
      idVille = newIdVille;
   }

}