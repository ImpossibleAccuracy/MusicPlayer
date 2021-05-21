package com.example.musicplayer.util;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    public static final int ALL_AUDIO_PLAYLIST_ID = -3;
    public static final int FAVORITES_PLAYLIST_ID = -2;
    public static final int RECENT_OPENED_PLAYLIST_ID = -1;

    private List<AudioModel> allAudio;
    private List<Playlist> playlists;

    public MusicRepository() {
        allAudio = new ArrayList<>();
        playlists = new ArrayList<>();
    }

    public void addAudio(AudioModel track) {
        allAudio.add(track);
    }
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }
    public void addAllAudio(List<AudioModel> audioModels) {
        allAudio.addAll(audioModels);
    }
    public void addAllPlaylists(List<Playlist> playlistList) {
        playlists.addAll(playlistList);
    }

    public void removeAudio(AudioModel track) {
        allAudio.remove(track);
    }
    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
    }

    public AudioModel getAudioById(long id) {
        for (AudioModel a : allAudio)
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

    public List<AudioModel> getAllAudio() {
        return allAudio;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }
}
