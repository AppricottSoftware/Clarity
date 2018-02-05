package appricottsoftware.clarity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

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
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.parceler.Parcels;

import java.util.ArrayList;

import appricottsoftware.clarity.fragments.HomeFragment;
import appricottsoftware.clarity.fragments.LikeFragment;
import appricottsoftware.clarity.fragments.PlayerFragment;
import appricottsoftware.clarity.fragments.SettingFragment;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.services.PlayerService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity implements PlayerInterface {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nv_drawer) NavigationView nvDrawer;
    @BindView(R.id.supl_home) SlidingUpPanelLayout suplPanel;

    private static final String TAG = "HomeActivity";

    private ActionBarDrawerToggle drawerToggle;

    private static HomeFragment homeFragment;
    private static LikeFragment likeFragment;
    private static SettingFragment settingFragment;
    private static PlayerFragment playerFragment;

    private Intent playerServiceIntent;
    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        context = this;

        // Replace toolbar
        setSupportActionBar(toolbar);

        // Set up nav drawer option clicking
        setUpDrawer();

        // Set up drawer toggling
        drawerToggle = setUpDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        // Set up the player fragment
        setUpPlayer();

        // Start the PlayerService
        startBackgroundPlayerService();

        // Create a media browser so we can track content from the service
        connectionCallback = getConnectionCallback();
        controllerCallback = getControllerCallback();
        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, PlayerService.class),
                connectionCallback,
                null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the service
        mediaBrowser.connect();
        // Set the initial player to be open or closed
        updatePlayer();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync state after activity startup is complete
        drawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        // Unregister the callback that's listening to the service
        if(MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this)
                    .unregisterCallback(controllerCallback);
        }
        // Disconnect from the service
        mediaBrowser.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Stop the service
        stopService(playerServiceIntent);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass changes to drawer toggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Drawer toggle handles events
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Close the nav drawer and keep the app on this page for onStart()
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            moveTaskToBack(true);
        }
    }

    private void logout() {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    private void setUpDrawer() {
        // Set up the nav drawer
        homeFragment = new HomeFragment();
        likeFragment = new LikeFragment();
        settingFragment = new SettingFragment();

        nvDrawer.setCheckedItem(R.id.nav_home_fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_home_activity_main, homeFragment)
                .commit();

        nvDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawer(item);
                return true;
            }
        });
    }

    private void selectDrawer(MenuItem item) {
        // Respond to clicking on an item in the nav drawer
        Fragment fragment = null;
        switch(item.getItemId()) {
            case R.id.nav_home_fragment:
                fragment = homeFragment;
                break;
            case R.id.nav_like_fragment:
                fragment = likeFragment;
                break;
            case R.id.nav_setting_fragment:
                fragment = settingFragment;
                break;
            case R.id.nav_logout:
                logout();
                break;
            default:
                break;
        }

        try {
            // Insert fragment into frame layout
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fl_home_activity_main, fragment)
                    .commit();

            // Highlight the choice
            item.setChecked(true);
            // Set toolbar title
            setTitle(item.getTitle());
            // Close nav drawer
            drawerLayout.closeDrawers();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private ActionBarDrawerToggle setUpDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
    }

    private void startBackgroundPlayerService() {
        // Create a runnable containing the service
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                playerServiceIntent = new Intent(context, PlayerService.class);
                context.startService(playerServiceIntent);
            }
        };
        // Launch it in a new thread so the UI thread isn't blocked
        final Thread thread = new Thread() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        thread.start();
    }

    private void setUpPlayer() {
        playerFragment = new PlayerFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_home_activity_player, playerFragment)
                .commit();

        // Update panel layout when swiping up or down
        suplPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) { }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                if(previousState == SlidingUpPanelLayout.PanelState.DRAGGING
                        && newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    // Opening panel, change fragment layout to full screen
                    playerFragment.openPanel();
                } else if(previousState == SlidingUpPanelLayout.PanelState.DRAGGING
                        && newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    // Closing panel, change fragment layout to bottom strip
                    playerFragment.closePanel();
                }
            }
        });
    }

    private void updatePlayer() {
        // Get the current state of the panel and update the fragment
        if(suplPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            playerFragment.closePanel();
        } else if(suplPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            playerFragment.openPanel();
        }
    }



    // Try to connect to the media session
    private void connectToSession(MediaSessionCompat.Token token) {
        try {
            // Create media controller
            mediaController = new MediaControllerCompat(this, token);

            // Save the controller
            MediaControllerCompat.setMediaController(this, mediaController);

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback);

            // Display initial state
            playerFragment.onConnected(mediaController.getPlaybackState(), mediaController.getMetadata());
            onMediaControllerConnected();

            // Finish building UI
            playerFragment.buildTransportControls(this);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onMediaControllerConnected() {
        //TODO: Browse fragment code
    }

    private MediaBrowserCompat.ConnectionCallback getConnectionCallback() {
        return new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d(TAG, "MediaBrowserCompat.ConnectionCallback: onConnected");
                // Connect to media session
                connectToSession(mediaBrowser.getSessionToken());
            }

            @Override
            public void onConnectionSuspended() {
                // TODO: Suspend until connection established
            }

            @Override
            public void onConnectionFailed() {
                // TODO: Failed connection
            }
        };
    }

    private MediaControllerCompat.Callback getControllerCallback() {
        /* TODO */
        return new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                Log.e(TAG, "OnMetadataChanged: " + metadata.toString());
                playerFragment.onMetadataChanged(metadata);
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                Log.e(TAG, "OnPlaybackStateChanged: " + state.toString());
                playerFragment.onPlaybackStateChanged(state, mediaController.getMetadata());
            }

            @Override
            public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
                Log.e(TAG, "onAudioInfoChanged: " + info.toString());
                super.onAudioInfoChanged(info);
            }
        };
    }

    @Override
    public void playChannel(Channel channel) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_channel_bundle), Parcels.wrap(channel));
        mediaController.getTransportControls()
                .playFromSearch(channel.getTopic(), bundle);
    }

    @Override
    public void playEpisode(Episode episode) {
        // TODO: Figure out why bundle is not transmitting data
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_episode_bundle), Parcels.wrap(episode));
        Log.e(TAG, bundle.toString());
        mediaController.getTransportControls()
                .playFromMediaId(episode.toString(), bundle);
//        playExo(episode);
    }

    @Override
    public void playPodcast(Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_podcast_bundle), Parcels.wrap(podcast));
        mediaController.getTransportControls()
                .playFromSearch(podcast.getTitle_original(), bundle);
    }

    // Get controller
//            mediaController.getMetadata()
    

    //    // Player interface functions
//    @Override
//    public void loadPlaylist(ArrayList<Podcast> podcasts) {
//        playerFragment.loadPlaylist(podcasts);
//    }
//
//    @Override
//    public void play(Episode episode) {
////        playerFragment.play(episode);
//    }
//
//    @Override
//    public void pause() {
//        playerFragment.pause();
//    }
//
//    @Override
//    public void skip() {
//        playerFragment.skip();
//    }


    public void playExo(Episode ep) {
        SimpleExoPlayer exoPlayer;
        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }
        });

        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true /* allow cross protocol redirects for feedproxy*/);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, null, httpDataSourceFactory);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource audioSource = new ExtractorMediaSource(Uri.parse(ep.getAudio()), dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);

    }

}
