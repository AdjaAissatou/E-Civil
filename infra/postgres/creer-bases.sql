-- Une base par microservice (aucun service ne lit la base d'un autre) + celle de Keycloak.
-- Les tables sont créées par Flyway au démarrage de chaque service.
CREATE DATABASE demande;
CREATE DATABASE paiement;
CREATE DATABASE registre;
CREATE DATABASE document;
CREATE DATABASE notification;
CREATE DATABASE keycloak;
