package com.example.musicplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.musicplayer.R;
import com.example.musicplayer.util.AudioModel;

public class TrackInfoFragment extends Fragment {
    private AudioModel track;

    private TextView trackTitleView,
        trackPathView,
        trackArtistView,
        trackAuthorView,
        trackAlbumView;

    public TrackInfoFragment() { /**/ }

    public void updateFromTrack(AudioModel track) {
        this.track = track;

        if (trackTitleView == null)
            return;

        trackTitleView.setText(track.getTitle());
        trackPathView.setText(track.getPath());

        String artist = track.getArtist();
        String author = track.getAuthor();
        String album = track.getAlbum();

        if (artist != null && !artist.equals(""))
            trackArtistView.setText(artist);
        else
            trackArtistView.setText(R.string.default_artist);

        if (author != null && !author.equals(""))
            trackAuthorView.setText(author);
        else
            trackAuthorView.setText(R.string.default_author);

        if (album != null && !album.equals(""))
            trackAlbumView.setText(album);
        else
            trackAlbumView.setText(R.string.default_album);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_track_info, container, false);

        trackTitleView = root.findViewById(R.id.TrackTitleView);
        trackPathView = root.findViewById(R.id.TrackPathView);
        trackArtistView = root.findViewById(R.id.TrackArtistView);
        trackAuthorView = root.findViewById(R.id.TrackAuthorView);
        trackAlbumView = root.findViewById(R.id.TrackAlbumView);

        if (track != null)
            updateFromTrack(track);

        return root;
    }
}
