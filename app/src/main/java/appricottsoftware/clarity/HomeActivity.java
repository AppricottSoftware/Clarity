package appricottsoftware.clarity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.parceler.Parcels;

import appricottsoftware.clarity.fragments.HomeFragment;
import appricottsoftware.clarity.fragments.LikeFragment;
import appricottsoftware.clarity.fragments.PlayerFragment;
import appricottsoftware.clarity.fragments.SettingFragment;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.services.PlayerService;
import appricottsoftware.clarity.models.Session;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements PlayerInterface {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nv_drawer) NavigationView nvDrawer;
    @BindView(R.id.supl_home) SlidingUpPanelLayout suplPanel;

    private static final String registeredLoginType = "1";
    private static final String facebookLoginType = "2";
    private static final String googleLoginType = "3";

    private static final String TAG = "HomeActivity";

    private ActionBarDrawerToggle drawerToggle;
    private String loginType;   // "1" is e-mail password, "2" is facebook, "3" is google

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

        // Get Login Type
        loginType = getIntent().getStringExtra("loginType");

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

        Log.e(TAG, "Login Type: " + loginType + "\tuserId: " + ClarityApp.getSession(this).getUserID());
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
        } else if(suplPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                suplPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                playerFragment.closePanel();
        } else {
            moveTaskToBack(true);
        }
    }

    private void logout() {
        ClarityApp.getSession(getApplicationContext()).setUserID(-1);
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    private void setUpDrawer() {
        // Set up the nav drawer
        homeFragment = new HomeFragment();
        likeFragment = new LikeFragment();
        settingFragment = new SettingFragment();

        // Start on the home fragment
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
                switch(loginType) {
                    case registeredLoginType:
                        logout();
                        break;
                    case facebookLoginType:
                        // Logout Facebook
                        LoginManager.getInstance().logOut();
                        logout();                       // This function returns to LoginActivity
                        break;
                    case googleLoginType:
                        // Logout Google
                        googleSignOut();
                        break;
                    default:
                        Toast.makeText(context,"Default", Toast.LENGTH_SHORT).show();
                        logout();
                        break;
                }
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
        // Inflate the player fragment
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

    private void connectToSession(MediaSessionCompat.Token token) {
        // Try to connect to the media session
        try {
            // Create media controller
            mediaController = new MediaControllerCompat(this, token);

            // Save the controller
            MediaControllerCompat.setMediaController(this, mediaController);

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback);

            // Display initial state
            playerFragment.onPlaybackStateChanged(mediaController.getPlaybackState(), mediaController.getMetadata());
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
        return new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                playerFragment.onMetadataChanged(metadata);
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                playerFragment.onPlaybackStateChanged(state, mediaController.getMetadata());
            }

            @Override
            public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
                // TODO: Use new audio info
            }

            @Override
            public void onSessionEvent(String event, Bundle extras) {
                // TODO: Use session event
            }
        };
    }

    @Override
    public void playChannel(Channel channel) {
        // TODO: Figure out why bundle is not transmitting data
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_channel_bundle), Parcels.wrap(channel));
        mediaController.getTransportControls()
                .playFromSearch(channel.toString(), bundle);
    }

    @Override
    public void playEpisode(Episode episode) {
        // TODO: Not used yet
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_episode_bundle), Parcels.wrap(episode));
        mediaController.getTransportControls()
                .playFromMediaId(episode.toString(), bundle);
    }

    @Override
    public void playPodcast(Podcast podcast) {
        // TODO: Not used yet
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.home_activity_podcast_bundle), Parcels.wrap(podcast));
        mediaController.getTransportControls()
                .playFromSearch(podcast.getTitle_original(), bundle);
    }

    public void googleSignOut() {
        ClarityApp clarityApp = new ClarityApp();
        GoogleSignInClient mGoogleSignInClient = clarityApp.getGoogleSignInClient();
        if (mGoogleSignInClient != null){
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            revokeAccess();
                        }
                    });
        }
    }

    private void revokeAccess() {
        ClarityApp clarityApp = new ClarityApp();
        GoogleSignInClient mGoogleSignInClient = clarityApp.getGoogleSignInClient();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            logout();
                        }
                    });
        }
        clarityApp.clearGoogleSignInClient();
    }
}