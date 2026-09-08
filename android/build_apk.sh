#!/usr/bin/env bash
set -e

echo "=== MD Viewer Android APK Builder ==="

# Find Android SDK
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "/home/dkchw/Android/Sdk" ]; then
        export ANDROID_HOME="/home/dkchw/Android/Sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    fi
fi

if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME not found!"
    exit 1
fi

SDK="$ANDROID_HOME"
BUILD_TOOLS_DIR="$SDK/build-tools"
# Pick highest build-tools
BT_VERSION=$(ls -1 "$BUILD_TOOLS_DIR" | sort -V | tail -n 1)
BT="$BUILD_TOOLS_DIR/$BT_VERSION"
echo "Using Build-Tools: $BT_VERSION from $BT"

PLATFORM_JAR="$SDK/platforms/android-34/android.jar"
if [ ! -f "$PLATFORM_JAR" ]; then
    PLATFORM_DIR=$(ls -d "$SDK/platforms"/android-* | sort -V | tail -n 1)
    PLATFORM_JAR="$PLATFORM_DIR/android.jar"
fi
echo "Using Platform Jar: $PLATFORM_JAR"

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$BASE_DIR/app"
SRC_DIR="$APP_DIR/src/main"
BUILD_DIR="$BASE_DIR/build"
OUT_DIR="$BASE_DIR/dist"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$OUT_DIR"

echo "1. Compiling resources with aapt2..."
"$BT/aapt2" compile --dir "$SRC_DIR/res" -o "$BUILD_DIR/res.zip"

echo "2. Linking resources and manifest..."
"$BT/aapt2" link "$BUILD_DIR/res.zip" \
    -I "$PLATFORM_JAR" \
    --manifest "$SRC_DIR/AndroidManifest.xml" \
    --java "$BUILD_DIR/gen" \
    -A "$SRC_DIR/assets" \
    -o "$BUILD_DIR/unaligned.apk"

echo "3. Compiling Kotlin sources with kotlinc..."
kotlinc -cp "$PLATFORM_JAR:$APP_DIR/libs/kotlin-stdlib.jar" \
    -jvm-target 1.8 \
    -d "$BUILD_DIR/classes" \
    "$BUILD_DIR/gen/com/mdviewer/app/R.java" \
    "$SRC_DIR/java/com/mdviewer/app/"*.kt

echo "4. Dexing classes with d8..."
"$BT/d8" --lib "$PLATFORM_JAR" \
    --output "$BUILD_DIR/" \
    "$APP_DIR/libs/kotlin-stdlib.jar" \
    $(find "$BUILD_DIR/classes" -name "*.class")

echo "5. Adding classes.dex to APK..."
cd "$BUILD_DIR"
zip -u unaligned.apk classes.dex
cd "$BASE_DIR"

echo "6. Aligning APK with zipalign..."
"$BT/zipalign" -p -f -v 4 "$BUILD_DIR/unaligned.apk" "$BUILD_DIR/aligned.apk"

# Generate debug keystore if not exists
KEYSTORE="$BASE_DIR/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    echo "Creating debug keystore..."
    keytool -genkeypair -validity 10000 -dname "CN=MDViewer,O=MDViewer,C=US" \
        -keystore "$KEYSTORE" -storepass android -keypass android \
        -alias androiddebugkey -keyalg RSA -keysize 2048
fi

FINAL_APK="$OUT_DIR/mdviewer.apk"
echo "7. Signing APK with apksigner (v1, v2, v3)..."
"$BT/apksigner" sign \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --ks-key-alias androiddebugkey \
    --key-pass pass:android \
    --out "$FINAL_APK" \
    "$BUILD_DIR/aligned.apk"

echo "8. Verifying APK signature..."
"$BT/apksigner" verify "$FINAL_APK"

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK location: $FINAL_APK"
ls -lh "$FINAL_APK"
