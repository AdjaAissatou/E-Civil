/***********************************************************************
 * Module:  Personne.java
 * Author:  Lenovo
 * Purpose: Defines the Class Personne
 ***********************************************************************/
package EtatCivil.modeles;

import java.util.*;

public abstract class Personne {
    private int id;
    private String prenom;
    private String nom;
    private String sexe;
    private Date datedenaissance;
    private String lieudenaissance;
    private String profession;
    private String residance;

    public Personne() {
        // Constructeur vide
    }

    // --- GETTERS & SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int newId) {
        this.id = newId;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public Date getDatedenaissance() {
        return datedenaissance;
    }

    public void setDatedenaissance(Date datedenaissance) {
        this.datedenaissance = datedenaissance;
    }

    public String getLieudenaissance() {
        return lieudenaissance;
    }

    public void setLieudenaissance(String lieudenaissance) {
        this.lieudenaissance = lieudenaissance;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getResidance() {
        return residance;
    }

    public void setResidance(String residance) {
        this.residance = residance;
    }

    // Gestion des comptes liés à la personne
    private Collection<Compte> compte;

    public Collection<Compte> getCompte() {
        if (compte == null)
            compte = new HashSet<>();
        return compte;
    }

    public Iterator<Compte> getIteratorCompte() {
        if (compte == null)
            compte = new HashSet<>();
        return compte.iterator();
    }

    public void setCompte(Collection<Compte> newCompte) {
        removeAllCompte();
        for (Iterator<Compte> iter = newCompte.iterator(); iter.hasNext();)
            addCompte(iter.next());
    }

    public void addCompte(Compte newCompte) {
        if (newCompte == null)
            return;
        if (this.compte == null)
            this.compte = new HashSet<>();
        if (!this.compte.contains(newCompte))
            this.compte.add(newCompte);
    }

    public void removeCompte(Compte oldCompte) {
        if (oldCompte == null)
            return;
        if (this.compte != null && this.compte.contains(oldCompte))
            this.compte.remove(oldCompte);
    }

    public void removeAllCompte() {
        if (compte != null)
            compte.clear();
    }

   
}