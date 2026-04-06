# Guide d'implémentation : Délai Audio/Vidéo dans NewPipe

## 🎯 Fonctionnalité proposée : Délai Audio/Vidéo

Cette fonctionnalité permet d'ajuster la synchronisation entre l'audio et la vidéo en millisecondes.

## 📋 Étapes d'implémentation

### 1️⃣ Ajouter les paramètres dans strings.xml

```xml
<!-- Audio/Video Sync Settings -->
<string name="audio_video_sync_delay_key">audio_video_sync_delay</string>
<string name="audio_video_sync_delay_title">Délai audio/vidéo</string>
<string name="audio_video_sync_delay_summary">Ajuste la synchronisation audio/vidéo (millisecondes)</string>
<string name="audio_video_sync_delay_default_value">0</string>
```

### 2️⃣ Ajouter les valeurs dans arrays.xml

```xml
<!-- Audio/Video Delay Options -->
<string-array name="audio_video_delay_values">
    <item>-1000</item>
    <item>-500</item>
    <item>-250</item>
    <item>-100</item>
    <item>-50</item>
    <item>0</item>
    <item>50</item>
    <item>100</item>
    <item>250</item>
    <item>500</item>
    <item>1000</item>
</string-array>

<string-array name="audio_video_delay_descriptions">
    <item>-1000ms (Audio en avance)</item>
    <item>-500ms</item>
    <item>-250ms</item>
    <item>-100ms</item>
    <item>-50ms</item>
    <item>0ms (Synchronisé)</item>
    <item>+50ms</item>
    <item>+100ms</item>
    <item>+250ms</item>
    <item>+500ms</item>
    <item>+1000ms (Audio en retard)</item>
</string-array>
```

### 3️⃣ Modifier exoplayer_settings.xml

Ajouter cette section dans `/app/src/main/res/xml/exoplayer_settings.xml` :

```xml
<ListPreference
    android:defaultValue="@string/audio_video_sync_delay_default_value"
    android:entries="@array/audio_video_delay_descriptions"
    android:entryValues="@array/audio_video_delay_values"
    android:key="@string/audio_video_sync_delay_key"
    android:summary="@string/audio_video_sync_delay_summary"
    android:title="@string/audio_video_sync_delay_title"
    app:singleLineTitle="false"
    app:iconSpaceReserved="false"
    app:useSimpleSummaryProvider="true" />
```

### 4️⃣ Modifier PlayerHelper.java

Ajouter ces méthodes dans `/app/src/main/java/org/schabi/newpipe/player/helper/PlayerHelper.java` :

```java
public static int retrieveAudioVideoSyncDelayFromPrefs(final Player player) {
    return Integer.parseInt(Objects.requireNonNull(player.getPrefs().getString(
            player.getContext().getString(R.string.audio_video_sync_delay_key),
            player.getContext().getString(R.string.audio_video_sync_delay_default_value))));
}

public static void saveAudioVideoSyncDelayToPrefs(final Player player, final int delayMs) {
    player.getPrefs().edit()
            .putString(player.getContext().getString(R.string.audio_video_sync_delay_key), 
                      String.valueOf(delayMs))
            .apply();
}
```

### 5️⃣ Modifier VideoPlaybackResolver.java

Dans la méthode `resolve()`, après créer les sources audio/vidéo :

```java
// Appliquer le délai audio/vidéo si configuré
final int syncDelayMs = PlayerHelper.retrieveAudioVideoSyncDelayFromPrefs(player);
if (syncDelayMs != 0 && audioSource != null) {
    // Créer une source audio avec délai
    final ClippingMediaSource delayedAudioSource = new ClippingMediaSource(
            audioSource,
            Math.max(0, -syncDelayMs * 1000L), // Début (microsecondes)
            C.TIME_END_OF_SOURCE);
            
    mediaSources.set(audioSourceIndex, delayedAudioSource);
}
```

### 6️⃣ Modifier Player.java 

Ajouter une méthode pour ajuster le délai en temps réel :

```java
public void setAudioVideoSyncDelay(final int delayMs) {
    PlayerHelper.saveAudioVideoSyncDelayToPrefs(this, delayMs);
    
    if (!exoPlayerIsNull()) {
        // Recharger les sources avec le nouveau délai
        saveStreamProgressState();
        setRecovery();
        reloadPlayQueueManager();
    }
}

public int getAudioVideoSyncDelay() {
    return PlayerHelper.retrieveAudioVideoSyncDelayFromPrefs(this);
}
```

### 7️⃣ Interface utilisateur avancée (optionnel)

Pour un contrôle plus précis, ajouter des boutons dans VideoPlayerUi :

```java
// Dans VideoPlayerUi.java
private void setupAudioVideoSyncControls() {
    binding.syncDelayButton.setOnClickListener(v -> showSyncDelayDialog());
}

private void showSyncDelayDialog() {
    // Dialog personnalisé avec seekbar pour ajustement précis
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Délai Audio/Vidéo");
    
    final SeekBar seekBar = new SeekBar(context);
    seekBar.setMax(2000); // -1000ms à +1000ms
    seekBar.setProgress(1000 + player.getAudioVideoSyncDelay());
    
    builder.setView(seekBar);
    builder.setPositiveButton("OK", (dialog, which) -> {
        final int delay = seekBar.getProgress() - 1000;
        player.setAudioVideoSyncDelay(delay);
    });
    builder.show();
}
```

## 🎨 Architecture de la fonctionnalité

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  UI Settings    │───▶│  PlayerHelper    │───▶│ VideoResolver   │
│  (ExoPlayer     │    │  (Preferences)   │    │ (Apply Delay)   │
│   Settings)     │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │     Player       │───▶│   ExoPlayer     │
                       │ (Sync Control)   │    │ (Audio/Video)   │
                       └──────────────────┘    └─────────────────┘
```

## 🧪 Comment tester

1. Compiler NewPipe avec les modifications  
2. Aller dans Paramètres → Vidéo et Audio → ExoPlayer
3. Ajuster "Délai audio/vidéo" 
4. Tester avec du contenu où l'audio/vidéo n'est pas synchronisé

## 💡 Avantages de cette approche

✅ **Intégration native** avec ExoPlayer  
✅ **Interface utilisateur cohérente** avec les autres paramètres  
✅ **Persistance** des préférences utilisateur  
✅ **Contrôle précis** au milliseconde près  
✅ **Compatible** avec tous les types de contenus (YouTube, PeerTube, etc.)

## 🔧 Alternative : Utilisation des Renderers ExoPlayer

Pour un contrôle encore plus précis, on pourrait modifier directement les renderers audio :

```java
// Dans CustomRenderersFactory.java
@Override
protected AudioRenderer buildAudioRenderer(/* params */) {
    return new MediaCodecAudioRenderer(
            context, 
            mediaCodecSelector,
            playbackParameters -> {
                // Appliquer le délai ici
                final int delay = getAudioSyncDelay();
                return playbackParameters.withSkipSilence(delay != 0);
            }
    );
}
```

Cette implémentation vous donnerait un contrôle complet sur la synchronisation audio/vidéo dans NewPipe ! 🎬🔊