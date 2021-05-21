package com.example.musicplayer.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import com.example.musicplayer.Database;
import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.fragment.LyricsFragment;
import com.example.musicplayer.fragment.PlaylistFragment;
import com.example.musicplayer.fragment.TrackIconFragment;
import com.example.musicplayer.fragment.TrackInfoFragment;
import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.listener.OnItemSelectedListener;
import com.example.musicplayer.service.PlayerService;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;
import com.example.musicplayer.widget.ControlButton;
import com.example.musicplayer.widget.PageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongActivity extends AppActivity
        implements View.OnClickListener, OnItemSelectedListener {
    private View background;
    private SeekBar seekBar;
    private ViewPager viewPager;
    private View nextSongButton;
    private TextView songTitleView;
    private View previousSongButton;
    private TextView songArtistView;
    private TextView currentTimeView;
    private TextView songDurationView;
    private ControlButton controlButton;
    private ImageView makeFavoriteButton;
    private ImageView selectPlayModeButton;
    private PageView pageView;

    private LyricsFragment lyricsFragment;
    private PlaylistFragment playlistFragment;
    private TrackIconFragment trackIconFragment;
    private TrackInfoFragment trackInfoFragment;

    private int progress;
    private boolean seekBarTouched;
    private AudioModel currentAudio;
    private PlayerService.PlayerServiceBinder playerService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
            if (fromUser) {
                progress = p;

                long audioDuration = currentAudio.getDuration();
                int time = (int) (audioDuration / 100.0 * progress);
                currentTimeView.setText(timeToString(time));
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBarTouched = true;
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBarTouched = false;
            if (progress != -1) {
                long audioDuration = currentAudio.getDuration();
                int time = (int) (audioDuration / 100.0 * progress);

                Player player = getPlayer();
                player.setTime(time);
                player.play();

                executor.execute(playerService::updateNotification);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        if (getDeviceTheme() == THEME_DARK) {
            setWindowMode(MODE_TRANSPARENT);
            SetViewMargins(songTitleView, 0, getStatusBarHeight(), 0, 0);
            SetViewMargins(findViewById(R.id.nav_bg), 0, getNavigationBarHeight(), 0, 0);
        } else if (getDeviceTheme() == THEME_LIGHT) {
            setWindowMode(MODE_LIGHT);
        }

        playerService = ((MusicApplication)getApplication()).getPlayerService();

        Player player = getPlayer();
        currentAudio = player.getMusicQueue().getCurrent();

        viewPager.setAdapter(new MainPagerAdapter());
        viewPager.setCurrentItem(2);
        pageView.setPagesCount(viewPager.getAdapter().getCount());

        new UpdateSeekBarThread().start();

        player.registerCallback(new Callback() {
            @Override
            public void onCurrentAudioChanged(Player player, AudioModel track) {
                currentAudio = track;
                fillUI();
            }

            @Override
            public void onPlayerStateChanged(Player player, int state) {
                controlButton.update(state);
            }
        });
    }

    @Override
    protected void findUI() {
        seekBar = findViewById(R.id.SeekBar);
        viewPager = findViewById(R.id.ViewPager);
        background = findViewById(R.id.Background);
        controlButton = findViewById(R.id.ControlButton);
        songTitleView = findViewById(R.id.SongTitleView);
        nextSongButton = findViewById(R.id.NextSongButton);
        songArtistView = findViewById(R.id.SongArtistView);
        currentTimeView = findViewById(R.id.CurrentTimeView);
        songDurationView = findViewById(R.id.SongDurationView);
        makeFavoriteButton = findViewById(R.id.MakeFavoriteButton);
        previousSongButton = findViewById(R.id.PreviousSongButton);
        selectPlayModeButton = findViewById(R.id.SelectPlayModeButton);
        pageView = findViewById(R.id.PageView);

        nextSongButton.setOnClickListener(this);
        previousSongButton.setOnClickListener(this);
        makeFavoriteButton.setOnClickListener(this);
        selectPlayModeButton.setOnClickListener(this);
        controlButton.setOnClickListener(this);

        lyricsFragment = new LyricsFragment();
        playlistFragment = new PlaylistFragment();
        trackIconFragment = new TrackIconFragment();
        trackInfoFragment = new TrackInfoFragment();

        playlistFragment.setOnItemSelectedListener(this);

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                pageView.setCurrentPage(position + 1);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
    @Override
    protected void fillUI() {
        runOnUiThread(() -> {
            Player player = getPlayer();
            AudioModel audioModel = player.getMusicQueue().getCurrent();
            currentAudio = audioModel;

            trackIconFragment.loadIconFromAudioModel(audioModel);
            trackInfoFragment.updateFromTrack(audioModel);

            updateTitleSection(audioModel);
            updateTimeSection(audioModel, player);
            updatePlaymodeButton(player);
            updateBackground(audioModel);

            executor.execute(() -> {
                if (player.getMode() == Player.MODE_SHUFFLE) {
                    List<AudioModel> list = new ArrayList<>(getPlayer().getCurrentPlaylist());
                    Collections.sort(list, new AudioModel.AudioModelComparator());
                    playlistFragment.setPlaylist(list);
                } else {
                    playlistFragment.setPlaylist(getPlayer().getCurrentPlaylist());
                }
            });
        });
    }

    private void updateTitleSection(AudioModel track) {
        songTitleView.setText(track.getTitle());
        if (track.getArtist() == null || track.getArtist().length() == 0)
            songArtistView.setText(R.string.default_artist);
        else
            songArtistView.setText(track.getArtist());
    }
    private void updateTimeSection(AudioModel track, Player player) {
        currentTimeView.setText("00:00");
        controlButton.update(player.getState());
        songDurationView.setText(timeToString(track.getDuration()));
        makeFavoriteButton.setImageResource((track.isFavorite() ? R.drawable.icon_collect_selected : R.drawable.icon_collect_normal));
    }
    private void updatePlaymodeButton(Player player) {
        switch (player.getMode()) {
            case Player.MODE_DEFAULT:
                selectPlayModeButton.setImageResource(R.drawable.btn_order_normal);
                break;
            case Player.MODE_REPEAT_ONE:
                selectPlayModeButton.setImageResource(R.drawable.btn_loop_one);
                break;
            case Player.MODE_REPEAT_PLAYLIST:
                selectPlayModeButton.setImageResource(R.drawable.btn_loop);
                break;
            case Player.MODE_SHUFFLE:
                selectPlayModeButton.setImageResource(R.drawable.btn_shuffle);
                break;
        }
    }
    private void updateBackground(AudioModel track) {
        if (track.getIcon() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                int color = getResources().getColor(R.color.backgroundColor);
                window.setStatusBarColor(color);
                window.setNavigationBarColor(color);
            }
            background.setBackgroundResource(R.color.backgroundColor);
        }
        else {
            Palette.generateAsync(track.getIcon(), (Palette palette) -> {
                int color = palette.getVibrantColor(getResources().getColor(R.color.backgroundColor));
                int mixedColor = mixTwoColors(color, getResources().getColor(R.color.backgroundColor), 0.65f);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(mixedColor);
                    window.setNavigationBarColor(mixedColor);
                }
                background.setBackgroundColor(mixedColor);
            });
        }
    }

    private void changeTrackFavoriteStatus() {
        MusicRepository musicRepository = getMusicRepository();

        Playlist favoritePlaylist = musicRepository.getPlaylistById(MusicRepository.FAVORITES_PLAYLIST_ID);

        AudioModel audioModel = getPlayer().getMusicQueue().getCurrent();

        if (audioModel.isFavorite()) {
            audioModel.setFavorite(false);
            favoritePlaylist.remove(audioModel);
            runOnUiThread(() -> makeFavoriteButton.setImageResource(R.drawable.icon_collect_normal));
        } else {
            audioModel.setFavorite(true);
            favoritePlaylist.add(audioModel);
            runOnUiThread(() -> makeFavoriteButton.setImageResource(R.drawable.icon_collect_selected));
        }

        playerService.updateNotification();

        executor.execute(() -> {
            Database.getInstance(this).updateAudio(audioModel);
            Collections.sort(favoritePlaylist, new AudioModel.AudioModelComparator());
        });
    }

    private void skipToNextPlaymode() {
        Player player = getPlayer();
        MusicQueue musicQueue = player.getMusicQueue();

        switch (player.getMode()) {
            case Player.MODE_DEFAULT:
                player.setMode(Player.MODE_REPEAT_PLAYLIST);
                break;
            case Player.MODE_REPEAT_PLAYLIST:
                player.setMode(Player.MODE_REPEAT_ONE);
                break;
            case Player.MODE_REPEAT_ONE:
                player.setMode(Player.MODE_SHUFFLE);

                AudioModel temp = musicQueue.getCurrent();
                musicQueue.shuffle();
                musicQueue.setCurrentAudio(temp);
                break;
            case Player.MODE_SHUFFLE:
                player.setMode(Player.MODE_DEFAULT);

                temp = musicQueue.getCurrent();
                musicQueue = new MusicQueue(player.getCurrentPlaylist());
                musicQueue.setCurrentAudio(temp);
                player.setMusicQueue(musicQueue);
                break;
        }

        updatePlaymodeButton(player);
    }

    @Override
    public void onItemSelected(int item, AudioModel audioModel) {
        if (audioModel.equals(currentAudio))
            return;

        Player player = getPlayer();
        player.getMusicQueue().setCurrentAudio(audioModel);
        player.play();
    }

    @Override
    public void onClick(View v) {
        Player player = getPlayer();
        if (v.getId() == R.id.MakeFavoriteButton) {
            executor.execute(this::changeTrackFavoriteStatus);
        } else if (v.getId() == R.id.NextSongButton) {
            player.skipToNext();
            player.play();
        } else if (v.getId() == R.id.PreviousSongButton) {
            player.skipToPrevious();
            player.play();
        } else if (v.getId() == R.id.SelectPlayModeButton) {
            executor.execute(this::skipToNextPlaymode);
        } else if (v.getId() == R.id.ControlButton) {
            executor.execute(() -> {
                if (player.getState() == Player.STATE_PLAYING)
                    player.stop();
                else
                    player.play();
            });
        }
    }

    public static String timeToString(long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        if (seconds < 10 && minutes < 10)
            return String.format("0%s:0%s", minutes, seconds);
        else if (minutes < 10)
            return String.format("0%s:%s", minutes, seconds);
        else if (seconds < 10)
            return String.format("%s:0%s", minutes, seconds);

        return String.format("0%s:0%s", minutes, seconds);
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) +
                ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    public class MainPagerAdapter extends FragmentPagerAdapter {
        public MainPagerAdapter() {
            super(
                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            );
        }

        @Override
        public int getCount() {
            return 4;
        }
        @NotNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return lyricsFragment;
                case 2:
                    return trackIconFragment;
                case 3:
                    return playlistFragment;
                default:
                    return trackInfoFragment;
            }
        }
    }

    public class UpdateSeekBarThread extends Thread {
        @Override
        public void run() {
            setName("UpdateSeekBarThread");
            Player player = getPlayer();

            while (activityExist()) {
                try {
                    if (!seekBarTouched) {
                        int progress = (int) (player.getTime() * 100.0 / currentAudio.getDuration());
                        String label = timeToString(player.getTime());

                        runOnUiThread(() -> {
                            if (!seekBarTouched) {
                                seekBar.setProgress(progress);
                                currentTimeView.setText(label);
                            }
                        });
                    }

                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
