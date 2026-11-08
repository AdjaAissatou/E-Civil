/***********************************************************************
 * Module:  MissionDiplomatique.java
 * Author:  Lenovo
 * Purpose: Defines the Class MissionDiplomatique
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid c8342927-74e6-40d3-83ca-c397910fadb6 */
public class MissionDiplomatique {
   /** @pdOid bd693a81-faed-4a11-b890-9bb4e17bee47 */
   private int idmission;
   /** @pdOid 7327e03d-8cd3-42f6-8123-dbc9c67ae428 */
   private java.lang.String nom;
   /** @pdOid c5cc724d-16a7-43fa-b1d0-980dda053785 */
   private java.lang.String zonecompetence;
   /** @pdOid c832aef7-847c-4641-ac6c-2f3f8ff28608 */
   private java.lang.String adresse;
   /** @pdOid 36108905-bb61-4216-be28-5cb0a9bd17c1 */
   private java.lang.String telephone;
   /** @pdOid ba6bdee4-7f36-459b-a273-fe199a9b68e6 */
   private java.lang.String email;
   
   /** @pdRoleInfo migr=no name=Ville assc=association13 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<Ville> ville;
   /** @pdRoleInfo migr=no name=Zonedecompetence assc=association27 coll=java.util.Collection impl=java.util.HashSet mult=1..* */
   public java.util.Collection<Zonedecompetence> zonedecompetence;
   
   /** @pdOid a261a6b7-9422-44a2-87a3-9d1b8a67b7ae */
   public int getIdmission() {
      return idmission;
   }
   
   /** @param newIdmission
    * @pdOid 09163190-5b8e-469e-bc95-2c2d1e5e0b47 */
   public void setIdmission(int newIdmission) {
      idmission = newIdmission;
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<Ville> getVille() {
      if (ville == null)
         ville = new java.util.HashSet<Ville>();
      return ville;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorVille() {
      if (ville == null)
         ville = new java.util.HashSet<Ville>();
      return ville.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newVille */
   public void setVille(java.util.Collection<Ville> newVille) {
      removeAllVille();
      for (java.util.Iterator iter = newVille.iterator(); iter.hasNext();)
         addVille((Ville)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newVille */
   public void addVille(Ville newVille) {
      if (newVille == null)
         return;
      if (this.ville == null)
         this.ville = new java.util.HashSet<Ville>();
      if (!this.ville.contains(newVille))
         this.ville.add(newVille);
   }
   
   /** @pdGenerated default remove
     * @param oldVille */
   public void removeVille(Ville oldVille) {
      if (oldVille == null)
         return;
      if (this.ville != null)
         if (this.ville.contains(oldVille))
            this.ville.remove(oldVille);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllVille() {
      if (ville != null)
         ville.clear();
   }
   /** @pdGenerated default getter */
   public java.util.Collection<Zonedecompetence> getZonedecompetence() {
      if (zonedecompetence == null)
         zonedecompetence = new java.util.HashSet<Zonedecompetence>();
      return zonedecompetence;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorZonedecompetence() {
      if (zonedecompetence == null)
         zonedecompetence = new java.util.HashSet<Zonedecompetence>();
      return zonedecompetence.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newZonedecompetence */
   public void setZonedecompetence(java.util.Collection<Zonedecompetence> newZonedecompetence) {
      removeAllZonedecompetence();
      for (java.util.Iterator iter = newZonedecompetence.iterator(); iter.hasNext();)
         addZonedecompetence((Zonedecompetence)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newZonedecompetence */
   public void addZonedecompetence(Zonedecompetence newZonedecompetence) {
      if (newZonedecompetence == null)
         return;
      if (this.zonedecompetence == null)
         this.zonedecompetence = new java.util.HashSet<Zonedecompetence>();
      if (!this.zonedecompetence.contains(newZonedecompetence))
         this.zonedecompetence.add(newZonedecompetence);
   }
   
   /** @pdGenerated default remove
     * @param oldZonedecompetence */
   public void removeZonedecompetence(Zonedecompetence oldZonedecompetence) {
      if (oldZonedecompetence == null)
         return;
      if (this.zonedecompetence != null)
         if (this.zonedecompetence.contains(oldZonedecompetence))
            this.zonedecompetence.remove(oldZonedecompetence);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllZonedecompetence() {
      if (zonedecompetence != null)
         zonedecompetence.clear();
   }

}