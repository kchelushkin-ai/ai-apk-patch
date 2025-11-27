#!/usr/bin/env bash
set -euo pipefail
INPUT_APK=“${1:?input apk}”
OUTPUT_APK=“${2:?output apk}”
BASE_URL=“${3:-}”
LABEL=“${4:-Автоуслуги AI}”

tools
SDK=/usr/local/lib/android
BT=$SDK/build-tools/34.0.0
PLAT=$SDK/platforms/android-34/android.jar
APKTOOL=“$(pwd)/apktool”
APKTOOLJ=“$(pwd)/apktool.jar”

WORKDIR=“$(pwd)/workdir”
rm -rf “$WORKDIR”
mkdir -p “$WORKDIR”

1) Decode APK
“$APKTOOL” d -f “$INPUT_APK” -o “$WORKDIR”

2) Detect package
PKG=$(grep -oP ‘package=“[^”]+"’ “$WORKDIR/AndroidManifest.xml” | head -1 | cut -d’"’ -f2)
echo “Package: $PKG”

3) Drop AI resources/config
mkdir -p “$WORKDIR”/{assets,res/layout,res/values,res/mipmap-anydpi-v26}
cp -r patch/files/res/* “$WORKDIR/res/” || true
cat > “$WORKDIR/assets/ai_config.json” <<EOF
{
“baseUrl”: “$(printf “%s” “$BASE_URL”)”,
“language”: “ru”,
“authHeader”: “”
}
EOF

4) Java sources → DEX
TMP=“$(pwd)/tmpsrc”
rm -rf “$TMP” && mkdir -p “$TMP/src/com/${PKG//.//}/ai” “$TMP/out” “$TMP/dexout”
sed “s|com\.\{pkg\}|${PKG}|g” patch/files/src/AiConfig.java > “$TMP/src/com/${PKG//.//}/ai/AiConfig.java”
sed “s|com\.\{pkg\}|${PKG}|g” patch/files/src/SseClient.java > “$TMP/src/com/${PKG//.//}/ai/SseClient.java”
sed “s|com\.\{pkg\}|${PKG}|g” patch/files/src/AiActivity.java > “$TMP/src/com/${PKG//.//}/ai/AiActivity.java”

javac -source 1.8 -target 1.8 -bootclasspath “$PLAT” -cp “$PLAT” 

-d “$TMP/out” $(find “$TMP/src” -name “*.java”)

“$BT/d8” “$TMP/out” --lib “$PLAT” --output “$TMP/dexout”

5) Merge manifest (permission + launcher activity)
python3 patch/merge_manifest.py “$WORKDIR/AndroidManifest.xml” “$PKG” “$LABEL”

6) Rebuild, zipalign, sign (debug key)
“$APKTOOL” b “$WORKDIR” -o unsigned.apk
“$BT/zipalign” -p 4 unsigned.apk aligned.apk
keytool -genkeypair -noprompt -alias debug -keyalg RSA -keysize 2048 -validity 3650 

-keystore debug.jks -storepass android -keypass android -dname “CN=AI, OU=AI, O=AI, L=NY, S=NY, C=US” >/dev/null 2>&1
“$BT/apksigner” sign --ks debug.jks --ks-pass pass:android --key-pass pass:android --out “$OUTPUT_APK” aligned.apk
“$BT/apksigner” verify --print-certs “$OUTPUT_APK”

echo “Patched APK -> $OUTPUT_APK”
