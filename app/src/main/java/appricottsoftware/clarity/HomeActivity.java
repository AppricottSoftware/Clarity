package appricottsoftware.clarity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
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
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.fragments.BrowseFragment;
import appricottsoftware.clarity.fragments.ChannelFragment;
import appricottsoftware.clarity.fragments.ChannelSearchFragment;
import appricottsoftware.clarity.fragments.HomeFragment;
import appricottsoftware.clarity.fragments.LikeFragment;
import appricottsoftware.clarity.fragments.PlayerFragment;
import appricottsoftware.clarity.fragments.SettingFragment;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.FragmentListener;
import appricottsoftware.clarity.models.PlayerFragmentListener;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.services.PlayerService;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.R.string.playback_speed_key;

public class HomeActivity extends AppCompatActivity implements PlayerInterface, FragmentListener, ChannelFragment.SendChannelsInterface, BrowseFragment.BrowseToChannelInterface, PlayerFragmentListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nv_drawer) NavigationView nvDrawer;
    @BindView(R.id.supl_home) SlidingUpPanelLayout suplPanel;

    private static final String registeredLoginType = "1";
    private static final String facebookLoginType = "2";
    private static final String googleLoginType = "3";
    private static final String TAG = "HomeActivity";

    private ActionBarDrawerToggle drawerToggle;
    private String loginType;

    private static HomeFragment homeFragment;
    private static LikeFragment likeFragment;
    private static SettingFragment settingFragment;
    private static PlayerFragment playerFragment;
    private static ChannelSearchFragment channelSearchFragment;

    private ArrayList<String> listenNotesTypeAhead;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private ArrayAdapter<String> newsAdapter;

    private MenuItem searchItem;

    private Intent playerServiceIntent;
    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    private Context context;

    private String searchChannelQuery;
    private boolean searchChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        context = this;

        // Initialize search bar members
        resetSearch();

        // Get Login Type
        loginType = getIntent().getStringExtra("loginType");

        // Initializing listenNotesTypeAhead Container
        listenNotesTypeAhead = new ArrayList<>();

        // Replace toolbar
        setSupportActionBar(toolbar);

        // Set up nav drawer option clicking and header
        setUpDrawer();
        setNavHeader();

        // Get the sortByDate info
        getSortByDate();

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

        Log.i(TAG, "Login Type: " + loginType + "\tuserId: " + ClarityApp.getSession(this).getUserID());
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

        // Save the current channel for next launch
        syncCurrentChannel();

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
        // Drawer toggle handles events\
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Get current fragment in view (Home, Likes, or Settings)
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fl_home_activity_main);
        if (fragment != null) {

            // Close the nav drawer and keep the app on this page for onStart()
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else if (suplPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                suplPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                playerFragment.closePanel();
            } else if (!(fragment instanceof HomeFragment)) {
                returnToHomeFragment();
            } else {
                // Exits app
                finish();

                // Statement below used to be here. Keeping just in case.
                // moveTaskToBack(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.action_search);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Hide results, replace with home fragment
                insertFragment(homeFragment, getString(R.string.home_fragment_tag));
                return true;
            }
        });

        final SearchView searchView = (SearchView) searchItem.getActionView();
        newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listenNotesTypeAhead);
        searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.white);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Respond to the user pressing search/enter
                searchView.clearFocus();

                // Checking if searchFragment is visible or not
                String searchFragmentTag = getString(R.string.channel_search_fragment_tag);
                Fragment frag = getSupportFragmentManager().findFragmentByTag(searchFragmentTag);
                ChannelSearchFragment channelSearchFragment = (ChannelSearchFragment) frag;

                // If not visible, set it up
                if (frag == null) {
                    initializeSearchFragmentAndSearchEpisodes(query);
                    return true;
                }
                // Fragment already visible so call search on it if the query isn't empty
                else {
                    if (!query.equals("")) {
                        searchChannelQuery = query;
                        channelSearchFragment.search(searchChannelQuery);
                        resetSearch();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() % 5 == 0 && query.length() > 0 || query.length() == 3) {
                    Log.e(TAG, "onQueryTextChange" + query);
                    getPodcastsTypeAhead(query);
                }
                return false;
            }
        });


        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                Log.e(TAG,"TERM SUGGESTED: " + term);
                cursor.close();
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                return onSuggestionSelect(position);
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreatedView() {
        // Run the search if there is one waiting
        if(searchChannel && searchChannelQuery != "" && channelSearchFragment.isVisible()) {
            channelSearchFragment.search(searchChannelQuery);
            resetSearch();
        }
    }

    @Override
    public void returnToHomeFragment() {
        // Show the home fragment
        setUpDrawer();
        searchItem.setVisible(true);
        setTitle("Home");
    }

    private void resetSearch() {
        searchChannelQuery = "";
        searchChannel = false;
    }

    private void initializeSearchFragmentAndSearchEpisodes(String query) {
        // Show channel search fragment, hide home fragment
        insertFragment(channelSearchFragment, getString(R.string.channel_search_fragment_tag));

        // Run the search when the fragment is ready
        searchChannel = true;
        searchChannelQuery = query;
    }

    // Clears user ID (uid) and returns to LoginActivity
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
        channelSearchFragment = new ChannelSearchFragment();

        // Start on the home fragment
        nvDrawer.setCheckedItem(R.id.nav_home_fragment);
        insertFragment(homeFragment, getString(R.string.home_fragment_tag));

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
        String fragmentTag = "";
        switch(item.getItemId()) {
            case R.id.nav_home_fragment:
                fragment = homeFragment;
                fragmentTag = getString(R.string.home_fragment_tag);
                break;
//            case R.id.nav_like_fragment:
//                fragment = likeFragment;
//                fragmentTag = getString(R.string.like_fragment_tag);
//                break;
            case R.id.nav_setting_fragment:
                fragment = settingFragment;
                fragmentTag = getString(R.string.setting_fragment_tag);
                break;
            case R.id.nav_logout:
                switch(loginType) {
                    case registeredLoginType:
                        logout();
                        break;
                    case facebookLoginType:
                        // Logout Facebook
                        LoginManager.getInstance().logOut();
                        logout();
                        break;
                    case googleLoginType:
                        // Logout Google
                        googleSignOut();
                        break;
                    default:
                        Log.e(TAG, "Error with logout. Shouldn't reach default statement");
                        logout();
                        break;
                }
            default:
                break;
        }

        try {
            // Populate home frame layout
            insertFragment(fragment, fragmentTag);

            // Show the search only if we are on home fragment
            if(searchItem != null) {
                if(fragment instanceof HomeFragment) {
                    searchItem.setVisible(true);
                } else {
                    searchItem.setVisible(false);
                }
            }

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

    private void insertFragment(Fragment fragment, String fragmentTag) {
        // Insert fragment into frame layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_home_activity_main, fragment, fragmentTag)
                .commit();
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
                if(Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(playerServiceIntent);
                } else {
                    context.startService(playerServiceIntent);
                }
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

        // Save the channel to shared preferences
        ClarityApp.getSession(this).setCurrentChannel(channel.getCid());
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

    @Override
    public void onDialogOK(float speed) {
        playerFragment.setPlaybackSpeed(speed);
        Bundle bundle = new Bundle();
        bundle.putFloat(getString(playback_speed_key), speed);
        mediaController.getTransportControls()
                .sendCustomAction(getString(R.string.playback_speed_action), bundle);
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

    // This method interfaced within ChannelFragment
    @Override
    public void sendChannels(List<Channel> channels) {
        // Get reference to BrowseFragment
        String homeFragmentTag = getString(R.string.home_fragment_tag);
        Fragment page = getSupportFragmentManager().findFragmentByTag(homeFragmentTag);
        HomeFragment homeFragment = (HomeFragment) page;

        // If successful use HomeFragment as medium to communicate between Channels and Browse
        if (homeFragment != null) {
            Log.i(TAG, "Receive callback from ChannelFragment, sending data to browse");
            homeFragment.sendDataToBrowseFragment(channels);
        }
        else {
            Log.e(TAG, "HomeFragment is null");
        }
    }

    // This method interfaced within BrowseFragment
    @Override
    public void requestChannels() {
        // Get reference to BrowseFragment
        String homeFragmentTag = getString(R.string.home_fragment_tag);
        Fragment page = getSupportFragmentManager().findFragmentByTag(homeFragmentTag);
        HomeFragment homeFragment = (HomeFragment) page;

        // If successful use HomeFragment as medium to communicate between Channels and Browse
        if (homeFragment != null) {
            Log.i(TAG, "Receive callback from BrowseFragment, requesting data from ChannelFrag");
            homeFragment.requestDataFromChannelFragment();
        }
        else {
            Log.e(TAG, "HomeFragment is null");
        }
    }

    // This method interfaced within BrowseFragment
    @Override
    public void addChannel(Channel channel) {
        // Get reference to BrowseFragment
        String homeFragmentTag = getString(R.string.home_fragment_tag);
        Fragment page = getSupportFragmentManager().findFragmentByTag(homeFragmentTag);
        HomeFragment homeFragment = (HomeFragment) page;

        // If successful use HomeFragment as medium to communicate between Channels and Browse
        if (homeFragment != null) {
            Log.i(TAG, "Receive callback from BrowseFragment, requesting data from ChannelFrag");
            homeFragment.addChannelToChannelFragment(channel);
        }
        else {
            Log.e(TAG, "HomeFragment is null");
        }
    }

    private void setNavHeader() {
        int uid = ClarityApp.getSession(this).getUserID();
        ClarityApp.getRestClient().getEmail(uid, this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    TextView header = findViewById(R.id.nav_header_text);
                    header.setText(response.getString("email"));
                } catch (Exception e) {
                    Log.e(TAG, "getOldEmail: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "getOldEmail Failed");
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void getPodcastsTypeAhead(String newText) {
        Log.e("HomeActivity", "On typeAhead: " + newText);

        ClarityApp.getRestClient().getTypeAheadPodcast(newText, this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("MainActivity 1", response.toString());
                addPodcastsTypeAhead(response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("MainActivity 2", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                Log.e("MainActivity 3", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                throwable.printStackTrace();
                Log.e("MainActivity 4", "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                throwable.printStackTrace();
                Log.e("MainActivity 5", "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                throwable.printStackTrace();
                Log.e("MainActivity 6", "");
            }

        });
    }


    private void addPodcastsTypeAhead(JSONObject response) {
        try {
            JSONArray terms = response.getJSONArray("terms");
            Log.i(TAG, terms.toString());
            listenNotesTypeAhead.clear();

            // Adding terms to the master listenNotesTypeAhead Container
            for (int i = 0; i < terms.length(); i++) {
                listenNotesTypeAhead.add(terms.get(i).toString());
            }

            // Create a new ArrayAdapter and add data to search auto complete object.
            searchAutoComplete.setAdapter(newsAdapter);
            // Listen to search view item on click event.
            searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                    String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                    searchAutoComplete.setText(queryString);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "ERROR IN addPodcastsTypeAhead: \n" + e);
        }
    }

    @Override
    public void onLoadCurrentChannel() {
        // Get the user's id
        int uid = ClarityApp.getSession(this).getUserID();
        if(uid > 0) {
            // Get the user's current channel
            ClarityApp.getRestClient().getCurrentChannel(uid, this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        // Grab the current channel and start playing
                        Channel channel = ClarityApp.getGson().fromJson(response.getJSONObject("currentChannel").toString(), Channel.class);
                        playChannel(channel);

                        // Save the current channel to shared preferences
                        ClarityApp.getSession(context).setCurrentChannel(channel.getCid());
                    } catch(Exception e) {
                        Log.e(TAG, "playCurrentChannel: Unable to parse response", e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, "playCurrentChannel: onFailure", throwable);
                }
            });
        }
    }

    private void syncCurrentChannel() {
        int uid = ClarityApp.getSession(this).getUserID();
        int cid = ClarityApp.getSession(this).getCurrentChannel();
        if(uid > 0 && cid > 0) {
            ClarityApp.getRestClient().updateCurrentChannel(uid, cid, this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.v(TAG, "syncCurrentChannel: onSuccess");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, "syncCurrentChannel: onFailure", throwable);
                }
            });
        }
    }

    private void getSortByDate() {
        int uid = ClarityApp.getSession(this).getUserID();
        if(uid > 0) {
            ClarityApp.getRestClient().getSortByDate(uid, this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        ClarityApp.getSession(context).setSortByDate(response.getInt("sortByDate"));
                    } catch (Exception e) {
                        Log.e(TAG, "getSortByDate: onSuccess: Failed to parse response", e);
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, "getSortByDate: onFailure", throwable);
                }
            });
        }
    }
}