#!/usr/bin/env bash
# Crée les trois comptes agents de démonstration dans Keycloak (realm ecivil) :
#   delegue@ecivil.sn (DELEGUE), registre@ecivil.sn (AGENT_REGISTRE), officier@ecivil.sn (OFFICIER)
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

creer() {  # identifiant prénom rôle
  local email="$1@ecivil.sn" prenom="$2" role="$3"
  curl -s -o /dev/null -H "Authorization: Bearer $JETON" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$email\",\"email\":\"$email\",\"emailVerified\":true,\"enabled\":true,\"firstName\":\"$prenom\",\"lastName\":\"Ndiaye\",\"credentials\":[{\"type\":\"password\",\"value\":\"$MOT_DE_PASSE\",\"temporary\":false}]}" \
    "$A/users"
  local id
  id=$(curl -s -H "Authorization: Bearer $JETON" "$A/users?exact=true&username=$email" | sed -n 's/^\[{"id":"\([^"]*\)".*/\1/p')
  local definition
  definition=$(curl -s -H "Authorization: Bearer $JETON" "$A/roles/$role")
  curl -s -o /dev/null -H "Authorization: Bearer $JETON" -H 'Content-Type: application/json' \
    -d "[$definition]" "$A/users/$id/role-mappings/realm"
  echo "  $email  ($role)"
}

echo "Comptes agents créés :"
creer delegue Awa DELEGUE
creer registre Ibrahima AGENT_REGISTRE
creer officier Mariama OFFICIER
echo "Connectez-vous sur http://localhost:8080 avec l'un de ces comptes et le mot de passe choisi."
