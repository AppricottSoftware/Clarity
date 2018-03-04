package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

import appricottsoftware.clarity.fragments.BrowseFragment;
import appricottsoftware.clarity.fragments.ChannelFragment;
import appricottsoftware.clarity.models.Channel;

public class TabPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "TabPagerAdapter";

    final int COUNT = 2;
    private String tabs[] = new String[] {"My Channels", "Browse"};
    private ChannelFragment channelFragment;
    private BrowseFragment browseFragment;

    public TabPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0: // First tab is channels fragment
                fragment = new ChannelFragment();
                break;
            case 1: // Second tab is browse fragment
                fragment = new BrowseFragment();
                break;
            default: // Invalid
                fragment = null;
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    // Here we can finally safely save a reference to the created
    // Fragment, no matter where it came from (either getItem() or
    // FragmentManger). Simply save the returned Fragment from
    // super.instantiateItem() into an appropriate reference depending
    // on the ViewPager position.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                channelFragment = (ChannelFragment) createdFragment;
                break;
            case 1:
                browseFragment = (BrowseFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

    public void sendDataToBrowseFragment(List<Channel> channels) {
        if (browseFragment != null) {
            browseFragment.receiveChannelsFromChannelFragment(channels);
        }
        else {
            Log.e(TAG, "BrowseFragment is null");
        }
    }

    public void requestDataFromChannelFragment() {
        if (channelFragment != null) {
            channelFragment.sendChannelsToBrowse();
        }
        else {
            Log.e(TAG, "ChannelFragment is null");
        }
    }

    public void addChannelToChannelFragment(Channel channel) {
        if (channelFragment != null) {
            channelFragment.addChannelFromBrowse(channel);
        }
        else {
            Log.e(TAG, "ChannelFragment is null");
        }
    }
}
