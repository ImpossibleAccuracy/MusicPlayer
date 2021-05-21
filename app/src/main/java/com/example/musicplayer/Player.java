package com.example.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;
import com.example.musicplayer.util.Playlist;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public static final int STATE_STOPPED = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_REPEAT_ONE = 1;
    public static final int MODE_REPEAT_PLAYLIST = 2;
    public static final int MODE_SHUFFLE = 3;

    private int mode;
    private int state;
    private MusicQueue musicQueue;
    private Playlist currentPlaylist;

    private final Context context;
    private AudioModel playingAudio;
    private MediaPlayer mediaPlayer;

    private final List<Callback> callbacks;

    public Player(@NotNull Context context) {
        this.context = context;
        this.callbacks = new ArrayList<>();
    }
    public synchronized void release() {
        mediaPlayer.release();
    }
    public synchronized void recreate() {
        if (mediaPlayer != null) {
            synchronized (mediaPlayer) {
                mediaPlayer.release();
            }
        }

        mediaPlayer = MediaPlayer.create(context, Uri.parse(playingAudio.getPath()));
        mediaPlayer.setOnCompletionListener((MediaPlayer mp) -> {
            switch (mode) {
                case MODE_REPEAT_PLAYLIST:
                case MODE_SHUFFLE:
                    skipToNext();
                    break;
                case MODE_REPEAT_ONE:
                    mp.seekTo(0);
                    play();
                    break;
                case MODE_DEFAULT:
                    if (currentPlaylist != null)
                        if (!musicQueue.getCurrent().equals(currentPlaylist.getLast()))
                            skipToNext();
                        else {
                            mp.seekTo(0);
                            pause();
                        }
                    break;
            }
        });

        state = STATE_STOPPED;

        for (Callback listener : callbacks) {
            listener.onPlayerStateChanged(this, state);
        }
    }

    private synchronized boolean prepareToPlay(AudioModel audioModel) {
        if (!audioModel.equals(playingAudio)) {
            playingAudio = audioModel;

            if (mediaPlayer != null) {
                synchronized (mediaPlayer) {
                    mediaPlayer.release();
                }
            }

            mediaPlayer = MediaPlayer.create(context, Uri.parse(audioModel.getPath()));
            mediaPlayer.setOnCompletionListener((MediaPlayer mp) -> {
                switch (mode) {
                    case MODE_REPEAT_PLAYLIST:
                    case MODE_SHUFFLE:
                        skipToNext();
                        break;
                    case MODE_REPEAT_ONE:
                        mp.seekTo(0);
                        play();
                        break;
                    case MODE_DEFAULT:
                        if (currentPlaylist != null)
                            if (!musicQueue.getCurrent().equals(currentPlaylist.getLast()))
                                skipToNext();
                            else {
                                mp.seekTo(0);
                                pause();
                            }
                        break;
                }
            });

            if (state == STATE_PLAYING)
                mediaPlayer.start();

            for (Callback listener : callbacks) {
                listener.onCurrentAudioChanged(this, playingAudio);
            }

            return true;
        }
        return false;
    }

    public void play() {
        if (prepareToPlay(musicQueue.getCurrent()) || state != STATE_PLAYING) {
            synchronized (mediaPlayer) {
                mediaPlayer.start();
            }
            state = STATE_PLAYING;

            for (Callback listener : callbacks) {
                listener.onPlayerStateChanged(this, state);
            }
        }
    }
    public void pause() {
        if (prepareToPlay(musicQueue.getCurrent()) || state != STATE_PAUSED) {
            synchronized (mediaPlayer) {
                mediaPlayer.pause();
            }
            state = STATE_PAUSED;

            for (Callback listener : callbacks) {
                listener.onPlayerStateChanged(this, state);
            }
        }
    }
    public void stop() {
        if (prepareToPlay(musicQueue.getCurrent()) || state != STATE_STOPPED) {
            synchronized (mediaPlayer) {
                mediaPlayer.pause();
            }
            state = STATE_STOPPED;

            for (Callback listener : callbacks) {
                listener.onPlayerStateChanged(this, state);
            }
        }
    }

    public void skipToNext() {
        prepareToPlay(musicQueue.getNext());
    }
    public void skipToPrevious() {
        prepareToPlay(musicQueue.getPrevious());
    }

    public void setVolume(float leftVolume, float rightVolume) {
        synchronized (mediaPlayer) {
            mediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    public synchronized int getTime() {
        synchronized (mediaPlayer) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) { /**/ }
        }
        return 0;
    }
    public synchronized void setTime(int msec) {
        synchronized (mediaPlayer) {
            try {
                mediaPlayer.seekTo(msec);
            } catch (IllegalStateException e) { /**/ }
        }
        for (Callback listener : callbacks) {
            listener.onTimeTouched(this, msec);
        }
    }

    public int getMode() {
        return mode;
    }
    public synchronized void setMode(int mode) {
        this.mode = mode;
    }

    public int getState() {
        return state;
    }
    public synchronized void setState(int state) {
        this.state = state;
    }

    public MusicQueue getMusicQueue() {
        return musicQueue;
    }
    public synchronized void setMusicQueue(@NotNull MusicQueue musicQueue) {
        if (!musicQueue.equals(this.musicQueue)) {
            this.musicQueue = musicQueue;
            synchronized (this) {
                for (Callback listener : callbacks) {
                    listener.onMusicQueueUpdated(this, musicQueue);
                }
            }
            prepareToPlay(musicQueue.getCurrent());
        }
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }
    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public synchronized void registerCallback(@NotNull Callback listener) {
        callbacks.add(listener);
    }
    public synchronized void unregisterCallback(Callback listener) {
        callbacks.remove(listener);
    }
}
