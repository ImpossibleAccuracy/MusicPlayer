package com.example.musicplayer.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;

import com.example.musicplayer.Config;
import com.example.musicplayer.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new MainSettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class MainSettingsFragment extends PreferenceFragmentCompat {
        private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sp, key) -> {
            SharedPreferences publicPreferences = getActivity().getSharedPreferences(Config.PREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = publicPreferences.edit();

            switch (key) {
                case "skip_short_tracks":
                    editor.putBoolean("skip_short_tracks",
                            sp.getBoolean("skip_short_tracks", false));
                    break;
                case "theme":
                    String theme = sp.getString(key, "Follow System");
                    editor.putString("theme", theme);

                    switch (theme) {
                        case "Dark":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case "Light":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "Follow System":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                    break;
            }

            editor.apply();
        };

        public MainSettingsFragment() { /**/ }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

            if (getDeviceTheme() == 1 &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#171717"));
            }
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
            super.onPause();
        }

        public int getDeviceTheme() {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_NO:
                    return 1;
                case Configuration.UI_MODE_NIGHT_YES:
                    return 2;
            }
            return 3;
        }
    }
}
