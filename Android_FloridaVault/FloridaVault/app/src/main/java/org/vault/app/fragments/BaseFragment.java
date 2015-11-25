package org.vault.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.vault.app.appcontroller.AppController;

/**
 * @author aqeeb.pathan This is a base fragment for all fragment created. We can
 *         use it for global declarations and memory optimization
 */
public class BaseFragment extends Fragment {

    public static Activity context;
    protected AppController mApp;
    protected FragmentManager mFragmentManager;
    private static MenuItem item;

    public BaseFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        mApp = (AppController) context.getApplication();
        mFragmentManager = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return null;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    /**
     * Base Fragment Class onActivityCreated Method
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Basic Fragments Method
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * refreshes the menu item of screen
     *
     * @param item
     */
    public void onRefresh(MenuItem item) {
        BaseFragment.item = item;
        item.setEnabled(false);
    }

    /**
     * after refreshing this method is called.
     */
    public void onRefreshDone() {
        if (item != null) {
            item.setEnabled(true);
            item = null;
        }
    }

    public void onBackPress() {
        context.onBackPressed();

    }


}
