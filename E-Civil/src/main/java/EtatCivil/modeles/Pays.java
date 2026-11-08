/***********************************************************************
 * Module:  Pays.java
 * Author:  Lenovo
 * Purpose: Defines the Class Pays
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

/** @pdOid 15777455-c2e9-4365-b8c0-e4fce3d8902b */
public class Pays {
   /** @pdOid 8c458a54-ca6d-4121-8ee8-24d37e95348e */
   private int idPays;
   /** @pdOid e1a4fb0f-6117-4af6-a19f-1aac9ae8aa3d */
   private java.lang.String nom;
   
   /** @pdOid c2bb42da-1c99-4654-9090-c2c0a2185a07 */
   public int getIdPays() {
      return idPays;
   }
   
   /** @param newIdPays
    * @pdOid 70f5a20e-9736-4df8-90b4-68864b049469 */
   public void setIdPays(int newIdPays) {
      idPays = newIdPays;
   }

}