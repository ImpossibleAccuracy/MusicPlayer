package com.example.musicplayer.util;

import com.example.musicplayer.exception.AudioNotFoundException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MusicQueue {
    private final LinkedList<AudioModel> list;

    public MusicQueue(List<AudioModel> list) {
        this.list = new LinkedList<>(list);
    }

    private void validate() {
        if (list.size() == 0)
            throw new AudioNotFoundException();
    }

    public AudioModel getCurrent() {
        validate();
        return list.peekFirst();
    }
    public AudioModel getNext() {
        validate();
        list.addLast(list.pollFirst());
        return getCurrent();
    }
    public AudioModel getPrevious() {
        validate();
        list.addFirst(list.pollLast());
        return getCurrent();
    }

    public AudioModel getFirst() {
        validate();
        return list.get(0);
    }
    public AudioModel getLast() {
        validate();
        return list.get(list.size() - 1);
    }

    public List<AudioModel> toList() {
        return list;
    }

    public void setCurrentAudio(AudioModel audio) {
        if (list.contains(audio)) {
            while (getCurrent() != audio) {
                getNext();
            }
        } else {
            list.add(audio);
        }
    }

    public void shuffle() {
        validate();

        Collections.shuffle(this.list);
    }
}
