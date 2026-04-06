package org.schabi.newpipe.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.Preference;

import org.schabi.newpipe.player.helper.AudioVideoSyncHelper;

/**
 * Helper class for audio/video synchronization settings in NewPipe
 * Provides UI components and utilities for managing sync delay preferences
 */
public final class AudioVideoSyncSettingsHelper {

    private AudioVideoSyncSettingsHelper() {
        // Utility class, no instantiation allowed
    }

    /**
     * Shows a dialog allowing user to adjust audio/video synchronization delay
     * @param context The context to use for creating the dialog
     */
    public static void showAudioVideoSyncDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Audio/Video Synchronization");

        // Info text
        TextView infoText = new TextView(context);
        infoText.setText("Adjust the delay between audio and video:\n• Positive values: audio ahead of video\n• Negative values: video ahead of audio");
        infoText.setPadding(20, 20, 20, 10);

        // Current delay value
        int currentDelay = AudioVideoSyncHelper.getAudioVideoSyncDelay(context);
        
        // Value display text
        TextView valueText = new TextView(context);
        updateDelayText(valueText, currentDelay);
        valueText.setPadding(20, 10, 20, 10);

        // SeekBar for delay adjustment (-1000ms to +1000ms)
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(2000); // 0 to 2000 (represents -1000 to +1000)
        seekBar.setProgress(currentDelay + 1000); // Convert -1000/+1000 to 0/2000
        seekBar.setPadding(20, 10, 20, 20);

        // Update text when user moves the seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int delay = progress - 1000; // Convert back to -1000 to +1000
                    updateDelayText(valueText, delay);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        layout.addView(infoText);
        layout.addView(seekBar);
        layout.addView(valueText);
        
        builder.setView(layout);
        
        // Buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            int finalDelay = seekBar.getProgress() - 1000;
            AudioVideoSyncHelper.setAudioVideoSyncDelay(context, finalDelay);
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.setNeutralButton("Reset", (dialog, which) -> {
            AudioVideoSyncHelper.setAudioVideoSyncDelay(context, 0);
        });
        
        builder.show();
    }
    
    /**
     * Updates the delay text display
     */
    private static void updateDelayText(TextView textView, int delayMs) {
        String text;
        if (delayMs == 0) {
            text = "0ms (Synchronized)";
        } else if (delayMs < 0) {
            text = delayMs + "ms (Audio ahead)";
        } else {
            text = "+" + delayMs + "ms (Audio behind)";
        }
        textView.setText("Current delay: " + text);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
    }
    
    /**
     * Updates preference summary with current delay value
     */
    public static void updateSyncDelayPreferenceSummary(Context context, Preference preference) {
        int delay = AudioVideoSyncHelper.getAudioVideoSyncDelay(context);
        String summary;
        if (delay == 0) {
            summary = "Synchronized (0ms)";
        } else if (delay < 0) {
            summary = "Audio ahead (" + delay + "ms)";
        } else {
            summary = "Audio behind (+" + delay + "ms)";
        }
        preference.setSummary(summary);
    }
}