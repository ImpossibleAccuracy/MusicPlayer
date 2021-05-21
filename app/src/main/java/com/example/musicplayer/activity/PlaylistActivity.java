package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.musicplayer.Database;
import com.example.musicplayer.Player;
import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;
import com.example.musicplayer.widget.PlaybarLayout;
import com.example.musicplayer.widget.PlaylistView;
import com.example.musicplayer.widget.TopBarButton;
import com.example.musicplayer.listener.OnItemSelectedListener;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;
import com.example.musicplayer.R;

import java.util.Collections;

public class PlaylistActivity extends AppActivity
        implements OnItemSelectedListener, View.OnClickListener {
    static final String TAG = PlaylistActivity.class.getSimpleName();

    private TextView pageNameView;
    private TopBarButton addButton;
    private TextView audioCountView;
    private PlaylistView playlistView;
    private PlaybarLayout playbarLayout;

    private Player player;
    private Playlist playlist;
    private MusicRepository musicRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        setWindowMode(MODE_LIGHT);

        player = getPlayer();
        musicRepository = getMusicRepository();

        Intent intent = getIntent();
        if (intent != null) {
            int playlistId = intent.getIntExtra("PlaylistId", Integer.MIN_VALUE);
            playlist = musicRepository.getPlaylistById(playlistId);

            if (playlistId < 0)
                addButton.setVisibility(View.GONE);

            playlistView.setPlaylist(playlist);
        }

        player.registerCallback(new Callback() {
            @Override
            public void onPlayerStateChanged(Player player, int state) {
                runOnUiThread(() -> {
                    playbarLayout.update(player.getMusicQueue().getCurrent(), state);
                    playlistView.update();
                });
            }

            @Override
            public void onCurrentAudioChanged(Player player, AudioModel currentAudio) {
                runOnUiThread(() -> {
                    playbarLayout.update(currentAudio, player.getState());
                    playlistView.update();
                });
            }
        });

        playlistView.setPlayer(player);

        new UpdateSeekBarThread().start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                updatePlaylistFromIntent(data);
                fillUI();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void findUI() {
        addButton = findViewById(R.id.AddButton);
        playlistView = findViewById(R.id.PlaylistView);
        pageNameView = findViewById(R.id.PageNameView);
        playbarLayout = findViewById(R.id.PlaybarLayout);
        audioCountView = findViewById(R.id.AudioCountView);

        findViewById(R.id.ShufflePlayLayout).setOnClickListener(this);
        addButton.setOnClickListener(this);
        playlistView.setOnItemSelectedListener(this);
        playbarLayout.setOnClickListener(this);
        playbarLayout.setOnControlButtonClickListener(this);
        playbarLayout.setOnNextSongButtonClickListener(this);
        playbarLayout.setOnPreviousSongButtonClickListener(this);
    }
    @Override
    protected void fillUI() {
        runOnUiThread(() -> {
            switch ((int) playlist.id) {
                case MusicRepository.ALL_AUDIO_PLAYLIST_ID:
                    pageNameView.setText(R.string.all_songs);
                    break;
                case MusicRepository.FAVORITES_PLAYLIST_ID:
                    pageNameView.setText(R.string.favorites);
                    break;
                case MusicRepository.RECENT_OPENED_PLAYLIST_ID:
                    pageNameView.setText(R.string.recent_opened);
                    break;
                default:
                    pageNameView.setText(playlist.getName());
                    break;
            }

            if (playlist.id == MusicRepository.ALL_AUDIO_PLAYLIST_ID ||
                    playlist.id == MusicRepository.FAVORITES_PLAYLIST_ID ||
                    playlist.id == MusicRepository.RECENT_OPENED_PLAYLIST_ID)
                addButton.setVisibility(View.GONE);
            else
                addButton.setVisibility(View.VISIBLE);

            audioCountView.setText(String.valueOf(playlist.size()));

            playlistView.update();
            playbarLayout.update(player.getMusicQueue().getCurrent(),
                player.getState());
            playbarLayout.updateSeekBar(player.getTime(),
                player.getMusicQueue().getCurrent().getDuration());
        });
    }

    @Override
    public void onItemSelected(int item, AudioModel selected) {
        MusicQueue musicQueue = player.getMusicQueue();
        Playlist currentPlaylist = player.getCurrentPlaylist();
        AudioModel currentAudio = player.getMusicQueue().getCurrent();

        if (selected.equals(currentAudio) && playlist.equals(currentPlaylist)) {
            if (player.getState() != Player.STATE_PLAYING)
                player.play();
            return;
        }

        if (!playlist.equals(currentPlaylist) &&
                player.getMode() == Player.MODE_SHUFFLE) {
            player.setMode(Player.MODE_DEFAULT);
            musicQueue = new MusicQueue(playlist);
            player.setMusicQueue(musicQueue);
            player.setCurrentPlaylist(playlist);
        }
        else if (!playlist.equals(currentPlaylist)) {
            musicQueue = new MusicQueue(playlist);
            player.setMusicQueue(musicQueue);
            player.setCurrentPlaylist(playlist);
        }
        else if (player.getMode() == Player.MODE_SHUFFLE) {
            player.setMode(Player.MODE_DEFAULT);
            musicQueue = new MusicQueue(playlist);
            player.setMusicQueue(musicQueue);
        }

        musicQueue.setCurrentAudio(selected);

        player.play();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.AddButton) {
            Intent intent = new Intent(this, SelectAudioActivity.class);
            intent.putExtra(Playlist.class.getSimpleName(), playlist.id);
            startActivityForResult(intent, 1);
        }
        else if (v.getId() == R.id.ShufflePlayLayout) {
            playShuffle();
        }
        else if (v.getId() == R.id.PlaybarLayout) {
            Intent intent = new Intent(this, SongActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.ControlButton) {
            if (player.getState() == Player.STATE_PLAYING)
                player.stop();
            else
                player.play();
        }
        else if (v.getId() == R.id.NextSongButton) {
            player.skipToNext();
        }
        else if (v.getId() == R.id.PreviousSongButton) {
            player.skipToPrevious();
        }
    }

    private void playShuffle() {
        if (playlist.size() > 0) {
            MusicQueue musicQueue = player.getMusicQueue();

            if (!playlist.equals(player.getCurrentPlaylist())) {
                musicQueue = new MusicQueue(playlist);
                player.setMusicQueue(musicQueue);
                player.setCurrentPlaylist(playlist);
            }

            player.setMode(Player.MODE_SHUFFLE);
            musicQueue.shuffle();
            player.play();
        }
    }

    private void updatePlaylistFromIntent(Intent data) {
        Database database = Database.getInstance(this);

        long[] ids = data.getLongArrayExtra("Selected");
        playlist.clear();
        for (long id : ids) {
            AudioModel track = musicRepository.getAudioById(id);
            playlist.add(track);
        }

        Collections.sort(playlist, new AudioModel.AudioModelComparator());
        database.updatePlaylist(playlist);
    }

    public class UpdateSeekBarThread extends Thread {
        @Override
        public void run() {
            setName("UpdateSeekBarThread");

            while (activityExist()) {
                try {
                    playbarLayout.updateSeekBar(player.getTime(), player.getMusicQueue().getCurrent().getDuration());

                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
