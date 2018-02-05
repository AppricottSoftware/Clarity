package appricottsoftware.clarity.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.sync.ClarityApp;

public class PlayerService extends MediaBrowserServiceCompat {

    private static final String TAG = "PlayerService";

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String NOTIFICATION_CHANNEL_ID = "clarity_channel_id";
    private static final int NOTIFICATION_ID = 1;

    private AudioManager.OnAudioFocusChangeListener afChangeListener;
//    TODO: private BecomingNoisyReceiver myNoisyAudioStreamReceiver;
    private PlaybackReceiver playbackReceiver;
    private Notification notification;
    private NotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaPlayer mediaPlayer;
    private SimpleExoPlayer exoMediaPlayer;
    private PlayerCallback playerCallback;
    private PlayerService mediaService;
    private IntentFilter intentFilter;
    private NotificationReceiver notificationReceiver;
    private Context context;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaService = this;
        context = getApplicationContext();
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        // Acquire a lock so CPU stays on even when screen is locked
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.player_service_wake_lock));
        wakeLock.acquire();

        // Acquire a wifi lock so wifi doesn't disconnect
        wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, getString(R.string.player_service_wifi_lock));
        wifiLock.acquire();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Initialize media player
        initializeExoMediaPlayer();

        // Create broadcast receiver
        playbackReceiver = new PlaybackReceiver(this);

        // Create media session
        mediaSession = new MediaSessionCompat(context, TAG);

        // Enable callbacks from media buttons and transport controls
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial playback state so media buttons start player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        mediaSession.setPlaybackState(stateBuilder.build());

        // Handle callbacks from media controller
        playerCallback = new PlayerCallback(this);
        mediaSession.setCallback(playerCallback);

        // Set session token to communicate with activities
        setSessionToken(mediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // TODO: Control level of access for packages
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        // Calling package is not whitelisted (invalid)
        if(TextUtils.equals(EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // TODO: assume audio catalog is already loaded/cached
        List<MediaBrowserCompat.MediaItem> mediaItems= new ArrayList<>();

        // TODO: check if this is root menu
        if(MEDIA_ROOT_ID.equals(parentId)) {
            // TODO: build mediaitem objects for top level, put into mediaItems
        } else {
            // Examine parentid to see what submenu, put children of submenu in mediaItems
        }
        result.sendResult(mediaItems);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(playbackReceiver);
        } catch(Exception e) {
            e.printStackTrace();
        }
        exoMediaPlayer.release();
        wakeLock.release();
        wifiLock.release();
        stopForeground(true);
        super.onDestroy();
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if(exoMediaPlayer.getPlaybackState() == PlaybackStateCompat.STATE_PLAYING) {
            Log.d(TAG, "getAvailableActions: mediaPlayer isPlaying()");
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            Log.d(TAG, "getAvailableActions: mediaPlayer !isPlaying()");
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    private void initializeExoMediaPlayer() {
        exoMediaPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        exoMediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }

            @Override
            public void onLoadingChanged(boolean isLoading) { }

            @Override
            public void onPlayerError(ExoPlaybackException error) { }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

            @Override
            public void onSeekProcessed() { }

            @Override
            public void onPositionDiscontinuity(int reason) { }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                // Push the state to the media session when the audio starts or stops playing
                if(playWhenReady && playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    playbackState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    playbackState = PlaybackState.STATE_PAUSED;
                }
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                        .setActions(getAvailableActions());
                stateBuilder.setState(playbackState, exoMediaPlayer.getCurrentPosition(), 1.0f, exoMediaPlayer.getDuration());
                mediaSession.setPlaybackState(stateBuilder.build());
                // Set the notification
                setNotification(playbackState);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) { }

            @Override
            public void onRepeatModeChanged(int repeatMode) { }
        });

    }

    private void setNotification(int playbackState) {
        try {
            MediaControllerCompat controller = mediaSession.getController();
            MediaMetadataCompat metadata = controller.getMetadata();
            MediaDescriptionCompat description = metadata.getDescription();

            // TODO: make the notification update
            int rDrawable = R.drawable.ic_player_pause;
            String actionState = getString(R.string.player_service_pause);
            int playState = PlaybackStateCompat.STATE_PAUSED;
            if(playbackState != PlaybackStateCompat.STATE_PLAYING) {
                rDrawable = R.drawable.ic_player_play;
                actionState = getString(R.string.player_service_play);
                playState = PlaybackStateCompat.STATE_PLAYING;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder // Metadata on notification bar
                    .setContentTitle(description.getTitle())
                    .setContentText(description.getDescription())
                    .setSubText(description.getSubtitle())
                    .setLargeIcon(description.getIconBitmap())

                    // Launch activity by clicking notification
                    .setContentIntent(controller.getSessionActivity())

                    // Swipe notification away to stop service
                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_STOP))

                    // Show notification on lock screen
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                    // App icon + color
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setColor(ContextCompat.getColor(context,
                            android.R.color.white))

                    // Pause button stops playback
                    .addAction(new NotificationCompat.Action(rDrawable,
                            actionState,
                            MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                    playState)))

                    // Use MediaStyle features
                    .setStyle(new MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0));

            // Show notification and put service in foreground
//            if(notification == null) {
                notification = builder.build();

                startForeground(NOTIFICATION_ID, notification);
//            }
//            else {

//            }


            Log.e(TAG, "Success notification");
        } catch(Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    //TODO: Decouple from PlayerService
    // Callback class
    public class PlayerCallback extends MediaSessionCompat.Callback {
        // TODO: Check if audio is playing before play/pause

        private PlayerService playerService;

        public PlayerCallback(PlayerService playerService) {
            this.playerService = playerService;
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for playback
            int result = audioManager.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(new Intent(context, PlayerService.class));

                // Set the session active
                mediaSession.setActive(true);

                // Register the broadcast receiver
                registerReceiver(playbackReceiver, intentFilter);
            }
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay");

            // Start the player
            exoMediaPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause");

            // Pause the player
            exoMediaPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onStop() {
            // Remove audio focus
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(afChangeListener);

            // Unregister broadcast receiver
            try {
                unregisterReceiver(playbackReceiver);
            } catch(Exception e) {
                Log.e(TAG, e.toString());
            }

            // Stop the player
            exoMediaPlayer.stop();

            // Set the session inactive
            mediaSession.setActive(false);

            // Stop notification, take the service out of the foreground
            stopForeground(false);
        }
//
//        public void handlePlayRequest() {
////            MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
//            if (currentMusic != null) {
//                mServiceCallback.onPlaybackStart();
//                mPlayback.play(currentMusic);
//            }
//        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.e(TAG, extras.toString());
            Episode episode = null;
            try {
                episode = ClarityApp.getGson().fromJson(mediaId, Episode.class);

                // Play the audio by creating a media source from the audio URL
                String userAgent = Util.getUserAgent(context, getString(R.string.app_name));
                DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent,
                        null,
                        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                        true /* allow cross protocol redirects for feedproxy*/);
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                        null,
                        httpDataSourceFactory);
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource audioSource = new ExtractorMediaSource(Uri.parse(episode.getAudio()), dataSourceFactory, extractorsFactory, null, null);
                exoMediaPlayer.prepare(audioSource);
                exoMediaPlayer.setPlayWhenReady(true);

                // Update the metadata for this session
                mediaSession.setMetadata(episode.toMediaMetadataCompat());

                Log.e("PlayerFragment", "Notification successful");
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
//            setNotification();

            super.onPlayFromSearch(query, extras);
        }
    }
}
