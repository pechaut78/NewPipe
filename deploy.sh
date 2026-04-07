#!/bin/bash

# Script de déploiement automatique NewPipe sur émulateur
# Usage: ./deploy.sh [--launch] [--clean]

set -e

# Configuration des variables d'environnement
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonctions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Variables
CLEAN_BUILD=false
LAUNCH_APP=false
PACKAGE_NAME="org.schabi.newpipe.debug.featureaudiovideosyncandfullscreencontrols"
MAIN_ACTIVITY="$PACKAGE_NAME/org.schabi.newpipe.MainActivity"
APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"

# Parsing des arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --launch)
            LAUNCH_APP=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --clean     Effectuer un clean build"
            echo "  --launch    Lancer l'application après l'installation"
            echo "  -h, --help  Afficher cette aide"
            exit 0
            ;;
        *)
            log_error "Option inconnue: $1"
            exit 1
            ;;
    esac
done

echo "============================================================="
echo "🚀 DÉPLOIEMENT AUTOMATIQUE NEWPIPE"
echo "============================================================="

# 1. Vérifier la connexion de l'émulateur
log_info "Vérification de la connexion de l'émulateur..."
if ! adb devices | grep -q "device$"; then
    log_error "Aucun émulateur détecté. Veuillez démarrer l'émulateur d'abord."
    exit 1
fi

DEVICE=$(adb devices | grep "device$" | head -n1 | awk '{print $1}')
log_success "Émulateur connecté: $DEVICE"

# 2. Compilation
if [ "$CLEAN_BUILD" = true ]; then
    log_info "Nettoyage et compilation complète..."
    ./gradlew clean assembleDebug -x runCheckstyle
else
    log_info "Compilation incrémentale..."
    ./gradlew assembleDebug -x runCheckstyle
fi

if [ ! -f "$APK_PATH" ]; then
    log_error "APK non trouvé après compilation: $APK_PATH"
    exit 1
fi

log_success "Compilation terminée ✓"

# 3. Installation
log_info "Installation de l'APK sur l'émulateur..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    log_success "Installation réussie ✓"
else
    log_error "Échec de l'installation"
    exit 1
fi

# 4. Lancement optionnel
if [ "$LAUNCH_APP" = true ]; then
    log_info "Lancement de NewPipe..."
    adb shell am start -n "$MAIN_ACTIVITY"
    
    if [ $? -eq 0 ]; then
        log_success "Application lancée ✓"
    else
        log_warning "Échec du lancement automatique"
    fi
fi

echo "============================================================="
echo -e "${GREEN}🎉 DÉPLOIEMENT TERMINÉ AVEC SUCCÈS !${NC}"
echo "============================================================="

# 5. Informations utiles
log_info "Commandes utiles :"
echo "  • Logs en temps réel: adb logcat | grep -E '(VideoCarousel|NewPipe)'"
echo "  • Relancer l'app: adb shell am start -n $MAIN_ACTIVITY"
echo "  • Désinstaller: adb uninstall $PACKAGE_NAME"
echo "  • Redéployer: ./deploy.sh --launch"