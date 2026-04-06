#!/bin/bash

# Script de test et lancement NewPipe
# Vérifie l'état de l'émulateur et lance l'application

echo "🔍 Vérification de l'environnement NewPipe..."

# Configuration environnement
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$PATH"

echo "✅ Configuration Java et Android SDK"

# Vérification émulateur
echo ""
echo "📱 Vérification de l'émulateur..."
adb devices

# Vérification NewPipe installé
echo ""
echo "🔍 Vérification installation NewPipe..."
NEWPIPE_PKG=$(adb shell pm list packages | grep newpipe)

if [ ! -z "$NEWPIPE_PKG" ]; then
    echo "✅ NewPipe installé: $NEWPIPE_PKG"
    
    # Informations sur le package
    echo ""
    echo "📋 Informations du package:"
    adb shell dumpsys package org.schabi.newpipe.debug | grep -A 3 "applicationInfo"
    
    echo ""
    echo "🎯 Pour lancer NewPipe:"
    echo "1. Ouvrez l'émulateur Android qui devrait être visible"
    echo "2. Cherchez l'icône 'NewPipe' dans le launcher Android"
    echo "3. Cliquez sur l'icône pour lancer l'application"
    
    echo ""
    echo "💡 Commandes utiles:"
    echo "• Voir l'émulateur: L'émulateur devrait être ouvert dans une fenêtre séparée"
    echo "• Capturer écran: adb exec-out screencap -p > screenshot.png"
    echo "• Logs en temps réel: adb logcat | grep NewPipe"
    
else
    echo "❌ NewPipe n'est pas installé"
    echo "Exécutez: adb install app/build/outputs/apk/debug/app-debug.apk"
fi

echo ""
echo "🏁 L'émulateur Android devrait être visible avec NewPipe installé."
echo "Vous pouvez maintenant utiliser NewPipe directement dans l'émulateur !"