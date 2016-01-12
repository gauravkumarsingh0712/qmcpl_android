package org.vault.app.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.flurry.android.FlurryAgent;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.viewpagerindicator.CirclePageIndicator;

import org.vault.app.adapters.PagerAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.customviews.CustomMediaController;
import org.vault.app.customviews.CustomVideoView;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.fragments.VideoInfoPagerFragment;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by aqeeb.pathan on 09-09-2015.
 */
public class VideoInfoActivity extends FragmentActivity {

    //Declare fields required
    private VideoDTO videoObject;
    private Map<String, String> articleParams;
    private Handler mVideoControlHandler = new Handler();
    private long prevVideoTime = 0;
    private CustomMediaController mController;
    private String videoCategory;
    private boolean isFavoriteChecked;
    private AsyncTask<Void, Void, Void> mPostTask;
    private String postResult;
    private Activity context;
    int displayHeight = 0, displayWidth = 0;

    //Declare UI elements
    private RelativeLayout rlVideoNameStrip, rlActionStrip, rlParentLayout;
    private FrameLayout rlVideoLayout;
    private CustomVideoView videoView;
    private TextView tvVideoName;
    private ImageView imgToggleButton;
    private ViewPager viewPager;
    private CirclePageIndicator circleIndicator;
    private ProgressBar bufferProgressBar;
    private ImageView imgVideoClose, imgVideoShare, imgVideoStillUrl;
    private LinearLayout llVideoLoader;

    //UI Elements and fields for Social Sharing
    private static CallbackManager callbackManager;
    private static ShareDialog shareDialog;
    AlertDialog alertDialog = null;
    private static boolean canPresentShareDialog;
    TwitterLoginButton twitterLoginButton;
    public ProgressDialog progressDialog;
    ProfileTracker profileTracker;
    private Animation animation;
    private Vector<Fragment> fragments;
    private PagerAdapter mPagerAdapter;
    private boolean isVideoCompleted;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.video_info_layout);
        context = VideoInfoActivity.this;
        isVideoCompleted = false;
        initializeFacebookUtil();
        initViews();
        setDimensions();

        //This is to check the orientation, if it is landscape before the start of this activity
        //i.e. if the user is already in landscape mode before starting this activity then we need to
        //show video in full screen mode.
        /*int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            moveToFullscreen();
        }else{
            performAnimations();
        }*/

        //The reason to put this thread, to make screen aware of what orientation it is using
        try {
            Thread thread = new Thread();
            thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getScreenOrientation() == 1)
            performAnimations();
        else
            moveToFullscreen();

        initData();
        initListener();

    }

    public int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        Point outSize = new Point();
        getOrient.getSize(outSize);

        if (outSize.x == outSize.y) {
            orientation = Configuration.ORIENTATION_UNDEFINED;
        } else {
            if (outSize.x < outSize.y) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null)
            if (videoView.isPlaying())
                videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null)
            if (!videoView.isPlaying()) {
//                bufferProgressBar.setVisibility(View.VISIBLE);
                mController.setVisibility(View.INVISIBLE);
                videoView.seekTo((int) prevVideoTime);
                System.out.println("Current Position : " + videoView.getCurrentPosition());
            }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (MainActivity.mIndicator != null && MainActivity.mPager != null) {
            MainActivity.mIndicator.setCurrentItem(GlobalConstants.CURRENT_TAB);
            MainActivity.mPager.setCurrentItem(GlobalConstants.CURRENT_TAB);
        }
        mVideoControlHandler.removeCallbacks(videoRunning);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoCategory != null) {
            // -----stopping the flurry event of video-----------
            FlurryAgent.endTimedEvent(videoCategory);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode,
                data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getScreenDimensions();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            rlActionStrip.setVisibility(View.GONE);
            rlVideoNameStrip.setVisibility(View.GONE);
            circleIndicator.setVisibility(View.GONE);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(displayWidth, displayHeight);
            rlVideoLayout.setLayoutParams(lp);
            videoView.setDimensions(displayWidth, displayHeight);
            videoView.getHolder().setFixedSize(displayWidth, displayHeight);
        } else {
            rlActionStrip.setVisibility(View.VISIBLE);
            rlVideoNameStrip.setVisibility(View.VISIBLE);
            circleIndicator.setVisibility(View.VISIBLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(displayWidth, (int) (displayHeight * 0.35));
            lp.addRule(RelativeLayout.BELOW, R.id.view_line);
            rlVideoLayout.setLayoutParams(lp);
            videoView.setDimensions(displayWidth, (int) (displayHeight * 0.35));
            videoView.getHolder().setFixedSize(displayWidth, (int) (displayHeight * 0.35));
        }
    }

    public void moveToFullscreen() {
        getScreenDimensions();

        rlActionStrip.setVisibility(View.GONE);
        rlVideoNameStrip.setVisibility(View.GONE);
        circleIndicator.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(displayWidth, displayHeight);
        rlVideoLayout.setLayoutParams(lp1);
        videoView.setDimensions(displayWidth, displayHeight);
        videoView.getHolder().setFixedSize(displayWidth, displayHeight);
    }

    public void performAnimations() {

        /*animation = AnimationUtils.loadAnimation(this, R.anim.slideup);
        rlParentLayout.setAnimation(animation);

        rlParentLayout.setVisibility(View.VISIBLE);*/


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(VideoInfoActivity.this, R.anim.slidedown_header);
                rlVideoNameStrip.setAnimation(animation);
                rlVideoNameStrip.setVisibility(View.VISIBLE);
            }
        }, 300);

        /*imgVideoClose.setVisibility(View.VISIBLE);
        imgVideoShare.setVisibility(View.VISIBLE);*/
    }

    void initViews() {
        rlVideoNameStrip = (RelativeLayout) findViewById(R.id.rl_header);
        rlActionStrip = (RelativeLayout) findViewById(R.id.rl_header);
        rlParentLayout = (RelativeLayout) findViewById(R.id.rl_parent_layout);
        rlVideoLayout = (FrameLayout) findViewById(R.id.rl_video_layout);
        videoView = (CustomVideoView) findViewById(R.id.video_view);
        tvVideoName = (TextView) findViewById(R.id.tv_video_name);
        imgToggleButton = (ImageView) findViewById(R.id.imgToggleButton);
        viewPager = (ViewPager) findViewById(R.id.pager);
        circleIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circleIndicator.setPageColor(getResources().getColor(R.color.app_dark_grey));
        circleIndicator.setStrokeColor(Color.parseColor("#999999"));
        circleIndicator.setFillColor(Color.parseColor("#999999"));

        llVideoLoader = (LinearLayout) findViewById(R.id.ll_video_loader);

        bufferProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            bufferProgressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        else
            bufferProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));

        imgVideoClose = (ImageView) findViewById(R.id.img_video_close);
        imgVideoShare = (ImageView) findViewById(R.id.img_video_share);
        imgVideoStillUrl = (ImageView) findViewById(R.id.image_video_still);

        bufferProgressBar.setVisibility(View.GONE);
    }

    void initData() {
        getIntentData();
        if (videoObject != null) {
            if (VaultDatabaseHelper.getInstance(VideoInfoActivity.this).isFavorite(videoObject.getVideoId()))
                imgToggleButton.setBackgroundResource(R.drawable.stargold);
            else
                imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
            Utils.addImageByCaching(imgVideoStillUrl, videoObject.getVideoStillUrl());
            tvVideoName.setText(videoObject.getVideoName().toString());
        }

        llVideoLoader.addView(AppController.getInstance().setViewToProgressDialog(this));


        // -------- starting the flurry event of video------
        articleParams = new HashMap<String, String>();
        articleParams.put(GlobalConstants.KEY_VIDEONAME, videoObject.getVideoName());
        FlurryAgent.logEvent(videoCategory, articleParams, true);

        //Set Video to videoview
        if (Utils.isInternetAvailable(this)) {
            String encodedVideoUrl = videoObject.getVideoLongUrl();
            llVideoLoader.setVisibility(View.VISIBLE);
            encodedVideoUrl = encodedVideoUrl.replace("(format=m3u8-aapl)", "(format=m3u8-aapl-v3)");
            String newTestUrl = "http://ugamedia.streaming.mediaservices.windows.net/f0925f58-1f30-48f3-8c23-13e2864bd8fb/Malcolm%20Mitchell_2011_OleMiss.ism/Manifest(format=m3u8-aapl-v3)";
            String testUrlWithoutCDN = "http://ugamedia.streaming.mediaservices.windows.net/4f862ed2-a737-483b-b302-4d94a35d24fc/Spring_64_Complete.ism/Manifest(format=m3u8-aapl-v3)";
            String testUrlWithCDN = "http://ugavault-ugamedia.streaming.mediaservices.windows.net/c3b67d2c-ee75-4301-8227-c3a3ce7cb583/Tim%20Crowe_2_master.ism/Manifest(format=m3u8-aapl-v3)";
            String bigVideoUrl = "http://ugavault-ugamedia.streaming.mediaservices.windows.net/25b8ad26-921a-4f73-8c57-f47e411fbb6c/1980_UGA_vs_SC_Game_Cut.ism/Manifest(format=m3u8-aapl-v3)";
            String auburnVideoUrl = "http://auburnstream-auburnmedia.streaming.mediaservices.windows.net/62a9e122-be98-414f-8338-ba2ee3a85e96/AU_FB_113013_AL_CGM_Final2.ism/Manifest(format=m3u8-aapl-v3)";

            System.out.println("Media Url : " + encodedVideoUrl);
            Uri videoUri = Uri.parse(encodedVideoUrl);
            mController = new CustomMediaController(this);
            videoView.setKeepScreenOn(true);
            videoView.setMediaController(mController);
//            mController.setAnchorView(videoView);
            videoView.setVideoURI(videoUri);

//            videoView.start();
            System.out.println("Video Length : " + videoView.getDuration());
        } else {
            Utils.showNoConnectionMessage(this);
            finish();
        }

        fragments = new Vector<Fragment>();
        Bundle bundleRelated1 = new Bundle();
        bundleRelated1.putInt("pageNumber", 1);
        bundleRelated1.putSerializable(GlobalConstants.VIDEO_OBJ, videoObject);
        fragments.add(Fragment.instantiate(this, VideoInfoPagerFragment.class.getName(), bundleRelated1));
        Bundle bundleRelated2 = new Bundle();
        bundleRelated2.putInt("pageNumber", 2);
        bundleRelated2.putSerializable(GlobalConstants.VIDEO_OBJ, videoObject);
        fragments.add(Fragment.instantiate(this, VideoInfoPagerFragment.class.getName(), bundleRelated2));

        this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        this.viewPager.setAdapter(this.mPagerAdapter);
        circleIndicator.setViewPager(viewPager);

        /*ViewPagerAdapter _adapter = new ViewPagerAdapter(super.getSupportFragmentManager());
        viewPager.setAdapter(_adapter);
        viewPager.setCurrentItem(0);

        circleIndicator.setViewPager(viewPager);*/
    }

    void initListener() {

        videoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {
                if (!Utils.isInternetAvailable(VideoInfoActivity.this)) {

                    isConnectionMessageShown = true;
                    if (isConnectionMessageShown) {
                        showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                        videoView.pause();
                    }
                }
                System.out.println("VideoInfo onPlay");
            }

            @Override
            public void onPause() {

                System.out.println("VideoInfo onPause");
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                llVideoLoader.setVisibility(View.GONE);
                showToastMessage("Unable to play video");
                videoView.stopPlayback();
                mVideoControlHandler.removeCallbacks(videoRunning);
                return true;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                            bufferProgressBar.setVisibility(View.VISIBLE);
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                            bufferProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                });

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM;
                mController.setLayoutParams(lp);

                mController.setAnchorView(videoView);
                System.out.println("VideoInfo on pre");
                mVideoControlHandler.postDelayed(videoRunning, 1000);

                ((ViewGroup) mController.getParent()).removeView(mController);

                rlVideoLayout.addView(mController);
                mController.setVisibility(View.INVISIBLE);

                /*mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

                    }
                });*/

                if (isVideoCompleted)
                    videoView.pause();
                else
                    videoView.start();
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (mController != null) {
                    mController.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mController.setVisibility(View.INVISIBLE);
                        }
                    }, 1000);
                }
                return false;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
               // isFirstTime = true;
                isVideoCompleted = true;
                imgVideoStillUrl.setVisibility(View.VISIBLE);
                mVideoControlHandler.removeCallbacks(videoRunning);

              //  mp.reset();
                 videoView.stopPlayback();

                if (mController != null) {
                    mController.removeAllViews();
                    mController = null;
//                    mController = mTempMediaController;
//                    videoView.setMediaController(mController);
//                    mController.setAnchorView(videoView);
//                  //  mController.show();
                }
//

                if (mController == null) {
                    mController = new CustomMediaController(VideoInfoActivity.this);
                }
                videoView.setKeepScreenOn(true);
                videoView.setMediaController(mController);

                String encodedVideoUrl = videoObject.getVideoLongUrl();
                encodedVideoUrl = encodedVideoUrl.replace("(format=m3u8-aapl)", "(format=m3u8-aapl-v3)");
                videoView.setVideoURI(Uri.parse(encodedVideoUrl));
                //videoView.requestFocus();
                prevVideoTime = 0;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        llVideoLoader.setVisibility(View.VISIBLE);
                    }
                }, 500);
            }
        });

        imgVideoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mIndicator != null && MainActivity.mPager != null) {
                    MainActivity.mIndicator.setCurrentItem(GlobalConstants.CURRENT_TAB);
                    MainActivity.mPager.setCurrentItem(GlobalConstants.CURRENT_TAB);
                }
                mVideoControlHandler.removeCallbacks(videoRunning);
                finish();
            }
        });

        imgVideoShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(VideoInfoActivity.this, "Share Button Clicked", Toast.LENGTH_LONG).show();
                makeShareDialog();
            }
        });

        imgToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(context)) {
                    if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                        imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                        showConfirmLoginDialog(GlobalConstants.LOGIN_MESSAGE);
                    } else {
                        if (VaultDatabaseHelper.getInstance(VideoInfoActivity.this).isFavorite(videoObject.getVideoId())) {
                            isFavoriteChecked = false;
                            VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(0, videoObject.getVideoId());
                            videoObject.setVideoIsFavorite(false);
                            imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                        } else {
                            isFavoriteChecked = true;
                            VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(1, videoObject.getVideoId());
                            videoObject.setVideoIsFavorite(true);
                            imgToggleButton.setBackgroundResource(R.drawable.stargold);
                        }

                        mPostTask = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    postResult = AppController.getInstance().getServiceManager().getVaultService().postFavoriteStatus(AppController.getInstance().getUserId(), videoObject.getVideoId(), videoObject.getPlaylistId(), isFavoriteChecked);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                System.out.println("Result of POST request : " + postResult);
                                if (isFavoriteChecked)
                                    VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(1, videoObject.getVideoId());
                                else
                                    VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(0, videoObject.getVideoId());
                            }
                        };

//                        mPostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        mPostTask.execute();
                    }
                } else {
                    ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                    imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                }
            }
        });
    }


    private boolean isConnectionMessageShown;
    private CustomMediaController mTempMediaController;
    private boolean isFirstTime = true;
    private CustomVideoView mTempVideoView;
    private Runnable videoRunning = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                isConnectionMessageShown = false;

                System.out.println("VideoInfo handler");
                if (llVideoLoader.isShown()
                        /*&& videoView.getCurrentPosition() > 500*/) {
                    llVideoLoader.setVisibility(View.GONE);
                    videoView.requestFocus();
                    mController.show();
                } else {
                    mController.hide();
                }

                if (videoView.isPlaying()) {
                    System.out.println("Video Playing and total duration "
                            + videoView.getDuration());
                    System.out.println("Video Playing and current duration "
                            + videoView.getCurrentPosition());

                    if (imgVideoStillUrl.isShown() && videoView.getCurrentPosition() > 500) {
                        Animation anim = AnimationUtils.loadAnimation(VideoInfoActivity.this, R.anim.fadein);
                        imgVideoStillUrl.setAnimation(anim);
                        imgVideoStillUrl.setVisibility(View.GONE);
                        llVideoLoader.setVisibility(View.GONE);
                        videoView.requestFocus();
                        mController.show();
                    } else {
                        mController.hide();
                    }
                    System.out.println("VideoInfo getCurrentPosition "
                            + videoView.getCurrentPosition());
                    prevVideoTime = videoView.getCurrentPosition();
                }
                if (!videoView.isPlaying()) {
                    System.out.println("VideoInfo isPlaying ");
                    videoView.seekTo((int) prevVideoTime);
                }

//                if (videoView.getCurrentPosition() >= videoView.getDuration() - 10000 && isFirstTime) {
//
//                    if (mTempMediaController == null) {
//                        mTempMediaController = new CustomMediaController(VideoInfoActivity.this);
//                    }
////                    String encodedVideoUrl = videoObject.getVideoLongUrl();
////                    encodedVideoUrl = encodedVideoUrl.replace("(format=m3u8-aapl)", "(format=m3u8-aapl-v3)");
////                    mTempVideoView = new CustomVideoView(VideoInfoActivity.this);
////                    mTempVideoView.setVideoURI(Uri.parse(encodedVideoUrl));
//
//                    isFirstTime = false;
//                }


            } else {
                if (!isConnectionMessageShown) {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                    videoView.pause();
                }
                isConnectionMessageShown = true;
            }
            mVideoControlHandler.postDelayed(this, 2000);

        }
    };

    public void loadVideoThumbnail() {
        DisplayImageOptions imgLoadingOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(videoObject.getVideoStillUrl(),
                imgVideoStillUrl, imgLoadingOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        imgVideoStillUrl.setBackgroundResource(R.drawable.camera_background);
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                    }
                });
    }

    public void showToastMessage(String message) {
        View includedLayout = findViewById(R.id.llToast);

        final TextView text = (TextView) includedLayout.findViewById(R.id.tv_toast_message);
        text.setText(message);

        animation = AnimationUtils.loadAnimation(this,
                R.anim.abc_fade_in);

        text.setAnimation(animation);
        text.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(VideoInfoActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }

    public void getScreenDimensions() {

        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            displayHeight = size.y;
            displayWidth = size.x;
        } else {
            Display d = w.getDefaultDisplay();
            displayHeight = d.getHeight();
            displayWidth = d.getWidth();
        }
    }

    public void setDimensions() {
        Point size = new Point();
        WindowManager w = getWindowManager();
        int measuredHeight = 0;
        int measuredWidth = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            measuredHeight = size.y;
            measuredWidth = size.x;
        } else {
            Display d = w.getDefaultDisplay();
            measuredHeight = d.getHeight();
            measuredWidth = d.getWidth();
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (measuredHeight * 0.35));
        lp.addRule(RelativeLayout.BELOW, R.id.view_line);
        rlVideoLayout.setLayoutParams(lp);

        videoView.setDimensions(measuredWidth, (int) (measuredHeight * 0.35));
    }

    public void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            videoObject = (VideoDTO) intent
                    .getSerializableExtra(GlobalConstants.VIDEO_OBJ);
            videoCategory = intent
                    .getStringExtra(GlobalConstants.KEY_CATEGORY);
        }
    }




    public void makeShareDialog() {
        View view = context.getLayoutInflater().inflate(R.layout.share_dialog, null);
        int Measuredwidth = 0;
        try {
            Point size = new Point();
            WindowManager w = getWindowManager();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                w.getDefaultDisplay().getSize(size);
                Measuredwidth = size.x;
            } else {
                Display d = w.getDefaultDisplay();
                Measuredwidth = d.getWidth();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button flatButtonFacebook = (Button) view.findViewById(R.id.facebookShare);
        Button flatButtonTwitter = (Button) view.findViewById(R.id.twitterShare);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (Measuredwidth * 0.40), LinearLayout.LayoutParams.WRAP_CONTENT);
        flatButtonFacebook.setLayoutParams(lp);
        flatButtonTwitter.setLayoutParams(lp);

        twitterLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button_share);

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                try {
                    if (videoObject.getVideoName() != null && videoObject.getVideoLongDescription() != null) {

                        Intent intent = new TweetComposer.Builder(VideoInfoActivity.this)
                                .text(videoObject.getVideoName() + "\n" + videoObject.getVideoLongDescription() + "\n\n")
                                .url(new URL(videoObject.getVideoShortUrl()))
                                .createIntent();

                        startActivityForResult(intent, 100);
                    } else if (videoObject.getVideoName() != null) {
                        Intent intent = new TweetComposer.Builder(VideoInfoActivity.this)
                                .text(videoObject.getVideoName() + "\n\n")
                                .url(new URL(videoObject.getVideoShortUrl()))
                                .createIntent();

                        startActivityForResult(intent, 100);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void failure(TwitterException e) {
                showToastMessage(GlobalConstants.TWITTER_LOGIN_CANCEL);
            }
        });

        flatButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                    if (videoObject.getVideoShortUrl() != null) {
                        if (videoObject.getVideoShortUrl().length() == 0) {
                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                        } else {
                            videoView.pause();
                            shareVideoUrlFacebook(videoObject.getVideoShortUrl(), videoObject.getVideoStillUrl(), videoObject.getVideoLongDescription(), videoObject.getVideoName(), context);
                        }
                    } else {
                        showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        flatButtonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                    if (videoObject.getVideoShortUrl() != null) {
                        if (videoObject.getVideoShortUrl().length() == 0) {
                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                        } else {
                            videoView.pause();
                            TwitterSession session = Twitter.getSessionManager().getActiveSession();
                            if (session == null) {
                                twitterLoginButton.performClick();
                            } else {
                                try {
                                    if (videoObject.getVideoName() != null && videoObject.getVideoLongDescription() != null) {

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(videoObject.getVideoName() + "\n" + videoObject.getVideoLongDescription() + "\n\n")
                                                .url(new URL(videoObject.getVideoShortUrl()));

                                        builder.show();
                                    } else if (videoObject.getVideoName() != null) {
                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(videoObject.getVideoName() + "\n\n")
                                                .url(new URL(videoObject.getVideoShortUrl()));

                                        builder.show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        progressDialog.setContentView(view);
    }

    public void stopVideoEvents() {
        mVideoControlHandler.removeCallbacks(videoRunning);
        videoView.stopPlayback();
        llVideoLoader.setVisibility(View.GONE);
        bufferProgressBar.setVisibility(View.GONE);
    }

    public void startRelatedVideo(VideoDTO videoObj) {
        videoObject = videoObj;
    }

    public void setRelatedVideoData(VideoDTO videoObject) {
        //Set Video to videoview
        if (videoCategory != null) {
            // -----stopping the flurry event of video-----------
            FlurryAgent.endTimedEvent(videoCategory);
        }
        videoCategory = GlobalConstants.RELATED_VIDEO_CATEGORY;
        if (videoObject != null) {
            if (Utils.isInternetAvailable(this)) {
                String encodedVideoUrl = videoObject.getVideoLongUrl();
                llVideoLoader.setVisibility(View.VISIBLE);
                encodedVideoUrl = encodedVideoUrl.replace("(format=m3u8-aapl)", "(format=m3u8-aapl-v3)");

                System.out.println("Media Url : " + encodedVideoUrl);
                Uri videoUri = Uri.parse(encodedVideoUrl);
            /*mController = new MediaController(this);
            videoView.setMediaController(mController);*/
                mController.setAnchorView(videoView);
                videoView.setMediaController(mController);
                videoView.setVideoURI(videoUri);

                videoView.requestFocus();
                videoView.start();
            } else {
                Utils.showNoConnectionMessage(this);
            }
        }
        if (videoObject != null) {
            if (VaultDatabaseHelper.getInstance(VideoInfoActivity.this).isFavorite(videoObject.getVideoId()))
                imgToggleButton.setBackgroundResource(R.drawable.stargold);
            else
                imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
            Utils.addImageByCaching(imgVideoStillUrl, videoObject.getVideoStillUrl());
            imgVideoStillUrl.setVisibility(View.VISIBLE);
            tvVideoName.setText(videoObject.getVideoName().toString());
        }

        // -------- starting the flurry event of video------
        articleParams = new HashMap<String, String>();
        articleParams.put(GlobalConstants.KEY_VIDEONAME, videoObject.getVideoName());
        FlurryAgent.logEvent(videoCategory, articleParams, true);

        if (mPagerAdapter.getCount() > 0) {
            VideoInfoPagerFragment descriptionFragment = (VideoInfoPagerFragment) mPagerAdapter.getItem(0);
            View fragmentView = descriptionFragment.getView();
            TextView tvLongDescription = (TextView) fragmentView.findViewById(R.id.tv_video_long_description);
            tvLongDescription.setText(videoObject.getVideoLongDescription());

            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void showConfirmLoginDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(message);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        stopService(new Intent(VideoInfoActivity.this, VideoDataService.class));

                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();

                        SharedPreferences prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).commit();
//                        prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).commit();

                        Intent intent = new Intent(context, LoginEmailActivity.class);
                        context.startActivity(intent);
                        context.finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        alertDialog.dismiss();
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void shareVideoUrlFacebook(String videourl, String imageurl, String description, String name, final Activity context) {
        try {
            final FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
                @Override
                public void onCancel() {
                    showToastMessage(GlobalConstants.FACEBOOK_SHARING_CANCEL);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;
                }

                @Override
                public void onError(FacebookException error) {
                    String title = "Error";
                    String alertMessage = error.getMessage();
                    showToastMessage(GlobalConstants.FACEBOOK_SHARING_CANCEL);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;
                }

                @Override
                public void onSuccess(Sharer.Result result) {
                    boolean installed = checkIfFacebookAppInstalled("com.facebook.android");
                    if (!installed)
                        installed = checkIfFacebookAppInstalled("com.facebook.katana");
                    if (!installed)
                        showToastMessage(GlobalConstants.FACEBOOK_POST_SUCCESS_MESSAGE);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;
                }
            };

            GlobalConstants.IS_SHARING_ON_FACEBOOK = true;
            FacebookSdk.sdkInitialize(context.getApplicationContext());

            callbackManager = CallbackManager.Factory.create();

            shareDialog = new ShareDialog(context);
            shareDialog.registerCallback(
                    callbackManager,
                    shareCallback);

            canPresentShareDialog = ShareDialog.canShow(
                    ShareLinkContent.class);

            Profile profile = Profile.getCurrentProfile();

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(name)
                    .setContentDescription(description)
                    .setImageUrl(Uri.parse(imageurl))
                    .setContentUrl(Uri.parse(videourl))
                    .build();

            if (profile != null) {
                if (canPresentShareDialog) {
                    shareDialog.show(linkContent);
                } else if (profile != null && hasPublishPermission()) {
                    ShareApi.share(linkContent, shareCallback);
                }
            } else {
                loginWithFacebook();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    public void loginWithFacebook() {
//        Toast.makeText(this, "Your are not logged in, please login", Toast.LENGTH_LONG).show();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile, email, user_birthday"));
    }

    private boolean checkIfFacebookAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
            //Check if the Facebook app is disabled
            ApplicationInfo ai = getPackageManager().getApplicationInfo(uri, 0);
            app_installed = ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }

        return app_installed;
    }

    public void initializeFacebookUtil() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        System.out.println("Facebook login successful");
                    }

                    @Override
                    public void onCancel() {
                        showAlert();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        showAlert();
                    }

                    private void showAlert() {
                        showToastMessage(GlobalConstants.FACEBOOK_LOGIN_CANCEL);
                        /*new AlertDialog.Builder(context)
                                .setTitle("Cancelled")
                                .setMessage("Permission not granted")
                                .setPositiveButton("Ok", null)
                                .show();*/
                    }
                });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null && GlobalConstants.IS_SHARING_ON_FACEBOOK) {
                    shareVideoUrlFacebook(videoObject.getVideoShortUrl(), videoObject.getVideoStillUrl(), videoObject.getVideoLongDescription(), videoObject.getVideoName(), context);
                }
            }
        };
    }

}
