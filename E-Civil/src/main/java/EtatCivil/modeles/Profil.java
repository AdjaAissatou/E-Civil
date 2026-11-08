/***********************************************************************
 * Module:  Profil.java
 * Author:  Lenovo
 * Purpose: Defines the Class Profil
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 72ec8251-000d-4bf6-b23c-f4a86ffaab61 */
public class Profil {
   /** @pdOid 7f302ae9-45ee-4440-b1a9-2bd4c96766b8 */
   private int idProfil;
   /** @pdOid bf25617e-d9d9-4892-85ca-b2a19e486c6f */
   private java.lang.String nom;
   
   /** @pdRoleInfo migr=no name=Role assc=association22 mult=1..1 */
   public Role role;
   
   /** @pdOid 33da2b05-71ef-4eff-b5e5-c23c3cc397e2 */
   public java.lang.String getNom() {
      return nom;
   }
   
   /** @param newNom
    * @pdOid cc9a60fb-df58-44dc-932e-57e76fc24282 */
   public void setNom(java.lang.String newNom) {
      nom = newNom;
   }
   
   /** @pdOid 6ede9b13-eaff-4341-99c6-7fdf9ccb686f */
   public java.lang.Object donnerprofil() {
      // TODO: implement
      return null;
   }

public int getIdProfil() {
	return idProfil;
}

public void setIdProfil(int idProfil) {
	this.idProfil = idProfil;
}

public Role getRole() {
	return role;
}

public void setRole(Role role) {
	this.role = role;
}

}