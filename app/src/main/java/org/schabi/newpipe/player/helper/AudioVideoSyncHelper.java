/*
 * Ajout de la fonctionnalité de délai audio/vidéo dans NewPipe
 * Ce fichier montre l'implémentation pratique de la synchronisation audio/vidéo
 */

package org.schabi.newpipe.player.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AudioVideoSyncHelper {
    
    private static final String AUDIO_VIDEO_SYNC_DELAY_KEY = "audio_video_sync_delay";
    private static final int DEFAULT_SYNC_DELAY = 0; // millisecondes
    
    /**
     * Récupère le délai de synchronisation audio/vidéo configuré par l'utilisateur
     * @param context Context de l'application
     * @return Délai en millisecondes (négatif = audio en avance, positif = audio en retard)
     */
    public static int getAudioVideoSyncDelay(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(AUDIO_VIDEO_SYNC_DELAY_KEY, DEFAULT_SYNC_DELAY);
    }
    
    /**
     * Sauvegarde le délai de synchronisation audio/vidéo
     * @param context Context de l'application  
     * @param delayMs Délai en millisecondes
     */
    public static void setAudioVideoSyncDelay(Context context, int delayMs) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(AUDIO_VIDEO_SYNC_DELAY_KEY, delayMs).apply();
    }
    
    /**
     * Convertit le délai en microsecondes pour ExoPlayer
     * @param delayMs Délai en millisecondes
     * @return Délai en microsecondes
     */
    public static long delayToMicroseconds(int delayMs) {
        return delayMs * 1000L;
    }
    
    /**
     * Vérifie si un délai audio/vidéo est configuré
     * @param context Context de l'application
     * @return true si un délai est configuré (différent de 0)
     */
    public static boolean hasSyncDelay(Context context) {
        return getAudioVideoSyncDelay(context) != DEFAULT_SYNC_DELAY;
    }
    
    /**
     * Obtient les valeurs de délai prédéfinies
     * @return Tableau des valeurs de délai en millisecondes
     */
    public static int[] getPredefinedDelayValues() {
        return new int[]{-1000, -500, -250, -100, -50, 0, 50, 100, 250, 500, 1000};
    }
    
    /**
     * Obtient les descriptions des valeurs de délai
     * @return Tableau des descriptions correspondant aux valeurs
     */
    public static String[] getPredefinedDelayDescriptions() {
        return new String[]{
            "-1000ms (Audio en avance)",
            "-500ms", 
            "-250ms",
            "-100ms",
            "-50ms",
            "0ms (Synchronisé)",
            "+50ms",
            "+100ms", 
            "+250ms",
            "+500ms",
            "+1000ms (Audio en retard)"
        };
    }
}