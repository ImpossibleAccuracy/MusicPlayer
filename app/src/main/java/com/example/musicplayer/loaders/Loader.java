package com.example.musicplayer.loaders;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.Player;
import com.example.musicplayer.util.Playlist;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Loader {
    public static final int LOAD_ALL = 0;
    public static final int LOAD_ONLY_AUDIO = 1;
    public static final int LOAD_ONLY_PLAYLISTS = 3;
    public static final int LOAD_ONLY_PLAYER = 4;

    public static final long UNKNOWN_TRACK_ID = Integer.MAX_VALUE;

    public static final String MUSIC_QUEUE_KEY = "music_queue";
    public static final String CURRENT_PLAYLIST_KEY = "current_playlist";
    public static final String CURRENT_TIME_KEY = "current_time";
    public static final String PLAYER_MODE_KEY = "player_mode";
    public static final String RECENT_OPENED_PLAYLIST_KEY = "recent_opened_playlist";

    private final int loaderType;
    protected final Context context;
    protected Player player;
    protected List<AudioModel> audio;
    protected List<Playlist> playlists;

    private OnElementFoundCallback callback;

    public Loader(Context context) {
        this(context, LOAD_ALL, null);
    }
    public Loader(Context context, int loaderType) {
        this(context, loaderType, null);
    }
    public Loader(Context context, OnElementFoundCallback callback) {
        this(context, LOAD_ALL, callback);
    }
    public Loader(Context context, int loaderType, OnElementFoundCallback callback) {
        this.context = context;
        this.callback = callback;
        this.loaderType = loaderType;
    }

    public void setCallback(OnElementFoundCallback callback) {
        this.callback = callback;
    }
    protected OnElementFoundCallback getCallback() { return callback; }

    public void loadData() {
        audio = new ArrayList<>();
        playlists = new ArrayList<>();

        switch (loaderType) {
            case LOAD_ONLY_AUDIO:
                loadAudio();
                break;
            case LOAD_ONLY_PLAYLISTS:
                loadPlaylists();
                break;
            case LOAD_ONLY_PLAYER:
                loadPlayer();
                break;
            default:
                loadAudio();
                loadPlaylists();
                loadPlayer();
                break;
        }
    }

    protected abstract void loadAudio();
    protected abstract void loadPlaylists();
    protected abstract void loadPlayer();

    public Player getPlayer() { return player; }
    public List<AudioModel> getAudio() { return audio; }
    public List<Playlist> getPlaylists() { return playlists; }

    public static abstract class OnElementFoundCallback {
        @Nullable
        public AudioModel onTrackFound(long id,
            String title,
            String path,
            String artist,
            String author,
            String album,
            long duration,
            boolean isFavorite,
            @Nullable Bitmap icon) {
            return null;
        }

        @Nullable
        public Playlist onPlaylistFound(long id, String title, @Nullable List<AudioModel> audio) {
            return null;
        }
    }
}
