package org.vault.app.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.flurry.android.FlurryAgent;
import com.ncsavault.floridavault.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import org.vault.app.activities.MainActivity;
import org.vault.app.activities.VideoInfoActivity;
import org.vault.app.adapters.VideoContentListAdapter;
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

/**
 * @author aqeeb.pathan
 */
public class FeaturedFragment extends BaseFragment {
    public static ListView listViewFeaturedVideo;
    public ImageView bannerCacheableImageView;
    public static TextView tvsearchRecordsNotAvailable;
    public VideoContentListAdapter videoListAdapter;
    public ArrayList<VideoDTO> featuredVideoList = new ArrayList<>();
    public static ProgressBar progressBar;
    SearchView searchView;
    public boolean isLastPageLoaded = false;
    public String url = "";
    Activity mActivity;
    private PullRefreshLayout refreshLayout;
    private TabBannerDTO tabBannerDTO = null;

    PullRefreshTask pullTask;
    FeaturedResponseReceiver receiver;
    private ProgressDialog pDialog;
    private ProgressBar mBannerProgressBar;
    private LinearLayout bannerLayout;

    public FeaturedFragment() {

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        String tabId = bundle.getString("tabId");
        // tabBannerDTO = (TabBannerDTO) bundle.getSerializable("tabObject");
        tabBannerDTO = VaultDatabaseHelper.getInstance(getActivity()).getLocalTabBannerDataByTabId(Long.valueOf(tabId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
//        Utils.hideSystemUI(getActivity());
        View view = inflater.inflate(R.layout.featured_video_layout, container,
                false);
        setHasOptionsMenu(true);
        isLastPageLoaded = false;
//        isFreshDataLoading = true;
        if (getActivity() != null)
            mActivity = getActivity();

        // -------initializing views-------------
        initComponents(view);
        System.out.println("Featured Video List Count : " + featuredVideoList.size());

        setHasOptionsMenu(true);

        AsyncTask<Void, Void, Void> mDbTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (featuredVideoList.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    IntentFilter filter = new IntentFilter(FeaturedResponseReceiver.ACTION_RESP);
                    filter.addCategory(Intent.CATEGORY_DEFAULT);
                    receiver = new FeaturedResponseReceiver();
                    mActivity.registerReceiver(receiver, filter);

                    featuredVideoList.clear();
                    featuredVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoList(GlobalConstants.OKF_FEATURED));

                    Collections.sort(featuredVideoList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            /*Double videoIndex = Double.valueOf(lhs.getVideoIndex());
                            Double videoIndex1 = Double.valueOf(rhs.getVideoIndex());
                            if (videoIndex.compareTo(videoIndex1) < 0)
                                return -1;
                            else if (videoIndex.compareTo(videoIndex1) > 0)
                                return 1;
                            else
                                return 0;*/
                            return Integer.valueOf(lhs.getVideoIndex())
                                    .compareTo(Integer.valueOf(rhs.getVideoIndex()));
                        }
                    });
                    videoListAdapter = new VideoContentListAdapter(featuredVideoList, mActivity, 1, false);
                    if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                        videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
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
                listViewFeaturedVideo.setAdapter(videoListAdapter);
                if (featuredVideoList.size() == 0 && VideoDataService.isServiceRunning) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                // ------- addBannerImage---------------------
                /*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_FEATUREDBANNER, mActivity);*/
                if(tabBannerDTO != null)
                    Utils.addBannerImage(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity);

                if (progressBar != null) {
                    if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                        refreshLayout.setEnabled(true);
                        refreshLayout.setOnRefreshListener(refreshListener);
                    }
                }

                if(featuredVideoList.size() == 0)
                    listViewFeaturedVideo.setFastScrollAlwaysVisible(false);
                else
                    listViewFeaturedVideo.setFastScrollAlwaysVisible(true);

            }
        };

        mDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        registerEvents();


        return view;
    }

    PullRefreshLayout.OnRefreshListener refreshListener = new PullRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Utils.isInternetAvailable(mActivity.getApplicationContext())) {
                pullTask = new PullRefreshTask();
                pullTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                refreshLayout.setRefreshing(false);
            }
        }
    };

    private void initComponents(View view) {
        // TODO Auto-generated method stub
        listViewFeaturedVideo = (ListView) view
                .findViewById(R.id.featured_list);

        listViewFeaturedVideo.setClickable(true);
        listViewFeaturedVideo.setFastScrollEnabled(true);

        mBannerProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mBannerProgressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        else
            mBannerProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.progress_large_material, null));

        bannerCacheableImageView = (ImageView) view
                .findViewById(R.id.img_featured_banner);
        bannerLayout = (LinearLayout) view
                .findViewById(R.id.ll_banner_block);

        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        else
            progressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));
//        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#CC0000"), android.graphics.PorterDuff.Mode.MULTIPLY);
        tvsearchRecordsNotAvailable = (TextView) view.findViewById(R.id.tvSearchStatus);

        refreshLayout = (PullRefreshLayout) view.findViewById(R.id.refresh_layout);

        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshLayout.setEnabled(false);

        if (Utils.hasNavBar(getActivity())) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, Utils.getNavBarStatusAndHeight(mActivity));
//            listViewFeaturedVideo.setLayoutParams(lp);
            refreshLayout.setLayoutParams(lp);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoListAdapter != null) {
            videoListAdapter.notifyDataSetChanged();
        }
        if(progressBar != null && refreshLayout != null) {
            if (progressBar.getVisibility() == View.GONE || progressBar.getVisibility() == View.INVISIBLE) {
                refreshLayout.setEnabled(true);
                refreshLayout.setOnRefreshListener(refreshListener);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        /*if (pullTask != null) {
            if (pullTask.getStatus() == AsyncTask.Status.RUNNING) {
                pullTask.cancel(true);
            }
        }
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }*/
        try {
            if (receiver != null && mActivity != null)
                mActivity.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void registerEvents() {
        // TODO Auto-generated method stub
        listViewFeaturedVideo.setOnTouchListener(new OnTouchListener() {

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

        listViewFeaturedVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int pos, long id) {
                View view = mActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (Utils.isInternetAvailable(mActivity)) {
                    if (featuredVideoList.get(pos).getVideoLongUrl() != null) {
                        if (featuredVideoList.get(pos).getVideoLongUrl().length() > 0 && !featuredVideoList.get(pos).getVideoLongUrl().toLowerCase().equals("none")) {
                            String videoCategory = GlobalConstants.FEATURED;
                            Intent intent = new Intent(mActivity,
                                    VideoInfoActivity.class);
                            intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                            intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, featuredVideoList.get(pos).getPlaylistReferenceId());
                            intent.putExtra(GlobalConstants.VIDEO_OBJ, featuredVideoList.get(pos));
                            GlobalConstants.LIST_FRAGMENT = new CoachesEraFragment();
                            GlobalConstants.LIST_ITEM_POSITION = pos;
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
        });

        listViewFeaturedVideo.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        listViewFeaturedVideo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

                                                         {
                                                             @Override
                                                             public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                                                                            long id) {
//                                                                 ((MainActivity) mActivity).makeShareDialog(featuredVideoList.get(position).getVideoLongUrl(), featuredVideoList.get(position).getVideoShortUrl(), featuredVideoList.get(position).getVideoStillUrl(), featuredVideoList.get(position).getVideoLongDescription(), featuredVideoList.get(position).getVideoName(), getActivity());
                                                                 return true;
                                                             }
                                                         }

        );

        bannerCacheableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tabBannerDTO != null){
                    if(tabBannerDTO.isBannerActive()){
                        if(tabBannerDTO.isHyperlinkActive() && tabBannerDTO.getBannerActionURL().length() > 0){
                            //Start the ActionUrl in Browser
                            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(tabBannerDTO.getBannerActionURL()));
                            startActivity(intent);
                        }else if(!tabBannerDTO.isHyperlinkActive() && tabBannerDTO.getBannerActionURL().length() > 0){
                            //The ActionUrl has DeepLink associated with it
                            HashMap videoMap = Utils.getInstance().getVideoInfoFromBanner(tabBannerDTO.getBannerActionURL());
                            if(videoMap != null) {
                                if (videoMap.get("VideoId") != null) {
                                    if(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).isVideoAvailableInDB(videoMap.get("VideoId").toString())){
                                        VideoDTO videoDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoDataByVideoId(videoMap.get("VideoId").toString());
                                        if(videoDTO != null){
                                            if (Utils.isInternetAvailable(mActivity)) {
                                                if (videoDTO.getVideoLongUrl() != null) {
                                                  //  if (videoDTO.getVideoLongUrl().length() > 0 && !videoDTO.getVideoLongUrl().toLowerCase().equals("none")) {
                                                        String videoCategory = GlobalConstants.FEATURED;
                                                        Intent intent = new Intent(mActivity,
                                                                VideoInfoActivity.class);
                                                        intent.putExtra(GlobalConstants.KEY_CATEGORY, videoCategory);
                                                        intent.putExtra(GlobalConstants.PLAYLIST_REF_ID, videoDTO.getPlaylistReferenceId());
                                                        intent.putExtra(GlobalConstants.VIDEO_OBJ, videoDTO);
                                                        startActivity(intent);
                                                        mActivity.overridePendingTransition(R.anim.slide_up_video_info, R.anim.nochange);
                                                    }/* else {
                                                        ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                                    }*/
                                                } else {
                                                    ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                                                }
                                            } else {
                                                ((MainActivity) mActivity).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                                            }
                                    }else{
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


    /*public void setUpPullOptionHeader(View view){
        final View pullView = view.findViewById(R.id.rl_pull_option);

        final SharedPreferences prefs = mActivity.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        boolean isPullHeaderSeen = prefs.getBoolean(GlobalConstants.PREF_PULL_HEADER_FEATURED, false);

        Button btnGotIt = (Button) pullView.findViewById(R.id.btn_got_it);

        btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean(GlobalConstants.PREF_PULL_HEADER_FEATURED, true).commit();

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

    @Override
    public void onBackPress() {
        // TODO Auto-generated method stub
        super.onBackPress();
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
                ArrayList<VideoDTO> videoList = VaultDatabaseHelper.getInstance(mActivity).getVideoList(GlobalConstants.OKF_FEATURED);
                if(videoList.size() > 0){
                    isRecordsAvailableInDb = true;
                    listViewFeaturedVideo.setFastScrollAlwaysVisible(true);
                }else
                    listViewFeaturedVideo.setFastScrollAlwaysVisible(false);
                GlobalConstants.SEARCH_VIEW_QUERY = newText;
                if (videoListAdapter != null && !newText.equals("")) {
                    videoListAdapter.filter(newText.toLowerCase(Locale
                            .getDefault()));
                    Collections.sort(featuredVideoList, new Comparator<VideoDTO>() {

                        @Override
                        public int compare(VideoDTO lhs, VideoDTO rhs) {
                            // TODO Auto-generated method stub
                            return Integer.valueOf(lhs.getVideoIndex())
                                    .compareTo(Integer.valueOf(rhs.getVideoIndex()));
                        }
                    });
                    videoListAdapter.notifyDataSetChanged();
                }
                if (!newText.isEmpty()) {
                    if ((featuredVideoList.size() == 0 && isRecordsAvailableInDb) || (featuredVideoList.size() == 0 && !VideoDataService.isServiceRunning)) {
                        tvsearchRecordsNotAvailable.setText("No Records Found");
                        tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        listViewFeaturedVideo.setFastScrollAlwaysVisible(false);
                    } else {
                        tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                        listViewFeaturedVideo.setFastScrollAlwaysVisible(true);
                    }
                } else {
                    tvsearchRecordsNotAvailable.setVisibility(View.INVISIBLE);
                    if(featuredVideoList.size() > 0)
                        listViewFeaturedVideo.setFastScrollAlwaysVisible(true);
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

            /*if (bannerCacheableImageView != null && mActivity != null) {
                // ---- addBannerImage--------
                *//*Utils.addVolleyBanner(bannerCacheableImageView,
                        GlobalConstants.URL_FEATUREDBANNER, mActivity);*//*
                Utils.addBannerImage(bannerCacheableImageView, GlobalConstants.URL_FEATUREDBANNER);
            }*/
            // it is used to track the ecent of opponennts fragment
            FlurryAgent.onEvent(GlobalConstants.FEATURED);

        }
    }

    public class VideoDataTask extends AsyncTask<HashMap, Void, ArrayList<VideoDTO>>{

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
            ArrayList<VideoDTO> videoList = AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(GlobalConstants.GET_VIDEO_DATA_FROM_BANNER+"?navTabId="+params[0].get("TabId").toString()+"&videoId="+ params[0].get("VideoId").toString()+"&userId="+AppController.getInstance().getUserId());
            System.out.println("Video List Size from server : "+videoList.size());
            return videoList;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoDTO> videoDTOs) {
            super.onPostExecute(videoDTOs);
            if(videoDTOs.size() > 0) {
                VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).insertVideosInDatabase(videoDTOs);
                if (Utils.isInternetAvailable(mActivity)) {
                    if (videoDTOs.get(0).getVideoLongUrl() != null) {
                        if (videoDTOs.get(0).getVideoLongUrl().length() > 0 && !videoDTOs.get(0).getVideoLongUrl().toLowerCase().equals("none")) {
                            String videoCategory = GlobalConstants.FEATURED;
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
            refreshLayout.setRefreshing(true);
            if(videoListAdapter != null) {
                videoListAdapter.isPullRefreshInProgress = true;
                videoListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<VideoDTO> doInBackground(Void... params) {
            ArrayList<VideoDTO> arrList = new ArrayList<VideoDTO>();
            try {
                String url = GlobalConstants.FEATURED_API_URL + "userId=" + AppController.getInstance().getUserId();
                arrList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                if(arrList.size() > 0){
                    VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).removeRecordsByTab("OKFFeatured");
                    VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).insertVideosInDatabase(arrList);
                }

                //Update Banner Data
                if(tabBannerDTO != null) {
                    TabBannerDTO serverObj = AppController.getInstance().getServiceManager().getVaultService().getTabBannerDataById(tabBannerDTO.getTabBannerId(), tabBannerDTO.getTabKeyword(), tabBannerDTO.getTabId());
                    if(serverObj != null){
                        if((tabBannerDTO.getBannerModified() != serverObj.getBannerModified()) || (tabBannerDTO.getBannerCreated() != serverObj.getBannerCreated())) {
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
                featuredVideoList.clear();
                featuredVideoList.addAll(result);

                Collections.sort(featuredVideoList, new Comparator<VideoDTO>() {

                    @Override
                    public int compare(VideoDTO lhs, VideoDTO rhs) {
                        // TODO Auto-generated method stub
                        return Integer.valueOf(lhs.getVideoIndex())
                                .compareTo(Integer.valueOf(rhs.getVideoIndex()));
                    }
                });

                if (videoListAdapter != null) {
                    videoListAdapter.listSearch.clear();
                    videoListAdapter.listSearch.addAll(result);
                    videoListAdapter.notifyDataSetChanged();
                }else {
                    videoListAdapter = new VideoContentListAdapter(featuredVideoList, mActivity, 1, false);
                    listViewFeaturedVideo.setAdapter(videoListAdapter);
                }
                if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                    videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                            .getDefault()));
                    videoListAdapter.notifyDataSetChanged();
                }
                if(videoListAdapter.getCount() == 0){
                    tvsearchRecordsNotAvailable.setText("No Records Found");
                    tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    listViewFeaturedVideo.setFastScrollAlwaysVisible(false);
                }
            }
            if(featuredVideoList.size() == 0){
                tvsearchRecordsNotAvailable.setText("No Records Found");
                tvsearchRecordsNotAvailable.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listViewFeaturedVideo.setFastScrollAlwaysVisible(false);
            }
            videoListAdapter.isPullRefreshInProgress = false;
            if(isBannerUpdated)
                if(tabBannerDTO != null) {
                    tabBannerDTO = VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getLocalTabBannerDataByTabId(tabBannerDTO.getTabId());
                    if(tabBannerDTO != null)
                        Utils.addBannerImagePullToRefresh(bannerCacheableImageView, bannerLayout, tabBannerDTO, mActivity,mBannerProgressBar);
                }
            refreshLayout.setRefreshing(false);
        }
    }

    public class FeaturedResponseReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "Message Processed";

        @Override
        public void onReceive(Context context, Intent intent) {
            featuredVideoList.clear();
            featuredVideoList.addAll(VaultDatabaseHelper.getInstance(mActivity.getApplicationContext()).getVideoList(GlobalConstants.OKF_FEATURED));

            Collections.sort(featuredVideoList, new Comparator<VideoDTO>() {

                @Override
                public int compare(VideoDTO lhs, VideoDTO rhs) {
                    // TODO Auto-generated method stub
                    return Integer.valueOf(lhs.getVideoIndex())
                            .compareTo(Integer.valueOf(rhs.getVideoIndex()));
                }
            });


            videoListAdapter = new VideoContentListAdapter(featuredVideoList, mActivity, 1, false);
            if (!GlobalConstants.SEARCH_VIEW_QUERY.isEmpty()) {
                videoListAdapter.filter(GlobalConstants.SEARCH_VIEW_QUERY.toLowerCase(Locale
                        .getDefault()));
            }
            listViewFeaturedVideo.setAdapter(videoListAdapter);

            if (featuredVideoList.size() == 0 && VideoDataService.isServiceRunning) {
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
        }
    }
}

