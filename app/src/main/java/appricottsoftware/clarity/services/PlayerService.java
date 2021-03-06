package appricottsoftware.clarity.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
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
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.Extractor;
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
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import appricottsoftware.clarity.HomeActivity;
import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.Metadata;
import appricottsoftware.clarity.sync.ClarityApp;
import appricottsoftware.clarity.sync.ClarityClient;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.R.string.playback_speed_key;

public class PlayerService extends MediaBrowserServiceCompat {

    private static final String TAG = "PlayerService";

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String NOTIFICATION_CHANNEL_ID = "clarity_channel_id";
    private static final String NOTIFICATION_CHANNEL_NAME = "clarity_channel_name";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "clarity_channel_description";

    private static final int NOTIFICATION_ID = 1;
    private static final int PREFETCH_CONSTANT = 5;

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

    private Queue<Episode> playlist; // Playlist to keep track of where we are in the playback
    private int nextOffset; // Next page of Podcast API results
    private int total; // Total Podcast API results for currentQuery
    private Channel currentChannel; // Current channel

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        
        // Reset the state of the playlist
        playlist = new LinkedList<>();
        resetCounters();
        
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
        // Starting with Android O, a Notification Channel needs to be configured
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            // Configure Notification Channel
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

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
                Log.v(TAG, "playbackStateRunnable: currentPosition: " + exoMediaPlayer.getCurrentPosition() / 1000 + " duration: " + exoMediaPlayer.getDuration() / 1000);
                playbackStateHandler.postDelayed(this, 1000);
            }
        };
        
        dynamicConcatenatingMediaSource = new DynamicConcatenatingMediaSource();

        // TODO: Attach the media player to the media session
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
        // Get the actions the media session can handle at the moment
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if(exoMediaPlayer.getPlaybackState() == PlaybackStateCompat.STATE_PLAYING) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    private void initializeExoMediaPlayer() {
        exoMediaPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        exoMediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { 
                Log.v(TAG, "onTracksChanged: trackGroups:" + trackGroups.toString() + " trackSelections: " + trackSelections.toString()); 
            }

            @Override
            public void onLoadingChanged(boolean isLoading) { 
                Log.v(TAG, "onLoadingChanged: isLoading: " + isLoading); 
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) { 
                Log.v(TAG, "onPlayerError: " + error.toString());
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "onPlaybackParametersChanged: " + playbackParameters.toString());
            }

            @Override
            public void onSeekProcessed() {
                Log.v(TAG, "onSeekProcessed");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                String r = "";
                switch(reason) {
                    case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                        // The next element in the playlist has started playing
                        updatePlaylist();
                        r = "0: DISCONTINUITY_REASON_PERIOD_TRANSITION"; 
                        break;
                    case Player.DISCONTINUITY_REASON_SEEK:
                        r = "1: DISCONTINUITY_REASON_SEEK"; 
                        break;
                    case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                        r = "2: DISCONTINUITY_REASON_SEEK_ADJUSTMENT"; 
                        break;
                    case Player.DISCONTINUITY_REASON_INTERNAL:
                        r = "3: DISCONTINUITY_REASON_INTERNAL"; 
                        break;
                    default:
                        r = "NO REASON DETECTED";
                        break;
                }
                Log.v(TAG, "onPositionDiscontinuity: " + r);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
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
                    mediaSession.setPlaybackState(stateBuilder.build());
                    playbackState = PlaybackStateCompat.STATE_PAUSED;
                    Log.v(TAG, "onPlayerStateChanged: NOT PLAYING currentPosition: "  + exoMediaPlayer.getCurrentPosition() / 1000 + " duration: " + exoMediaPlayer.getDuration() / 1000);
                }
                
                // Set the notification
                setNotification(playbackState);
                notificationManager.notify(NOTIFICATION_ID, notification);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.v(TAG, "onShuffleModeEnabledChanged: " + shuffleModeEnabled);
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                if(timeline != null && manifest != null) {
                    Log.v(TAG, "onTimelineChanged: timeline: " + timeline.toString() + " manifest: " + manifest.toString());
                } else {
                    Log.v(TAG, "onTimelineChanged");
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
                Log.v(TAG, "onRepeatModeChanged: " + r);
            }
        });
    }

    private void updatePlaylist() {
        if(dynamicConcatenatingMediaSource.getSize() > 1
                && playlist.size() > 1) {
            // If there are items left in the playlist, pop and move to the next item
            dynamicConcatenatingMediaSource.removeMediaSource(0);
            playlist.remove();
            setMetadata(playlist.peek());
        }

        // Prefetch the next page of results
        playChannel(currentChannel);
    }

    private void playChannel(Channel channel) {
        // If the query is new, reset and load the new query
        if(currentChannel == null
            || !channel.equals(currentChannel)) {
            currentChannel = channel;
            resetCounters();
            clearQueue();
        }

        // If the playlist is running low, fetch the next page if it exists
        if(dynamicConcatenatingMediaSource.getSize() < PREFETCH_CONSTANT
            && playlist.size() < PREFETCH_CONSTANT
            && nextOffset < total
            && ClarityApp.getRestClient().isSearchQuotaRemaining()) {
            try {
                // Fetch the next page of results from this channel
                ClarityApp.getRestClient()
                    .getFullTextSearch(currentChannel.getGenreIds(), nextOffset, currentChannel.getSearchTerm(context), ClarityApp.getSession(context).getSortByDate(), "episode", context, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Add the response to the playlist
                        ClarityApp.getRestClient().setSearchQuotaRemaining(headers, context);
                        playPlaylist(response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        super.onSuccess(statusCode, headers, responseString);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private MediaSource toAudioSource(Episode episode) {
        // Convert episode into a media source for the player to play
        String userAgent = Util.getUserAgent(context, getString(R.string.app_name));
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allow cross protocol redirects for feedproxy*/);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                null,
                httpDataSourceFactory);
        ExtractorMediaSource.Factory extractorFactory = new ExtractorMediaSource.Factory(dataSourceFactory);

        return extractorFactory.createMediaSource(Uri.parse(episode.getAudio()), null, null);
    }

    private void clearQueue() {
        // Stop the media player
        exoMediaPlayer.stop();

        // Empty the play queue
        dynamicConcatenatingMediaSource = new DynamicConcatenatingMediaSource();

        // Empty the playlist
        playlist.clear();
    }

    private void insertIntoQueue(Episode episode) {
        // Add episode to the playlist and media source
        playlist.add(episode);
        dynamicConcatenatingMediaSource.addMediaSource(toAudioSource(episode));
    }

    private void resetCounters() {
        // Reset Podcast API parameters
        nextOffset = -1;
        total = 0;
    }

    private void playPlaylist(JSONObject response) {
        try {
            // Save the next offset and total
            nextOffset = response.getInt("next_offset") + 1;
            total = response.getInt("total");

            // Convert the response into Episodes
            TypeToken<ArrayList<Episode>> token = new TypeToken<ArrayList<Episode>>() {};
            ArrayList<Episode> episodes = ClarityApp.getGson().fromJson(response.getString("results"), token.getType());

            int prevPlaylistSize = dynamicConcatenatingMediaSource.getSize();

            // Load the episodes into the playlist
            for(Episode episode : episodes) {
                if(episode != null && episode.isValid()) {
                    insertIntoQueue(episode);
                }
            }

            if(prevPlaylistSize < 1) {
                exoMediaPlayer.prepare(dynamicConcatenatingMediaSource);
                exoMediaPlayer.setPlayWhenReady(true);
                setMetadata(playlist.peek());
            }

        } catch(Exception e) {
            Log.e(TAG, "playPlaylist", e);
        }
    }

    private void setMetadata(final Episode episode) {
        // Launch a thread to download the Bitmap for the Metadata
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mediaSession.setMetadata(episode.toMediaMetadataCompat());
            }
        });
        thread.start();
    }

    private void downvoteCurrent() {
        Episode episode = playlist.peek();
        int channelId = currentChannel.getCid();
        if(episode != null && channelId > 0) {
            ClarityApp.getRestClient().metadataDownVoteRequest(channelId, episode.getIntGenres(), context, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.v(TAG, "onSuccess");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.v(TAG, "onFailure");
                }
            });
        }
    }

    private void setNotification(int playbackState) {
       try {
           // Get metadata for the current audio being played
           MediaControllerCompat controller = mediaSession.getController();
           MediaMetadataCompat metadata = controller.getMetadata();

           // By default, the media is paused and ready to play
           int rDrawable = R.drawable.ic_notification_play;
           String actionState = "Play";
           int nextPlayState = PlaybackStateCompat.STATE_PLAYING;

           // If the media is paused, set the icon appropriately
           if(playbackState == PlaybackState.STATE_PLAYING) {
               rDrawable = R.drawable.ic_notification_pause;
               actionState = "Pause";
               nextPlayState = PlaybackStateCompat.STATE_PAUSED;
           }

           // Create the notification
           NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

           builder // Metadata on notification bar
                   .setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE))
                   .setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION))
                   .setSubText(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE))
                   .setLargeIcon(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))

                   // Launch activity by clicking notification
                   .setContentIntent(controller.getSessionActivity())

                   // TODO: Swipe notification away to stop service
                   // .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                   //        PlaybackStateCompat.ACTION_STOP))

                   // Show notification on lock screen
                   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                   // App icon + color
                   .setSmallIcon(R.drawable.ic_notification_small_icon)
                   .setColor(ContextCompat.getColor(context, R.color.colorPrimary))

                   // Pause button stops playback
                   .addAction(new NotificationCompat.Action(rDrawable,
                           actionState,
                           MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                   PlaybackStateCompat.ACTION_PLAY_PAUSE)))

                   // Use MediaStyle features
                   .setStyle(new MediaStyle()
                           .setMediaSession(mediaSession.getSessionToken())
                           .setShowActionsInCompactView(0))

                   // Hide the time
                   .setShowWhen(false)

                    .setChannelId(NOTIFICATION_CHANNEL_ID);

           // Build notification
           notification = builder.build();
       } catch(Exception e) {
           // Construct default notification
           NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
           builder // Metadata on notification bar
                   .setContentTitle("")
                   .setContentText("")
                   .setSubText("Podcasts Unlimited")
                   .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))

                   // Show notification on lock screen
                   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                   // App icon + color
                   .setSmallIcon(R.drawable.ic_notification_small_icon)
                   .setColor(ContextCompat.getColor(context,
                           android.R.color.white))

                   // Hide the time
                   .setShowWhen(false)

                   .setChannelId(NOTIFICATION_CHANNEL_ID);

           // Build notification
           notification = builder.build();
       }
    }

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
                setNotification(PlaybackStateCompat.STATE_PLAYING);
                startForeground(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onPlay() {
            Log.v(TAG, "onPlay");
            // Start the player
            exoMediaPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            Log.v(TAG, "onPause");
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            // Pause the player
            exoMediaPlayer.setPlayWhenReady(false);
            // Unregister the BroadcastReceiver
            try {
                unregisterReceiver(playbackReceiver);
            } catch(Exception e) {
                Log.e(TAG, "onPause", e);
            }
            // Take the service out of the foreground, leave the notification
            stopForeground(false);
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
                Log.e(TAG, "onStop", e);
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
            Log.v(TAG, "onSeekTo: " + pos);
            exoMediaPlayer.seekTo(pos);
        }

        @Override
        public void onSkipToNext() {
            Log.v(TAG, "onSkipToNext");
            downvoteCurrent();
            updatePlaylist();
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            if(rating.getRatingStyle() == RatingCompat.RATING_THUMB_UP_DOWN) {
                int cid = 0;
                if(currentChannel != null) {
                    cid = currentChannel.getCid();
                }

                Episode currentEpisode = playlist.peek();
                ArrayList<Integer> genres = new ArrayList<>();
                if(currentEpisode != null) {
                    genres = currentEpisode.getIntGenres();
                }

                if(rating.isThumbUp()) {
                    ClarityApp.getRestClient().metadataUpVoteRequest(cid, genres, context, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Log.v(TAG, "onSuccess");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            Log.v(TAG, "onFailure");
                        }
                    });

                } else {
                    ClarityApp.getRestClient().metadataDownVoteRequest(cid, genres, context, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Log.v(TAG, "onSuccess");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            Log.v(TAG, "onFailure");
                        }
                    });
                }
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.v(TAG, "onPlayFromMediaId: " + mediaId + " " + extras.toString());
            try {
                Episode episode = ClarityApp.getGson().fromJson(mediaId, Episode.class);

                // Play the audio by creating a media source from the audio URL
                insertIntoQueue(episode);
                if(dynamicConcatenatingMediaSource.getSize() == 1) {
                    exoMediaPlayer.prepare(dynamicConcatenatingMediaSource, false, false);
                }

                // Start playing the audio
                exoMediaPlayer.setPlayWhenReady(true);

                // Update the metadata for this session
                setMetadata(episode);
            } catch(Exception e) {
                Log.e(TAG, "onPlayFromMediaId", e);
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            Log.v(TAG,"onPlayFromSearch: query: " + query + " extras: " + extras.toString());
            Channel channel = ClarityApp.getGson().fromJson(query, Channel.class);
            playChannel(channel);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            Log.v(TAG, "onCustomAction: action: " + action + " extras: " + extras.toString());
            // If the action requested is changing the playback speed
            if(action.equals(context.getString(R.string.playback_speed_action))) {
                // Get the bundled playback speed
                float speed = extras.getFloat(context.getString(playback_speed_key));

                // Validate the speed
                if(speed < 0.5f || speed > 3f) {
                    speed = 1.0f;
                }

                // Set the media player to play at the playback speed
                PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0f);
                exoMediaPlayer.setPlaybackParameters(playbackParameters);
            }
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
