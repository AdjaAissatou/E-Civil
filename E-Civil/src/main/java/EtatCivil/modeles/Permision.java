/***********************************************************************
 * Module:  Permision.java
 * Author:  Lenovo
 * Purpose: Defines the Class Permision
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 9eaf77bf-6028-47b9-b997-31d8e48c6fe0 */
public class Permision {
   /** @pdOid f77d305f-03de-4c49-97a8-fe31fac05c55 */
   private int idpermission;
   /** @pdOid b915e3f6-c838-4570-9630-6bed3a532c26 */
   private java.lang.String nom;
   /** @pdOid e31bb2f8-f603-4f87-9da2-2edea07808e6 */
   private java.lang.String description;
   /** @pdOid 0414daba-b110-40fe-9e7c-f8e0389e3a99 */
   private java.lang.String avantage;
   /** @pdOid d8574612-b345-4c9b-8c5f-ea2ed8b13da6 */
   private java.lang.String interdiction;
   
   /** @pdRoleInfo migr=no name=Role assc=association18 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<Role> role;
   
   /** @pdOid 584af74b-2796-4826-b747-39d001b974ce */
   public int getIdpermission() {
      return idpermission;
   }
   
   /** @param newIdpermission
    * @pdOid 736616c3-6ee0-4ebc-be7c-fdfee2f2ce13 */
   public void setIdpermission(int newIdpermission) {
      idpermission = newIdpermission;
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<Role> getRole() {
      if (role == null)
         role = new java.util.HashSet<Role>();
      return role;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorRole() {
      if (role == null)
         role = new java.util.HashSet<Role>();
      return role.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newRole */
   public void setRole(java.util.Collection<Role> newRole) {
      removeAllRole();
      for (java.util.Iterator iter = newRole.iterator(); iter.hasNext();)
         addRole((Role)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newRole */
   public void addRole(Role newRole) {
      if (newRole == null)
         return;
      if (this.role == null)
         this.role = new java.util.HashSet<Role>();
      if (!this.role.contains(newRole))
         this.role.add(newRole);
   }
   
   /** @pdGenerated default remove
     * @param oldRole */
   public void removeRole(Role oldRole) {
      if (oldRole == null)
         return;
      if (this.role != null)
         if (this.role.contains(oldRole))
            this.role.remove(oldRole);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllRole() {
      if (role != null)
         role.clear();
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

public java.lang.String getAvantage() {
	return avantage;
}

public void setAvantage(java.lang.String avantage) {
	this.avantage = avantage;
}

public java.lang.String getInterdiction() {
	return interdiction;
}

public void setInterdiction(java.lang.String interdiction) {
	this.interdiction = interdiction;
}

}