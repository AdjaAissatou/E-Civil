/***********************************************************************
 * Module:  DemandeDocument.java
 * Author:  Lenovo
 * Purpose: Defines the Class DemandeDocument
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 7fc1e32b-44d7-4f69-968b-ded15385f3f1 */
public class DemandeDocument extends Personne {
   /** @pdOid 4b0374f2-095c-4dc7-bd99-f4ebe6c40775 */
   private int idDemande;
   /** @pdOid 9235c402-a380-4dac-ac87-2054c91a27b1 */
   private java.util.Date date;
   /** @pdOid d5087074-54f7-42b1-bec6-dd4436249c5a */
   private java.lang.String format;
   /** @pdOid 7bb1fb27-4422-4cf9-810f-a78b46dd367b */
   private java.lang.String qrCode;
   /** @pdOid 2ced9f77-d920-47d6-8b9a-34e336784c44 */
   private java.lang.String status;
   private String adresseLivraison;
   private String villeLivraison;
   private String destinataire; // "utilisateur" ou "autre"
   private String natureDocument;
   private int idDocument;   // Nouveau champ
   private int idNature;     // Nouveau champ
   private int idMotif;      // Nouveau champ
   private int idutilisateur;
private Integer idpaiement;
private boolean paiementRequis;
private Map<String, Object> informationsSupplementaires;
private boolean rechercheLancee;
private Date dateCreation; 

public Map<String, Object> getInformationsSupplementaires() {
	return informationsSupplementaires;
}

public void setInformationsSupplementaires(Map<String, Object> informationsSupplementaires) {
	this.informationsSupplementaires = informationsSupplementaires;
}

public boolean isRechercheLancee() {
	return rechercheLancee;
}

public void setRechercheLancee(boolean rechercheLancee) {
	this.rechercheLancee = rechercheLancee;
}

public Date getDateCreation() {
	return dateCreation;
}

public void setDateCreation(Date dateCreation) {
	this.dateCreation = dateCreation;
}

public DemandeDocument() {
    this.informationsSupplementaires = new HashMap<>();
    this.dateCreation = new Date();
}

   public boolean isPaiementRequis() {
	return paiementRequis;
}

public void setPaiementRequis(boolean paiementRequis) {
	this.paiementRequis = paiementRequis;
}

public Integer  getIdpaiement() {
	return idpaiement;
}

public void setIdpaiement(Integer  idpaiement) {
	this.idpaiement = idpaiement;
}

public int getIdutilisateur() {
	return idutilisateur;
}

public void setIdutilisateur(int idutilisateur) {
	this.idutilisateur = idutilisateur;
}

public int getIdDocument() {
	return idDocument;
}

public void setIdDocument(int idDocument) {
	this.idDocument = idDocument;
}

public int getIdNature() {
	return idNature;
}

public void setIdNature(int idNature) {
	this.idNature = idNature;
}

public int getIdMotif() {
	return idMotif;
}

public void setIdMotif(int idMotif) {
	this.idMotif = idMotif;
}

public String getNatureDocument() {
	return natureDocument;
}

public void setNatureDocument(String natureDocument) {
	this.natureDocument = natureDocument;
}

public String getAdresseLivraison() {
	return adresseLivraison;
}

public void setAdresseLivraison(String adresseLivraison) {
	this.adresseLivraison = adresseLivraison;
}

public String getVilleLivraison() {
	return villeLivraison;
}

public void setVilleLivraison(String villeLivraison) {
	this.villeLivraison = villeLivraison;
}

public String getDestinataire() {
	return destinataire;
}

public void setDestinataire(String destinataire) {
	this.destinataire = destinataire;
}

private  Nature nature;
   public Nature getNature() {
	return nature;
}

public void setNature(Nature nature) {
	this.nature = nature;
}

/** @pdRoleInfo migr=no name=Paiement assc=association20 mult=1..1 */
   public Paiement paiement;
   /** @pdRoleInfo migr=no name=Commune assc=association23 coll=java.util.Collection impl=java.util.HashSet mult=1..* */
   public java.util.Collection<Commune> commune;
   private String nomDocument; // Champ temporaire pour l'affichage

   public String getNomDocument() {
       return nomDocument;
   }

   public void setNomDocument(String nomDocument) {
       this.nomDocument = nomDocument;
   }
   /** @pdOid 8d6da5ea-0f97-459b-bcca-5c71b199fc69 */
   public int getIdDemande() {
      return idDemande;
   }
   
   /** @param newIdDemande
    * @pdOid 6bb5f5f9-01b8-4dd6-9d39-15103ed86966 */
   public void setIdDemande(int newIdDemande) {
      idDemande = newIdDemande;
   }
   public void ajouterInformation(String cle, Object valeur) {
       this.informationsSupplementaires.put(cle, valeur);
   }

   public Object getInformation(String cle) {
       return this.informationsSupplementaires.get(cle);
   }
   
//   /** @pdGenerated default getter */
//   public java.util.Collection<Commune> getCommune() {
//      if (commune == null)
//         commune = new java.util.HashSet<Commune>();
//      return commune;
//   }
//   
//   /** @pdGenerated default iterator getter */
//   public java.util.Iterator getIteratorCommune() {
//      if (commune == null)
//         commune = new java.util.HashSet<Commune>();
//      return commune.iterator();
//   }
//   
//   /** @pdGenerated default setter
//     * @param newCommune */
//   public void setCommune(java.util.Collection<Commune> newCommune) {
//      removeAllCommune();
//      for (java.util.Iterator iter = newCommune.iterator(); iter.hasNext();)
//         addCommune((Commune)iter.next());
//   }
//   
//   /** @pdGenerated default add
//     * @param newCommune */
//   public void addCommune(Commune newCommune) {
//      if (newCommune == null)
//         return;
//      if (this.commune == null)
//         this.commune = new java.util.HashSet<Commune>();
//      if (!this.commune.contains(newCommune))
//         this.commune.add(newCommune);
//   }
//   
//   /** @pdGenerated default remove
//     * @param oldCommune */
//   public void removeCommune(Commune oldCommune) {
//      if (oldCommune == null)
//         return;
//      if (this.commune != null)
//         if (this.commune.contains(oldCommune))
//            this.commune.remove(oldCommune);
//   }
//   
//   /** @pdGenerated default removeAll */
//   public void removeAllCommune() {
//      if (commune != null)
//         commune.clear();
//   }

public java.util.Date getDate() {
	return date;
}

public void setDate(java.util.Date date) {
	this.date = date;
}

public java.lang.String getFormat() {
	return format;
}

public void setFormat(java.lang.String format) {
	this.format = format;
}

public java.lang.String getQrCode() {
	return qrCode;
}

public void setQrCode(java.lang.String qrCode) {
	this.qrCode = qrCode;
}

public java.lang.String getStatus() {
	return status;
}

public void setStatus(java.lang.String status) {
	this.status = status;
}

public Paiement getPaiement() {
	return paiement;
}

public void setPaiement(Paiement paiement) {
	this.paiement = paiement;
}

//Méthode utilitaire pour obtenir le nom de la nature
public String getNatureNom() {
    return this.natureDocument;
}

// Alias pour la date de demande
public java.util.Date getDateDemande() {
    return this.date;
}

}