/***********************************************************************
 * Module:  Typemission.java
 * Author:  Lenovo
 * Purpose: Defines the Class Typemission
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid f27d903d-9bd5-4511-b133-ebe7a62e23fe */
public class Typemission {
   /** @pdOid 61c789a2-7dbb-4191-a773-9ecfa3d12e87 */
   private int idtype;
   /** @pdOid 54919968-f5db-4fc5-9ccd-17e7c28e5154 */
   private java.lang.String libelle;
   
   /** @pdRoleInfo migr=no name=MissionDiplomatique assc=association14 mult=1..* */
   public MissionDiplomatique[] missionDiplomatique;
   
   /** @pdOid 37d3b981-e0ae-490a-8f85-efdfd0812e97 */
   public int getIdtype() {
      return idtype;
   }
   
   /** @param newIdtype
    * @pdOid abdaf7d4-9073-4313-8516-c20d6ab4459a */
   public void setIdtype(int newIdtype) {
      idtype = newIdtype;
   }

}