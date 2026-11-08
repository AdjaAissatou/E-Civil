/***********************************************************************
 * Module:  TypeUtilisateur.java
 * Author:  Lenovo
 * Purpose: Defines the Class TypeUtilisateur
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 919d9401-bc56-4cab-b0da-6de751875996 */
public class TypeUtilisateur {
   /** @pdOid d99993d4-9020-4d53-8060-29247294342a */
   private int idtype;
   /** @pdOid ac2ebd88-d723-451f-9be6-31770e613fd3 */
   private java.lang.String nom;
   /** @pdOid c2bdb6cc-cf8b-4c31-9a34-3551ee163a06 */
   private java.lang.String paysderesidence;
   
   /** @pdRoleInfo migr=no name=MissionDiplomatique assc=association17 coll=java.util.Collection impl=java.util.HashSet mult=0..* */
   public java.util.Collection<MissionDiplomatique> missionDiplomatique;
   
   /** @pdOid 3c5de20f-1f83-4ed5-8fed-034b1d2e91ba */
   public int getIdtype() {
      return idtype;
   }
   
   public java.lang.String getNom() {
	return nom;
}

public void setNom(java.lang.String nom) {
	this.nom = nom;
}

public java.lang.String getPaysderesidence() {
	return paysderesidence;
}

public void setPaysderesidence(java.lang.String paysderesidence) {
	this.paysderesidence = paysderesidence;
}

/** @param newIdtype
    * @pdOid e6f6601a-87c3-4142-ad37-a412e6c7d819 */
   public void setIdtype(int newIdtype) {
      idtype = newIdtype;
   }
   
   
   /** @pdGenerated default getter */
   public java.util.Collection<MissionDiplomatique> getMissionDiplomatique() {
      if (missionDiplomatique == null)
         missionDiplomatique = new java.util.HashSet<MissionDiplomatique>();
      return missionDiplomatique;
   }
   
   /** @pdGenerated default iterator getter */
   public java.util.Iterator getIteratorMissionDiplomatique() {
      if (missionDiplomatique == null)
         missionDiplomatique = new java.util.HashSet<MissionDiplomatique>();
      return missionDiplomatique.iterator();
   }
   
   /** @pdGenerated default setter
     * @param newMissionDiplomatique */
   public void setMissionDiplomatique(java.util.Collection<MissionDiplomatique> newMissionDiplomatique) {
      removeAllMissionDiplomatique();
      for (java.util.Iterator iter = newMissionDiplomatique.iterator(); iter.hasNext();)
         addMissionDiplomatique((MissionDiplomatique)iter.next());
   }
   
   /** @pdGenerated default add
     * @param newMissionDiplomatique */
   public void addMissionDiplomatique(MissionDiplomatique newMissionDiplomatique) {
      if (newMissionDiplomatique == null)
         return;
      if (this.missionDiplomatique == null)
         this.missionDiplomatique = new java.util.HashSet<MissionDiplomatique>();
      if (!this.missionDiplomatique.contains(newMissionDiplomatique))
         this.missionDiplomatique.add(newMissionDiplomatique);
   }
   
   /** @pdGenerated default remove
     * @param oldMissionDiplomatique */
   public void removeMissionDiplomatique(MissionDiplomatique oldMissionDiplomatique) {
      if (oldMissionDiplomatique == null)
         return;
      if (this.missionDiplomatique != null)
         if (this.missionDiplomatique.contains(oldMissionDiplomatique))
            this.missionDiplomatique.remove(oldMissionDiplomatique);
   }
   
   /** @pdGenerated default removeAll */
   public void removeAllMissionDiplomatique() {
      if (missionDiplomatique != null)
         missionDiplomatique.clear();
   }

}