package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.nio.channels.Channel;

import appricottsoftware.clarity.fragments.BrowseFragment;
import appricottsoftware.clarity.fragments.ChannelFragment;

/**
 * Created by karen on 1/19/18.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {


    final int COUNT = 2;
    private String tabs[] = new String[] {"My Channels", "Browse"};

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
            case 0:
                fragment = new ChannelFragment();
                break;
            case 1:
                fragment = new BrowseFragment();
                break;
            default:
                fragment = null;
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }
}
