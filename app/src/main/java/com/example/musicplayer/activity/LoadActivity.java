package com.example.musicplayer.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.musicplayer.Config;
import com.example.musicplayer.Database;
import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.R;
import com.example.musicplayer.loaders.DatabaseLoader;
import com.example.musicplayer.loaders.DeviceLoader;
import com.example.musicplayer.loaders.Loader;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class LoadActivity extends AppCompatActivity {
    private static final String TAG = LoadActivity.class.getSimpleName();

    public static final String[] permissions;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };
        } else {
            permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    private void Main() {
        Loader loader = createLoader();
        MusicRepository repository = new MusicRepository();
        MusicApplication application = (MusicApplication) getApplication();

        final boolean firstStart = loader.getClass().equals(DeviceLoader.class);

        loader.setCallback(new Loader.OnElementFoundCallback() {
            @Override
            public AudioModel onTrackFound(long id, String title, String path, String artist, String author, String album, long duration, boolean isFavorite, Bitmap icon) {
                Database database = Database.getInstance(LoadActivity.this);

                AudioModel track;

                if (firstStart || id == Loader.UNKNOWN_TRACK_ID) {
                    id = database.insertAudio(
                        title,
                        path,
                        artist,
                        author,
                        album,
                        duration,
                        isFavorite,
                        icon
                    );
                }
                track = new AudioModel(id,
                    title, path, artist, author, album, duration, isFavorite);
                track.setIcon(icon);

                return track;
            }

            @Override
            public Playlist onPlaylistFound(long id, String title, List<AudioModel> audio) {
                Playlist p = new Playlist(id, title);
                if (audio != null) p.addAll(audio);
                return p;
            }
        });

        loader.loadData();

        repository.addAllAudio(loader.getAudio());
        repository.addAllPlaylists(loader.getPlaylists());


        boolean skipShortTracks = getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean("skip_short_tracks", false);
        if (skipShortTracks) {
            Playlist list = repository.getPlaylistById(MusicRepository.ALL_AUDIO_PLAYLIST_ID);

            for (Iterator<AudioModel> iterator = list.iterator(); iterator.hasNext(); ) {
                AudioModel a = iterator.next();
                if (a.getDuration() < Config.MINIMAL_TRACK_DURATION) {
                    iterator.remove();
                }
            }
        }

        application.setPlayer(loader.getPlayer());
        application.setMusicRepository(repository);

        if (application.getMusicRepository().getAllAudio().isEmpty()) {
            showDialog();
        }
        else {
            runOnUiThread(this::openMainActivity);
        }
    }

    private void startMainAsync() {
        synchronized (this) {
            findViewById(R.id.LoadDataBtn).setEnabled(false);

            Thread thread = new Thread(this::Main);
            thread.setName("Loading Thread");
            thread.start();
        }
    }
    private void openMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private Loader createLoader() {
        Database database = Database.getInstance(this);

        if (database.isEmpty() || Config.NEED_UPDATE) {
            return new DeviceLoader(this);
        }
        else {
            return new DatabaseLoader(this, database);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (getSharedPreferences(Config.PREFERENCES_NAME, MODE_PRIVATE).getString("theme", "Follow System")) {
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }

        super.onCreate(savedInstanceState);

        MusicRepository musicRepository = ((MusicApplication)getApplication()).getMusicRepository();
        if (musicRepository != null) {
            openMainActivity();
            return;
        }

        setContentView(R.layout.activity_load);
        findViewById(R.id.LoadDataBtn).setOnClickListener(this::loadData);

        Database database = Database.getInstance(this);
        if (database.isEmpty() || Config.NEED_UPDATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 2);
            }
        }
        else {
            findViewById(R.id.LoadDataBtn).setVisibility(View.GONE);
            startMainAsync();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainAsync();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_songs_alert);
        builder.setCancelable(true);
        builder.setTitle("Alert");

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finishAffinity();
            }
        });

        runOnUiThread(() -> {
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    public void loadData(View view) {
        for (String p : permissions) {
            if (!checkPermission(p)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                return;
            }
        }
        startMainAsync();
    }
}
