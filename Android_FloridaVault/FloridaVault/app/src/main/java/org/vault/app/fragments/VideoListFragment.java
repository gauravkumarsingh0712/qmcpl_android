package org.vault.app.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.flurry.android.FlurryAgent;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.activities.VideoInfoActivity;
import org.vault.app.adapters.VideoContentHeaderListAdapter;
import org.vault.app.adapters.VideoContentListAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by aqeeb.pathan on 28-08-2015.
 */
public class VideoListFragment extends BaseFragment {

    // ------ Declare/Initialize required fields
    private ArrayList<VideoDTO> videoDTOArrayList = new ArrayList<>();
    private VideoContentListAdapter videoListAdapter;
    private VideoContentHeaderListAdapter headerVideoListAdapter;

    private Activity mActivity;
    private String tabIdentifier;
    private int tabType;
    PullRefreshTask pullTask;
    public boolean isFreshDataLoading = true;

    //--------Declare views elements
    private ListView videoListView;
    private StickyListHeadersListView videoStickyListView;
    private TextView tvSearchStatus;
    private ProgressBar progressBar;
    private PullRefreshLayout refreshLayoutNormal, refreshLayoutSticky;
    private ImageView bannerImageView;
    private SearchView searchView;


    public VideoListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View edgeToEdgeView = inflater.inflate(R.layout.video_list_layout, container,
                false);
        setHasOptionsMenu(true);
        mActivity = getActivity();

        initViews(edgeToEdgeView);
        initData();
        initListeners();

        return edgeToEdgeView;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFreshDataLoading = false;
        try {
            if (edgeToEdgeReceiver != null && mActivity != null)
                mActivity.unregisterReceiver(edgeToEdgeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoListAdapter != null) {
            videoListAdapter.notifyDataSetChanged();
        } else if (headerVideoListAdapter != null) {
            headerVideoListAdapter.notifyDataSetChanged();
        }
        if (tabIdentifier.contains(GlobalConstants.FAVORITES)) {
            if (mActivity != null) {
                videoDTOArrayList.clear();
                videoDTOArrayList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());
                Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                    @Override
                    public int compare(VideoDTO lhs, VideoDTO rhs) {
                        // TODO Auto-generated method stub
                        return lhs.getVideoName().toLowerCase()
                                .compareTo(rhs.getVideoName().toLowerCase());
                    }
                });
                videoListAdapter = new VideoContentListAdapter(videoDTOArrayList, mActivity, tabType, false);
                refreshLayoutNormal.setVisibility(View.VISIBLE);
                videoListView.setAdapter(videoListAdapter);
            }
            isFreshDataLoading = false;
            if (VideoDataService.isServiceRunning) {
                if (videoDTOArrayList.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    tvSearchStatus.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                if (videoDTOArrayList.size() == 0) {
                    tvSearchStatus.setVisibility(View.VISIBLE);
                } else {
                    tvSearchStatus.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (progressBar != null) {
            if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                refreshLayoutNormal.setEnabled(true);
                refreshLayoutNormal.setOnRefreshListener(refreshListener);

                refreshLayoutSticky.setEnabled(true);
                refreshLayoutSticky.setOnRefreshListener(refreshListener);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void initViews(View view) {
        videoListView = (ListView) view.findViewById(R.id.edgeVideoList);
        videoListView.setClickable(true);
        videoListView.setFastScrollEnabled(true);

        videoStickyListView = (StickyListHeadersListView) view.findViewById(R.id.wide_list_view);
        videoStickyListView.setFastScrollEnabled(true);

        bannerImageView = (ImageView) view.findViewById(R.id.img_banner);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        tvSearchStatus = (TextView) view.findViewById(R.id.tvSearchStatus);
        refreshLayoutNormal = (PullRefreshLayout) view.findViewById(R.id.refresh_layout_normal);
        refreshLayoutNormal.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshLayoutNormal.setEnabled(false);

        refreshLayoutSticky = (PullRefreshLayout) view.findViewById(R.id.refresh_layout_sticky);
        refreshLayoutSticky.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshLayoutSticky.setEnabled(false);

        if (Utils.hasNavBar(getActivity())) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, Utils.getNavBarStatusAndHeight(mActivity));
            /*videoListView.setLayoutParams(lp);
            videoStickyListView.setLayoutParams(lp);*/
            refreshLayoutNormal.setLayoutParams(lp);
            refreshLayoutSticky.setLayoutParams(lp);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        else
            progressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));
    }

    public void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            tabIdentifier = bundle.getString("tabIdentifier");
            tabType = bundle.getInt("tabType");
        }
        AsyncTask<Void, Void, Void> mDbTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (videoDTOArrayList.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    IntentFilter filter = new IntentFilter(tabIdentifier);
                    filter.addCategory(Intent.CATEGORY_DEFAULT);
                    mActivity.registerReceiver(edgeToEdgeReceiver, filter);

                    videoDTOArrayList.clear();
                    if (!tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                        videoDTOArrayList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoListByTab(tabIdentifier));
                    } else {
                        videoDTOArrayList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (tabIdentifier.contains(GlobalConstants.FEATURED) || tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return lhs.getVideoName().toLowerCase()
                                    .compareTo(rhs.getVideoName().toLowerCase());
                        }
                    });
                    videoListAdapter = new VideoContentListAdapter(videoDTOArrayList, mActivity, tabType, false);
                    refreshLayoutNormal.setVisibility(View.VISIBLE);
                    videoListView.setAdapter(videoListAdapter);
                } else {
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return lhs.getPlaylistName().toLowerCase()
                                    .compareTo(rhs.getPlaylistName().toLowerCase());
                        }
                    });
                    headerVideoListAdapter = new VideoContentHeaderListAdapter(videoDTOArrayList, mActivity, tabType, true, false);
                    refreshLayoutSticky.setVisibility(View.VISIBLE);
                    videoStickyListView.setAdapter(headerVideoListAdapter);
                }

                if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                    if (videoListAdapter != null)
                        videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                    else if (headerVideoListAdapter != null)
                        headerVideoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                }

                if (VideoDataService.isServiceRunning) {
                    if (videoDTOArrayList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        tvSearchStatus.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    if (videoDTOArrayList.size() == 0) {
                        tvSearchStatus.setVisibility(View.VISIBLE);
                    } else {
                        tvSearchStatus.setVisibility(View.INVISIBLE);
                    }
                }
                // ------- addImageByCaching---------------------
                /*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_FEATUREDBANNER, mActivity);*/
                Utils.addImageByCaching(bannerImageView, GlobalConstants.URL_FEATUREDBANNER);

                if (progressBar != null) {
                    if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                        refreshLayoutNormal.setEnabled(true);
                        refreshLayoutNormal.setOnRefreshListener(refreshListener);

                        refreshLayoutSticky.setEnabled(true);
                        refreshLayoutSticky.setOnRefreshListener(refreshListener);
                    }
                }
            }
        };

        mDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void initListeners() {
        //Listeners for normal ListView
        videoListView.setOnTouchListener(new View.OnTouchListener() {

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

        videoListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {
                        // TODO Auto-generated method stub
                        openVideoPlayer(pos);
                    }
                });

        videoListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (((MainActivity) mActivity).progressDialog != null)
                    if (((MainActivity) mActivity).progressDialog.isShowing())
                        ((MainActivity) mActivity).progressDialog.dismiss();
            }
        });

        videoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) mActivity).makeShareDialog(videoDTOArrayList.get(position).getVideoLongUrl(), videoDTOArrayList.get(position).getVideoShortUrl(), videoDTOArrayList.get(position).getVideoStillUrl(), videoDTOArrayList.get(position).getVideoLongDescription(), videoDTOArrayList.get(position).getVideoName(), getActivity());
                return true;
            }
        });

        //Listeners for StickyHeaderListView
        videoStickyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) mActivity).makeShareDialog(videoDTOArrayList.get(position).getVideoLongUrl(), videoDTOArrayList.get(position).getVideoShortUrl(), videoDTOArrayList.get(position).getVideoStillUrl(), videoDTOArrayList.get(position).getVideoLongDescription(), videoDTOArrayList.get(position).getVideoName(), getActivity());
                return true;
            }
        });

        videoStickyListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (((MainActivity) mActivity).progressDialog != null)
                    if (((MainActivity) mActivity).progressDialog.isShowing())
                        ((MainActivity) mActivity).progressDialog.dismiss();
            }
        });

        // TODO Auto-generated method stub
        videoStickyListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {
                        // TODO Auto-generated method stub
                        openVideoPlayer(pos);
                    }
                });

        videoStickyListView.setOnTouchListener(new View.OnTouchListener() {

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
    }

    public void openVideoPlayer(int pos){
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        if (Utils.isInternetAvailable(mActivity)) {
            if (videoDTOArrayList.get(pos).getVideoLongUrl() != null) {
                if (videoDTOArrayList.get(pos).getVideoLongUrl().length() > 0) {
                    String videoCategory = GlobalConstants.PLAYERS;
                    Intent intent = new Intent(mActivity,
                            VideoInfoActivity.class);
                    intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                    intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, videoDTOArrayList.get(pos).getPlaylistReferenceId());
                    intent.putExtra(GlobalConstants.VIDEO_OBJ, videoDTOArrayList.get(pos));
                    GlobalConstants.LIST_FRAGMENT = new PlayerFragment();
                    GlobalConstants.LIST_ITEM_POSITION = pos;
                    startActivity(intent);
                } else {
//                                Toast.makeText(context, "Video Data Not Available", Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);

                }
            } else {
//                            Toast.makeText(context, "Video Data Not Available", Toast.LENGTH_SHORT).show();
                ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
            }
        } else {
            ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }
    }


    PullRefreshLayout.OnRefreshListener refreshListener = new PullRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Utils.isInternetAvailable(mActivity.getApplicationContext())) {
                pullTask = new PullRefreshTask();
                pullTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                refreshLayoutNormal.setRefreshing(false);
                refreshLayoutSticky.setRefreshing(false);
            }
        }
    };

    public class PullRefreshTask extends AsyncTask<Void, Void, ArrayList<VideoDTO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (videoListAdapter != null)
                refreshLayoutNormal.setRefreshing(true);
            else
                refreshLayoutSticky.setRefreshing(true);
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(Void... params) {
            ArrayList<VideoDTO> arrList = new ArrayList<VideoDTO>();
            try {
                String url = "";
                if (tabIdentifier.toLowerCase().contains("featured"))
                    url = GlobalConstants.FEATURED_API_URL + "userId=" + AppController.getInstance().getUserId();
                else if (tabIdentifier.toLowerCase().contains("player"))
                    url = GlobalConstants.PLAYER_API_URL + "userId=" + AppController.getInstance().getUserId();
                else if (tabIdentifier.toLowerCase().contains("coach"))
                    url = GlobalConstants.COACH_API_URL + "userId=" + AppController.getInstance().getUserId();
                else if (tabIdentifier.toLowerCase().contains("opponent"))
                    url = GlobalConstants.OPPONENT_API_URL + "userId=" + AppController.getInstance().getUserId();
                else if (tabIdentifier.toLowerCase().contains("favorite"))
                    url = GlobalConstants.FAVORITE_API_URL + "userId=" + AppController.getInstance().getUserId();

                arrList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                if (arrList.size() > 0) {
                    if (!tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                        VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).removeRecordsByTab(tabIdentifier);
                        VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).insertVideosInDatabase(arrList);
                    } else {
                        VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setAllFavoriteStatusToFalse();
                        for (VideoDTO vidDto : arrList) {
                            if (VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).checkVideoAvailability(vidDto.getVideoId())) {
                                VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).setFavoriteFlag(1, vidDto.getVideoId());
                            }
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
                videoDTOArrayList.clear();
                videoDTOArrayList.addAll(result);

                if (tabIdentifier.contains(GlobalConstants.FAVORITES) || tabIdentifier.contains(GlobalConstants.FEATURED)) {
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

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
                        videoListAdapter = new VideoContentListAdapter(videoDTOArrayList, mActivity, tabType, false);
                        videoListView.setAdapter(videoListAdapter);
                    }
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                        videoListAdapter.notifyDataSetChanged();
                    }
                } else {
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return lhs.getPlaylistName().toLowerCase()
                                    .compareTo(rhs.getPlaylistName().toLowerCase());
                        }
                    });
                    if (headerVideoListAdapter != null)
                        headerVideoListAdapter.notifyDataSetChanged();
                    else {
                        headerVideoListAdapter = new VideoContentHeaderListAdapter(videoDTOArrayList,mActivity,tabType, true, false);
                        videoStickyListView.setAdapter(headerVideoListAdapter);
                    }
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        headerVideoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                        headerVideoListAdapter.notifyDataSetChanged();
                    }
                }

                if (VideoDataService.isServiceRunning) {
                    if (videoDTOArrayList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        tvSearchStatus.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    if (videoDTOArrayList.size() == 0) {
                        tvSearchStatus.setVisibility(View.VISIBLE);
                    } else {
                        tvSearchStatus.setVisibility(View.INVISIBLE);
                    }
                }
            }
            refreshLayoutNormal.setRefreshing(false);
            refreshLayoutSticky.setRefreshing(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(true);

        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                boolean isRecordsAvailableInDb = false;
                ArrayList<VideoDTO> videoList = new ArrayList<>();
                if (!tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                    videoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoListByTab(tabIdentifier));
                } else {
                    videoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());
                }
                if (videoList.size() > 0)
                    isRecordsAvailableInDb = true;
                GlobalConstants.SEARCH_VIEW_QUERY = newText;
                if (videoListAdapter != null) {
                    videoListAdapter.filter(newText.toLowerCase(Locale
                            .getDefault()));
//                    listViewVideo.setAdapter(featuredVideosListAdapter);
                    videoListAdapter.notifyDataSetChanged();
                } else if (headerVideoListAdapter != null) {
                    headerVideoListAdapter.filter(newText.toLowerCase(Locale
                            .getDefault()));
                    headerVideoListAdapter.notifyDataSetChanged();
                }
                if (!newText.isEmpty()) {
                    if ((videoDTOArrayList.size() == 0 && isRecordsAvailableInDb) || (videoDTOArrayList.size() == 0 && !VideoDataService.isServiceRunning)) {
                        tvSearchStatus.setText("No Records Found");
                        tvSearchStatus.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else
                        tvSearchStatus.setVisibility(View.INVISIBLE);
                } else {
                    if (VideoDataService.isServiceRunning) {
                        if (videoDTOArrayList.size() == 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            tvSearchStatus.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        if (videoDTOArrayList.size() == 0) {
                            tvSearchStatus.setVisibility(View.VISIBLE);
                        } else {
                            tvSearchStatus.setVisibility(View.INVISIBLE);
                        }
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            if (bannerImageView != null && mActivity != null) {
                // ---- addImageByCaching--------
                /*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_FEATUREDBANNER, mActivity);*/
                Utils.addImageByCaching(bannerImageView, GlobalConstants.URL_FEATUREDBANNER);
            }
            // it is used to track the ecent of opponennts fragment
            FlurryAgent.onEvent(GlobalConstants.FEATURED);

        }
    }

    private final BroadcastReceiver edgeToEdgeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String identifier = intent.getStringExtra("tabIdentifier");
            if (identifier.equals(tabIdentifier)) {
                videoDTOArrayList.clear();
                if (!tabIdentifier.contains(GlobalConstants.FAVORITES))
                    videoDTOArrayList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoListByTab(tabIdentifier));
                else
                    videoDTOArrayList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getFavouriteVideosArrayList());

                if (tabIdentifier.contains(GlobalConstants.FEATURED) || tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                    videoListAdapter = new VideoContentListAdapter(videoDTOArrayList, mActivity, tabType, false);
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return lhs.getVideoName().toLowerCase()
                                    .compareTo(rhs.getVideoName().toLowerCase());
                        }
                    });
                } else {
                    headerVideoListAdapter = new VideoContentHeaderListAdapter(videoDTOArrayList, mActivity, tabType, true, false);
                    Collections.sort(videoDTOArrayList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return lhs.getPlaylistName().toLowerCase()
                                    .compareTo(rhs.getPlaylistName().toLowerCase());
                        }
                    });
                }

                if (tabIdentifier.contains(GlobalConstants.FEATURED) || tabIdentifier.contains(GlobalConstants.FAVORITES)) {
                    videoListAdapter = new VideoContentListAdapter(videoDTOArrayList, mActivity, tabType, false);
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                    }
                    videoListView.setAdapter(videoListAdapter);
                    refreshLayoutNormal.setVisibility(View.VISIBLE);
                } else {
                    headerVideoListAdapter = new VideoContentHeaderListAdapter(videoDTOArrayList,mActivity,tabType, true, false);
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        headerVideoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                                .getDefault()));
                    }
                    videoStickyListView.setAdapter(headerVideoListAdapter);
                    refreshLayoutSticky.setVisibility(View.VISIBLE);
                }

                if (VideoDataService.isServiceRunning) {
                    if (videoDTOArrayList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        tvSearchStatus.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    if (videoDTOArrayList.size() == 0) {
                        tvSearchStatus.setVisibility(View.VISIBLE);
                    } else {
                        tvSearchStatus.setVisibility(View.INVISIBLE);
                    }
                }

                if (progressBar != null) {
                    if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                        refreshLayoutNormal.setEnabled(true);
                        refreshLayoutNormal.setOnRefreshListener(refreshListener);

                        refreshLayoutSticky.setEnabled(true);
                        refreshLayoutSticky.setOnRefreshListener(refreshListener);
                    }
                }
            }
        }
    };
}