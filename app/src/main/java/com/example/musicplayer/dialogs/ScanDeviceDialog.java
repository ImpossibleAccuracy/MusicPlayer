package com.example.musicplayer.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.musicplayer.Database;
import com.example.musicplayer.R;
import com.example.musicplayer.loaders.DeviceLoader;
import com.example.musicplayer.loaders.Loader;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;

import java.util.Collections;
import java.util.List;

public class ScanDeviceDialog extends Dialog {
    private final TextView scanResultView;
    private final ProgressBar progressBar;
    private final LinearLayout scanResultParentView;

    private final Activity activity;
    private final MusicRepository musicRepository;

    public ScanDeviceDialog(Activity activity, MusicRepository musicRepository) {
        super(activity);
        this.activity = activity;
        this.musicRepository = musicRepository;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scan_device);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);

        progressBar = findViewById(R.id.ProgressBar);
        scanResultView = findViewById(R.id.ScanResult);
        scanResultParentView = findViewById(R.id.ScanResultParent);


        findViewById(R.id.cancel_button).setOnClickListener((View v) -> dismiss());

        new Thread(new RefreshAudioListThread()).start();
    }

    private class RefreshAudioListThread extends Thread {
        @Override
        public void run() {
            Loader loader = new DeviceLoader(getContext(), Loader.LOAD_ONLY_AUDIO);

            final Playlist allAudioPlaylist = musicRepository.getPlaylistById(MusicRepository.ALL_AUDIO_PLAYLIST_ID);

            loader.setCallback(new Loader.OnElementFoundCallback() {
                public final List<AudioModel> existed = musicRepository.getAllAudio();

                @Nullable
                @Override
                public AudioModel onTrackFound(long id, String title, String path, String artist, String author, String album, long duration, boolean isFavorite, @Nullable Bitmap icon) {
                    Database database = Database.getInstance(getContext());

                    if (isNewTrack(path)) {
                        id = database.insertAudio(
                                title,
                                path,
                                artist,
                                author,
                                album,
                                duration,
                                isFavorite,
                                icon);

                        AudioModel track = new AudioModel(id,
                                title, path, artist, author, album, duration, isFavorite);
                        track.setIcon(icon);
                        allAudioPlaylist.add(track);

                        return track;
                    }

                    return null;
                }

                public boolean isNewTrack(String path) {
                    for (AudioModel a : existed) {
                        if (a.getPath().equals(path))
                            return false;
                    }

                    return true;
                }
            });

            loader.loadData();
            List<AudioModel> loaded = loader.getAudio();
            musicRepository.addAllAudio(loaded);
            Collections.sort(allAudioPlaylist, new AudioModel.AudioModelComparator());

            String msg;
            if (loaded.size() > 0)
                msg = String.format(getContext().getString(R.string.scan_result_success), loaded.size());
            else
                msg = getContext().getString(R.string.scan_result_fail);

            activity.runOnUiThread(() -> {
                scanResultView.setText(msg);
                progressBar.setVisibility(View.GONE);
                scanResultParentView.setVisibility(View.VISIBLE);
            });
        }
    }
}
