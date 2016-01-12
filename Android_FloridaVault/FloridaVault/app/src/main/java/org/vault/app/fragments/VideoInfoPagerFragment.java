package org.vault.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ncsavault.floridavault.R;

import org.vault.app.activities.VideoInfoActivity;
import org.vault.app.adapters.RelatedVideoListAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by aqeeb.pathan on 09-09-2015.
 */
public class VideoInfoPagerFragment extends BaseFragment {

    //Declare UI elements
    private TextView tvVideoLongDescription, tvRecordsStatus, tvRelatedTitle;
    private ListView relatedVideoListview;
    private LinearLayout llVideoDescription, llRelatedVideos;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    //Declare required fields
    private VideoDTO videoObject;
    private RelatedVideoListAdapter relatedVideoAdapter;
    private int pageNo;
    private ArrayList<VideoDTO> relatedVideoArrayList = new ArrayList<>();
    FetchRelatedRecords relatedVideosTask;
    StartRelatedVideos mStartRelatedVideoTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_info_pager_fragment, null, false);

        initViews(view);
        initData();
        initListener();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (relatedVideosTask != null) {
            if (relatedVideosTask.getStatus() == AsyncTask.Status.RUNNING) {
                relatedVideosTask.cancel(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initViews(View view){
        tvVideoLongDescription = (TextView) view.findViewById(R.id.tv_video_long_description);
        tvRelatedTitle = (TextView) view.findViewById(R.id.tv_related_title);
        relatedVideoListview = (ListView) view.findViewById(R.id.relatedVideoList);
        llVideoDescription = (LinearLayout) view.findViewById(R.id.ll_long_description);
        llRelatedVideos = (LinearLayout) view.findViewById(R.id.ll_related_videos);
        scrollView = (ScrollView) view.findViewById(R.id.scroll_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        else
            progressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));

        tvRecordsStatus = (TextView) view.findViewById(R.id.tv_records_status);

    }

    public void initData(){
        getFragmentArguments();
        if(pageNo == 1) {
            llRelatedVideos.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            tvVideoLongDescription.setText(videoObject.getVideoLongDescription());
        }else {
            scrollView.setVisibility(View.GONE);
            llRelatedVideos.setVisibility(View.VISIBLE);
            relatedVideosTask = new FetchRelatedRecords();
            relatedVideosTask.execute(videoObject);
        }
    }

    public void initListener(){

        relatedVideoListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int pos, long id) {
                View keyboardView = getActivity().getCurrentFocus();
                if (keyboardView != null) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (Utils.isInternetAvailable(getActivity())) {
                    if (relatedVideoArrayList.get(pos).getVideoLongUrl() != null) {
                        if (relatedVideoArrayList.get(pos).getVideoLongUrl().length() > 0 && !relatedVideoArrayList.get(pos).getVideoLongUrl().toLowerCase().equals("none")) {
                            ((VideoInfoActivity) getActivity()).stopVideoEvents();
                            progressBar.setVisibility(View.GONE);
                            mStartRelatedVideoTask = new StartRelatedVideos();
                            mStartRelatedVideoTask.execute(relatedVideoArrayList.get(pos));

                        } else {
                            ((VideoInfoActivity) getActivity()).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                        }
                    } else {
                        ((VideoInfoActivity) getActivity()).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                    }
                } else {
                    ((VideoInfoActivity) getActivity()).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });
    }

    public void getFragmentArguments(){
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoObject = (VideoDTO) bundle.getSerializable(GlobalConstants.VIDEO_OBJ);
            pageNo = bundle.getInt("pageNumber");
        }
    }

    public class StartRelatedVideos extends AsyncTask<VideoDTO, Void, Void>{
        ProgressDialog pDialog;
        VideoDTO videoDTO;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity(), R.style.CustomDialogTheme);
            pDialog.show();
            pDialog.setContentView(AppController.getInstance().showRelatedVideoLoader(getActivity(), false));
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(VideoDTO... params) {
            try {
                Thread.currentThread();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(params[0] != null) {
                videoDTO = params[0];
                ((VideoInfoActivity) getActivity()).startRelatedVideo(params[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(videoDTO != null)
                ((VideoInfoActivity)getActivity()).setRelatedVideoData(videoDTO);
            pDialog.dismiss();
            relatedVideosTask = new FetchRelatedRecords();
            relatedVideosTask.execute(videoDTO);
        }
    }

    public class FetchRelatedRecords extends AsyncTask<VideoDTO, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            relatedVideoArrayList.clear();
            if(relatedVideoAdapter != null)
                relatedVideoAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(VideoDTO... params) {
            relatedVideoArrayList.addAll(VaultDatabaseHelper.getInstance(getActivity()).getRelatedVideosArrayListByNameAndTag(params[0].getVideoTags(), params[0].getVideoName(), params[0].getVideoId()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            if(relatedVideoArrayList.size() > 0) {
                relatedVideoAdapter = new RelatedVideoListAdapter(relatedVideoArrayList, getActivity());
                relatedVideoListview.setAdapter(relatedVideoAdapter);
            }else{
                tvRelatedTitle.setVisibility(View.GONE);
                tvRecordsStatus.setVisibility(View.VISIBLE);
            }
        }
    }
}
