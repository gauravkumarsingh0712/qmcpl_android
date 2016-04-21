package org.vault.app.adapters;

/**
 * Created by aqeeb.pathan on 25-07-2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.vault.app.globalconstants.GlobalConstants;

import java.util.List;

/**
 * The <code>PagerAdapter</code> serves the fragments when paging.
 * @author mwho
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;
    private static final String[] CONTENT = { GlobalConstants.FEATURED, GlobalConstants.GAMES,
            GlobalConstants.PLAYERS, GlobalConstants.OPPONENTS,GlobalConstants.COACHES_ERA,
            GlobalConstants.FAVORITES};

    /**
     * @param fm
     * @param fragments
     */
    public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(fragments.get(position).getArguments() != null) {
            Bundle bundle = fragments.get(position).getArguments();
            String tabName = bundle.getString("tabName");
            if (tabName != null)
                return tabName.toUpperCase();
            return "";
        }else{
            return CONTENT[5].toUpperCase();
        }
//        return PagerAdapter.CONTENT[position].toUpperCase();
//        return GlobalConstants.tabsList[position].toUpperCase();
    }




}