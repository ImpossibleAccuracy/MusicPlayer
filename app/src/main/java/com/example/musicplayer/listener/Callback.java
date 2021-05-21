package com.example.musicplayer.listener;

import com.example.musicplayer.Player;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;

public abstract class Callback {
    public void onPlayerStateChanged(Player player, int state) {}
    public void onMusicQueueUpdated(Player player, MusicQueue musicQueue) {}
    public void onCurrentAudioChanged(Player player, AudioModel playingAudio) {}
    public void onTimeTouched(Player player, int msec) {}
}
