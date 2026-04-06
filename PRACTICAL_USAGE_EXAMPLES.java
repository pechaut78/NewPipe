/*
 * Exemple pratique d'utilisation de la fonctionnalité délai audio/vidéo
 * 
 * Ce fichier montre comment intégrer concrètement la fonctionnalité 
 * dans les différents composants de NewPipe
 */

// =============================================================================
// 1. UTILISATION DANS UN FRAGMENT DE PARAMÈTRES
// =============================================================================

public class ExoPlayerSettingsFragment extends BasePreferenceFragment {
    
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, 
                                   @Nullable String rootKey) {
        addPreferencesFromResourceRegistry();
        
        // ... code existant ...
        
        // Ajout de la préférence délai audio/vidéo  
        PreferenceScreen screen = getPreferenceScreen();
        AudioVideoSyncSettingsHelper.addAudioVideoSyncPreference(this, screen);
    }
}

// =============================================================================
// 2. UTILISATION DANS UNE ACTIVITÉ OU FRAGMENT
// =============================================================================

public class VideoPlayerActivity extends AppCompatActivity {
    
    private Player player;
    
    private void setupAudioVideoSyncControls() {
        // Bouton pour ouvrir les paramètres de délai
        Button syncButton = findViewById(R.id.audio_video_sync_button);
        syncButton.setOnClickListener(v -> showSyncDialog());
        
        // Afficher le délai actuel
        updateSyncDelayDisplay();
    }
    
    private void showSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Délai Audio/Vidéo");
        
        // Créer une SeekBar
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(2000); // -1000ms à +1000ms
        
        int currentDelay = player.getAudioVideoSyncDelay();
        seekBar.setProgress(1000 + currentDelay);
        
        builder.setView(seekBar);
        builder.setPositiveButton("OK", (dialog, which) -> {
            int newDelay = seekBar.getProgress() - 1000;
            player.setAudioVideoSyncDelay(newDelay);
            updateSyncDelayDisplay();
        });
        
        builder.show();
    }
    
    private void updateSyncDelayDisplay() {
        TextView delayText = findViewById(R.id.sync_delay_text);
        int delay = player.getAudioVideoSyncDelay();
        
        String text;
        if (delay == 0) {
            text = "Synchronisé";
        } else if (delay < 0) {
            text = "Audio " + Math.abs(delay) + "ms en avance";
        } else {
            text = "Audio " + delay + "ms en retard";
        }
        
        delayText.setText(text);
    }
}

// =============================================================================
// 3. INTÉGRATION DANS LE GESTURE DETECTOR (CONTRÔLES TACTILES)
// =============================================================================

public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
    
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Double-tap pour ajuster rapidement le délai
        float x = e.getX();
        float screenWidth = getScreenWidth();
        
        if (x < screenWidth * 0.3f) {
            // Côté gauche : diminuer le délai (audio plus en avance)
            adjustSyncDelay(-50);
            showSyncToast("Audio -50ms");
        } else if (x > screenWidth * 0.7f) {
            // Côté droit : augmenter le délai (audio plus en retard)  
            adjustSyncDelay(+50);
            showSyncToast("Audio +50ms");
        }
        
        return true;
    }
    
    private void adjustSyncDelay(int deltaMs) {
        int currentDelay = player.getAudioVideoSyncDelay();
        int newDelay = Math.max(-1000, Math.min(1000, currentDelay + deltaMs));
        player.setAudioVideoSyncDelay(newDelay);
    }
    
    private void showSyncToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

// =============================================================================
// 4. CONTRÔLES DANS L'INTERFACE VIDÉO
// =============================================================================

public abstract class VideoPlayerUi extends PlayerUi {
    
    private void setupSyncControls() {
        // Boutons +/- pour ajustement rapide
        Button syncMinus = binding.syncMinusButton;
        Button syncPlus = binding.syncPlusButton;
        TextView syncDisplay = binding.syncDelayDisplay;
        
        syncMinus.setOnClickListener(v -> {
            int currentDelay = player.getAudioVideoSyncDelay();
            int newDelay = Math.max(-1000, currentDelay - 50);
            player.setAudioVideoSyncDelay(newDelay);
            updateSyncDisplay(syncDisplay);
        });
        
        syncPlus.setOnClickListener(v -> {
            int currentDelay = player.getAudioVideoSyncDelay();
            int newDelay = Math.min(1000, currentDelay + 50);
            player.setAudioVideoSyncDelay(newDelay);
            updateSyncDisplay(syncDisplay);
        });
        
        // Long press pour reset
        syncDisplay.setOnLongClickListener(v -> {
            player.setAudioVideoSyncDelay(0);
            updateSyncDisplay(syncDisplay);
            Toast.makeText(context, "Délai reseté", Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    
    private void updateSyncDisplay(TextView textView) {
        int delay = player.getAudioVideoSyncDelay();
        if (delay == 0) {
            textView.setText("♫");
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText(delay > 0 ? ("+" + delay) : String.valueOf(delay));
            textView.setTextColor(delay > 0 ? Color.RED : Color.BLUE);
        }
    }
}

// =============================================================================
// 5. SAUVEGARDE ET RESTAURATION POUR DIFFÉRENTS CONTENUS
// =============================================================================

public class ContentSpecificSyncManager {
    
    private static final String SYNC_DELAYS_KEY = "content_specific_sync_delays";
    
    /**
     * Sauvegarde le délai pour un contenu spécifique (par URL ou ID)
     */
    public static void saveDelayForContent(Context context, String contentId, int delayMs) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> delays = new HashSet<>(prefs.getStringSet(SYNC_DELAYS_KEY, new HashSet<>()));
        
        // Retirer l'ancienne entrée si elle existe
        delays.removeIf(entry -> entry.startsWith(contentId + ":"));
        
        // Ajouter la nouvelle entrée
        delays.add(contentId + ":" + delayMs);
        
        prefs.edit().putStringSet(SYNC_DELAYS_KEY, delays).apply();
    }
    
    /**
     * Récupère le délai sauvegardé pour un contenu spécifique
     */
    public static int getDelayForContent(Context context, String contentId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> delays = prefs.getStringSet(SYNC_DELAYS_KEY, new HashSet<>());
        
        for (String entry : delays) {
            if (entry.startsWith(contentId + ":")) {
                try {
                    return Integer.parseInt(entry.split(":")[1]);
                } catch (Exception e) {
                    // Ignorer les entrées malformées
                }
            }
        }
        
        // Délai par défaut si pas trouvé
        return AudioVideoSyncHelper.getAudioVideoSyncDelay(context);
    }
    
    /**
     * Applique automatiquement le délai lors du changement de contenu
     */
    public static void applyDelayForContent(Player player, StreamInfo streamInfo) {
        String contentId = streamInfo.getUrl();
        int delay = getDelayForContent(player.getContext(), contentId);
        player.setAudioVideoSyncDelay(delay);
    }
}

// =============================================================================
// 6. EXEMPLE D'UTILISATION DANS LE CYCLE DE VIE DU PLAYER
// =============================================================================

public class Player implements PlaybackListener, Listener {
    
    @Override 
    public void onPlaybackSynchronize(@NonNull PlayQueueItem item, boolean wasBlocked) {
        super.onPlaybackSynchronize(item, wasBlocked);
        
        // Appliquer automatiquement le délai sauvegardé pour ce contenu
        if (item.getStreamInfo() != null) {
            ContentSpecificSyncManager.applyDelayForContent(this, item.getStreamInfo());
        }
    }
    
    // Méthode appelée lors du changement manual du délai
    @Override
    public void setAudioVideoSyncDelay(int delayMs) {
        super.setAudioVideoSyncDelay(delayMs);
        
        // Sauvegarder le délai pour ce contenu spécifique (optionnel)
        PlayQueueItem currentItem = playQueue.getItem();
        if (currentItem != null && currentItem.getStreamInfo() != null) {
            String contentId = currentItem.getStreamInfo().getUrl();
            ContentSpecificSyncManager.saveDelayForContent(context, contentId, delayMs);
        }
    }
}