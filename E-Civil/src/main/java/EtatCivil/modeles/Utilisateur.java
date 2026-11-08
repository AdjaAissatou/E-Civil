/***********************************************************************
 * Module:  Utilisateur.java
 * Author:  Lenovo
 * Purpose: Defines the Class Utilisateur
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid aac38bc8-409f-4521-ac00-1a8c05968205 */
public class Utilisateur extends Personne {
   /** @pdOid d0938459-fa18-402b-b2f7-11fa7b429072 */
   private int idutilisateur;
   /** @pdOid 7ee5c5b2-d018-4566-95bc-8bf4c31e9eeb */
   private java.util.Date datedinscription;
   /** @pdOid f7c721ef-64c3-4cef-93fd-dd8c3630d178 */
   private int numerocartedintentite_visa;
   /** @pdOid 2e95ac14-1697-41c6-b3b8-a21d66c24dc6 */
   private int numerotelephone;
   /** @pdOid 32c19a2c-d6a6-40d6-92a3-c0b1fa70a101 */
   private int nombredenfants;
   
   /** @pdOid f84cf76d-d77b-4a8e-8bdc-ae1bdb0faaaa */
   public Date getDatedinscription() {
	    return datedinscription;
	}

	public void setDatedinscription(Date datedinscription) {
	    this.datedinscription = datedinscription;
	}

	public int getNumerocartedintentite_visa() {
	    return numerocartedintentite_visa;
	}

	public void setNumerocartedintentite_visa(int numerocartedintentite_visa) {
	    this.numerocartedintentite_visa = numerocartedintentite_visa;
	}

	public int getNumerotelephone() {
	    return numerotelephone;
	}

	public void setNumerotelephone(int numerotelephone) {
	    this.numerotelephone = numerotelephone;
	}

	public int getNombredenfants() {
	    return nombredenfants;
	}

	public void setNombredenfants(int nombredenfants) {
	    this.nombredenfants = nombredenfants;
	}

   protected void finalize() {
      // TODO: implement
   }
   
   /** @pdRoleInfo migr=no name=TypeUtilisateur assc=association16 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<TypeUtilisateur> typeUtilisateur;
   /** @pdRoleInfo migr=no name=Profil assc=association19 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<Profil> profil;
   /** @pdRoleInfo migr=no name=Compte assc=association2 mult=1..1 */
   public Compte compte;
   /** @pdRoleInfo migr=no name=DemandeDocument assc=association15 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<DemandeDocument> demandeDocument;
   
   /** @pdOid e9f0a2e8-69db-478a-bbb1-3fd6dc78acfb */
   public void renseignerinfo() {
      // TODO: implement
   }
   
   /** @pdOid d816a6c6-6ed0-47b9-9f93-90487bf7b4fe */
   public int getIdutilisateur() {
      return idutilisateur;
   }
   
   /** @param newIdutilisateur
    * @pdOid 4f035a7d-d88e-4a7a-82c8-b2d176e9a880 */
   public void setIdutilisateur(int newIdutilisateur) {
      idutilisateur = newIdutilisateur;
   }
   
   /** @pdOid 9b7e7870-1cd1-4b72-9c3b-0cae0d994c7b */
   public Utilisateur() {
      // TODO: implement
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<TypeUtilisateur> getTypeUtilisateur() {
      if (typeUtilisateur == null)
         typeUtilisateur = new java.util.HashSet<TypeUtilisateur>();
      return typeUtilisateur;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorTypeUtilisateur() {
      if (typeUtilisateur == null)
         typeUtilisateur = new java.util.HashSet<TypeUtilisateur>();
      return typeUtilisateur.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newTypeUtilisateur */
   public void setTypeUtilisateur(java.util.Collection<TypeUtilisateur> newTypeUtilisateur) {
      removeAllTypeUtilisateur();
      for (java.util.Iterator iter = newTypeUtilisateur.iterator(); iter.hasNext();)
         addTypeUtilisateur((TypeUtilisateur)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newTypeUtilisateur */
   public void addTypeUtilisateur(TypeUtilisateur newTypeUtilisateur) {
      if (newTypeUtilisateur == null)
         return;
      if (this.typeUtilisateur == null)
         this.typeUtilisateur = new java.util.HashSet<TypeUtilisateur>();
      if (!this.typeUtilisateur.contains(newTypeUtilisateur))
         this.typeUtilisateur.add(newTypeUtilisateur);
   }
   
   /** @pdGenerated default remove
     * @param oldTypeUtilisateur */
   public void removeTypeUtilisateur(TypeUtilisateur oldTypeUtilisateur) {
      if (oldTypeUtilisateur == null)
         return;
      if (this.typeUtilisateur != null)
         if (this.typeUtilisateur.contains(oldTypeUtilisateur))
            this.typeUtilisateur.remove(oldTypeUtilisateur);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllTypeUtilisateur() {
      if (typeUtilisateur != null)
         typeUtilisateur.clear();
   }
   /** @pdGenerated default getter */
   public java.util.Collection<Profil> getProfil() {
      if (profil == null)
         profil = new java.util.HashSet<Profil>();
      return profil;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorProfil() {
      if (profil == null)
         profil = new java.util.HashSet<Profil>();
      return profil.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newProfil */
   public void setProfil(java.util.Collection<Profil> newProfil) {
      removeAllProfil();
      for (java.util.Iterator iter = newProfil.iterator(); iter.hasNext();)
         addProfil((Profil)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newProfil */
   public void addProfil(Profil newProfil) {
      if (newProfil == null)
         return;
      if (this.profil == null)
         this.profil = new java.util.HashSet<Profil>();
      if (!this.profil.contains(newProfil))
         this.profil.add(newProfil);
   }
   
   /** @pdGenerated default remove
     * @param oldProfil */
   public void removeProfil(Profil oldProfil) {
      if (oldProfil == null)
         return;
      if (this.profil != null)
         if (this.profil.contains(oldProfil))
            this.profil.remove(oldProfil);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllProfil() {
      if (profil != null)
         profil.clear();
   }
   /** @pdGenerated default getter */
   public java.util.Collection<DemandeDocument> getDemandeDocument() {
      if (demandeDocument == null)
         demandeDocument = new java.util.HashSet<DemandeDocument>();
      return demandeDocument;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorDemandeDocument() {
      if (demandeDocument == null)
         demandeDocument = new java.util.HashSet<DemandeDocument>();
      return demandeDocument.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newDemandeDocument */
   public void setDemandeDocument(java.util.Collection<DemandeDocument> newDemandeDocument) {
      removeAllDemandeDocument();
      for (java.util.Iterator iter = newDemandeDocument.iterator(); iter.hasNext();)
         addDemandeDocument((DemandeDocument)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newDemandeDocument */
   public void addDemandeDocument(DemandeDocument newDemandeDocument) {
      if (newDemandeDocument == null)
         return;
      if (this.demandeDocument == null)
         this.demandeDocument = new java.util.HashSet<DemandeDocument>();
      if (!this.demandeDocument.contains(newDemandeDocument))
         this.demandeDocument.add(newDemandeDocument);
   }
   
   /** @pdGenerated default remove
     * @param oldDemandeDocument */
   public void removeDemandeDocument(DemandeDocument oldDemandeDocument) {
      if (oldDemandeDocument == null)
         return;
      if (this.demandeDocument != null)
         if (this.demandeDocument.contains(oldDemandeDocument))
            this.demandeDocument.remove(oldDemandeDocument);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllDemandeDocument() {
      if (demandeDocument != null)
         demandeDocument.clear();
   }

}