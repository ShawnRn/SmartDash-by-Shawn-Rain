#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env
require_cmd security
require_cmd openssl
require_cmd keytool

mkdir -p "$DEFAULT_RELEASE_KEYSTORE_DIR"

store_file="$(keychain_get "habe.android.release.store.path" || true)"
store_password="$(keychain_get "habe.android.release.store.password" || true)"
key_alias="$(keychain_get "habe.android.release.key.alias" || true)"
key_password="$(keychain_get "habe.android.release.key.password" || true)"

if [[ -z "$store_file" ]]; then
  store_file="$DEFAULT_RELEASE_KEYSTORE_FILE"
fi

if [[ -f "$store_file" && -n "$store_password" && -n "$key_alias" && -n "$key_password" ]]; then
  echo "RELEASE_SIGNING_READY=1"
  echo "KEYSTORE_FILE=$store_file"
  echo "KEY_ALIAS=$key_alias"
  exit 0
fi

if [[ -f "$store_file" && ( -z "$store_password" || -z "$key_alias" || -z "$key_password" ) ]]; then
  echo "Keystore exists but Keychain items are missing." >&2
  echo "Please sync iCloud Keychain first, or re-add the passwords manually from another Mac." >&2
  exit 1
fi

store_password="$(openssl rand -base64 24 | tr -d '\n' | tr '/+' 'AB' | cut -c1-24)"
key_alias="habe_release"
key_password="$store_password"

keytool -genkeypair -v \
  -keystore "$store_file" \
  -storetype PKCS12 \
  -storepass "$store_password" \
  -alias "$key_alias" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500 \
  -keypass "$key_password" \
  -dname "CN=Shawn Rain, OU=Habe Dashboard, O=Shawn Rain, L=Shanghai, ST=Shanghai, C=CN"

security add-generic-password -U -a "$USER" -s "habe.android.release.store.path" -w "$store_file"
security add-generic-password -U -a "$USER" -s "habe.android.release.store.password" -w "$store_password"
security add-generic-password -U -a "$USER" -s "habe.android.release.key.alias" -w "$key_alias"
security add-generic-password -U -a "$USER" -s "habe.android.release.key.password" -w "$key_password"

echo "RELEASE_SIGNING_CREATED=1"
echo "KEYSTORE_FILE=$store_file"
echo "KEY_ALIAS=$key_alias"
