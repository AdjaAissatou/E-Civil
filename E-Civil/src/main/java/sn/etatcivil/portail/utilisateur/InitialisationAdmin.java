package sn.etatcivil.portail.utilisateur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sn.etatcivil.portail.config.ProprietesPortail;

/** Crée le premier administrateur à partir de ECIVIL_ADMIN_EMAIL / ECIVIL_ADMIN_MOT_DE_PASSE. */
@Component
class InitialisationAdmin implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialisationAdmin.class);

    private final UtilisateurRepository repository;
    private final CompteService comptes;
    private final ProprietesPortail proprietes;

    InitialisationAdmin(UtilisateurRepository repository, CompteService comptes, ProprietesPortail proprietes) {
        this.repository = repository;
        this.comptes = comptes;
        this.proprietes = proprietes;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.existsByRole(Role.ADMIN)) return;
        var admin = proprietes.admin();
        if (admin == null || admin.email() == null || admin.email().isBlank()
                || admin.motDePasse() == null || admin.motDePasse().length() < 12) {
            log.warn("Aucun administrateur : définissez ECIVIL_ADMIN_EMAIL et ECIVIL_ADMIN_MOT_DE_PASSE "
                    + "(12 caractères minimum) puis redémarrez pour le créer.");
            return;
        }
        Inscription f = new Inscription();
        f.setEmail(admin.email());
        f.setMotDePasse(admin.motDePasse());
        f.setPrenom("Administrateur");
        f.setNom("E-Civil");
        f.setRole(Role.ADMIN);
        comptes.creerCompte(f);
        log.info("Compte administrateur créé pour {}", admin.email());
    }
}
