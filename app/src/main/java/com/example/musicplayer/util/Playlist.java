package com.example.musicplayer.util;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Playlist extends ArrayList<AudioModel> implements Comparable<Playlist> {
    private static final long serialVersionUID = 131196835428427589L;

    public final long id;
    private String name;
    protected static int objectsCount = 0;

    public Playlist(String name) {
        this.name = name;

        id = ++objectsCount;
    }
    public Playlist(long id, String name) {
        this.id = id;
        this.name = name;
        objectsCount++;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public AudioModel getFirst() {
        return get(0);
    }
    public AudioModel getLast() {
        return get(size() - 1);
    }

    public boolean contains(int id) {
        for (AudioModel track : this) {
            if (track.id == id)
                return true;
        }
        return false;
    }

    @Override
    public boolean add(AudioModel audioModel) {
        if (contains(audioModel))
            return false;
        return super.add(audioModel);
    }

    @Override
    public int compareTo(Playlist o) {
        return (this.name.compareTo(o.name));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Playlist playlist = (Playlist) o;
        return id == playlist.id &&
                name.equals(playlist.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name);
    }

    @Override
    public String toString() {
        return String.format("id: %s, name: %s", id, name);
    }

    public static class PlaylistComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist o1, Playlist o2) {
            return o1.compareTo(o2);
        }
    }
}
