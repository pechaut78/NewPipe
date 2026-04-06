# Configuration NewPipe - Guide d'installation

## ✅ Installation terminée avec succès !

Le projet NewPipe a été cloné et tous les prérequis ont été installés.

### 🔧 Prérequis installés

- ✅ **Git**: Pour cloner le projet
- ✅ **Java 17** (`/opt/homebrew/opt/openjdk@17`): Requis pour la compilation
- ✅ **Java 21** (`/opt/homebrew/opt/openjdk@21`): Requis pour checkStyle
- ✅ **Android Studio**: IDE recommandé
- ✅ **Android SDK Command Line Tools**: Pour la compilation
- ✅ **Android SDK Components**:
  - Platform Tools
  - Build Tools 36.0.0
  - Android API 35 (platforms;android-35)
  - Android API 36 (platforms;android-36)

### 🚀 Compilation rapide

Pour compiler le projet, utilisez simplement :

```bash
./compile.sh
```

### 🔨 Compilation manuelle

Si vous préférez compiler manuellement :

```bash
# Configuration environnement
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

# Compilation
./gradlew assembleDebug -x runCheckstyle
```

### 📁 Fichiers générés

- **APK de debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Configuration SDK**: `local.properties` 
- **Script de compilation**: `compile.sh`

### ⚠️ Problème connu

**CheckStyle nécessite Java 21** mais Gradle n'arrive pas à le détecter automatiquement. 
Pour l'instant, nous compilons avec `-x runCheckstyle` pour contourner ce problème.

### 💡 Configuration permanente (optionnel)

Pour éviter de reconfigurer les variables à chaque terminal, ajoutez ceci à votre `~/.zshrc` :

```bash
# Configuration NewPipe
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

### 🎯 Développement avec Android Studio

1. Ouvrir Android Studio
2. Ouvrir le dossier `/Users/pec/projets/newpipe` 
3. Android Studio détectera automatiquement le projet
4. Synchroniser le projet avec les fichiers Gradle

### 📖 Documentation utile

- [Guide de contribution NewPipe](.github/CONTRIBUTING.md)
- [Documentation Android Studio](https://developer.android.com/studio/)
- [NewPipe sur GitHub](https://github.com/TeamNewPipe/NewPipe)

---

**🎉 Félicitations !** Vous pouvez maintenant compiler et développer NewPipe.