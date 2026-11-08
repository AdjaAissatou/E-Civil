/***********************************************************************
 * Module:  Varietes.java
 * Author:  Lenovo
 * Purpose: Defines the Class Varietes
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 75cfb5e5-3b6b-4005-b918-400046b7c269 */
public class Varietes {
   /** @pdOid 988d5805-1452-4256-9102-0714dfaeebc8 */
   private int idvariete;
   /** @pdOid d8a6a1fa-92df-4198-a6cb-8f47ad726885 */
   private java.lang.String nom;
   /** @pdOid 3505c9a7-7261-463d-a46e-8de2259a7ed4 */
   private java.lang.String description;
   /** @pdOid 0eb8a674-df0a-4f25-9e40-6e941c77b842 */
   private java.lang.Boolean conjoint;
   /** @pdOid 71820646-6844-4e98-a768-e91e44d42960 */
   private java.lang.Boolean temoins;
   /** @pdOid 34bf281a-4c4f-414f-8cd9-d3bbe7e9b85a */
   private java.lang.Boolean parents;
   /** @pdOid 3b701a00-a28a-45b1-948c-d1fa7ee18b80 */
   private java.lang.Boolean medecin;
   /** @pdOid 2250cf17-daeb-49ec-82bd-ad2f15e22664 */
   private java.lang.Boolean mentionMarginale;
   /** @pdOid ca5b6250-5586-4e59-9eb5-50b08ae6859c */
   private java.lang.Boolean causedeces;
   
   /** @pdRoleInfo migr=no name=Nature assc=association26 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<Nature> nature;
   
   /** @pdOid 892faf9d-e982-4418-ae82-8830f2adad46 */
   public int getIdvariete() {
      return idvariete;
   }
   
   /** @param newIdvariete
    * @pdOid 7fe9aa19-b809-4508-835f-988631604f00 */
   public void setIdvariete(int newIdvariete) {
      idvariete = newIdvariete;
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<Nature> getNature() {
      if (nature == null)
         nature = new java.util.HashSet<Nature>();
      return nature;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorNature() {
      if (nature == null)
         nature = new java.util.HashSet<Nature>();
      return nature.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newNature */
   public void setNature(java.util.Collection<Nature> newNature) {
      removeAllNature();
      for (java.util.Iterator iter = newNature.iterator(); iter.hasNext();)
         addNature((Nature)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newNature */
   public void addNature(Nature newNature) {
      if (newNature == null)
         return;
      if (this.nature == null)
         this.nature = new java.util.HashSet<Nature>();
      if (!this.nature.contains(newNature))
         this.nature.add(newNature);
   }
   
   /** @pdGenerated default remove
     * @param oldNature */
   public void removeNature(Nature oldNature) {
      if (oldNature == null)
         return;
      if (this.nature != null)
         if (this.nature.contains(oldNature))
            this.nature.remove(oldNature);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllNature() {
      if (nature != null)
         nature.clear();
   }

}