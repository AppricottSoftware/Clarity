package appricottsoftware.clarity;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import appricottsoftware.clarity.adapters.TabPagerAdapter;
import appricottsoftware.clarity.fragments.HomeFragment;
import appricottsoftware.clarity.fragments.LikeFragment;
import appricottsoftware.clarity.fragments.PlayerFragment;
import appricottsoftware.clarity.fragments.SettingFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nv_drawer) NavigationView nvDrawer;
    @BindView(R.id.supl_home) SlidingUpPanelLayout suplPanel;

    private ActionBarDrawerToggle drawerToggle;

    private static HomeFragment homeFragment;
    private static LikeFragment likeFragment;
    private static SettingFragment settingFragment;
    private static PlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // Replace toolbar
        setSupportActionBar(toolbar);

        // Set up nav drawer option clicking
        setUpDrawer();

        // Set up drawer toggling
        drawerToggle = setUpDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        // Set up the player fragment
        setUpPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        moveTaskToBack(true);
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
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(previousState == SlidingUpPanelLayout.PanelState.COLLAPSED
                        && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    // Opening panel, change fragment layout to full screen
                    playerFragment.openPanel();
                } else if(previousState == SlidingUpPanelLayout.PanelState.EXPANDED
                        && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
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

    private void logout() {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    private void setUpDrawer() {
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
        // Make fragment
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
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
    }
}
