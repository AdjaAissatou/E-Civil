/***********************************************************************
 * Module:  Zonedecompetence.java
 * Author:  Lenovo
 * Purpose: Defines the Class Zonedecompetence
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 7275aa44-3b63-462a-826c-c40230e3997d */
public class Zonedecompetence {
   /** @pdOid d7b5deb9-d677-4ebd-8a4f-539e13bcb66f */
   private int idZone;
   
   /** @pdRoleInfo migr=no name=Pays assc=association28 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<Pays> pays;
   
   /** @pdOid 44a5fef9-e39f-48ee-b59e-2696849f6832 */
   public int getIdZone() {
      return idZone;
   }
   
   /** @param newIdZone
    * @pdOid 45db8b83-ebff-41b6-9ee9-88be8b8f2cef */
   public void setIdZone(int newIdZone) {
      idZone = newIdZone;
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<Pays> getPays() {
      if (pays == null)
         pays = new java.util.HashSet<Pays>();
      return pays;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorPays() {
      if (pays == null)
         pays = new java.util.HashSet<Pays>();
      return pays.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newPays */
   public void setPays(java.util.Collection<Pays> newPays) {
      removeAllPays();
      for (Iterator<Pays> iter = newPays.iterator(); iter.hasNext();)
         addPays((Pays)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newPays */
   public void addPays(Pays newPays) {
      if (newPays == null)
         return;
      if (this.pays == null)
         this.pays = new java.util.HashSet<Pays>();
      if (!this.pays.contains(newPays))
         this.pays.add(newPays);
   }
   
   /** @pdGenerated default remove
     * @param oldPays */
   public void removePays(Pays oldPays) {
      if (oldPays == null)
         return;
      if (this.pays != null)
         if (this.pays.contains(oldPays))
            this.pays.remove(oldPays);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllPays() {
      if (pays != null)
         pays.clear();
   }

}