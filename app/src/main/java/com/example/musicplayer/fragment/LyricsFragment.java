package com.example.musicplayer.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;

public class LyricsFragment extends Fragment {
    public LyricsFragment() { /**/ }

    public static LyricsFragment newInstance() {
        LyricsFragment fragment = new LyricsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lyrics, container, false);



        return root;
    }
}
