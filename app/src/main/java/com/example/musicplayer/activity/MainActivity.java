package com.example.musicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.dialogs.ScanDeviceDialog;
import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.service.PlayerService;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;
import com.example.musicplayer.widget.ControlButton;

public class MainActivity extends AppActivity
        implements View.OnClickListener {
    static final String TAG = MainActivity.class.getSimpleName();

    private ImageView songIcon;
    private TextView songNameView;
    private TextView songArtistView;
    private ControlButton controlButton;

    private MusicApplication application;

    private final ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerServiceBinder playerService = (PlayerService.PlayerServiceBinder) service;
            playerService.setPlayer(getPlayer());
            playerService.updateNotification();
            application.setPlayerService(playerService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final Callback updateUICallback = new Callback() {
        @Override
        public void onCurrentAudioChanged(Player player, AudioModel currentAudio) {
            fillUI();
        }
        @Override
        public void onPlayerStateChanged(Player player, int state) {
            controlButton.update(state);
        }
    };

    private final Callback dataControlCallback = new Callback() {
        @Override
        public void onCurrentAudioChanged(Player player, AudioModel playingAudio) {
            MusicRepository musicRepository = getMusicRepository();
            Playlist recentOpened = musicRepository.getPlaylistById(MusicRepository.RECENT_OPENED_PLAYLIST_ID);
            recentOpened.remove(playingAudio);
            recentOpened.add(0, playingAudio);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setWindowMode(MODE_TRANSPARENT);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            View nav_bg = findViewById(R.id.nav_bg);
            if (getDeviceTheme() == THEME_LIGHT)
                nav_bg.setBackgroundResource(R.color.black);
            SetViewMargins(findViewById(R.id.ControlButtonLayout), 0, getStatusBarHeight(), 0, 0);
            SetViewSize(nav_bg, ViewGroup.LayoutParams.MATCH_PARENT, getNavigationBarHeight());
        }

        application = ((MusicApplication) getApplication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, PlayerService.class));
        } else {
            super.startService(new Intent(this, PlayerService.class));
        }
        bindService(new Intent(this, PlayerService.class), playerServiceConnection, BIND_AUTO_CREATE);

        Player p = getPlayer();
        if (savedInstanceState == null) {
            p.registerCallback(dataControlCallback);
        } else {
            application.getPlayerService().updateNotification();
        }

        p.registerCallback(updateUICallback);
    }
    @Override
    protected void onPause() {
        if (application.getPlayer().getState() != Player.STATE_PLAYING)
            application.safeData();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(playerServiceConnection);
        getPlayer().unregisterCallback(updateUICallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("RecreatePayer", true);
    }

    @Override
    protected void findUI() {
        songIcon = findViewById(R.id.SongIcon);
        songNameView = findViewById(R.id.SongNameView);
        songArtistView = findViewById(R.id.SongArtistView);
        controlButton = findViewById(R.id.ControlButton);

        controlButton.setOnClickListener(this);
        findViewById(R.id.ScanDeviceButton).setOnClickListener(this::refreshAudio);
        findViewById(R.id.SettingsBtn).setOnClickListener(this);
        findViewById(R.id.AllSongsButton).setOnClickListener(this);
        findViewById(R.id.FavoritesButton).setOnClickListener(this);
        findViewById(R.id.RecentOpenedSongsButton).setOnClickListener(this);
        findViewById(R.id.AllPlaylistsButton).setOnClickListener(this);
        findViewById(R.id.MainPreviewLayout).setOnClickListener(this);
        findViewById(R.id.MainControlPanel).setOnClickListener(this);
    }
    @Override
    protected void fillUI() {
        runOnUiThread(() -> {
            Player player = getPlayer();
            AudioModel track = player.getMusicQueue().getCurrent();

            songNameView.setText(track.getTitle());
            if (track.getArtist() == null || track.getArtist().length() == 0) {
                songArtistView.setText(R.string.default_artist);
            } else {
                songArtistView.setText(track.getArtist());
            }

            if (track.getIcon() != null) {
                songIcon.setImageBitmap(track.getIcon());
            }
            else {
                songIcon.setImageResource(R.drawable.default_track_icon);
            }

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                findViewById(R.id.SongInfoView).setPadding(0, 0, getNavigationBarHeight(), 0);
            } else {
                findViewById(R.id.SongInfoView).setPadding(0, 0, 0, 0);
            }

            controlButton.update(player.getState());
        });
    }

    @Override
    public void onClick(View v) {
        Player player = getPlayer();

        if (v.getId() == R.id.ControlButton) {
            if (player.getState() == Player.STATE_PLAYING) player.stop();
            else player.play();
        } else if (v.getId() == R.id.AllSongsButton) {
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("PlaylistId", MusicRepository.ALL_AUDIO_PLAYLIST_ID);
            startActivity(intent);
        } else if (v.getId() == R.id.FavoritesButton) {
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("PlaylistId", MusicRepository.FAVORITES_PLAYLIST_ID);
            startActivity(intent);
        } else if (v.getId() == R.id.RecentOpenedSongsButton) {
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("PlaylistId", MusicRepository.RECENT_OPENED_PLAYLIST_ID);
            startActivity(intent);
        } else if (v.getId() == R.id.AllPlaylistsButton) {
            Intent intent = new Intent(this, AllPlaylistsActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.MainPreviewLayout || v.getId() == R.id.MainControlPanel) {
            startActivity(new Intent(this, SongActivity.class));
        } else if (v.getId() == R.id.SettingsBtn) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    private void refreshAudio(View v) {
        ScanDeviceDialog d = new ScanDeviceDialog(this, getMusicRepository());
        d.show();
    }
}
