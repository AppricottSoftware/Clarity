package appricottsoftware.clarity.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
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
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

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
    private PlaybackReceiver playbackReceiver;
    private Notification notification;
    private NotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private SimpleExoPlayer exoMediaPlayer;
    private PlayerCallback playerCallback;
    private IntentFilter intentFilter;
    private Context context;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    private Handler playbackStateHandler;
    private Runnable playbackStateRunnable;
    private DynamicConcatenatingMediaSource dynamicConcatenatingMediaSource;

    @Override
    public void onCreate() {
        super.onCreate();
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

        // TODO: use this to update notification
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
        playerCallback = new PlayerCallback();
        mediaSession.setCallback(playerCallback);

        // Set session token to communicate with activities
        setSessionToken(mediaSession.getSessionToken());

        // Initialize the handler to sync with the seekbar
        playbackStateHandler = new Handler();
        playbackStateRunnable = new Runnable() {
            @Override
            public void run() {
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                        .setActions(getAvailableActions())
                        .setBufferedPosition(exoMediaPlayer.getDuration())
                        .setState(PlaybackStateCompat.STATE_PLAYING, exoMediaPlayer.getCurrentPosition(), 1.0f, exoMediaPlayer.getDuration());
                mediaSession.setPlaybackState(stateBuilder.build());
                Log.i(TAG, "playbackStateRunnable: currentPosition: " + exoMediaPlayer.getCurrentPosition() / 1000 + " duration: " + exoMediaPlayer.getDuration() / 1000);
                playbackStateHandler.postDelayed(this, 1000);
            }
        };

        // TODO: Attach the media player to the media session
        dynamicConcatenatingMediaSource = new DynamicConcatenatingMediaSource();
//        exoMediaPlayer.prepare(dynamicConcatenatingMediaSource, false, false);
        // MediaSessionConnector mediaSessionConnector= new MediaSessionConnector(mediaSession);
        // mediaSessionConnector.setPlayer(exoMediaPlayer, null, null);
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

        // TODO: Assume audio catalog is already loaded/cached
        List<MediaBrowserCompat.MediaItem> mediaItems= new ArrayList<>();

        // TODO: Check if this is root menu
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
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.i(TAG, "onTracksChanged: trackGroups:" + trackGroups.toString() + " trackSelections: " + trackSelections.toString());
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.i(TAG, "onLoadingChanged: isLoading: " + isLoading);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: " + error.toString());
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.i(TAG, "onPlaybackParametersChanged: " + playbackParameters.toString());
            }

            @Override
            public void onSeekProcessed() {
                Log.d(TAG, "onSeekProcessed");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                String r = "";
                switch(reason) {
                    case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                        r = "0: DISCONTINUITY_REASON_PERIOD_TRANSITION"; break;
                    case Player.DISCONTINUITY_REASON_SEEK:
                        r = "1: DISCONTINUITY_REASON_SEEK"; break;
                    case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                        r = "2: DISCONTINUITY_REASON_SEEK_ADJUSTMENT"; break;
                    case Player.DISCONTINUITY_REASON_INTERNAL:
                        r = "3: DISCONTINUITY_REASON_INTERNAL"; break;
                    default:
                        r = " NO REASON DETECTED";
                }
                Log.i(TAG, "onPositionDiscontinuity: " + r);
                // TODO: Signal next element in playlist started
                /*
                    Called when a position discontinuity occurs without a change to the timeline. A position discontinuity occurs when the current window or period index changes (as a result of playback transitioning from one period in the timeline to the next), or when the playback position jumps within the period currently being played (as a result of a seek being performed, or when the source introduces a discontinuity internally).
                    When a position discontinuity occurs as a result of a change to the timeline this method is not called. onTimelineChanged(Timeline, Object) is called in this case.
                 */
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: playWhenReady: " + playWhenReady + " playbackState: " + getStateChanged(playbackState));
                // Push the state to the media session when the audio starts or stops playing
                if(playWhenReady && playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    playbackStateHandler.post(playbackStateRunnable);
                } else {
                    playbackStateHandler.removeCallbacks(playbackStateRunnable);
                    PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                            .setActions(getAvailableActions())
                            .setBufferedPosition(exoMediaPlayer.getDuration())
                            .setState(PlaybackStateCompat.STATE_PAUSED,
                                    exoMediaPlayer.getCurrentPosition(),
                                    1.0f,
                                    exoMediaPlayer.getDuration());
                    Log.i(TAG, "onPlayerStateChanged: NOT PLAYING currentPosition: "  + exoMediaPlayer.getCurrentPosition() / 1000 + " duration: " + exoMediaPlayer.getDuration() / 1000);
                    mediaSession.setPlaybackState(stateBuilder.build());
                }
                // TODO: Set the notification
//                setNotification(playbackState);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.i(TAG, "onShuffleModeEnabledChanged: " + shuffleModeEnabled);
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                if(timeline != null && manifest != null) {
                    Log.i(TAG, "onTimelineChanged: timeline: " + timeline.toString() + " manifest: " + manifest.toString());
                } else {
                    Log.i(TAG, "onTimelineChanged");
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                String r = "";
                switch(repeatMode) {
                    case Player.REPEAT_MODE_OFF:
                        r = "0: REPEAT_MODE_OFF"; break;
                    case Player.REPEAT_MODE_ONE:
                        r = "1: REPEAT_MODE_ONE"; break;
                    case Player.REPEAT_MODE_ALL:
                        r = "2: REPEAT_MODE_ALL"; break;
                    default:
                        r = "NO REPEATMODE DETECTED";
                }
                Log.i(TAG, "onRepeatModeChanged: " + r);
            }
        });
    }

    private void setNotification(int playbackState) {
        try {
            // Get metadata for the current audio being played
            MediaControllerCompat controller = mediaSession.getController();
            MediaMetadataCompat metadata = controller.getMetadata();
            MediaDescriptionCompat description = metadata.getDescription();

            // TODO: Make the notification play/pause button update
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

            // Build notification and put service in foreground
            notification = builder.build();
            startForeground(NOTIFICATION_ID, notification);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Decouple from PlayerService
    public class PlayerCallback extends MediaSessionCompat.Callback {

        public PlayerCallback() {
            // Request audio focus for playback
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if(afChangeListener == null) {
                afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        // TODO: Pause playback on autofocus change
                    }
                };
            }
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
            // Remove audio focus so other apps can play audio
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(afChangeListener);

            // Unregister broadcast receiver
            try {
                unregisterReceiver(playbackReceiver);
            } catch(Exception e) {
                e.printStackTrace();
            }

            // Stop the player
            exoMediaPlayer.stop();

            // Set the session inactive
            mediaSession.setActive(false);

            // Stop notification, take the service out of the foreground
            stopForeground(false);
        }

        @Override
        public void onSeekTo(long pos) {
            Log.d(TAG, "Seeking to " + pos);
            exoMediaPlayer.seekTo(pos);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "onPlayFromMediaId: " + mediaId + " " + extras.toString());
            try {
                Episode episode = ClarityApp.getGson().fromJson(mediaId, Episode.class);

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
                dynamicConcatenatingMediaSource.addMediaSource(audioSource);
                if(dynamicConcatenatingMediaSource.getSize() == 1) {
                    exoMediaPlayer.prepare(dynamicConcatenatingMediaSource, false, false);
                }
                Log.i(TAG, "onPlayFromMediaId: mediaSource.getSize(): " + dynamicConcatenatingMediaSource.getSize());
//                exoMediaPlayer.prepare(audioSource);

                // Start playing the audio
                exoMediaPlayer.setPlayWhenReady(true);

                // Update the metadata for this session
                mediaSession.setMetadata(episode.toMediaMetadataCompat());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
            // TODO: Play channel
        }
    }

    public String getStateChanged(int playbackState) {
        String s = "";
        switch(playbackState) {
            case PlaybackStateCompat.STATE_NONE:
                s = "0: STATE_NONE"; break;
            case PlaybackStateCompat.STATE_STOPPED:
                s = "1: STATE_STOPPED"; break;
            case  PlaybackStateCompat.STATE_PAUSED:
                s = "2: STATE_PAUSED"; break;
            case PlaybackStateCompat.STATE_PLAYING:
                s = "3: STATE_PLAYING"; break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                s = "4: STATE_FAST_FORWARDING"; break;
            case PlaybackStateCompat.STATE_REWINDING:
                s = "5: STATE_REWINDING"; break;
            case PlaybackStateCompat.STATE_BUFFERING:
                s = "6: STATE_BUFFERING"; break;
            case PlaybackStateCompat.STATE_ERROR:
                s = "7: STATE_ERROR"; break;
            case PlaybackStateCompat.STATE_CONNECTING:
                s = "8: STATE_CONNECTING"; break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                s = "9: STATE_SKIPPING_TO_PREVIOUS"; break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                s = "10: STATE_SKIPPING_TO_NEXT"; break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                s = "11: STATE_SKIPPING_TO_QUEUE_ITEM"; break;
            default:
                s = "NO STATE DETECTED";
        }
        return s;
    }
}
