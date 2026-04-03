#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

require_cmd brew

echo "[1/6] Installing Java 17 and Android CLI tools via Homebrew"
brew install openjdk@17 android-commandlinetools
brew install --cask android-platform-tools

echo "[2/6] Preparing Android SDK root at $ANDROID_HOME"
mkdir -p "$ANDROID_HOME/cmdline-tools"
ln -sfn /opt/homebrew/share/android-commandlinetools/cmdline-tools/latest "$ANDROID_HOME/cmdline-tools/latest"

export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:/opt/homebrew/bin:$PATH"

echo "[3/6] Verifying Java and sdkmanager"
java -version
sdkmanager --version

echo "[4/6] Accepting Android SDK licenses"
yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses >/tmp/habe-bootstrap-licenses.log

echo "[5/6] Installing required Android SDK packages"
sdkmanager --sdk_root="$ANDROID_HOME" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0" \
  >/tmp/habe-bootstrap-install.log

echo "[6/6] Writing shell profile"
cat >"$HOME/.zprofile" <<EOF
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME="/Users/shawnrain/android-sdk"
export ANDROID_SDK_ROOT="\$ANDROID_HOME"
export PATH="\$JAVA_HOME/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/cmdline-tools/latest/bin:/opt/homebrew/bin:\$PATH"
EOF

cat >"$HOME/.zshrc" <<'EOF'
if [ -f "$HOME/.zprofile" ]; then
  source "$HOME/.zprofile"
fi
EOF

echo "BOOTSTRAP_DONE=1"
echo "JAVA_HOME=$JAVA_HOME"
echo "ANDROID_HOME=$ANDROID_HOME"
echo "ADB_VERSION=$(adb version | head -n 1)"
echo "SDKMANAGER_VERSION=$(sdkmanager --version)"
echo "NEXT_STEP=.agents/scripts/preflight.sh"
