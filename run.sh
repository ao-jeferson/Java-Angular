#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# Homebrew installs this JDK separately; other environments can use JAVA_HOME.
if [[ -z "${JAVA_HOME:-}" && -d /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ]]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
fi
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi

if [[ "${1:-backend}" == frontend ]]; then
  cd frontend
  exec npm start
fi

if [[ ! -f .env ]]; then
  (umask 077; printf 'MOVIMENTO_USERNAME=admin\nMOVIMENTO_PASSWORD=%s\nCONFIG_SERVER_PASSWORD=%s\n' "Movimento@2026" "$(openssl rand -hex 24)" > .env)
  echo 'Credenciais locais criadas no arquivo .env.'
fi
set -a
source .env
set +a

case "${1:-backend}" in
  backend)
    shift || true
    exec ./mvnw -B -ntp -pl backend spring-boot:run "$@"
    ;;
  config)
    exec ./mvnw -B -ntp -pl config-server spring-boot:run
    ;;
  *) echo 'Uso: bash run.sh [backend|frontend|config]' >&2; exit 1 ;;
esac
