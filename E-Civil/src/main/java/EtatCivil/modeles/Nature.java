/***********************************************************************
 * Module:  Nature.java
 * Author:  Lenovo
 * Purpose: Defines the Class Nature
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 6d777298-ff21-4465-900a-fb3d6f155e40 */
public class Nature {
   /** @pdOid ef079ca3-f60c-4386-a35f-38b49ad1751f */
   private int idnature;
   /** @pdOid 823516da-d3d6-4866-9e1b-70c8faf64574 */
   private java.lang.String description;
   /** @pdOid 67250279-a66e-4e71-8ed4-69d6c3662195 */
   private java.lang.String nom;
   private int delaiTraitement;
   private int idVariete;
   public int getDelaiTraitement() {
	return delaiTraitement;
}

public void setDelaiTraitement(int delaiTraitement) {
	this.delaiTraitement = delaiTraitement;
}

public int getIdVariete() {
	return idVariete;
}

public void setIdVariete(int idVariete) {
	this.idVariete = idVariete;
}

private double tarif_base;
   private int duree_validite;
   
   public int getIdnature() {
	return idnature;
}

public void setIdnature(int idnature) {
	this.idnature = idnature;
}

public java.lang.String getDescription() {
	return description;
}

public void setDescription(java.lang.String description) {
	this.description = description;
}

public java.lang.String getNom() {
	return nom;
}

public void setNom(java.lang.String nom) {
	this.nom = nom;
}

public double getTarif_base() {
	return tarif_base;
}

public void setTarif_base(double tarif_base) {
	this.tarif_base = tarif_base;
}

public int getDuree_validite() {
	return duree_validite;
}

public void setDuree_validite(int duree_validite) {
	this.duree_validite = duree_validite;
}

/** @pdRoleInfo migr=no name=DocumentEtatCivil assc=association24 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<DocumentEtatCivil> documentEtatCivil;
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<DocumentEtatCivil> getDocumentEtatCivil() {
      if (documentEtatCivil == null)
         documentEtatCivil = new java.util.HashSet<DocumentEtatCivil>();
      return documentEtatCivil;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorDocumentEtatCivil() {
      if (documentEtatCivil == null)
         documentEtatCivil = new java.util.HashSet<DocumentEtatCivil>();
      return documentEtatCivil.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newDocumentEtatCivil */
   public void setDocumentEtatCivil(java.util.Collection<DocumentEtatCivil> newDocumentEtatCivil) {
      removeAllDocumentEtatCivil();
      for (java.util.Iterator iter = newDocumentEtatCivil.iterator(); iter.hasNext();)
         addDocumentEtatCivil((DocumentEtatCivil)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newDocumentEtatCivil */
   public void addDocumentEtatCivil(DocumentEtatCivil newDocumentEtatCivil) {
      if (newDocumentEtatCivil == null)
         return;
      if (this.documentEtatCivil == null)
         this.documentEtatCivil = new java.util.HashSet<DocumentEtatCivil>();
      if (!this.documentEtatCivil.contains(newDocumentEtatCivil))
         this.documentEtatCivil.add(newDocumentEtatCivil);
   }
   
   /** @pdGenerated default remove
     * @param oldDocumentEtatCivil */
   public void removeDocumentEtatCivil(DocumentEtatCivil oldDocumentEtatCivil) {
      if (oldDocumentEtatCivil == null)
         return;
      if (this.documentEtatCivil != null)
         if (this.documentEtatCivil.contains(oldDocumentEtatCivil))
            this.documentEtatCivil.remove(oldDocumentEtatCivil);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllDocumentEtatCivil() {
      if (documentEtatCivil != null)
         documentEtatCivil.clear();
   }

}