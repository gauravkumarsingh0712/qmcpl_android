package org.vault.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.flurry.android.FlurryAgent;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.activities.VideoInfoActivity;
import org.vault.app.adapters.VideoContentListAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.model.LocalModel;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author aqeeb.pathan
 */
public class FavoritesFragment extends BaseFragment {
    public static ListView listViewFavouriteVideos;
    public VideoContentListAdapter videoListAdapter = null;
    public static TextView tvsearchRecordsNotAvailable;
    public ArrayList<VideoDTO> favoriteVideoList = new ArrayList<>();

    SearchView searchView;
    public static ProgressBar progressBar;
    public boolean isLastPageLoaded = false;

    public boolean isFreshDataLoading = true;

    PullRefreshTask pullRefreshTask;
    PullRefreshLayout refreshLayout;
    Activity mActivity;

    public FavoritesFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.favourites_video_layout,
                container, false);
        try {

            mActivity = getActivity();
            isLastPageLoaded = false;
//        isFreshDataLoading = true;
            // --------Intializing Views---------
            initComponents(v);
            System.out.println("Favorite Video List Count : " + favoriteVideoList.size());
            setHasOptionsMenu(true);

            registerEvents();
            getFavoriteDataFromDataBase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    /**
     *
     */
    private void getFavoriteDataFromDataBase() {
        try {
            favoriteVideoList.clear();
            favoriteVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());

            Collections.sort(favoriteVideoList, new Comparator<VideoDTO>() {

                @Override
                public int compare(VideoDTO lhs, VideoDTO rhs) {
                    // TODO Auto-generated method stub
                    return lhs.getVideoName().toLowerCase()
                            .compareTo(rhs.getVideoName().toLowerCase());
                }
            });

            videoListAdapter = new VideoContentListAdapter(favoriteVideoList, mActivity, 2, true);
            listViewFavouriteVideos.setAdapter(videoListAdapter);
            if (VideoDataService.isServiceRunning) {
                if (favoriteVideoList.size() == 0) {
                    if (progressBar.isShown()) {
                        System.out.println("favorite isServiceRunning");
                        progressBar.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        //tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    //tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                }

            } else {
                if (favoriteVideoList.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                } else {
                    //progressBar.setVisibility(View.VISIBLE);
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                }
            }

            //visibility of scroll bar set dynamically list height
            Utils.setVisibilityOfScrollBarHeightForNormalList(mActivity, GlobalConstants.SEARCH_VIEW_QUERY, listViewFavouriteVideos);

            // ----------add banner-----------
            /*Utils.addVolleyBanner(bannerCacheableImageView,
                    GlobalConstants.URL_FAVORITESBANNER, getActivity());*/
//            Utils.addBannerImage(bannerCacheableImageView, GlobalConstants.FAVORITES, mActivity);


            countDownTimer = new CountDownTimer(1000, 3000) {


                @Override
                public void onTick(long millisUntilFinished) {
                    if (progressBar.isShown()) {
                        progressBar.setVisibility(View.GONE);
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        if (progressBar != null) {
                            if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                                refreshLayout.setEnabled(true);
                                refreshLayout.setOnRefreshListener(refreshListener);
                            }
                        }
                    }
                    if (favoriteVideoList.size() < 7) {
                        //  listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    }
                }

                @Override
                public void onFinish() {

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    LocalModel localModel = LocalModel.getInstance();
                    localModel.setmListViewHeight(calculateHeight(listViewFavouriteVideos));

                    if (localModel.getmListViewHeight() < localModel.getmDisplayHeight()) {
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    }
                }
            }.start();

            autoRefresh();
            //  startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFreshDataLoading = false;
        /*if (pullRefreshTask != null) {
            if (pullRefreshTask.getStatus() == AsyncTask.Status.RUNNING) {
                pullRefreshTask.cancel(true);
            }
        }
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        System.out.println("favorite onResume 111");
        if (GlobalConstants.SEARCH_VIEW_QUERY.isEmpty() || GlobalConstants.IS_RETURNED_FROM_PLAYER) {
            if (mActivity != null) {
                favoriteVideoList.clear();
                favoriteVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());

                videoListAdapter = new VideoContentListAdapter(
                        favoriteVideoList, mActivity, 2, true);
                listViewFavouriteVideos.setAdapter(videoListAdapter);

                listViewFavouriteVideos.setFastScrollEnabled(true);
            }
            isFreshDataLoading = false;
            if (VideoDataService.isServiceRunning) {
                if (favoriteVideoList.size() == 0) {
                    if (progressBar.isShown()) {
                        System.out.println("favorite isServiceRunning");
                        progressBar.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        // tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    // tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                }

            } else {
                if (favoriteVideoList.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                } else {
                    //progressBar.setVisibility(View.VISIBLE);
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                }
            }


//            if (searchView != null) {
//                videoListAdapter.listSearch.clear();
//                videoListAdapter.listSearch.addAll(favoriteVideoList);
//                videoListAdapter.filter(searchView.getQuery().toString().toLowerCase(Locale
//                        .getDefault()));
//                videoListAdapter.notifyDataSetChanged();
//            }
            GlobalConstants.IS_RETURNED_FROM_PLAYER = false;
        }

        if (videoListAdapter != null) {
            videoListAdapter.listSearch.clear();
            videoListAdapter.listSearch.addAll(favoriteVideoList);
            videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                    .getDefault()));
            videoListAdapter.notifyDataSetChanged();
        }
        if (progressBar != null && refreshLayout != null) {
            if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                refreshLayout.setEnabled(true);
                refreshLayout.setOnRefreshListener(refreshListener);
            }
        }

        countDownTimer = new CountDownTimer(1000, 200) {


            @Override
            public void onTick(long millisUntilFinished) {
                if (favoriteVideoList.size() <= 7) {
                    // listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                }

            }

            @Override
            public void onFinish() {

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }

                LocalModel localModel = LocalModel.getInstance();
                localModel.setmListViewHeight(calculateHeight(listViewFavouriteVideos));

                if (localModel.getmListViewHeight() < localModel.getmDisplayHeight()) {
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                }
            }
        }.start();

        gethideKeyboard();

    }

    /**
     * hiding keyboard
     */
    private void gethideKeyboard() {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            System.out.println("onResume gethideKeyboard111 ");
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    PullRefreshLayout.OnRefreshListener refreshListener = new PullRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Utils.isInternetAvailable(mActivity.getApplicationContext())) {
                if (MainActivity.autoRefreshProgressBar.getVisibility() == View.VISIBLE) {
                    refreshLayout.setEnabled(false);
                    refreshLayout.setRefreshing(false);
                } else {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(true);
                    pullRefreshTask = new PullRefreshTask();
                    pullRefreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                refreshLayout.setRefreshing(false);
            }
        }
    };

    private void registerEvents() {
        // TODO Auto-generated method stub
        listViewFavouriteVideos.setOnScrollListener(new AbsListView.OnScrollListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                //set default scrollbar size to 0, because we were getting two scrollbars
                //one default one and other was fastScrollBar (red one)
                view.setScrollBarSize(0);
                if (scrollState == 0) {
                    countDownTimer = new CountDownTimer(1000, 1000) {


                        @Override
                        public void onTick(long millisUntilFinished) {
                            // listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                        }

                        @Override
                        public void onFinish() {

                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            LocalModel localModel = LocalModel.getInstance();
                            localModel.setmListViewHeight(calculateHeight(listViewFavouriteVideos));

                            if (localModel.getmListViewHeight() < localModel.getmDisplayHeight()) {
                                listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                            }

                        }
                    }.start();

                } else {
                    // listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                if (((MainActivity) mActivity).progressDialog != null) {
                    if (((MainActivity) mActivity).progressDialog.isShowing())
                        ((MainActivity) mActivity).progressDialog.dismiss();
                }


            }


        });

        listViewFavouriteVideos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

                                                           {
                                                               @Override
                                                               public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                                                                              long id) {
//                                                                   ((MainActivity) mActivity).makeShareDialog(favoriteVideoList.get(position).getVideoLongUrl(), favoriteVideoList.get(position).getVideoShortUrl(), favoriteVideoList.get(position).getVideoStillUrl(), favoriteVideoList.get(position).getVideoLongDescription(), favoriteVideoList.get(position).getVideoName(), getActivity());
                                                                   return true;
                                                               }
                                                           }

        );

        listViewFavouriteVideos
                .setOnItemClickListener(new

                                                OnItemClickListener() {

                                                    @Override
                                                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                                                            int pos, long arg3) {
                                                        // TODO Auto-generated method stub
                                                        View view = mActivity.getCurrentFocus();
                                                        if (view != null) {
                                                            InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                                        }
                                                        if (Utils.isInternetAvailable(mActivity)) {
                                                            if (favoriteVideoList.get(pos).getVideoLongUrl() != null) {
                                                                if (favoriteVideoList.get(pos).getVideoLongUrl().length() > 0 && !favoriteVideoList.get(pos).getVideoLongUrl().toLowerCase().equals("none")) {
                                                                    String videoCategory = GlobalConstants.FAVORITES;
                                                                    GlobalConstants.IS_RETURNED_FROM_PLAYER = true;
                                                                    Intent intent = new Intent(getActivity(),
                                                                            VideoInfoActivity.class);
                                                                    intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                                                                    intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, favoriteVideoList.get(pos).getPlaylistReferenceId());
                                                                    intent.putExtra(GlobalConstants.VIDEO_OBJ, favoriteVideoList.get(pos));
                                                                    GlobalConstants.LIST_FRAGMENT = new FavoritesFragment();
                                                                    GlobalConstants.LIST_ITEM_POSITION = pos;
                                                                    startActivity(intent);
                                                                    mActivity.overridePendingTransition(R.anim.slide_up_video_info, R.anim.nochange);
//                                                                    videoListAdapter = null;
                                                                } else {
                                                                    ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                                                }
                                                            } else {
                                                                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                                            }
                                                        } else {
                                                            ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                                                        }
                                                    }
                                                }

                );
        listViewFavouriteVideos.setOnTouchListener(new

                                                           OnTouchListener() {

                                                               @Override
                                                               public boolean onTouch(View v, MotionEvent event) {
                                                                   // TODO Auto-generated method stub
                                                                   View view = mActivity.getCurrentFocus();
                                                                   if (view != null) {
                                                                       InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                       inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                                                   }


                                                                   return false;
                                                               }
                                                           }

        );


    }

    private void initComponents(View v) {
        // TODO Auto-generated method stub
        listViewFavouriteVideos = (ListView) v
                .findViewById(R.id.favorite_list);

        progressBar = (ProgressBar) v.findViewById(R.id.progressbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));
        }
//        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#CC0000"), android.graphics.PorterDuff.Mode.MULTIPLY);
        tvsearchRecordsNotAvailable = (TextView) v.findViewById(R.id.tvSearchStatus);
        refreshLayout = (PullRefreshLayout) v.findViewById(R.id.refresh_layout);
        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshLayout.setEnabled(false);

        if (Utils.hasNavBar(getActivity())) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, Utils.getNavBarStatusAndHeight(mActivity));
            listViewFavouriteVideos.setLayoutParams(lp);
            refreshLayout.setLayoutParams(lp);
        }

//        listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
//        listViewFavouriteVideos.setFastScrollEnabled(true);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            /*if (bannerCacheableImageView != null && getActivity() != null) {
                // adding the banner
                *//*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_FAVORITESBANNER, getActivity());*//*
                Utils.addBannerImage(bannerCacheableImageView, GlobalConstants.URL_FAVORITESBANNER);
            }*/
            // it is used to track the ecent of opponennts fragment
            FlurryAgent.onEvent(GlobalConstants.FAVORITES);
            if (VideoDataService.isServiceRunning) {
                if (favoriteVideoList.size() == 0) {
                    if (progressBar.isShown()) {
                        System.out.println("favorite isServiceRunning");
                        progressBar.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        //  tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    // tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                }

            } else {
                if (favoriteVideoList.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                } else {
                    //progressBar.setVisibility(View.VISIBLE);
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                }
            }
            countDownTimer = new CountDownTimer(1000, 200) {


                @Override
                public void onTick(long millisUntilFinished) {


                }

                @Override
                public void onFinish() {

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    LocalModel localModel = LocalModel.getInstance();
                    localModel.setmListViewHeight(calculateHeight(listViewFavouriteVideos));

                    if (localModel.getmListViewHeight() < localModel.getmDisplayHeight()) {
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    }
                }
            }.start();

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        //progressBarItem = menu.findItem(R.id.miActionProgress);

        searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(true);

        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                boolean isRecordsAvailableInDb = false;
                favoriteVideoList.clear();
                favoriteVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity).getFavouriteVideosArrayList());
                if (favoriteVideoList.size() > 0) {
                    isRecordsAvailableInDb = true;
                }
//                    listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
//                } else {
//                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
//                }
                if (videoListAdapter != null) {
                    videoListAdapter.listSearch.clear();
                    videoListAdapter.listSearch.addAll(favoriteVideoList);
                    videoListAdapter.filter(newText.toLowerCase(Locale
                            .getDefault()));
                    videoListAdapter.notifyDataSetChanged();
                }

                GlobalConstants.SEARCH_VIEW_QUERY = newText;
                //set Visibility of scroll bar runtime
                Utils.setVisibilityOfScrollBarHeightForNormalList(mActivity,newText, listViewFavouriteVideos);

                if (!newText.isEmpty()) {
                    //check for size after filtering, record availability in db and service running
                    if (favoriteVideoList.size() == 0 && isRecordsAvailableInDb && !VideoDataService.isServiceRunning) {
                        tvsearchRecordsNotAvailable.setText("No Records Found");
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    }
                    if (!isRecordsAvailableInDb) {
                        tvsearchRecordsNotAvailable.setText("No Records Found");
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    if (favoriteVideoList.size() > 0)
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                }

                return false;
            }

        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (VideoDataService.isServiceRunning) {
                    if (favoriteVideoList.size() == 0) {
                        if (progressBar.isShown()) {
                            progressBar.setVisibility(View.VISIBLE);
                            tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        } else {
                            if (!tvsearchRecordsNotAvailable.isShown()) {
                                tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                            }
                            tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    if (favoriteVideoList.size() == 0) {
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    } else {
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        String input = searchView.getQuery().toString();
        if (input != null && !input.equalsIgnoreCase("")) {
            searchView.setIconified(false);
        } else {
            searchView.setIconified(true);
        }
        searchView.clearFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            /*case R.id.action_sync_now:
                syncDialog.showDatabaseConfirmationDialog();
                break;*/

            default:
                break;
        }

        return true;
    }

    public class PullRefreshTask extends AsyncTask<Void, Void, ArrayList<VideoDTO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listViewFavouriteVideos != null) {
                listViewFavouriteVideos.setEnabled(false);
                listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                listViewFavouriteVideos.setVerticalScrollBarEnabled(false);
                listViewFavouriteVideos.setFastScrollEnabled(false);
            }
            refreshLayout.setRefreshing(true);
            if (videoListAdapter != null) {
                videoListAdapter.isPullRefreshInProgress = true;
                videoListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(Void... params) {
            ArrayList<VideoDTO> arrList = new ArrayList<VideoDTO>();
            try {
                String url = GlobalConstants.FAVORITE_API_URL + "userId=" + AppController.getInstance().getUserId();
                arrList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return arrList;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoDTO> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setAllFavoriteStatusToFalse();
                for (VideoDTO vidDto : result) {
                    if (VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).checkVideoAvailability(vidDto.getVideoId())) {
                        VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setFavoriteFlag(1, vidDto.getVideoId());
                    }
                }
                favoriteVideoList.clear();
                favoriteVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());

                Collections.sort(favoriteVideoList, new Comparator<VideoDTO>() {

                    @Override
                    public int compare(VideoDTO lhs, VideoDTO rhs) {
                        // TODO Auto-generated method stub
                        return lhs.getVideoName().toLowerCase()
                                .compareTo(rhs.getVideoName().toLowerCase());
                    }
                });

                if (videoListAdapter != null)
                    videoListAdapter.notifyDataSetChanged();
                else {
                    videoListAdapter = new VideoContentListAdapter(favoriteVideoList, mActivity, 2, true);
                    listViewFavouriteVideos.setAdapter(videoListAdapter);
                }
                videoListAdapter.listSearch.clear();
                videoListAdapter.listSearch.addAll(result);

                if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                    videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                            .getDefault()));
                    videoListAdapter.notifyDataSetChanged();
                }

                if (VideoDataService.isServiceRunning) {
                    if (favoriteVideoList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    if (favoriteVideoList.size() == 0) {
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    } else {
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                    }
                }
                if (videoListAdapter.getCount() == 0) {
                    tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                }
            } else {
                if (favoriteVideoList != null) {
                    favoriteVideoList.clear();
                }
                if (videoListAdapter != null) {
                    videoListAdapter.notifyDataSetChanged();
                }

            }
            if (favoriteVideoList.size() == 0) {
                tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
            }
            if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                tvsearchRecordsNotAvailable.setText("No Records Found");
            }
            videoListAdapter.isPullRefreshInProgress = false;
            refreshLayout.setRefreshing(false);

            if (listViewFavouriteVideos != null) {
                listViewFavouriteVideos.setEnabled(true);
                listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                listViewFavouriteVideos.setVerticalScrollBarEnabled(true);
                listViewFavouriteVideos.setFastScrollEnabled(true);
            }
        }
    }

    private AutoRefreshTask autoRefreshTask;
    CountDownTimer countDownTimer;
    private void autoRefresh() {
        if (Utils.isInternetAvailable(mActivity.getApplicationContext())) {


            countDownTimer = new CountDownTimer(GlobalConstants.AUTO_REFRESH_INTERVAL, GlobalConstants.AUTO_REFRESH_INTERVAL) {

                public void onTick(long millisUntilFinished) {

                    //here you can have your logic to set text to edittext
                    autoRefreshTask = new AutoRefreshTask();
                    autoRefreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }

                public void onFinish() {
                    if (countDownTimer != null) {
                        countDownTimer.start();
                    }
                }

            }.start();


        } else {
            ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }
    }



    public class AutoRefreshTask extends AsyncTask<Void, Void, ArrayList<VideoDTO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (videoListAdapter != null) {
                videoListAdapter.isPullRefreshInProgress = true;
                videoListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(Void... params) {
            ArrayList<VideoDTO> arrList = new ArrayList<VideoDTO>();
            try {
                String url = GlobalConstants.FAVORITE_API_URL + "userId=" + AppController.getInstance().getUserId();
                arrList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return arrList;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoDTO> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setAllFavoriteStatusToFalse();
                for (VideoDTO vidDto : result) {
                    if (VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).checkVideoAvailability(vidDto.getVideoId())) {
                        VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setFavoriteFlag(1, vidDto.getVideoId());
                    }
                }
                favoriteVideoList.clear();
                favoriteVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());

                Collections.sort(favoriteVideoList, new Comparator<VideoDTO>() {

                    @Override
                    public int compare(VideoDTO lhs, VideoDTO rhs) {
                        // TODO Auto-generated method stub
                        return lhs.getVideoName().toLowerCase()
                                .compareTo(rhs.getVideoName().toLowerCase());
                    }
                });

                if (videoListAdapter != null)
                    videoListAdapter.notifyDataSetChanged();
                else {
                    videoListAdapter = new VideoContentListAdapter(favoriteVideoList, mActivity, 2, true);
                    listViewFavouriteVideos.setAdapter(videoListAdapter);
                }
                videoListAdapter.listSearch.clear();
                videoListAdapter.listSearch.addAll(result);

                if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                    videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                            .getDefault()));
                    videoListAdapter.notifyDataSetChanged();
                }

                if (VideoDataService.isServiceRunning) {
                    if (favoriteVideoList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    if (favoriteVideoList.size() == 0) {
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                    } else {
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        listViewFavouriteVideos.setFastScrollAlwaysVisible(true);
                    }
                }
                if (videoListAdapter.getCount() == 0) {
                    tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
                }
            } else {
                if (favoriteVideoList != null) {
                    favoriteVideoList.clear();
                }
                if (videoListAdapter != null) {
                    videoListAdapter.notifyDataSetChanged();
                }

            }
            if (favoriteVideoList.size() == 0) {
                tvsearchRecordsNotAvailable.setText("No favorites have been saved");
                tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listViewFavouriteVideos.setFastScrollAlwaysVisible(false);
            }
            if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                tvsearchRecordsNotAvailable.setText("No Records Found");
            }

            videoListAdapter.isPullRefreshInProgress = false;

        }
    }

    private int calculateHeight(ListView list) {

        int height = 0;

        for (int i = 0; i < list.getCount(); i++) {
            View childView = list.getAdapter().getView(i, null, list);
            childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            height+= childView.getMeasuredHeight();
        }

        //dividers height
        height += list.getDividerHeight() * list.getCount();

        return height;
    }

    /**
     * Method used for get list view height dynamically
     *
     * @param listView
     * @return
     */
    public static int getTotalHeightofNormalListView(ListView listView) {
        int totalHeight = 0;
        try {
            ListAdapter mAdapter = listView.getAdapter();

            for (int i = 0; i < mAdapter.getCount(); i++) {
                View mView = mAdapter.getView(i, null, listView);

                mView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                totalHeight += mView.getMeasuredHeight();
                if (LocalModel.getInstance().getmDisplayHeight() < totalHeight) {
                    break;
                }
                Log.w("HEIGHT" + i, String.valueOf(totalHeight));

            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight
                    + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalHeight;

    }



}