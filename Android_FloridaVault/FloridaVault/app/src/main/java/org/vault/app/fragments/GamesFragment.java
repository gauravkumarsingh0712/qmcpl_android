package org.vault.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.activities.VideoInfoActivity;
import org.vault.app.adapters.VideoContentHeaderListAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * @author aqeeb.pathan
 */
public class GamesFragment extends BaseFragment {
    public static StickyListHeadersListView stickyListHeadersListView;

    public ImageView bannerCacheableImageView;
    public VideoContentHeaderListAdapter videoHeaderListAdapter;
    public ArrayList<VideoDTO> gamesVideoList = new ArrayList<VideoDTO>();
    public static TextView tvsearchRecordsNotAvailable;
    public static ProgressBar progressBar;
    public SearchView searchView;

    public boolean isLastPageLoaded = false;
    GamesResponseReceiver receiver;
    PullRefreshLayout refreshLayout;
    PullRefreshTask pullTask;

    String url = "";
    Activity mActivity;
    private TabBannerDTO tabBannerDTO;
    private ProgressDialog pDialog;
    private ProgressBar mBannerProgressBar;
    private LinearLayout bannerLayout;
    private ProgressBar auto_refresh_progress_bar;
    static GamesFragment gamesFragment;
    private String tabId;
    CountDownTimer countDownTimer;
    public GamesFragment() {

    }

    public static GamesFragment getInstance() {
        if (gamesFragment == null) {
            gamesFragment = new GamesFragment();
        }
        return gamesFragment;

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (receiver != null && mActivity != null) {
                //    mActivity.unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (videoHeaderListAdapter != null)
            videoHeaderListAdapter.notifyDataSetChanged();
        if (progressBar != null && refreshLayout != null) {
            if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                refreshLayout.setEnabled(true);
                refreshLayout.setOnRefreshListener(refreshListener);
            }
        }

        if (gamesVideoList != null && gamesVideoList.size() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        tabId = bundle.getString("tabId");

        // tabBannerDTO = (TabBannerDTO) bundle.getSerializable("tabObject");
        tabBannerDTO = VaultDatabaseHelper.getInstance(getActivity()).getLocalTabBannerDataByTabId(Long.valueOf(tabId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.opponets_coaches_playersfragment_layout, container, false);
        mActivity = getActivity();
        isLastPageLoaded = false;
//        isFreshDataLoading = true;
        // --------Intializing Views---------
        initComponents(v);
        System.out.println("Games Video List Count : " + gamesVideoList.size());

        setHasOptionsMenu(true);

        // ------registerevents---------
        registerEvents();
        getGameDataFromDataBase();

        return v;
    }

    private void getGameDataFromDataBase() {


        AsyncTask<Void, Void, Void> mDbTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (gamesVideoList.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    IntentFilter filter = new IntentFilter(GamesResponseReceiver.ACTION_RESP);
                    filter.addCategory(Intent.CATEGORY_DEFAULT);
                    receiver = new GamesResponseReceiver();
                    mActivity.registerReceiver(receiver, filter);

                    gamesVideoList.clear();
                    gamesVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoList(GlobalConstants.OKF_GAMES));

                    Collections.sort(gamesVideoList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return rhs.getPlaylistName().toLowerCase()
                                    .compareTo(lhs.getPlaylistName().toLowerCase());
                        }
                    });

                    /*stickyHeaderListAdapter = new StickyHeaderListAdapter(mActivity, gamesVideoList);
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        stickyHeaderListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                    }*/
                    videoHeaderListAdapter = new VideoContentHeaderListAdapter(gamesVideoList, mActivity, 1, false, true);
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        videoHeaderListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                stickyListHeadersListView.setAdapter(videoHeaderListAdapter);

                if (gamesVideoList.size() == 0 && VideoDataService.isServiceRunning) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                // ---- add banner---------
                if (tabBannerDTO != null)
                    Utils.addBannerImage(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity);

                if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setOnRefreshListener(refreshListener);
                }
                //visibility of scroll bar set dynamically list height
                Utils.setVisibilityOfScrollBarHeightForHeader(GlobalConstants.SEARCH_VIEW_QUERY, stickyListHeadersListView);
            }
        };

        mDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        updateBannerImage();
    }

    /*public void setUpPullOptionHeader(View view){
        final View pullView = view.findViewById(R.id.rl_pull_option);

        final SharedPreferences prefs = mActivity.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        boolean isPullHeaderSeen = prefs.getBoolean(GlobalConstants.PREF_PULL_HEADER_GAMES, false);

        Button btnGotIt = (Button) pullView.findViewById(R.id.btn_got_it);

        btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean(GlobalConstants.PREF_PULL_HEADER_GAMES, true).commit();

                Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.abc_fade_out);
                pullView.setVisibility(View.GONE);
                pullView.setAnimation(anim);
                if (!progressBar.isShown()) {
                    if (Utils.isInternetAvailable(mActivity.getApplicationContext())) {
                        pullTask = new PullRefreshTask();
                        pullTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                        refreshLayout.setRefreshing(false);
                    }
                }
            }
        });

        if(isPullHeaderSeen){
            pullView.setVisibility(View.GONE);
        }
    }*/

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
                    pullTask = new PullRefreshTask();
                    pullTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else {
                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                refreshLayout.setRefreshing(false);
            }
        }
    };

    private void registerEvents() {
        stickyListHeadersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                ((MainActivity) mActivity).makeShareDialog(gamesVideoList.get(position).getVideoLongUrl(), gamesVideoList.get(position).getVideoShortUrl(), gamesVideoList.get(position).getVideoStillUrl(), gamesVideoList.get(position).getVideoLongDescription(), gamesVideoList.get(position).getVideoName(), getActivity());
                return true;
            }
        });



        // TODO Auto-generated method stub
        stickyListHeadersListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {

                        if (!videoHeaderListAdapter.isPullRefreshInProgress) {
                            // TODO Auto-generated method stub
                            View view = mActivity.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                            if (Utils.isInternetAvailable(mActivity)) {
                                if (gamesVideoList.get(pos).getVideoLongUrl() != null) {
                                    if (gamesVideoList.get(pos).getVideoLongUrl().length() > 0 && !gamesVideoList.get(pos).getVideoLongUrl().toLowerCase().equals("none")) {
                                        String videoCategory = GlobalConstants.GAMES;
                                        Intent intent = new Intent(mActivity,
                                                VideoInfoActivity.class);
                                        intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                                        intent.putExtra(GlobalConstants.VIDEO_OBJ, gamesVideoList.get(pos));
                                        GlobalConstants.LIST_FRAGMENT = new GamesFragment();
                                        GlobalConstants.LIST_ITEM_POSITION = pos;
                                        mActivity.startActivity(intent);
                                        mActivity.overridePendingTransition(R.anim.slide_up_video_info, R.anim.nochange);
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
                });

        stickyListHeadersListView.setOnTouchListener(new OnTouchListener() {

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
        });

        stickyListHeadersListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                view.setScrollBarSize(0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (((MainActivity) mActivity).progressDialog != null)
                    if (((MainActivity) mActivity).progressDialog.isShowing())
                        ((MainActivity) mActivity).progressDialog.dismiss();
            }

        });


        bannerCacheableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabBannerDTO != null) {
                    if (tabBannerDTO.isBannerActive()) {
                        if (tabBannerDTO.isHyperlinkActive() && tabBannerDTO.getBannerActionURL().length() > 0) {
                            //Start the ActionUrl in Browser
                            Uri uri = Uri.parse(tabBannerDTO.getBannerActionURL());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(tabBannerDTO.getBannerActionURL())));

                            }
                        } else if (!tabBannerDTO.isHyperlinkActive() && tabBannerDTO.getBannerActionURL().length() > 0) {
                            //The ActionUrl has DeepLink associated with it
                            HashMap videoMap = Utils.getInstance().getVideoInfoFromBanner(tabBannerDTO.getBannerActionURL());
                            if (videoMap != null) {
                                if (videoMap.get("VideoId") != null) {
                                    if (VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).isVideoAvailableInDB(videoMap.get("VideoId").toString())) {
                                        VideoDTO videoDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoDataByVideoId(videoMap.get("VideoId").toString());
                                        if (Utils.isInternetAvailable(mActivity)) {
                                            if (videoDTO != null) {
                                                if (videoDTO.getVideoLongUrl() != null) {
                                                    //  if (videoDTO.getVideoLongUrl().length() > 0 && !videoDTO.getVideoLongUrl().toLowerCase().equals("none")) {
                                                    String videoCategory = GlobalConstants.GAMES;
                                                    Intent intent = new Intent(mActivity,
                                                            VideoInfoActivity.class);
                                                    intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                                                    intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, videoDTO.getPlaylistReferenceId());
                                                    intent.putExtra(GlobalConstants.VIDEO_OBJ, videoDTO);
                                                    startActivity(intent);
                                                    mActivity.overridePendingTransition(R.anim.slide_up_video_info, R.anim.nochange);
                                                } /*else {
                                                        ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                                    }*/
                                            } else {
                                                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                            }
                                        } else {
                                            ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                                        }

                                    } else {
                                        //Make an API call to get video data
                                        System.out.println("Video not available in the local database. Making server call for video.");
                                        VideoDataTask task = new VideoDataTask();
                                        task.execute(videoMap);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void initComponents(View v) {
        // TODO Auto-generated method stub
        stickyListHeadersListView = (StickyListHeadersListView) v
                .findViewById(R.id.lv_stickyheader);
        stickyListHeadersListView.setFastScrollEnabled(true);


        mBannerProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBannerProgressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mBannerProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.progress_large_material, null));
        }
        bannerCacheableImageView = (ImageView) v
                .findViewById(R.id.imv_opponents_coaches_playe_banner);
        bannerLayout = (LinearLayout) v
                .findViewById(R.id.ll_banner_block);

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
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, Utils.getNavBarStatusAndHeight(mActivity));
            refreshLayout.setLayoutParams(lp);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        // progressBarItem = menu.findItem(R.id.miActionProgress);

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
                boolean isRecordsAvailableInDb = false;
                ArrayList<VideoDTO> videoList = VaultDatabaseHelper.getInstance(mActivity).getVideoList(GlobalConstants.OKF_GAMES);
                if (videoList.size() > 0) {
                    isRecordsAvailableInDb = true;
                }
//                    stickyListHeadersListView.setFastScrollAlwaysVisible(true);
//                } else
//                    stickyListHeadersListView.setFastScrollAlwaysVisible(false);
                GlobalConstants.SEARCH_VIEW_QUERY = newText;

                if (videoHeaderListAdapter != null) {
                    videoHeaderListAdapter.filter(newText.toLowerCase(Locale
                            .getDefault()));
                    videoHeaderListAdapter.notifyDataSetChanged();
                }

                //visibility of scroll bar set dynamically list height
                if (stickyListHeadersListView != null) {
                    Utils.setVisibilityOfScrollBarHeightForHeader(newText, stickyListHeadersListView);
                }

                if (!newText.isEmpty()) {

                    if ((gamesVideoList.size() == 0 && isRecordsAvailableInDb) || (gamesVideoList.size() == 0 && !VideoDataService.isServiceRunning)) {
                        tvsearchRecordsNotAvailable.setText("No Records Found");
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        stickyListHeadersListView.setFastScrollAlwaysVisible(false);
                    } else {
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        stickyListHeadersListView.setFastScrollAlwaysVisible(true);
                    }
                } else {
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    if (gamesVideoList.size() > 0)
                        stickyListHeadersListView.setFastScrollAlwaysVisible(true);
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
//        searchView.setSubmitButtonEnabled(true);
        String input = searchView.getQuery().toString();
        if (input != null && !input.equalsIgnoreCase("")) {
            searchView.setIconified(false);
        } else {
            searchView.setIconified(true);
        }
        searchView.clearFocus();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            /*if (bannerCacheableImageView != null && mActivity != null) {
                // adding the banner
                *//*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_GAMESBANNER, mActivity);*//*
                Utils.addBannerImage(bannerCacheableImageView, GlobalConstants.URL_GAMESBANNER);
            }*/
            // it is used to track the ecent of opponennts fragment
            FlurryAgent.onEvent(GlobalConstants.GAMES);
            if (progressBar != null) {
                if (gamesVideoList != null && gamesVideoList.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

        }
    }


    public class VideoDataTask extends AsyncTask<HashMap, Void, ArrayList<VideoDTO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mActivity, R.style.CustomDialogTheme);
            pDialog.show();
            pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(mActivity));
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(HashMap... params) {
            ArrayList<VideoDTO> videoList = AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(GlobalConstants.GET_VIDEO_DATA_FROM_BANNER + "?navTabId=" + params[0].get("TabId").toString() + "&videoId=" + params[0].get("VideoId").toString() + "&userId=" + AppController.getInstance().getUserId());
            System.out.println("Video List Size from server : " + videoList.size());
            return videoList;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoDTO> videoDTOs) {
            super.onPostExecute(videoDTOs);
            if (videoDTOs.size() > 0) {
                VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).insertVideosInDatabase(videoDTOs);
                if (Utils.isInternetAvailable(mActivity)) {
                    if (videoDTOs.get(0).getVideoLongUrl() != null) {
                        if (videoDTOs.get(0).getVideoLongUrl().length() > 0 && !videoDTOs.get(0).getVideoLongUrl().toLowerCase().equals("none")) {
                            String videoCategory = GlobalConstants.GAMES;
                            Intent intent = new Intent(mActivity,
                                    VideoInfoActivity.class);
                            intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                            intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, videoDTOs.get(0).getPlaylistReferenceId());
                            intent.putExtra(GlobalConstants.VIDEO_OBJ, videoDTOs.get(0));
                            startActivity(intent);
                            mActivity.overridePendingTransition(R.anim.slide_up_video_info, R.anim.nochange);
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
            pDialog.dismiss();
        }
    }

    public class PullRefreshTask extends AsyncTask<Void, Void, ArrayList<VideoDTO>> {

        public boolean isBannerUpdated = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (stickyListHeadersListView != null) {
                Utils.setDisabledStickyListHeadersListViewScrolling(stickyListHeadersListView);
            }

            refreshLayout.setRefreshing(true);
            if (videoHeaderListAdapter != null) {
                videoHeaderListAdapter.isPullRefreshInProgress = true;
                videoHeaderListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(Void... params) {
            ArrayList<VideoDTO> arrList = new ArrayList<VideoDTO>();
            try {
                String url = GlobalConstants.GAMES_API_URL + "page=0&userId=" + AppController.getInstance().getUserId();
                arrList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));

                if (arrList.size() > 0) {
                    VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).removeRecordsByTab("OKFGames");
                    VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).insertVideosInDatabase(arrList);
                }

                //Update Banner Data
                if (tabBannerDTO != null) {
                    TabBannerDTO serverObj = AppController.getInstance().getServiceManager().getVaultService().getTabBannerDataById(tabBannerDTO.getTabBannerId(), tabBannerDTO.getTabKeyword(), tabBannerDTO.getTabId());
                    if (serverObj != null) {
                        if ((tabBannerDTO.getBannerModified() != serverObj.getBannerModified()) || (tabBannerDTO.getBannerCreated() != serverObj.getBannerCreated())) {
                            File imageFile = ImageLoader.getInstance().getDiscCache().get(tabBannerDTO.getBannerURL());
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                            MemoryCacheUtils.removeFromCache(tabBannerDTO.getBannerURL(), ImageLoader.getInstance().getMemoryCache());

                            VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).updateTabBannerData(serverObj);
                            isBannerUpdated = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return arrList;
        }

        @Override
        protected void onPostExecute(final ArrayList<VideoDTO> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                gamesVideoList.clear();

                gamesVideoList.addAll(result);

                Collections.sort(gamesVideoList, new Comparator<VideoDTO>() {

                    @Override
                    public int compare(VideoDTO lhs, VideoDTO rhs) {
                        // TODO Auto-generated method stub
                        return rhs.getPlaylistName().toLowerCase()
                                .compareTo(lhs.getPlaylistName().toLowerCase());
                    }
                });

                if (videoHeaderListAdapter != null) {
                    videoHeaderListAdapter.listSearch.clear();
                    videoHeaderListAdapter.listSearch.addAll(result);

                    videoHeaderListAdapter.notifyDataSetChanged();
                    videoHeaderListAdapter.updateIndexer();
                } else {
                    videoHeaderListAdapter = new VideoContentHeaderListAdapter(gamesVideoList, mActivity, 1, false, true);
                    stickyListHeadersListView.setAdapter(videoHeaderListAdapter);
                }
                if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                    videoHeaderListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                            .getDefault()));
                    videoHeaderListAdapter.notifyDataSetChanged();
                }
                if (videoHeaderListAdapter.getCount() == 0) {
                    tvsearchRecordsNotAvailable.setText("No Records Found");
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    stickyListHeadersListView.setFastScrollAlwaysVisible(false);
                    progressBar.setVisibility(View.GONE);
                }
            }
            if (gamesVideoList.size() == 0) {
                tvsearchRecordsNotAvailable.setText("No Records Found");
                tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                stickyListHeadersListView.setFastScrollAlwaysVisible(false);
                progressBar.setVisibility(View.GONE);
            }
            if (isBannerUpdated)
                if (tabBannerDTO != null) {
                    tabBannerDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getLocalTabBannerDataByTabId(tabBannerDTO.getTabId());
                    if (tabBannerDTO != null)
                        Utils.addBannerImagePullToRefresh(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity, mBannerProgressBar);
                }
            videoHeaderListAdapter.isPullRefreshInProgress = false;
            refreshLayout.setRefreshing(false);


            if (stickyListHeadersListView != null) {
                Utils.setEnabledStickyListHeadersListViewScrolling(stickyListHeadersListView);
            }


        }
    }

    public class GamesResponseReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "Message Processed";

        @Override
        public void onReceive(Context context, Intent intent) {

            gamesVideoList.clear();
            gamesVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoList(GlobalConstants.OKF_GAMES));

            Collections.sort(gamesVideoList, new Comparator<VideoDTO>() {

                @Override
                public int compare(VideoDTO lhs, VideoDTO rhs) {
                    // TODO Auto-generated method stub
                    return rhs.getPlaylistName().toLowerCase()
                            .compareTo(lhs.getPlaylistName().toLowerCase());
                }
            });

            /*stickyHeaderListAdapter = new StickyHeaderListAdapter(mActivity, gamesVideoList);
            if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                stickyHeaderListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                        .getDefault()));
            }
            stickyListHeadersListView.setAdapter(stickyHeaderListAdapter);*/

            videoHeaderListAdapter = new VideoContentHeaderListAdapter(gamesVideoList, mActivity, 1, false, true);
            if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                videoHeaderListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                        .getDefault()));
            }
            stickyListHeadersListView.setAdapter(videoHeaderListAdapter);
            System.out.println("tabBannerDTO gamesVideoList " + gamesVideoList.size() + " isServiceRunning " + VideoDataService.isServiceRunning);
            if (gamesVideoList.size() == 0 /*&& VideoDataService.isServiceRunning*/) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (progressBar != null) {
                if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setOnRefreshListener(refreshListener);
                }
            }

            tabBannerDTO = VaultDatabaseHelper.getInstance(getActivity()).getLocalTabBannerDataByTabId(Long.valueOf(tabId));
            System.out.println("tabBannerDTO game " + tabBannerDTO);
            if (tabBannerDTO != null) {
                tabBannerDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getLocalTabBannerDataByTabId(tabBannerDTO.getTabId());
                if (tabBannerDTO != null)
                    Utils.addBannerImagePullToRefresh(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity, mBannerProgressBar);
            }
        }
    }




    private void updateBannerImage() {
        countDownTimer = new CountDownTimer(GlobalConstants.AUTO_REFRESH_INTERVAL, GlobalConstants.AUTO_REFRESH_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                tabBannerDTO = VaultDatabaseHelper.getInstance(getActivity()).getLocalTabBannerDataByTabId(Long.valueOf(tabId));
                if (tabBannerDTO != null) {
                    tabBannerDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getLocalTabBannerDataByTabId(tabBannerDTO.getTabId());
                    if (tabBannerDTO != null) {
                        Utils.addBannerImagePullToRefresh(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity, mBannerProgressBar);
                    }
                }
            }

            @Override
            public void onFinish() {

                if (countDownTimer != null) {
                    countDownTimer.start();
                }

            }
        }.start();
    }


}