package sn.etatcivil.demande.application.service;

import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.entree.GererPreferences;
import sn.etatcivil.demande.application.port.sortie.PreferencesDemandeurs;

@CasDUsage
public class ServicePreferences implements GererPreferences {

    private final PreferencesDemandeurs preferences;

    public ServicePreferences(PreferencesDemandeurs preferences) {
        this.preferences = preferences;
    }

    @Override
    public void definirNotifications(String demandeurId, boolean notifications) {
        preferences.definir(demandeurId, notifications);
    }
}
