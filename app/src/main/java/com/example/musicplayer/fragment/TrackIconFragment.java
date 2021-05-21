package com.example.musicplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.R;

public class TrackIconFragment extends Fragment {
    private ImageView songIcon;
    private AudioModel audioModel;

    public TrackIconFragment() {
        super();
    }

    public void loadIconFromAudioModel(AudioModel audioModel) {
        if (songIcon == null) {
            View root = getView();
            if (root == null) {
                this.audioModel = audioModel;
                return;
            }

            songIcon = root.findViewById(R.id.SongIcon);
        }

        if (audioModel.getIcon() == null) {
            songIcon.setImageResource(R.drawable.default_track_icon);
        }
        else {
            songIcon.setImageBitmap(audioModel.getIcon());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_track_icon, container, false);

        songIcon = root.findViewById(R.id.SongIcon);
        if (audioModel != null)
            loadIconFromAudioModel(audioModel);

        return root;
    }
}
