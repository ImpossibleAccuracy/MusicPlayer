package com.example.musicplayer.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.musicplayer.activity.SongActivity;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class PlaybarLayout extends LinearLayout {
    private final View nextSongBtn;
    private final View previousSongBtn;
    private final TextView songName;
    private final TextView songArtist;
    private final CircularImageView songIcon;
    private final ControlButton controlButton;
    private final CircularProgressBar circularProgressBar;

    public PlaybarLayout(Context context) {
        this(context, null, 0);
    }
    public PlaybarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PlaybarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_playbar, this, true);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(10, 0, 10, 0);

        songIcon = findViewById(R.id.SongIcon);
        songName = findViewById(R.id.SongTitleView);
        songArtist = findViewById(R.id.SongArtistView);
        nextSongBtn = findViewById(R.id.NextSongButton);
        controlButton = findViewById(R.id.ControlButton);
        previousSongBtn = findViewById(R.id.PreviousSongButton);
        circularProgressBar = findViewById(R.id.CircularProgressBar);

        setOnClickListener((View view) -> {
            getContext().startActivity(new Intent(getContext(), SongActivity.class));
        });
    }

    public void setOnNextSongButtonClickListener(View.OnClickListener listener) {
        nextSongBtn.setOnClickListener(listener);
    }
    public void setOnPreviousSongButtonClickListener(View.OnClickListener listener) {
        previousSongBtn.setOnClickListener(listener);
    }
    public void setOnControlButtonClickListener(View.OnClickListener listener) {
        controlButton.setOnClickListener(listener);
    }

    public void update(AudioModel currentAudio, int state) {
        songName.setText(currentAudio.getTitle());
        if (currentAudio.getArtist() == null || currentAudio.getArtist().length() == 0)
            songArtist.setText(R.string.default_artist);
        else
            songArtist.setText(currentAudio.getArtist());

        if (currentAudio.getIcon() == null)
            songIcon.setImageResource(R.drawable.default_track_icon);
        else
            songIcon.setImageBitmap(currentAudio.getIcon());

        controlButton.update(state);
    }
    public void updateSeekBar(int currentTime, long duration) {
        float progress = (float) (currentTime * 1.0 / duration * 100);
        circularProgressBar.setProgress(progress);
    }
}
