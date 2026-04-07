# Makefile pour NewPipe Development
# Usage: make [target]

# Configuration
JAVA_HOME := /opt/homebrew/opt/openjdk@17
ANDROID_HOME := /opt/homebrew/share/android-commandlinetools
PACKAGE_NAME := org.schabi.newpipe.debug.featureaudiovideosyncandfullscreencontrols
MAIN_ACTIVITY := $(PACKAGE_NAME)/org.schabi.newpipe.MainActivity
APK_PATH := ./app/build/outputs/apk/debug/app-debug.apk

# Variables d'environnement
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/platform-tools:$(PATH)

.PHONY: help deploy quick clean install launch logs device-check

# Aide par défaut
help:
	@echo "🚀 NewPipe Development Commands"
	@echo "================================"
	@echo "make quick      - Déploiement rapide (build + install + launch)"
	@echo "make deploy     - Build et installation seulement"
	@echo "make clean      - Clean build et installation"
	@echo "make install    - Installation de l'APK existant"
	@echo "make launch     - Lancement de l'application"
	@echo "make logs       - Surveillance des logs carousel"
	@echo "make device     - Vérifier les émulateurs connectés"
	@echo ""
	@echo "Exemples:"
	@echo "  make quick    # Le plus utilisé pour développement"
	@echo "  make clean    # Quand il y a des problèmes de cache"
	@echo "  make logs     # Pour debugger les problèmes"

# Déploiement rapide (le plus utilisé)
quick: device-check
	@echo "🚀 Déploiement rapide..."
	@./gradlew assembleDebug -x runCheckstyle
	@adb install -r $(APK_PATH)
	@adb shell am start -n $(MAIN_ACTIVITY)
	@echo "✅ Déployé et lancé !"

# Build et installation
deploy: device-check
	@echo "🔨 Build et installation..."
	@./gradlew assembleDebug -x runCheckstyle
	@adb install -r $(APK_PATH)
	@echo "✅ Installé ! Utilisez 'make launch' pour lancer."

# Clean build
clean: device-check
	@echo "🧹 Clean build..."
	@./gradlew clean assembleDebug -x runCheckstyle
	@adb install -r $(APK_PATH)
	@echo "✅ Clean build installé !"

# Installation seulement
install: device-check
	@echo "📱 Installation..."
	@adb install -r $(APK_PATH)
	@echo "✅ Installé !"

# Lancement seulement
launch: device-check
	@echo "🎬 Lancement de NewPipe..."
	@adb shell am start -n $(MAIN_ACTIVITY)
	@echo "✅ Lancé !"

# Surveillance des logs
logs: device-check
	@echo "📋 Logs du carousel (Ctrl+C pour arrêter)..."
	@adb logcat -s VideoCarouselController:D VideoCarouselAdapter:D

# Vérification émulateur
device-check:
	@if ! adb devices | grep -q "device$$"; then \
		echo "❌ Aucun émulateur connecté !"; \
		echo "Lancez l'émulateur d'abord."; \
		exit 1; \
	fi
	@echo "✅ Émulateur connecté"

device:
	@echo "📱 Émulateurs connectés:"
	@adb devices