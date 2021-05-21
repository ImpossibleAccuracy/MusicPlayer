package com.example.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import com.example.musicplayer.Database;
import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicQueue;

import static androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent;

public class PlayerService extends Service {
    static final String TAG = PlayerService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 200;
    private static final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
        | PlaybackStateCompat.ACTION_STOP
        | PlaybackStateCompat.ACTION_PAUSE
        | PlaybackStateCompat.ACTION_PLAY_PAUSE
        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        | PlaybackStateCompat.ACTION_SEEK_TO
        | PlaybackStateCompat.ACTION_SET_RATING
    );

    private Player player;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;
    private int currentState = PlaybackStateCompat.STATE_STOPPED;
    private MusicApplication application;
    private boolean receiverRegistered;

    private final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            player.play();
        }

        @Override
        public void onPause() {
            player.pause();
        }

        @Override
        public void onStop() {
            player.stop();
        }

        @Override
        public void onSkipToNext() {
            player.skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            player.skipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            player.setTime((int) pos);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            AudioModel track = player.getMusicQueue().getCurrent();
            track.setFavorite(rating.equals(RatingCompat.newHeartRating(true)));
            Database.getInstance(getApplicationContext()).updateAudio(track);
        }
    };

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                player.play();
                player.setVolume(1f, 1f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                player.setVolume(0.2f, 0.2f);
                break;
            default:
                player.pause();
                break;
        }
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };

    private final Callback playerCallback = new Callback() {
        @Override
        public void onPlayerStateChanged(Player player, int state) {
            if (state == Player.STATE_PLAYING) {
                play();
            }
            else if (state == Player.STATE_PAUSED) {
                pause();
            }
            else if (state == Player.STATE_STOPPED) {
                stop();
            }
        }

        @Override
        public void onMusicQueueUpdated(Player player, MusicQueue musicQueue) {

        }

        @Override
        public void onCurrentAudioChanged(Player player, AudioModel playingAudio) {
            updateMetadataFromTrack(playingAudio);
            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onTimeTouched(Player player, int msec) {
            refreshNotificationAndForegroundStatus(currentState);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        application = ((MusicApplication) getApplication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_DEFAULT_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build();
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        Intent activityIntent = new Intent(appContext, MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, 0));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, 0));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    private void play() {
        if (currentState == PlaybackStateCompat.STATE_PLAYING)
            return;

        startService(new Intent(getApplicationContext(), PlayerService.class));

        AudioModel track = player.getMusicQueue().getCurrent();
        updateMetadataFromTrack(track);

        if (!audioFocusRequested) {
            audioFocusRequested = true;

            int audioFocusResult;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return;
        }

        mediaSession.setActive(true);

        if (!receiverRegistered) {
            registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            receiverRegistered = true;
        }

        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

        currentState = PlaybackStateCompat.STATE_PLAYING;
        refreshNotificationAndForegroundStatus(currentState);
    }
    private void pause() {
        if (currentState == PlaybackStateCompat.STATE_PAUSED)
            return;

        if (receiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            receiverRegistered = false;
        }

        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

        currentState = PlaybackStateCompat.STATE_PAUSED;
        refreshNotificationAndForegroundStatus(currentState);

        application.safeData();
    }
    private void stop() {
        if (currentState == PlaybackStateCompat.STATE_STOPPED)
            return;

        if (receiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            receiverRegistered = false;
        }

        if (audioFocusRequested) {
            audioFocusRequested = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
        }

        mediaSession.setActive(false);

        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

        currentState = PlaybackStateCompat.STATE_STOPPED;
        refreshNotificationAndForegroundStatus(currentState);

        stopSelf();

        application.safeData();
    }

    private Notification getNotification(int playbackState) {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession);

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getMusicQueue().getCurrent().getDuration());
        mediaSession.setMetadata(metadataBuilder.build());

        stateBuilder.setState(currentState, player.getTime(), 1f);
        mediaSession.setPlaybackState(stateBuilder.build());

        builder.addAction(
            new NotificationCompat.Action(
                android.R.drawable.ic_media_previous,
                getString(R.string.previous),
                buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

        NotificationCompat.Action action;
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            action = new NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE));
        else
            action = new NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.play),
                buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE));
        builder.addAction(action);

        builder.addAction(
            new NotificationCompat.Action(
                android.R.drawable.ic_media_next,
                getString(R.string.next),
                buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

        builder.addAction(
            new NotificationCompat.Action(
                R.drawable.ic_media_stop,
                getString(R.string.stop),
                buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)));

        MediaStyle style = new MediaStyle();
        style.setShowActionsInCompactView(1);
        style.setShowCancelButton(false);
        style.setCancelButtonIntent(buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP));
        style.setMediaSession(mediaSession.getSessionToken());
        builder.setStyle(style);

        builder.setSmallIcon(R.drawable.app_notification_icon);
        builder.setColor(ContextCompat.getColor(this, R.color.backgroundColor));
        builder.setShowWhen(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID);
        builder.setSound(null, AudioManager.STREAM_MUSIC);

        return builder.build();
    }
    private void updateMetadataFromTrack(AudioModel track) {
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, track.getIcon());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbum());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, track.getAuthor());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
        metadataBuilder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, RatingCompat.newHeartRating(track.isFavorite()));
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getDuration());
        mediaSession.setMetadata(metadataBuilder.build());
    }
    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                NotificationManagerCompat.from(PlayerService.this).notify(NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);
                break;
            }
            default: {
                stopForeground(true);
                break;
            }
        }
    }

    public class PlayerServiceBinder extends Binder {
        public void setPlayer(Player player) {
            PlayerService.this.player = player;

            player.registerCallback(playerCallback);

            updateMetadataFromTrack(player.getMusicQueue().getCurrent());
            refreshNotificationAndForegroundStatus(Player.STATE_PAUSED);
        }

        public void updateNotification() {
            updateMetadataFromTrack(player.getMusicQueue().getCurrent());
            refreshNotificationAndForegroundStatus(currentState);
        }

        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }
}
