package com.example.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.musicplayer.loaders.Loader;
import com.example.musicplayer.service.PlayerService;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;

import java.util.List;

public class MusicApplication extends Application {
    private Player player;
    private MusicRepository musicRepository;
    private PlayerService.PlayerServiceBinder playerService;

    @Override
    public void onCreate() {
        super.onCreate();
        // Toast.makeText(getApplicationContext(), "MusicApplication::onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Toast.makeText(getApplicationContext(), "MusicApplication::onTerminate", Toast.LENGTH_SHORT).show();
    }

    public void safeData() {
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(Loader.PLAYER_MODE_KEY, player.getMode());
        editor.putInt(Loader.CURRENT_TIME_KEY, player.getTime());
        editor.putLong(Loader.CURRENT_PLAYLIST_KEY, player.getCurrentPlaylist().id);
        editor.putString(Loader.MUSIC_QUEUE_KEY, ListToString(player.getMusicQueue().toList()));
        editor.putString(Loader.RECENT_OPENED_PLAYLIST_KEY,
            ListToString(musicRepository.getPlaylistById(MusicRepository.RECENT_OPENED_PLAYLIST_ID)));

        editor.apply();
    }

    public PlayerService.PlayerServiceBinder getPlayerService() {
        return playerService;
    }
    public void setPlayerService(PlayerService.PlayerServiceBinder playerService) {
        this.playerService = playerService;
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public MusicRepository getMusicRepository() {
        return musicRepository;
    }
    public void setMusicRepository(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public static String ListToString(List<AudioModel> list) {
        StringBuilder str = new StringBuilder();
        for (AudioModel track : list) {
            str.append(track.id);
            if (!track.equals(list.get(list.size() - 1))) {
                str.append(Config.MUSIC_QUEUE_DELIMITER);
            }
        }
        return str.toString();
    }
}
