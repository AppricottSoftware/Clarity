package appricottsoftware.clarity.services;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
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

import org.parceler.Parcels;

import java.io.IOException;
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
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaPlayer mediaPlayer;
    private PlayerCallback playerCallback;
    private PlayerService mediaService;
    private IntentFilter intentFilter;
    private NotificationReceiver notificationReceiver;
    private Context context;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaService = this;
        context = getApplicationContext();
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mediaService = this;

        // Initialize media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // Acquire a lock so CPU stays on even when screen is locked
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, getString(R.string.player_service_wifi_lock));
        // Get the wifi lock
        wifiLock.acquire();

        // TODO: create receiver
        playbackReceiver = new PlaybackReceiver();
        // myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

        // Create media session
        mediaSession = new MediaSessionCompat(context, TAG);

        // Enable callbacks from media buttons and transport controls
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial playback state so media buttons start player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        mediaSession.setPlaybackState(stateBuilder.build());

        // TODO: Handle callbacks from media controller
        playerCallback = new PlayerCallback(this);
        mediaSession.setCallback(playerCallback);

        // Set session token to communicate with activities
        setSessionToken(mediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

        // TODO: Control level of access for package
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
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
        try {
            unregisterReceiver(playbackReceiver);
        } catch(Exception e) {
            e.printStackTrace();
        }
        wifiLock.release();
        stopForeground(true);
        super.onDestroy();
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if(mediaPlayer.isPlaying()) {
            Log.d(TAG, "getAvailableActions: mediaPlayer isPlaying()");
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            Log.d(TAG, "getAvailableActions: mediaPlayer !isPlaying()");
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
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
//                startService()
//                mediaService.start();

                startService(new Intent(getApplicationContext(), PlayerService.class));
                // Set the session active
                mediaSession.setActive(true);

;
                // TODO: Register BECOME_NOISY
                registerReceiver(playbackReceiver, intentFilter);
//                setNotification();
            }
        }

        @Override
        public void onPlay() {
            try {
                Log.d(TAG, "onPlay");
                // Start the player
                mediaPlayer.start();
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                        .setActions(getAvailableActions());
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f, mediaPlayer.getDuration());
                mediaSession.setPlaybackState(stateBuilder.build());
            } catch(Exception e) {

            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause");
            mediaPlayer.pause();
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(getAvailableActions());
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1.0f, mediaPlayer.getDuration());
            mediaSession.setPlaybackState(stateBuilder.build());
            // TODO: unregister broadcast receiver
            try {
                unregisterReceiver(playbackReceiver);
            } catch(Exception e) {
//                e.printStackTrace();
            }

            // Stop notification, take the service out of the foreground
            stopForeground(false);
        }

        @Override
        public void onStop() {
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            // Remove audio focus
            audioManager.abandonAudioFocus(afChangeListener);
            // TODO: Unregister receiver
            try {
                unregisterReceiver(playbackReceiver);
            } catch(Exception e) {
                //TODO: clean up receivers
//                e.printStackTrace();
            }
            // TODO: Stop the service
//            service.stop(self;
            // Set the session inactive
            mediaSession.setActive(false);
            // stop the player
            mediaPlayer.stop();
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
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
            if(episode != null) {
                //TODO: Stop if already playing
                try {
                    mediaPlayer.setDataSource(episode.getAudio());
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                                    .setActions(getAvailableActions());
                            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f, mediaPlayer.getDuration());
                            mediaSession.setPlaybackState(stateBuilder.build());
//                            setNotification();
                        }
                    });
                    mediaPlayer.prepareAsync();
                    mediaSession.setMetadata(episode.toMediaMetadataCompat());

                    Log.e("PlayerFragment", "Playing audio");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
//            setNotification();
            super.onPlayFromSearch(query, extras);
        }


        private void setNotification() {
            try {
                MediaControllerCompat controller = mediaSession.getController();
                MediaMetadataCompat metadata = controller.getMetadata();
                MediaDescriptionCompat description = metadata.getDescription();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
                builder.setContentTitle(description.getTitle()) // Metadata on notification bar
                        .setContentText(description.getSubtitle())
                        .setSubText(description.getDescription())
                        .setLargeIcon(description.getIconBitmap())
                        .setContentIntent(controller.getSessionActivity()) // Launch activity by clicking notification
                        .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)) // Swipe notification away to stop service
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show notification on lock screen
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // App icon + color
                        .setColor(ContextCompat.getColor(context,
                                R.color.colorPrimary))
                        .addAction(new NotificationCompat.Action(R.drawable.ic_player_pause, // Pause button
                                getString(R.string.player_service_pause),
                                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                        PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                        .setStyle(new MediaStyle() // Use MediaStyle features
                                .setMediaSession(mediaSession.getSessionToken())
                                .setShowActionsInCompactView(0));

                // Show notification and put service in foreground
                notification = builder.build();
                startForeground(NOTIFICATION_ID, notification);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
