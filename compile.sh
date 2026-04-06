#!/bin/bash

# Script de compilation NewPipe
# Configure automatiquement l'environnement et compile le projet

echo "🚀 Configuration de l'environnement NewPipe..."

# Configuration Java 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

# Configuration Android SDK
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

echo "✅ Java version: $(java --version | head -1)"
echo "✅ Android SDK: $ANDROID_HOME"

cd /Users/pec/projets/newpipe

echo ""
echo "🔨 Compilation en cours..."

# Compilation sans checkStyle (problème de toolchain Java 21)
./gradlew assembleDebug -x runCheckstyle

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Compilation réussie !"
    echo "📱 APK généré: app/build/outputs/apk/debug/app-debug.apk"
    ls -lah app/build/outputs/apk/debug/app-debug.apk
else
    echo ""
    echo "❌ Échec de la compilation"
    exit 1
fi