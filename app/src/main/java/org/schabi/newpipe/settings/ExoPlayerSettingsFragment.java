package org.schabi.newpipe.settings;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import org.schabi.newpipe.R;

public class ExoPlayerSettingsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(@Nullable final Bundle savedInstanceState,
                                    @Nullable final String rootKey) {
        addPreferencesFromResourceRegistry();

        final String disabledMediaTunnelingAutomaticallyKey =
                getString(R.string.disabled_media_tunneling_automatically_key);
        final SwitchPreferenceCompat disableMediaTunnelingPref =
                (SwitchPreferenceCompat) requirePreference(R.string.disable_media_tunneling_key);
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());
        final boolean mediaTunnelingAutomaticallyDisabled =
                prefs.getInt(disabledMediaTunnelingAutomaticallyKey, -1) == 1;
        final String summaryText = getString(R.string.disable_media_tunneling_summary);
        disableMediaTunnelingPref.setSummary(mediaTunnelingAutomaticallyDisabled
                ? summaryText + " " + getString(R.string.disable_media_tunneling_automatic_info)
                : summaryText);

        disableMediaTunnelingPref.setOnPreferenceChangeListener((Preference p, Object enabled) -> {
                    if (Boolean.FALSE.equals(enabled)) {
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit()
                                .putInt(disabledMediaTunnelingAutomaticallyKey, 0)
                                .apply();
                        // the info text might have been shown before
                        p.setSummary(R.string.disable_media_tunneling_summary);
                    }
                    return true;
                });

        // Setup audio/video sync delay preference
        setupAudioVideoSyncDelayPreference();
    }

    private void setupAudioVideoSyncDelayPreference() {
        final ListPreference audioVideoSyncPref = 
                (ListPreference) requirePreference(R.string.audio_video_sync_delay_key);
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());
        
        // Update summary with current value
        updateAudioVideoSyncSummary(audioVideoSyncPref, prefs);
        
        audioVideoSyncPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
            final String selectedValue = (String) newValue;
            
            if ("custom".equals(selectedValue)) {
                // Show dialog for custom value input
                showCustomDelayDialog(audioVideoSyncPref, prefs);
                return false; // Don't update the preference value yet
            } else {
                // Update summary with new value
                updateAudioVideoSyncSummary(audioVideoSyncPref, prefs, selectedValue);
                return true;
            }
        });
    }
    
    private void showCustomDelayDialog(final ListPreference preference, 
                                       final SharedPreferences prefs) {
        final EditText editText = new EditText(requireContext());
        editText.setHint("Enter delay in milliseconds (-1000 to +1000)");
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                             android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Custom Audio/Video Sync Delay")
            .setMessage("Enter custom delay value in milliseconds.\nPositive values: audio ahead of video\nNegative values: video ahead of audio")
            .setView(editText)
            .setPositiveButton("OK", (dialog, which) -> {
                try {
                    final String inputText = editText.getText().toString().trim();
                    if (inputText.isEmpty()) {
                        return;
                    }
                    
                    final int customDelay = Integer.parseInt(inputText);
                    if (customDelay < -1000 || customDelay > 1000) {
                        Toast.makeText(requireContext(), 
                                     "Delay must be between -1000 and +1000 ms", 
                                     Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Save custom value
                    prefs.edit()
                         .putString(getString(R.string.audio_video_sync_delay_key), 
                                   String.valueOf(customDelay))
                         .apply();
                    
                    // Update preference value and summary
                    preference.setValue(String.valueOf(customDelay));
                    updateAudioVideoSyncSummary(preference, prefs, String.valueOf(customDelay));
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), 
                                 "Invalid number format", 
                                 Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateAudioVideoSyncSummary(final ListPreference preference, 
                                             final SharedPreferences prefs) {
        final String currentValue = prefs.getString(getString(R.string.audio_video_sync_delay_key), "0");
        updateAudioVideoSyncSummary(preference, prefs, currentValue);
    }
    
    private void updateAudioVideoSyncSummary(final ListPreference preference, 
                                             final SharedPreferences prefs, 
                                             final String value) {
        final String summaryFormat = getString(R.string.audio_video_sync_delay_summary);
        final String formattedSummary = String.format(summaryFormat, value);
        preference.setSummary(formattedSummary);
    }
}
