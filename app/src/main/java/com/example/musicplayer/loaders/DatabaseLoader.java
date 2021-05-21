package com.example.musicplayer.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.musicplayer.Config;
import com.example.musicplayer.Database;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.Player;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;
import com.example.musicplayer.util.Playlist;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseLoader extends Loader {
    static final String TAG = DatabaseLoader.class.getSimpleName();

    private final Database database;

    public DatabaseLoader(Context context, Database database) {
        super(context);
        this.database = database;
    }

    public DatabaseLoader(Context context, Database database, int loaderType) {
        super(context, loaderType);
        this.database = database;
    }

    public DatabaseLoader(Context context, Database database, OnElementFoundCallback callback) {
        super(context, callback);
        this.database = database;
    }

    public DatabaseLoader(Context context, Database database, int loaderType, OnElementFoundCallback callback) {
        super(context, loaderType, callback);
        this.database = database;
    }

    @Override
    protected void loadAudio() {
        Cursor cursor = database.getAudio();
        OnElementFoundCallback callback = getCallback();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long duration = cursor.getLong(3);

            if (fileExist(cursor.getString(2))) {
                    long id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String path = cursor.getString(2);
                    String artist = cursor.getString(4);
                    String author = cursor.getString(5);
                    String album = cursor.getString(6);
                    boolean isFavorite = cursor.getInt(8) > 0;
                    Bitmap icon = null;

                    byte[] data = cursor.getBlob(7);
                    if (data != null) {
                        InputStream is = new ByteArrayInputStream(data);
                        icon = BitmapFactory.decodeStream(is);
                    }

                    audio.add(
                        callback.onTrackFound(
                            id,
                            title,
                            path,
                            artist,
                            author,
                            album,
                            duration,
                            isFavorite,
                            icon));
                }
                else {
                    database.deleteAudio(cursor.getInt(0));
                }
            }
        } else {
            Log.i(TAG, "You have no saved songs.");
        }

        if (cursor != null)
            cursor.close();

        Collections.sort(audio, new AudioModel.AudioModelComparator());
    }

    @Override
    protected void loadPlaylists() {
        OnElementFoundCallback callback = getCallback();

        List<AudioModel> favorite = new ArrayList<>();
        for (AudioModel a : audio)
            if (a.isFavorite())
                favorite.add(a);

        Playlist allAudioPlaylist = callback.onPlaylistFound(MusicRepository.ALL_AUDIO_PLAYLIST_ID, "all_songs", audio);
        Playlist favoritePlaylist = callback.onPlaylistFound(MusicRepository.FAVORITES_PLAYLIST_ID, "favorites", favorite);
        Playlist recentOpenedPlaylist = loadRecentOpenedPlaylist();

        playlists.add(allAudioPlaylist);
        playlists.add(favoritePlaylist);
        playlists.add(recentOpenedPlaylist);

        Cursor cursor = database.getPlaylists();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                List<AudioModel> list = new ArrayList<>();
                Cursor audioCursor = database.getPlaylistAudio(cursor.getInt(0));
                if (audioCursor != null) {
                    while (audioCursor.moveToNext()) {
                        AudioModel track = getAudioById(audioCursor.getInt(0));
                        if (track != null)
                            list.add(track);
                    }
                } else {
                    Log.i("AppTag", "You have no saved songs.");
                }

                Playlist playlist = callback.onPlaylistFound(cursor.getInt(0), cursor.getString(1), list);

                playlists.add(playlist);
            }
        } else {
            Log.i("AppTag", "You have no saved songs.");
        }

        for (Playlist playlist : playlists) {
            Collections.sort(playlist, new AudioModel.AudioModelComparator());
        }
        Collections.sort(playlists, new Playlist.PlaylistComparator());
    }

    @Override
    protected void loadPlayer() {
        player = new Player(context);
        SharedPreferences sPreferences = context.getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE);

        int playmode = sPreferences.getInt(Loader.PLAYER_MODE_KEY, Player.MODE_DEFAULT);
        int currentTime = sPreferences.getInt(Loader.CURRENT_TIME_KEY, 0);
        long currentPlaylistId = sPreferences.getLong(Loader.CURRENT_PLAYLIST_KEY, MusicRepository.ALL_AUDIO_PLAYLIST_ID);
        String rawMusicQueue = sPreferences.getString(Loader.MUSIC_QUEUE_KEY, "");

        Playlist currentPlaylist = getPlaylistById(currentPlaylistId);
        MusicQueue musicQueue;
        if (rawMusicQueue.length() > 0) {
            String[] musicQueueArray = rawMusicQueue.split(Config.MUSIC_QUEUE_DELIMITER);
            List<AudioModel> list = new ArrayList<>();
            for (String rawId : musicQueueArray) {
                int id = Integer.parseInt(rawId);
                AudioModel track = getAudioById(id);
                if (track != null)
                    list.add(track);
            }
            musicQueue = new MusicQueue(list);
        } else {
            musicQueue = new MusicQueue(currentPlaylist);
        }

        player.setMusicQueue(musicQueue);
        player.setCurrentPlaylist(currentPlaylist);
        player.setMode(playmode);
        player.setTime(currentTime);
    }

    private Playlist loadRecentOpenedPlaylist() {
        List<AudioModel> list = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE);
        String rawPlaylist = sp.getString(Loader.RECENT_OPENED_PLAYLIST_KEY, "");
        if (rawPlaylist.length() > 0) {
            String[] arr = rawPlaylist.split(Config.MUSIC_QUEUE_DELIMITER);
            for (String s : arr) {
                AudioModel track = getAudioById(Integer.parseInt(s));
                if (track != null)
                    list.add(track);
            }
        }

        return getCallback().onPlaylistFound(MusicRepository.RECENT_OPENED_PLAYLIST_ID, "recent_opened", list);
    }

    public AudioModel getAudioById(long id) {
        for (AudioModel a : audio)
            if (a.id == id)
                return a;
        return null;
    }
    public Playlist getPlaylistById(long id) {
        for (Playlist p : playlists)
            if (p.id == id)
                return p;
        return null;
    }

    private boolean fileExist(String fileName) {
        return new File(fileName).exists();
    }
}
