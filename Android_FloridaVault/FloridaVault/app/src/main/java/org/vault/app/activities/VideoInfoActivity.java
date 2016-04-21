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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
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
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;
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

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by aqeeb.pathan on 09-09-2015.
 */
public class VideoInfoActivity extends PermissionActivity {

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
    public static LinearLayout llVideoLoader,bufferLinearLayout;

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
    private RelativeLayout viewPagerRelativeView;
    private LinearLayout shareVideoLayout;
    private boolean askAgainForMustPermissions = false;
    private boolean goToSettingsScreen = false;
    private boolean isTouchScreen = true;


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
        System.out.println("VideoInfoActivity111111 onCreate");
        getIntentData();
        try {
            //Marshmallow permissions for read phone.
            if (haveAllMustPermissions(readPhonePermissions, PERMISSION_REQUEST_MUST)) {
                initlizeAllVideoInfoActivityData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initlizeAllVideoInfoActivityData() {
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

            if (getScreenOrientation() == 1) {
                performAnimations();
            } else {
                moveToFullscreen();
            }

            initData();
            initListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionResult(int requestCode, boolean isGranted, Object extras) {

        try {
            switch (requestCode) {
                case PERMISSION_REQUEST_MUST:
                    if (isGranted) {
                        //perform action here
                        initlizeAllVideoInfoActivityData();
                    } else {
                        if (!askAgainForMustPermissions) {
                            askAgainForMustPermissions = true;
                            System.out.println("i am here Gaurav444");
                            // Toast.makeText(this, "Please provide all the Permissions to Make the App work for you.", Toast.LENGTH_LONG).show();
                            //haveAllMustPermissions();
                            showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_RWAD_PHONE_STATE_PERMISSION);
                        } else if (!goToSettingsScreen) {
                            goToSettingsScreen = true;

                            showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_RWAD_PHONE_STATE_PERMISSION);

                        } else {
                            showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_RWAD_PHONE_STATE_PERMISSION);
                        }

                    }
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isBackToSplashScreen) {
                isBackToSplashScreen = false;
                if (haveAllMustPermissions()) {
                    initlizeAllVideoInfoActivityData();
                }
            }
        }
    }


    public void showPermissionsConfirmationDialog(String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Permission Denied");
        alertDialogBuilder
                .setMessage(message);


        alertDialogBuilder.setPositiveButton("Go to Settings",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Utils.getInstance().registerWithGCM(mActivity);
                        goToSettings();

                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // alertDialog.dismiss();
                        showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_RWAD_PHONE_STATE_PERMISSION);
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getResources().getColor(R.color.green));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(getResources().getColor(R.color.green));
    }


    private boolean isBackToSplashScreen = false;

    public void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, 400);
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
        try {
            if (videoView != null)
                if (videoView.isPlaying())
                    videoView.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (videoView != null)
                if (!videoView.isPlaying()) {
//                bufferLinearLayout.setVisibility(View.VISIBLE);
                    bufferProgressBar.setVisibility(View.VISIBLE);
                    mController.setVisibility(View.INVISIBLE);
                    videoView.seekTo((int) prevVideoTime);
                    videoView.setKeepScreenOn(true);
                    videoView.start();
                }


            boolean installedFbApp = checkIfAppInstalled("com.facebook.katana");
            boolean installedTwitterApp = checkIfAppInstalled("com.twitter.android");

            if (facebookShareView != null && facebookShareView.getVisibility() == View.VISIBLE && installedFbApp) {
                facebookShareView.setVisibility(View.GONE);
                if (flatButtonFacebook != null && flatButtonFacebook.getVisibility() == View.GONE) {
                    flatButtonFacebook.setVisibility(View.VISIBLE);
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        makeShareDialog();
                    }

                }
            }
            if (facebookShareView != null && facebookShareView.getVisibility() == View.VISIBLE && !installedFbApp) {
                if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                    makeShareDialog();
                }

            }
            if (twitterShareView != null && twitterShareView.getVisibility() == View.VISIBLE && !installedTwitterApp) {
                if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                    makeShareDialog();
                }
            }

            if (twitterShareView != null && twitterShareView.getVisibility() == View.VISIBLE && installedTwitterApp) {
                twitterShareView.setVisibility(View.GONE);
                if (flatButtonTwitter != null && flatButtonTwitter.getVisibility() == View.GONE) {
                    flatButtonTwitter.setVisibility(View.VISIBLE);
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        makeShareDialog();
                    }
                }
            }

            if (flatButtonFacebook != null && flatButtonFacebook.getVisibility() == View.VISIBLE && !installedFbApp) {
                flatButtonFacebook.setVisibility(View.GONE);
                if (facebookShareView != null && facebookShareView.getVisibility() == View.GONE) {
                    facebookShareView.setVisibility(View.VISIBLE);
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        makeShareDialog();
                    }
                }
            }

            if (flatButtonTwitter != null && flatButtonTwitter.getVisibility() == View.VISIBLE && !installedTwitterApp) {
                flatButtonTwitter.setVisibility(View.GONE);
                if (twitterShareView != null && twitterShareView.getVisibility() == View.GONE) {
                    twitterShareView.setVisibility(View.VISIBLE);
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
                        if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                            makeShareDialog();
                        }
                    }
                }
            }

            if (flatButtonFacebook != null && flatButtonFacebook.getVisibility() == View.VISIBLE && installedFbApp) {
                if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                    makeShareDialog();
                }
            }
            if (flatButtonTwitter != null && flatButtonTwitter.getVisibility() == View.VISIBLE && installedTwitterApp) {
                if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                    makeShareDialog();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            try {
                FlurryAgent.endTimedEvent(videoCategory);

                if (videoView != null && mVideoControlHandler != null && videoRunning != null) {
                    mVideoControlHandler.removeCallbacks(videoRunning);
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        if (twitterLoginButton != null) {
            twitterLoginButton.onActivityResult(requestCode, resultCode,
                    data);
        }

        if (requestCode == 400) {
            isBackToSplashScreen = true;
        }

        if (requestCode == 100) {
            // Toast.makeText(VideoInfoActivity.this, "Request Code : ", Toast.LENGTH_SHORT).show();
            boolean installedFbApp = checkIfAppInstalled("com.facebook.katana");
            boolean installedTwitterApp = checkIfAppInstalled("com.twitter.android");

            if (facebookShareView != null && facebookShareView.getVisibility() == View.VISIBLE && installedFbApp) {
                facebookShareView.setVisibility(View.GONE);
            }
            if (twitterShareView != null && twitterShareView.getVisibility() == View.VISIBLE && installedTwitterApp) {
                twitterShareView.setVisibility(View.GONE);
            }

            if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                linearLayout.setVisibility(View.GONE);
                makeShareDialog();
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getScreenDimensions();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            rlActionStrip.setVisibility(View.GONE);
            rlVideoNameStrip.setVisibility(View.GONE);
            circleIndicator.setVisibility(View.GONE);
            if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                linearLayout.setVisibility(View.GONE);
            }

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
        if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
            linearLayout.setVisibility(View.GONE);
        }
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
                if (rlVideoNameStrip != null && animation != null) {
                    rlVideoNameStrip.setAnimation(animation);
                    rlVideoNameStrip.setVisibility(View.VISIBLE);
                }
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
        viewPagerRelativeView = (RelativeLayout) findViewById(R.id.relative_view_pager);
        circleIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circleIndicator.setPageColor(getResources().getColor(R.color.app_dark_grey));
        circleIndicator.setStrokeColor(Color.parseColor("#999999"));
        circleIndicator.setFillColor(Color.parseColor("#999999"));

        llVideoLoader = (LinearLayout) findViewById(R.id.ll_video_loader);
        bufferLinearLayout = (LinearLayout) findViewById(R.id.buffer_layout);
        bufferLinearLayout.setVisibility(View.GONE);
        bufferProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bufferProgressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            bufferProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));
        }

        shareVideoLayout = (LinearLayout) findViewById(R.id.share_video_layout);
        imgVideoClose = (ImageView) findViewById(R.id.img_video_close);
        imgVideoShare = (ImageView) findViewById(R.id.img_video_share);
        imgVideoStillUrl = (ImageView) findViewById(R.id.image_video_still);

    }

    void initData() {

        if (videoObject != null) {
            if (VaultDatabaseHelper.getInstance(VideoInfoActivity.this).isFavorite(videoObject.getVideoId()))
                imgToggleButton.setBackgroundResource(R.drawable.stargold);
            else
                imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
            if (imgVideoStillUrl != null)
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
            // http://testingmobile.streaming.mediaservices.windows.net/1093cec3-b555-4184-bd8c-4242fa1e3bee/394.ism/Manifest(format=m3u8-aapl)
//            http://testingmobile.streaming.mediaservices.windows.net/1093cec3-b555-4184-bd8c-4242fa1e3bee/394.ism/Manifest(format=m3u8-aapl-v3)
            // https://www.youtube.com/watch?v=EY0vwK7a2yg+"format=m3u8-aapl-v3";
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
            }

            @Override
            public void onPause() {

            }
        });


        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                llVideoLoader.setVisibility(View.GONE);
                showToastMessageForBanner("Unable to play video");
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
                        bufferLinearLayout.setVisibility(View.VISIBLE);

                        switch (what) {

                            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                                bufferProgressBar.setVisibility(View.GONE);
                                return true;
                            }
                            case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                                bufferProgressBar.setVisibility(View.VISIBLE);

                                return true;
                            }
                            case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                                bufferProgressBar.setVisibility(View.GONE);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM;
                mController.setLayoutParams(lp);

                mController.setAnchorView(videoView);
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

                try {
                    if (mController != null) {

                        if (isTouchScreen) {
                            mController.setVisibility(View.VISIBLE);
                            isTouchScreen = false;
                        } else {
                            mController.setVisibility(View.INVISIBLE);
                            isTouchScreen = true;
                        }
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (mController != null && mController.isShown()) {
                                    mController.setVisibility(View.INVISIBLE);
                                }
                            }
                        }, 5000);


                        if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                            linearLayout.setVisibility(View.GONE);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

                try {
                    if (!VideoInfoActivity.llVideoLoader.isShown()) {
                        if (MainActivity.mIndicator != null && MainActivity.mPager != null) {
                            MainActivity.mIndicator.setCurrentItem(GlobalConstants.CURRENT_TAB);
                            MainActivity.mPager.setCurrentItem(GlobalConstants.CURRENT_TAB);
                        }
                        if (mVideoControlHandler != null && videoRunning != null) {
                            mVideoControlHandler.removeCallbacks(videoRunning);

                            if (videoView != null && mController != null) {
                                videoView.pause();
                                videoView.stopPlayback();
                                mController.removeAllViews();

                                videoView = null;
                                mController = null;
                            }
                            finish();
                            // showConfirmCloseButton("Do you want stop video?");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        shareVideoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(VideoInfoActivity.this, "Share Button Clicked", Toast.LENGTH_LONG).show();

                boolean installedFbApp = checkIfAppInstalled("com.facebook.katana");
                boolean installedTwitterApp = checkIfAppInstalled("com.twitter.android");

                System.out.println("installed app : " + installedFbApp + " twitter " + installedTwitterApp);
//                if (!installedFbApp && !installedTwitterApp) {
//
//                    if (videoView != null) {
//                        if (videoView.isPlaying()) {
//                            videoView.pause();
//                        }
//                    }
//                    String PlayStoreUrl = "https://play.google.com/store?hl=en";
//                    showConfirmSharingDialog("Please installed facebook App and Twitter App for sharing with your friends.", PlayStoreUrl);
//                } else {
                //  new ShareTwitter().execute();

                makeShareDialog();
//                }


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
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                    imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                }
            }
        });

        viewPagerRelativeView.setOnClickListener(new View.OnClickListener()

                                                 {
                                                     @Override
                                                     public void onClick(View v) {
                                                         if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                                                             linearLayout.setVisibility(View.GONE);
                                                         }
                                                     }
                                                 }

        );
    }


    private boolean isConnectionMessageShown;
    private CustomMediaController mTempMediaController;
    private boolean isFirstTime = true;
    private CustomVideoView mTempVideoView;
    private Runnable videoRunning = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (videoView != null) {
                if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                    isConnectionMessageShown = false;

                    System.out.println("VideoInfo handler");
                    if (llVideoLoader.isShown()
                        /*&& videoView.getCurrentPosition() > 500*/) {
                        llVideoLoader.setVisibility(View.GONE);
                        bufferLinearLayout.setVisibility(View.VISIBLE);
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

    public void showToastMessageForBanner(String message) {
        View includedLayout = findViewById(R.id.llToast);
        // Handler handler = new Handler();
        final TextView text = (TextView) includedLayout.findViewById(R.id.tv_toast_message);
        text.setText(message);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            animation = AnimationUtils.loadAnimation(this,
                    R.anim.abc_fade_in);

            text.setAnimation(animation);
            text.setVisibility(View.VISIBLE);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    text.setVisibility(View.VISIBLE);
                }
            }, 50);
        }


        new Handler().postDelayed(new Runnable() {
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
        try {
            Intent intent = getIntent();
            if (intent != null) {
                videoObject = (VideoDTO) intent
                        .getSerializableExtra(GlobalConstants.VIDEO_OBJ);
                videoCategory = intent
                        .getStringExtra(GlobalConstants.KEY_CATEGORY);

                if (videoObject != null) {
                    new ShareTwitter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ProgressDialog pDialog;
    private View view;
    private Uri imageUri = null;
    public static LinearLayout linearLayout;
    private ImageView facebookShareView;
    private ImageView twitterShareView;
    private ImageView flatButtonFacebook;
    private ImageView flatButtonTwitter;
    private String longDescription;
    private String videoName;
    public void makeShareDialog() {
        boolean installedFbApp = checkIfAppInstalled("com.facebook.katana");
        boolean installedTwitterApp = checkIfAppInstalled("com.twitter.android");
        view = findViewById(R.id.sharinglayout);

        if (videoObject.getVideoLongDescription() != null && videoObject.getVideoName() != null) {
            longDescription = videoObject.getVideoLongDescription();
            videoName = videoObject.getVideoName();
            try {
                if (longDescription.length() > 40) {
                    longDescription = longDescription.substring(0, 40);
                }

                if (videoName.length() > 60) {
                    longDescription = longDescription.substring(0, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        linearLayout = (LinearLayout) view.findViewById(R.id.social_sharing_linear_layout);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(VideoInfoActivity.this, R.anim.sliding_up_dialog);
                linearLayout.setAnimation(animation);
                linearLayout.setVisibility(View.VISIBLE);
            }
        }, 500);

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

        flatButtonFacebook = (ImageView) view.findViewById(R.id.facebookShare);
        flatButtonTwitter = (ImageView) view.findViewById(R.id.twitterShare);
        facebookShareView = (ImageView) view.findViewById(R.id.facebookShareView);
        twitterShareView = (ImageView) view.findViewById(R.id.twitterShareView);

        twitterLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button_share);

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                try {

                    if (imageUri == null) {
//                        pDialog = new ProgressDialog(VideoInfoActivity.this, R.style.CustomDialogTheme);
//                        pDialog.show();
//                        pDialog.setContentView(AppController.getInstance().showRelatedVideoLoader(VideoInfoActivity.this, false));
//                        pDialog.setCanceledOnTouchOutside(false);
//                        pDialog.setCancelable(false);

                    } else {
                        if (videoObject.getVideoName() != null && videoObject.getVideoLongDescription() != null) {

                            Intent intent = new TweetComposer.Builder(VideoInfoActivity.this)
                                    .text(videoName + "\n" + longDescription + "\n\n")
                                    .url(new URL(videoObject.getVideoSocialUrl()))
                                    .image(imageUri)
                                    .createIntent();
                            startActivityForResult(intent, 100);
                        } else if (videoObject.getVideoName() != null) {

                            Intent intent = new TweetComposer.Builder(VideoInfoActivity.this)
                                    .text(videoName + "\n" + longDescription + "\n\n")
                                    .url(new URL(videoObject.getVideoSocialUrl()))
                                    .image(imageUri)
                                    .createIntent();

                            startActivityForResult(intent, 100);

                        }
                    }

                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
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

        if (!installedFbApp) {

            facebookShareView.setVisibility(View.VISIBLE);
            flatButtonFacebook.setVisibility(View.GONE);
            facebookShareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoView != null) {
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        }
                    }
                    String facebookPlayStoreUrl = "https://play.google.com/store/apps/details?id=com.facebook.katana&hl=en";
                    showConfirmSharingDialog("Facebook app is not installed would you like to install it now?", facebookPlayStoreUrl);
                }
            });


        } else {
            facebookShareView.setVisibility(View.GONE);
            flatButtonFacebook.setVisibility(View.VISIBLE);
            flatButtonFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //   progressDialog.dismiss();
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
                    }
                    if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                        showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                    } else if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                        if (videoObject.getVideoSocialUrl() != null) {
                            if (videoObject.getVideoSocialUrl().length() == 0) {
                                showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                            } else {
                                videoView.pause();
                                shareVideoUrlFacebook(videoObject.getVideoId(), videoObject.getVideoSocialUrl(), videoObject.getVideoStillUrl(), videoObject.getVideoLongDescription(), videoObject.getVideoName(), context);
                            }
                        } else {
                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                        }
                    } else {
                        showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                    }
                }
            });
        }
        if (!installedTwitterApp) {

            twitterShareView.setVisibility(View.VISIBLE);
            flatButtonTwitter.setVisibility(View.GONE);
            twitterShareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoView != null) {
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        }
                    }
                    String twitterPlayStoreUrl = "https://play.google.com/store/apps/details?id=com.twitter.android&hl=en";
                    showConfirmSharingDialog("Twitter app is not installed would you like to install it now?", twitterPlayStoreUrl);
                }
            });


        } else {

            twitterShareView.setVisibility(View.GONE);
            flatButtonTwitter.setVisibility(View.VISIBLE);
            flatButtonTwitter.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    //  progressDialog.dismiss();
                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
                    }
                    if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                        showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                    } else if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
                        if (videoObject.getVideoSocialUrl() != null) {
                            if (videoObject.getVideoSocialUrl().length() == 0) {
                                showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                            } else {

                                videoView.pause();
                                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                                if (session == null) {
                                    twitterLoginButton.performClick();
                                } else {
                                    try {
                                        if (imageUri == null) {
                                            pDialog = new ProgressDialog(VideoInfoActivity.this, R.style.CustomDialogTheme);
                                            pDialog.show();
                                            pDialog.setContentView(AppController.getInstance().showRelatedVideoLoader(VideoInfoActivity.this, false));
                                            pDialog.setCanceledOnTouchOutside(false);
//                                            pDialog.setCancelable(false);

                                        } else {

                                            if (videoObject.getVideoName() != null && videoObject.getVideoLongDescription() != null) {
                                                try {

                                                    TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                            .text(videoName + "\n" + longDescription + "\n\n")
                                                            .url(new URL(videoObject.getVideoSocialUrl()))
                                                            .image(imageUri);

                                                    builder.show();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            } else if (videoObject.getVideoName() != null) {

                                                try {

                                                    TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                            .text(videoName + "\n" + longDescription + "\n\n")
                                                            .url(new URL(videoObject.getVideoSocialUrl()))
                                                            .image(imageUri);

                                                    builder.show();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
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
        }

//        progressDialog = new ProgressDialog(context);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setCancelable(true);
//        progressDialog.setCanceledOnTouchOutside(true);
//        progressDialog.show();
//        progressDialog.setContentView(view);
    }

    public void stopVideoEvents() {
        try {
            videoView.stopPlayback();
            mVideoControlHandler.removeCallbacks(videoRunning);
            llVideoLoader.setVisibility(View.GONE);
            bufferLinearLayout.setVisibility(View.GONE);
            bufferProgressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startRelatedVideo(VideoDTO videoObj) {
        videoObject = videoObj;

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (rlVideoNameStrip != null) {
//                    rlVideoNameStrip.setVisibility(View.GONE);
//                }
//            }
//        });

        if (videoObject != null) {
            new ShareTwitter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
                videoView.clearFocus();
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
                        try {
                            stopService(new Intent(VideoInfoActivity.this, VideoDataService.class));

                            VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();

                            SharedPreferences prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).commit();
//                        prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).commit();

                            Intent intent = new Intent(context, LoginEmailActivity.class);
                            context.startActivity(intent);
                            context.finish();

                            MainActivity.context.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setAllCaps(false);
        nbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        pbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        pbutton.setAllCaps(false);
    }

    public void showConfirmSharingDialog(String message, final String playStoreUrl) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(message);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Install",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                            linearLayout.setVisibility(View.GONE);
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(playStoreUrl));
                        startActivityForResult(intent, 100);

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
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setAllCaps(false);
        nbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        pbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        pbutton.setAllCaps(false);
    }


    public void showConfirmDialogBox(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(message);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        alertDialog.dismiss();

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
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setAllCaps(false);
        nbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        pbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        pbutton.setAllCaps(false);
    }

    public void shareVideoUrlFacebook(final long videoId, String videourl, String imageurl, String description, String name, final Activity context) {
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

                    if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
                    }

                    String videoIdData = String.valueOf(videoId);

//                    shareInfoTask = new shareInfoTask();
//                    shareInfoTask.execute(videoIdData);
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

    String postResultData;
    AsyncTask<String, Void, String> shareInfoTask;

    public class shareInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                postResultData = AppController.getInstance().getServiceManager().getVaultService().postSharingInfo(params[0].toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return postResultData;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

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

    private boolean checkIfAppInstalled(String uri) {
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
                    shareVideoUrlFacebook(videoObject.getVideoId(), videoObject.getVideoSocialUrl(), videoObject.getVideoStillUrl(), videoObject.getVideoLongDescription(), videoObject.getVideoName(), context);
                }
            }
        };
    }

    private class ShareTwitter extends AsyncTask<Void, Void, Uri> {

        protected Uri doInBackground(Void... arg0) {
            Uri imageUri = null;
            try {
                if (videoObject.getVideoStillUrl() != null) {
                    InputStream is = new URL(videoObject.getVideoStillUrl().trim()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                    imageUri = Uri.parse(path);
                } else {
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return imageUri;
        }

        protected void onPostExecute(Uri result) {
            try {
                imageUri = result;
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                    sharingImageOnTwitter();
                }
                System.out.println("imageUri value ");
//                if (getScreenOrientation() == 1)
//                    performAnimations();
//                else
//                    moveToFullscreen();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void sharingImageOnTwitter() {
        if (linearLayout != null && linearLayout.getVisibility() == View.VISIBLE) {
            linearLayout.setVisibility(View.GONE);
        }
        if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
            showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
        } else if (Utils.isInternetAvailable(VideoInfoActivity.this)) {
            if (videoObject.getVideoSocialUrl() != null) {
                if (videoObject.getVideoSocialUrl().length() == 0) {
                    showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                } else {

                    videoView.pause();
                    TwitterSession session = Twitter.getSessionManager().getActiveSession();
                    if (session == null) {
                        twitterLoginButton.performClick();
                    } else {
                        try {
                            if (imageUri == null) {
                                pDialog = new ProgressDialog(VideoInfoActivity.this, R.style.CustomDialogTheme);
                                pDialog.show();
                                pDialog.setContentView(AppController.getInstance().showRelatedVideoLoader(VideoInfoActivity.this, false));
                                pDialog.setCanceledOnTouchOutside(false);
                                pDialog.setCancelable(false);

                            } else {

                                if (videoObject.getVideoName() != null && videoObject.getVideoLongDescription() != null) {
                                    try {

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(videoName + "\n" + longDescription + "\n\n")
                                                .url(new URL(videoObject.getVideoSocialUrl()))
                                                .image(imageUri);

                                        builder.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else if (videoObject.getVideoName() != null) {

                                    try {

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(videoName + "\n" + longDescription + "\n\n")
                                                .url(new URL(videoObject.getVideoSocialUrl()))
                                                .image(imageUri);

                                        builder.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
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
}

