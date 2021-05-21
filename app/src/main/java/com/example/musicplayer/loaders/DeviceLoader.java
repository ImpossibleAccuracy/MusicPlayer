package com.example.musicplayer.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.example.musicplayer.Config;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.Player;
import com.example.musicplayer.util.MusicQueue;
import com.example.musicplayer.util.Playlist;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeviceLoader extends Loader {
    static final String TAG = DeviceLoader.class.getSimpleName();

    private static final String[] ForbiddenDirectories = {
        "Android",
        "WhatsApp"
    };

    private Playlist allAudioPlaylist;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public DeviceLoader(Context context) {
        super(context);
    }

    public DeviceLoader(Context context, int loaderType) {
        super(context, loaderType);
    }

    public DeviceLoader(Context context, OnElementFoundCallback callback) {
        super(context, callback);
    }

    public DeviceLoader(Context context, int loaderType, OnElementFoundCallback callback) {
        super(context, loaderType, callback);
    }

    @Override
    public void loadData() {
        audio = new ArrayList<>();
        playlists = new ArrayList<>();
        super.loadData();
    }

    @Override
    protected void loadAudio() {
        findAudio(Environment.getExternalStorageDirectory());
        waitExecutor();
        Collections.sort(audio, new AudioModel.AudioModelComparator());
    }

    @Override
    protected void loadPlaylists() {
        OnElementFoundCallback callback = getCallback();

        allAudioPlaylist = callback.onPlaylistFound(MusicRepository.ALL_AUDIO_PLAYLIST_ID, "all_songs", audio);
        Playlist favoritesPlaylist = callback.onPlaylistFound(MusicRepository.FAVORITES_PLAYLIST_ID, "favorites", null);
        Playlist recentOpenedPlaylist = callback.onPlaylistFound(
                MusicRepository.RECENT_OPENED_PLAYLIST_ID,
                "recent_opened",
                audio.isEmpty() ? null : Collections.singletonList(audio.get(0)));

        playlists.add(allAudioPlaylist);
        playlists.add(favoritesPlaylist);
        playlists.add(recentOpenedPlaylist);
        Collections.sort(playlists, new Playlist.PlaylistComparator());
    }

    @Override
    protected void loadPlayer() {
        player = new Player(context);
        if (!audio.isEmpty()) player.setMusicQueue(new MusicQueue(audio));
        player.setCurrentPlaylist(allAudioPlaylist);
        player.setMode(Player.MODE_DEFAULT);
    }

    private void waitExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {}
    }

    public void findAudio(File file) {
        if (file.isFile()) {
            if (isFileSuitable(file.getName())) {
                loadAudioFromFile(file);
            }
        }
        else if (file.isDirectory()) {
            String fileName = file.getName();
            boolean flag = false;
            for (String dir : ForbiddenDirectories) {
                if (fileName.equals(dir)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                File[] files = file.listFiles();
                if (files != null)
                for (File f : files) {
                    findAudio(f);
                }
            }
        }
    }

    private void loadAudioFromFile(File file) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getPath());

        String title = getTrackName(file, mmr);
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
        long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Bitmap icon = null;

        if (artist != null)
            artist = artist.trim();
        if (album != null)
            album = album.trim();
        if (author != null)
            author = author.trim();

        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            InputStream is = new ByteArrayInputStream(data);
            icon = BitmapFactory.decodeStream(is);
        }

        executor.execute(new LoadAudioThread(title, file.getAbsolutePath(), artist, album, author, duration, icon));
    }


    private static String getTrackName(File file, MediaMetadataRetriever mmr) {
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        if (title == null || title.equals("")) {
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                fileName = fileName.substring(0, i);
            }
            title = fileName.replaceAll("_", " ");
        }

        return title.trim();
    }

    private static boolean isFileSuitable(String fileName) {
        String fileExtension = getFileExtension(fileName);
        for (String extension : Config.FileExtensions) {
            if (extension.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private static String getFileExtension(String fileName) {
        String extension = null;

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }

        return extension;
    }

    private class LoadAudioThread implements Runnable {
        private final String title;
        private final String path;
        private final String artist;
        private final String album;
        private final String author;
        private final long duration;
        private final Bitmap icon;

        public LoadAudioThread(String title, String path, String artist, String album, String author, long duration, Bitmap icon) {
            this.title = title;
            this.path = path;
            this.artist = artist;
            this.album = album;
            this.author = author;
            this.duration = duration;
            this.icon = icon;
        }

        @Override
        public void run() {
            AudioModel track = getCallback().onTrackFound(
                Loader.UNKNOWN_TRACK_ID,
                title,
                path,
                artist,
                author,
                album,
                duration,
                false,
                icon
            );
            if (track != null) audio.add(track);
        }
    }
}
