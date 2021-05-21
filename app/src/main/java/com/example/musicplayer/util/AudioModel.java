package com.example.musicplayer.util;

import android.graphics.Bitmap;

import java.util.Comparator;
import java.util.Objects;

public final class AudioModel {
    public final long id;
    private String title;
    private String path;
    private String artist;
    private String author;
    private String album;
    private long duration;
    private Bitmap icon;
    private boolean isFavorite;

    public AudioModel(int id) {
        this.id = id;
        this.title = "";
        this.path = "";
        this.author = "";
        this.album = "";
        this.icon = null;
        this.isFavorite = false;
    }
    public AudioModel(long id, String title, String path, String artist, String author, String album, long duration, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.artist = artist;
        this.author = author;
        this.album = album;
        this.duration = duration;
        this.isFavorite = isFavorite;
        this.icon = null;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String name) {
        this.title = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getAuthor() {
        return author;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public String getArtist() {
        return artist;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public Bitmap getIcon() {
        return icon;
    }
    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return String.format("id: %s, name: %s, path: %s.", id, title, path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioModel that = (AudioModel) o;
        return id == that.id &&
            path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class AudioModelComparator implements Comparator<AudioModel> {
        @Override
        public int compare(AudioModel o1, AudioModel o2) {
            return (o1.getTitle().compareTo(o2.getTitle()));
        }
    }
}
