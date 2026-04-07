#!/bin/bash

# Déploiement rapide NewPipe (version courte)
# Usage: ./quick-deploy.sh

export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$PATH"

echo "🚀 Déploiement rapide NewPipe..."

# Compilation et installation en une ligne
./gradlew assembleDebug -x runCheckstyle && \
adb install -r ./app/build/outputs/apk/debug/app-debug.apk && \
adb shell am start -n org.schabi.newpipe.debug.featureaudiovideosyncandfullscreencontrols/org.schabi.newpipe.MainActivity && \
echo "✅ Déployé et lancé avec succès !"