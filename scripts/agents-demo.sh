#!/usr/bin/env bash
# Crée les trois comptes agents de démonstration et les range dans leur profil :
#   delegue@ecivil.sn  → profil « Délégué de commune »
#   registre@ecivil.sn → profil « Agent du registre »
#   officier@ecivil.sn → profil « Officier de l'état civil »
# Les profils sont créés par le service des habilitations à son démarrage. On peut aussi créer les agents
# depuis le portail : Administration › Utilisateurs › Nouvel agent.
#
# Usage (depuis la racine du projet, plateforme démarrée) :
#   ./scripts/agents-demo.sh MotDePasseDesAgents
#
# Fonctionne sous Linux, macOS et Windows (Git Bash). Nécessite seulement curl, rien d'autre à installer.
# Les identifiants d'administration Keycloak sont lus dans .env, jamais écrits ici.
set -euo pipefail

MOT_DE_PASSE="${1:-}"
if [ -z "$MOT_DE_PASSE" ]; then
  echo "Usage : $0 <mot de passe des comptes agents>" >&2
  exit 1
fi
cd "$(dirname "$0")/.."
[ -f .env ] || { echo "Fichier .env introuvable : copiez .env.example en .env et remplissez-le." >&2; exit 1; }
KEYCLOAK_ADMIN=$(grep -E '^KEYCLOAK_ADMIN=' .env | cut -d= -f2- | tr -d '\r')
KEYCLOAK_ADMIN_PASSWORD=$(grep -E '^KEYCLOAK_ADMIN_PASSWORD=' .env | cut -d= -f2- | tr -d '\r')
K=http://localhost:8180

JETON=$(curl -s --data-urlencode "username=$KEYCLOAK_ADMIN" --data-urlencode "password=$KEYCLOAK_ADMIN_PASSWORD" \
  -d grant_type=password -d client_id=admin-cli "$K/realms/master/protocol/openid-connect/token" \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
[ -n "$JETON" ] || { echo "Connexion à Keycloak impossible : la plateforme est-elle démarrée ? (docker compose ps)" >&2; exit 1; }
A="$K/admin/realms/ecivil"
auth=(-H "Authorization: Bearer $JETON")

# identifiant du profil (groupe Keycloak) portant exactement ce nom
profil() {
  curl -s -G "${auth[@]}" --data-urlencode "search=$1" --data-urlencode "exact=true" "$A/groups" \
    | sed -n 's/^\[{"id":"\([^"]*\)".*/\1/p'
}

CITOYEN=$(profil "Citoyen")
if [ -z "$CITOYEN" ]; then
  echo "Les profils n'existent pas encore : attendez que le service des habilitations ait démarré" >&2
  echo "(docker compose logs habilitation-service), puis relancez ce script." >&2
  exit 1
fi

creer() {  # identifiant prénom profil
  local email="$1@ecivil.sn" prenom="$2" nom_profil="$3"
  curl -s -o /dev/null "${auth[@]}" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$email\",\"email\":\"$email\",\"emailVerified\":true,\"enabled\":true,\"firstName\":\"$prenom\",\"lastName\":\"Ndiaye\",\"credentials\":[{\"type\":\"password\",\"value\":\"$MOT_DE_PASSE\",\"temporary\":false}]}" \
    "$A/users"
  local id groupe
  id=$(curl -s "${auth[@]}" "$A/users?exact=true&username=$email" | sed -n 's/^\[{"id":"\([^"]*\)".*/\1/p')
  groupe=$(profil "$nom_profil")
  [ -n "$id" ] && [ -n "$groupe" ] || { echo "  $email : échec (profil « $nom_profil » introuvable ?)" >&2; return; }
  curl -s -o /dev/null -X PUT "${auth[@]}" "$A/users/$id/groups/$groupe"
  # un agent n'est pas rangé parmi les citoyens (profil attribué par défaut à toute création de compte)
  curl -s -o /dev/null -X DELETE "${auth[@]}" "$A/users/$id/groups/$CITOYEN"
  echo "  $email  ($nom_profil)"
}

echo "Comptes agents créés :"
creer delegue Awa "Délégué de commune"
creer registre Ibrahima "Agent du registre"
creer officier Mariama "Officier de l'état civil"
echo "Connectez-vous sur http://localhost:8080 avec l'un de ces comptes et le mot de passe choisi."
