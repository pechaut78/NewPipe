# Scripts de Déploiement NewPipe

Ce projet contient des scripts automatisés pour faciliter le développement et déploiement sur émulateur.

## 🚀 Scripts Disponibles

### 1. **Déploiement Rapide** `./quick-deploy.sh`
```bash
./quick-deploy.sh
```
- ✅ Compilation incrémentale
- ✅ Installation automatique sur émulateur  
- ✅ Lancement automatique de l'application
- ⚡ Le plus rapide pour le développement quotidien

### 2. **Déploiement Complet** `./deploy.sh`
```bash
./deploy.sh [--clean] [--launch]
```

Options :
- `--clean` : Effectue un clean build complet
- `--launch` : Lance automatiquement l'application après installation
- `--help` : Affiche l'aide

Exemples :
```bash
./deploy.sh                    # Compilation + installation seulement
./deploy.sh --launch          # + lancement automatique  
./deploy.sh --clean --launch  # Clean build + installation + lancement
```

## 🛠️ Outils de Développement

### Surveillance des logs
```bash
# Logs généraux NewPipe
adb logcat | grep NewPipe

# Logs spécifiques au carousel  
adb logcat -s VideoCarouselController:D VideoCarouselAdapter:D

# Logs d'erreurs uniquement
adb logcat | grep -E "(ERROR|FATAL)"
```

### Gestion de l'application
```bash
# Relancer l'app manuellement
adb shell am start -n org.schabi.newpipe.debug.featureaudiovideosyncandfullscreencontrols/org.schabi.newpipe.MainActivity

# Désinstaller l'app
adb uninstall org.schabi.newpipe.debug.featureaudiovideosyncandfullscreencontrols

# Vérifier les émulateurs connectés
adb devices
```

## 🎯 Workflow de Développement Recommandé

### Développement Quotidien
1. Modifier le code
2. `./quick-deploy.sh` → Test immédiat
3. Répéter

### Build de Test Complet  
1. `./deploy.sh --clean --launch` → Test avec build propre
2. Surveillance des logs si nécessaire

### Debug d'un Problème Spécifique
1. `./deploy.sh --launch`
2. `adb logcat -s VideoCarouselController:D VideoCarouselAdapter:D`
3. Reproduire le problème et analyser les logs

## 🔧 Fonctionnalités Testables

Après déploiement, testez :

1. **🎵 Délai Audio/Vidéo** 
   - Paramètres → Lecteur → Synchronisation

2. **🔁 Contrôles Plein Écran**
   - Mode plein écran → Boutons repeat/shuffle visibles

3. **📱 Carousel de Vidéos**
   - Lance une vidéo → Carousel apparaît en bas
   - Clique sur miniatures → Navigation entre vidéos  
   - Scroll dans carousel → Interface reste visible

## ⚡ One-liner pour Tests Rapides
```bash
# Modifier code → déployer → tester en une commande
./quick-deploy.sh && echo "🎉 Prêt pour test !"
```

---
*Auto-généré pour le projet NewPipe avec fonctionnalités carousel et contrôles audio/vidéo*