#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${1:-$HOME/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding}"
STAMP="$(date '+%Y%m%d-%H%M%S')"
OUT_DIR="$BASE_DIR/habe-android-signing-$STAMP"
STORE_FILE="$OUT_DIR/habe-release.jks"
STORE_PASSWORD="$(openssl rand -base64 24 | tr -d '\n' | tr '/+' 'AB' | cut -c1-24)"
KEY_ALIAS="habe_release_$STAMP"
KEY_PASSWORD="$STORE_PASSWORD"

mkdir -p "$OUT_DIR"
chmod 700 "$OUT_DIR"

keytool -genkeypair -v \
  -keystore "$STORE_FILE" \
  -storetype PKCS12 \
  -storepass "$STORE_PASSWORD" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500 \
  -keypass "$KEY_PASSWORD" \
  -dname "CN=Shawn Rain, OU=Habe Dashboard, O=Shawn Rain, L=Shanghai, ST=Shanghai, C=CN"

cat > "$OUT_DIR/SIGNING_INFO.txt" <<INFO
Habe Android Release Signing
Created at: $(date '+%Y-%m-%d %H:%M:%S')

Keystore file:
$STORE_FILE

Store password:
$STORE_PASSWORD

Key alias:
$KEY_ALIAS

Key password:
$KEY_PASSWORD

Important:
- This is a NEW signing identity.
- Existing installs signed with the old key cannot be upgraded in-place.
- To use this key with the repo scripts, write these values back into macOS Keychain.
- Do not commit this directory to Git.
INFO

cat > "$OUT_DIR/signing.env" <<ENV
HABE_RELEASE_STORE_FILE='$STORE_FILE'
HABE_RELEASE_STORE_PASSWORD='$STORE_PASSWORD'
HABE_RELEASE_KEY_ALIAS='$KEY_ALIAS'
HABE_RELEASE_KEY_PASSWORD='$KEY_PASSWORD'
ENV

cat > "$OUT_DIR/restore-habe-signing-keychain.sh" <<'SCRIPT'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/signing.env"

security add-generic-password -U -a "$USER" -s "habe.android.release.store.path" -w "$HABE_RELEASE_STORE_FILE" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.store.password" -w "$HABE_RELEASE_STORE_PASSWORD" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.key.alias" -w "$HABE_RELEASE_KEY_ALIAS" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.key.password" -w "$HABE_RELEASE_KEY_PASSWORD" >/dev/null

echo "Keychain entries restored."
SCRIPT

chmod 600 "$OUT_DIR/SIGNING_INFO.txt" "$OUT_DIR/signing.env" "$STORE_FILE"
chmod 700 "$OUT_DIR/restore-habe-signing-keychain.sh"

security add-generic-password -U -a "$USER" -s "habe.android.release.store.path" -w "$STORE_FILE" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.store.password" -w "$STORE_PASSWORD" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.key.alias" -w "$KEY_ALIAS" >/dev/null
security add-generic-password -U -a "$USER" -s "habe.android.release.key.password" -w "$KEY_PASSWORD" >/dev/null

cat <<EOF
OUT_DIR=$OUT_DIR
STORE_FILE=$STORE_FILE
KEY_ALIAS=$KEY_ALIAS
INFO_FILE=$OUT_DIR/SIGNING_INFO.txt
RESTORE_SCRIPT=$OUT_DIR/restore-habe-signing-keychain.sh
EOF
