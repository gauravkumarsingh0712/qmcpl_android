package org.vault.app.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.flurry.android.FlurryAgent;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;
import com.viewpagerindicator.TitlePageIndicator;

import net.hockeyapp.android.CrashManager;

import org.vault.app.adapters.PagerAdapter;
import org.vault.app.adapters.TabsFragmentPagerAdapter;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.VideoDTO;
import org.vault.app.fragments.CoachesEraFragment;
import org.vault.app.fragments.FavoritesFragment;
import org.vault.app.fragments.FeaturedFragment;
import org.vault.app.fragments.GamesFragment;
import org.vault.app.fragments.OpponentsFragment;
import org.vault.app.fragments.PlayerFragment;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.model.LocalModel;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import io.fabric.sdk.android.Fabric;

@SuppressWarnings("serial")
public class MainActivity extends FragmentActivity implements Serializable {

    private static final String[] CONTENT = {"FeaturedFragment",
            "PlayerFragment", "OpponentsFragment", "CoachesEraFragment",
            "FavoritesFragment"};

    public static TabsFragmentPagerAdapter mAdapter;
    public static PagerAdapter mPagerAdapter;
    public static ViewPager mPager;
    public static TitlePageIndicator mIndicator;
    private ActionBar actionBar;
    private Activity context;
    private String gryColor = "#999999";
    public static List<Fragment> fragments;

    Animation animation;

    AppController aController;
    SharedPreferences prefs;
    public SearchView searchView;

    public static String videourl;
    public static String imageurl;
    public static String description;
    public static String name;
    public static long videoId;
    public static String videoSocialUrl;

    private static CallbackManager callbackManager;

    private static ShareDialog shareDialog;

    AlertDialog alertDialog = null;
    private static boolean canPresentShareDialog;

    TwitterLoginButton twitterLoginButton;
    public ProgressDialog progressDialog;

    ProfileTracker profileTracker;
    String message = "You have been joined on Mail Chimp successfully!";
    String videoUrl;
    public static boolean isPullToRefreshRunning = false;

    public static ProgressBar autoRefreshProgressBar;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity);

        context = MainActivity.this;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoUrl = bundle.getString("videoUrl");
        }
        CrashManager.initialize(this, GlobalConstants.HOCKEY_APP_ID, null);

        if (VaultDatabaseHelper.getInstance(getApplicationContext()).getVideoCount() == 0 && !VideoDataService.isServiceRunning) {
            Toast.makeText(this, "No videos in local database, starting background service", Toast.LENGTH_LONG).show();
            startService(new Intent(this, VideoDataService.class));
        }

        File cacheDir = StorageUtils.getCacheDirectory(context);
        ImageLoaderConfiguration config;
        config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3) // default
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .build();
        ImageLoader.getInstance().init(config);

		/*ImageLoader.getInstance().init(ImageLoaderConfiguration
                .createDefault(getBaseContext()));*/

        aController = (AppController) getApplicationContext();

        prefs = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);

        // --------forcing to show overflow menu---------
        Utils.forcingToShowOverflowMenu(context);

        // ----- start flurry session-------------
        FlurryAgent.onStartSession(context, GlobalConstants.FLURRY_KEY);

/*
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig("fqOPqtp50XP84ThpOcIQlol5L",
                        "fMMeRCDMHj4zcpsdy1Fq50H1eKkL4NTNTKvy5KqzJOmXZ3Kk6N");
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new TweetComposer());*/

        /*TwitterAuthConfig authConfig =
                new TwitterAuthConfig("fqOPqtp50XP84ThpOcIQlol5L",
                        "fMMeRCDMHj4zcpsdy1Fq50H1eKkL4NTNTKvy5KqzJOmXZ3Kk6N");*/

        TwitterAuthConfig authConfig = new TwitterAuthConfig(GlobalConstants.TWITTER_CONSUMER_KEY, GlobalConstants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new TweetUi());
        Fabric.with(this, new TweetComposer());

        initViews();

        //get Wifi strength
        int numberOfLevels = 5;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        System.out.println("Wifi strength : " + level);

        //get cellular internet connectivity strength
        /*TelephonyManager telephonyManager =        (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
        cellSignalStrengthGsm.getDbm();*/

        // ----------adjustPagerIndicator when making transparent to the
        // navigation bar from onwards kitkat----------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.addPagerIndicatorBelowActionBar(context, mIndicator);
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //calculating runtime device width and height
        setDimensions();

        // -------- initializing adapter and indicator-----------------
        initialisePaging();

        /*mAdapter = new TabsFragmentPagerAdapter(getSupportFragmentManager(),
                context);
        mPager.setAdapter(mAdapter);*/
        mIndicator.setViewPager(mPager);

        setUpPullOptionHeader();

//        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // TODO Auto-generated method stub
//                mIndicator.setCurrentItem(position);
//                mPager.setCurrentItem(position, true);
                GlobalConstants.CURRENT_TAB = position;
                android.support.v4.app.Fragment fragment = ((FragmentStatePagerAdapter) mPager.getAdapter()).getItem(position);
                fragment.onAttach(MainActivity.this);
                /*if(fragment instanceof FavoritesFragment)
                    fragment.onResume();*/
                fragment.onResume();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });

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
                    shareVideoUrl(videoId, videoSocialUrl, imageurl, description, name, context);
                }
            }
        };

        //  getIntentData();
        autoRefresh();


    }


    private void getIntentData() {
        if (getIntent().getExtras() != null) {
            boolean intent = getIntent().getExtras().getBoolean("is_success");

            if (intent) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showToastMessage(message);
                    }
                }, 2000);

            }
//            else {
//                message = "You don't want join on Mail Chimp.";
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        showToastMessage(message);
//                    }
//                }, 2000);
//            }
        }
    }

    /**
     * Method used for facebook install or not
     *
     * @param uri
     * @return
     */
    private boolean checkIfFacebookAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging() {


        fragments = new Vector<Fragment>();

        /*for(int i=0; i<GlobalConstants.tabType.length; i++){
            Bundle bundle = new Bundle();
            bundle.putString("tabIdentifier", GlobalConstants.tabsDbIdentifierList[i].toString());
            if(GlobalConstants.tabType[i].toLowerCase().equals("edgetoedge")) {
                bundle.putInt("tabType", 1);
            }else if(GlobalConstants.tabType[i].toLowerCase().equals("wide")) {
                bundle.putInt("tabType", 2);
            }
            fragments.add(Fragment.instantiate(this, VideoListFragment.class.getName(), bundle));
        }*/
        ArrayList<TabBannerDTO> lstTabBannerData = VaultDatabaseHelper.getInstance(getApplicationContext()).getAllLocalTabBannerData();
        Collections.sort(lstTabBannerData, new Comparator<TabBannerDTO>() {

            @Override
            public int compare(TabBannerDTO lhs, TabBannerDTO rhs) {
                // TODO Auto-generated method stub
                return Long.valueOf(lhs.getTabIndexPosition())
                        .compareTo(Long.valueOf(rhs.getTabIndexPosition()));
            }
        });
        for (TabBannerDTO tabBannerDTO : lstTabBannerData) {
            Bundle bundle = new Bundle();
            bundle.putString("tabId", String.valueOf(tabBannerDTO.getTabId()));
            bundle.putString("tabName", tabBannerDTO.getTabName());
            bundle.putString("videoUrl", videoUrl);

            if (tabBannerDTO.getTabName().toLowerCase().contains("featured")) {
                fragments.add(Fragment.instantiate(this, FeaturedFragment.class.getName(), bundle));
            } else if (tabBannerDTO.getTabName().toLowerCase().contains("games"))
                fragments.add(Fragment.instantiate(this, GamesFragment.class.getName(), bundle));
            else if (tabBannerDTO.getTabName().toLowerCase().contains("players"))
                fragments.add(Fragment.instantiate(this, PlayerFragment.class.getName(), bundle));
            else if (tabBannerDTO.getTabName().toLowerCase().contains("coach"))
                fragments.add(Fragment.instantiate(this, CoachesEraFragment.class.getName(), bundle));
            else if (tabBannerDTO.getTabName().toLowerCase().contains("opponent"))
                fragments.add(Fragment.instantiate(this, OpponentsFragment.class.getName(), bundle));
        }
        fragments.add(Fragment.instantiate(this, FavoritesFragment.class.getName()));

        this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        this.mPager.setAdapter(this.mPagerAdapter);

    }

    public void setUpPullOptionHeader() {
        final View pullView = findViewById(R.id.rl_pull_option);

        final SharedPreferences prefs = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        boolean isPullHeaderSeen = prefs.getBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false);

        Button btnGotIt = (Button) pullView.findViewById(R.id.btn_got_it);

        btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, true).commit();

                Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.abc_fade_out);
                pullView.setVisibility(View.GONE);
                pullView.setAnimation(anim);
            }
        });

        if (isPullHeaderSeen) {
            pullView.setVisibility(View.GONE);
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

                        stopService(new Intent(MainActivity.this, VideoDataService.class));
//                        VideoDataFetchingService.isServiceRunning = false;

                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();

                        SharedPreferences prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).commit();
//                        prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).commit();

                        Intent intent = new Intent(context, LoginEmailActivity.class);
                        context.startActivity(intent);
                        context.finish();
//                        context.finish();
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

    View view;
    public static LinearLayout linearLayout;
    View facebookShareView;
    View twitterShareView;
    public void makeShareDialog(final long videoId, final String videoSocialUrl, final String videoShortUrl, final String imageUrl, final String description, final String name, final Activity context) {

        //   shareData(videoSocialUrl, imageUrl, description, name);
        view = findViewById(R.id.sharinglayout);

        linearLayout = (LinearLayout) view.findViewById(R.id.social_sharing_linear_layout);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.sliding_up_dialog);
                linearLayout.setAnimation(animation);
                linearLayout.setVisibility(View.VISIBLE);
            }
        }, 300);

//        Handler handlerThread = new Handler();
//        handlerThread.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                animation = AnimationUtils.loadAnimation(VideoInfoActivity.this, R.anim.sliding_down_dialog);
//                linearLayout.setAnimation(animation);
//                linearLayout.setVisibility(View.GONE);
//            }
//        }, 10000);

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
//        Button flatButtonFacebook = (Button) view.findViewById(R.id.facebookShare);
//        Button flatButtonTwitter = (Button) view.findViewById(R.id.twitterShare);

        ImageView flatButtonFacebook = (ImageView) view.findViewById(R.id.facebookShare);
        ImageView flatButtonTwitter = (ImageView) view.findViewById(R.id.twitterShare);
        facebookShareView = (View) view.findViewById(R.id.facebookShareView);
        twitterShareView = (View) view.findViewById(R.id.twitterShareView);

//        View view = context.getLayoutInflater().inflate(R.layout.share_dialog, null);
//
//        int Measuredwidth = 0;
//        try {
//            Point size = new Point();
//            WindowManager w = getWindowManager();
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                w.getDefaultDisplay().getSize(size);
//                Measuredwidth = size.x;
//            } else {
//                Display d = w.getDefaultDisplay();
//                Measuredwidth = d.getWidth();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        //        Button flatButtonFacebook = (Button) view.findViewById(R.id.facebookShare);
////        Button flatButtonTwitter = (Button) view.findViewById(R.id.twitterShare);
//
//        ImageView flatButtonFacebook = (ImageView) view.findViewById(R.id.facebookShare);
//        ImageView flatButtonTwitter = (ImageView) view.findViewById(R.id.twitterShare);
//
//
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (Measuredwidth * 0.40), LinearLayout.LayoutParams.WRAP_CONTENT);
//        flatButtonFacebook.setLayoutParams(lp);
//        flatButtonTwitter.setLayoutParams(lp);

//        flatButtonFacebook.setButtonColor(Color.parseColor("#3B5898"));
//        flatButtonTwitter.setButtonColor(Color.parseColor("#5EA9DD"));

        twitterLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button_share);

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
//                Toast.makeText(context,"Twitter Login Done",Toast.LENGTH_SHORT).show();
                try {
                    /*TweetComposer.Builder builder = new TweetComposer.Builder(context)
                            .text("UGA Vault Tweet")
                            .url(new URL(videoUrl))
                            .image(Uri.parse(imageUrl));

                    builder.show();*/
                    if (name != null && description != null) {
                        Intent intent = new TweetComposer.Builder(MainActivity.this)
                                .text(name + "\n" + description + "\n\n")
                                .url(new URL(videoShortUrl))
                                .createIntent();

                        startActivityForResult(intent, 100);
                    } else if (name != null) {
                        Intent intent = new TweetComposer.Builder(MainActivity.this)
                                .text(name + "\n\n")
                                .url(new URL(videoShortUrl))
                                .createIntent();

                        startActivityForResult(intent, 100);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void failure(TwitterException e) {
                //Toast.makeText(MainActivity.this, "Login was cancelled", Toast.LENGTH_LONG).show();
                showToastMessage(GlobalConstants.TWITTER_LOGIN_CANCEL);
            }
        });

        flatButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(MainActivity.this)) {
                    if (videoShortUrl != null) {
//                        if (videoShortUrl.length() == 0) {
//                            //Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
//                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
//                        } else {


                        ((MainActivity) context).shareVideoUrl(videoId, videoSocialUrl, imageUrl, description, name, context);
//                        }
                    } else {
//                    Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
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
                // progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(MainActivity.this)) {
                    if (videoShortUrl != null) {
//                        if (videoShortUrl.length() == 0) {
////                        Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
//                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
//                        } else
                        {
                            TwitterSession session = Twitter.getSessionManager().getActiveSession();
                            if (session == null) {
                                twitterLoginButton.performClick();
                            } else {
                                try {
                                    if (name != null && description != null) {
                                    /*Intent intent = new TweetComposer.Builder(MainActivity.this)
                                            .text(name + "\n" + description + "\n\n")
                                            .url(new URL(videoShortUrl))
                                            .createIntent();

                                    startActivityForResult(intent, 100);*/

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(name + "\n" + description + "\n\n")
                                                .url(new URL("http://0b78b111a9d0410784caa8a634aa3b90.cloudapp.net/Sample.html"));

                                        builder.show();
                                    } else if (name != null) {
                                    /*Intent intent = new TweetComposer.Builder(MainActivity.this)
                                            .text(name + "\n\n")
                                            .url(new URL(videoShortUrl))
                                            .createIntent();

                                    startActivityForResult(intent, 100);*/

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(name + "\n\n")
                                                .url(new URL("http://0b78b111a9d0410784caa8a634aa3b90.cloudapp.net/Sample.html"));
                                        builder.show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
//                    Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
                        showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }

            }
        });

//        progressDialog = new ProgressDialog(context);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setCancelable(true);
//        progressDialog.setCanceledOnTouchOutside(true);
//        progressDialog.show();
//        progressDialog.setContentView(view);
    }


    private void shareData(final String videoSocialUrl, final String imageUrl, final String description, final String name) {

//
//        List<Intent> targetedShareIntents = new ArrayList<Intent>();
//
//        Intent facebookIntent = getShareIntent("facebook", "subject", "text",videoSocialUrl,imageUrl,description,name);
//        if (facebookIntent != null)
//            targetedShareIntents.add(facebookIntent);
//
//        Intent twitterIntent = getShareIntent("com.twitter.android", "subject", "text",videoSocialUrl,imageUrl,description,name);
//        if (twitterIntent != null)
//            targetedShareIntents.add(twitterIntent);

        String message = "Text I want to share.";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(share, "Share text!"));

//        Intent gmailIntent = getShareIntent("gmail", "subject", "text");
//        if(gmailIntent != null)
//            targetedShareIntents.add(gmailIntent);
//
//        Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), "Share with Friends");
//
//        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
//
//        startActivity(chooser);
    }


    private Intent getShareIntent(String type, String subject, String text, String videoSocialUrl, String imageUrl, String description, String name) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.setType("image/*");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type)) {

                    share.putExtra(Intent.EXTRA_SUBJECT, description);
                    share.putExtra(Intent.EXTRA_SUBJECT, name);
                    String videoUrl = videoSocialUrl;

                    File videoUrlToShare = new File(videoUrl);

                    Uri videouri = Uri.fromFile(videoUrlToShare);
                    share.putExtra(Intent.EXTRA_STREAM, videouri);
                    String imagePath = imageUrl;

                    File imageFileToShare = new File(imagePath);

                    Uri uri = Uri.fromFile(imageFileToShare);
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.putExtra(Intent.EXTRA_SUBJECT, subject);
                    share.putExtra(Intent.EXTRA_TEXT, text);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;

            return share;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode,
                data);

        //  Toast.makeText(MainActivity.this, "Request Code : " + requestCode, Toast.LENGTH_SHORT).show();
    }

    public void shareVideoUrl(final long videoId, String videoSocialurl, String imageurl, String description, String name, final Activity context) {
        try {
            final FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {

                @Override
                public void onCancel() {
                    //Log.d("HelloFacebook", "Canceled");
                    showToastMessage(GlobalConstants.FACEBOOK_SHARING_CANCEL);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;
                }

                @Override
                public void onError(FacebookException error) {
                    //Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
                    String title = "Error";
                    String alertMessage = error.getMessage();
                    showToastMessage(GlobalConstants.FACEBOOK_SHARING_CANCEL);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;
                    //showResult(title, alertMessage);
                }


                @Override
                public void onSuccess(Sharer.Result result) {
                    //Log.d("HelloFacebook", "Success!");
                    boolean installed = checkIfFacebookAppInstalled("com.facebook.android");
                    if (!installed)
                        installed = checkIfFacebookAppInstalled("com.facebook.katana");
                    if (!installed)
                        showToastMessage(GlobalConstants.FACEBOOK_POST_SUCCESS_MESSAGE);
                    GlobalConstants.IS_SHARING_ON_FACEBOOK = false;

                    String videoIdData = String.valueOf(videoId);

                    shareInfoTask = new shareInfoTask();
                    shareInfoTask.execute(videoIdData);

                    /*************** share post data *******************/
                    /**************execute asan task**********/

                    /************************/
                /*if (result.getPostId() != null) {
                    String title = "Success";
                    String id = result.getPostId();
                    String alertMessage = "Successfully Posted";
                    showResult(title, alertMessage);
                }*/
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

            // Can we present the share dialog for photos?
        /*canPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);
        canPresentShareDialogWithVideos = ShareDialog.canShow(ShareVideoContent.class);*/

            Profile profile = Profile.getCurrentProfile();

            if (imageurl.contains("10.10.10"))
                imageurl = "http://static.parastorage.com/services/wixapps/2.460.0/javascript/wixapps/apps/blog/images/no-image-icon.png";


            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(name)
                    .setContentDescription(description)
                    .setImageUrl(Uri.parse(imageurl))
                    .setContentUrl(Uri.parse(videoSocialurl))
                    .build();


            if (profile != null) {
                if (canPresentShareDialog) {
                    shareDialog.show(linkContent);
                } else if (profile != null && hasPublishPermission()) {
                    ShareApi.share(linkContent, shareCallback);
                }
            } else {
                ((MainActivity) context).loginWithFacebook(videoId, videoSocialurl, imageurl, description, name, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSuccessfullPost = false;

    private Uri openApp(String str) {
        Uri targetUrl = Uri.parse("");// Uri.parse(str);
        // if (isSuccessfullPost)
        {
            Toast.makeText(MainActivity.this, "Request Code : ", Toast.LENGTH_SHORT).show();
            boolean installed = appInstalledOrNot("com.ncsavault.floridavault");
            if (installed) {
                //This intent will help you to launch if the package is already installed
//                LaunchIntent = getPackageManager()
//                        .getLaunchIntentForPackage("com.ncsavault.floridavault");
//                startActivity(LaunchIntent);
//            }
//            targetUrl =
//                    AppLinks.getTargetUrlFromInboundIntent(this, LaunchIntent);
            }
        }
        Log.i("targetUrl", "targetUrl123 " + targetUrl.toString());
        return targetUrl;

    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        CrashManager.execute(this, null);
        AppEventsLogger.activateApp(this);
       // System.out.println("onResume gethideKeyboard");
       // gethideKeyboard();
    }

    /**
     * hiding keyboard
     */
    private void gethideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            System.out.println("onResume gethideKeyboard111 ");
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        GlobalConstants.SEARCH_VIEW_QUERY = "";
    }


    public void loginWithFacebook(long videoIds, String vidurl, String imgurl, String desc, String n, Activity ctx) {
        imageurl = imgurl;
        description = desc;
        name = n;
        videoId = videoIds;
        videoSocialUrl =vidurl;
//        Toast.makeText(ctx, "Your are not logged in, please login", Toast.LENGTH_LONG).show();
        LoginManager.getInstance().logInWithReadPermissions(ctx, Arrays.asList("public_profile, email, user_birthday"));
    }

    //public static ProgressBar auto_refresh_progress_bar;
    private void initViews() {
        mPager = (ViewPager) findViewById(R.id.pager);
        // auto_refresh_progress_bar = (ProgressBar) findViewById(R.id.auto_refresh_progress_bar);
        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        View autoRefreshView = findViewById(R.id.auto_refresh_progress_main);
        autoRefreshProgressBar = (ProgressBar) autoRefreshView.findViewById(R.id.auto_refresh_progress_bar);

        actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color
                .parseColor(gryColor)));
//        actionBar.setLogo(R.drawable.actionbaricon);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


     /*SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG,0);
        boolean isSkipLogin = pref.getBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false);
        if (userId > 0 && isSkipLogin)  {
            // try to see if already exists
            MenuItem editItem = menu.findItem(1001);
            if (editItem == null) {
                menu.add(0, 1001, 0,
                        "Log In");
            }
        } else {
            // we need to remove it when the condition fails
            menu.removeItem(1001);
        }*/
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        boolean isSkipLogin = pref.getBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false);
        if (isSkipLogin || AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
            // try to see if already exists
            MenuItem item = menu.findItem(R.id.action_profile);
            if (item != null) {
                menu.removeItem(R.id.action_profile);
            }

            MenuItem editItem = menu.findItem(1001);
            if (editItem == null) {
                menu.add(0, 1001, 0,
                        "Log In");
            }
        } else if (AppController.getInstance().getUserId() > 0 || Profile.getCurrentProfile() != null) {
            MenuItem item = menu.findItem(1001);
            if (item != null)
                menu.removeItem(1001);
        }

        /*// Find the menuItem to add your SubMenu
        MenuItem myMenuItem = menu.findItem(R.id.action_contact);

        // Inflating the sub_menu menu this way, will add its menu items
        // to the empty SubMenu you created in the xml
        getMenuInflater().inflate(R.menu.contact_menu, myMenuItem.getSubMenu());*/

        // ---------- intializing searchview in actionbar------------
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setIconified(true);
        searchView.clearFocus();

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
//        int searchSubmitId = searchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
        // Getting the 'search_plate' LinearLayout.
        View searchPlate = searchView.findViewById(searchPlateId);
//        View searchSubmit = searchView.findViewById(searchSubmitId);

        // Setting background of 'search_plate' to earlier defined drawable.
        searchPlate.setBackgroundResource(R.drawable.searchview_selector);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//         end the flurry session
        FlurryAgent.onEndSession(context);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {

            case R.id.action_toggle_notifications:
                Utils.getInstance().showNotificationToggleSetting(MainActivity.this);
                break;
            case R.id.action_profile:
                Intent intentProfile = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intentProfile);
                return true;
            case R.id.action_contact:
                long userId = AppController.getInstance().getUserId();
                if (userId == GlobalConstants.DEFAULT_USER_ID) {
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    //add no login TagId
                    intent.putExtra("tagId", GlobalConstants.NO_LOGIN_TAG_ID);
                    startActivity(intent);
                } else {
                    CharSequence supportOptions[] = new CharSequence[]{"Somethings Wrong", "I have a suggestion", "Clip Request"};

                    TextView title = new TextView(this);
                    title.setText("Support");
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
//                title.setTextColor(Color.RED);
                    title.setTextSize(20);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCustomTitle(title);
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setItems(supportOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent supportIntent = new Intent(MainActivity.this, ContactActivity.class);
                            if (which == 0) {
                                supportIntent.putExtra("title", getResources().getString(R.string.support_title));
                                supportIntent.putExtra("subtitle", getResources().getString(R.string.support_sub_title));
                                supportIntent.putExtra("tagId", GlobalConstants.SUPPORT_TAG_ID);
                                startActivity(supportIntent);
                            } else if (which == 1) {
                                supportIntent.putExtra("title", getResources().getString(R.string.feedback_title));
                                supportIntent.putExtra("subtitle", getResources().getString(R.string.feedback_sub_title));
                                supportIntent.putExtra("tagId", GlobalConstants.FEEDBACK_TAG_ID);
                                startActivity(supportIntent);
                            } else if (which == 2) {
                                supportIntent.putExtra("title", getResources().getString(R.string.clip_request_title));
                                supportIntent.putExtra("subtitle", getResources().getString(R.string.clip_request_sub_title));
                                supportIntent.putExtra("tagId", GlobalConstants.CLIP_REQUEST_TAG_ID);
                                startActivity(supportIntent);
                            }
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                        }
                    });
                    builder.show();
                }
                break;
            case 1001:

                stopService(new Intent(MainActivity.this, VideoDataService.class));
//                VideoDataFetchingService.isServiceRunning = false;

                VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();

                SharedPreferences prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).commit();
                prefs.edit().putBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false).commit();
//                prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).commit();

                Intent intentLogin = new Intent(MainActivity.this, LoginEmailActivity.class);
                startActivity(intentLogin);
                finish();
            default:
                break;
        }
        return false;
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
                animation = AnimationUtils.loadAnimation(MainActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }

    String postResult;
    AsyncTask<String, Void, String> shareInfoTask;

    public class shareInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                postResult = AppController.getInstance().getServiceManager().getVaultService().postSharingInfo(params[0].toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return postResult;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    /*public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }*/

    public void setDimensions() {

        LocalModel localModel = LocalModel.getInstance();
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            localModel.setmDisplayHeight(size.y);
            localModel.setmDisplayWidth(size.x);
        } else {
            Display d = w.getDefaultDisplay();
            localModel.setmDisplayHeight(d.getHeight());
            localModel.setmDisplayWidth(d.getWidth());
        }

    }

    CountDownTimer countDownTimer;
    public void autoRefresh() {
        countDownTimer = new CountDownTimer(GlobalConstants.AUTO_REFRESH_INTERVAL, GlobalConstants.AUTO_REFRESH_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                loadBannerData();
            }


            public void onFinish() {
                if (countDownTimer != null) {
                    countDownTimer.start();
                }

                loadBannerData();
            }

        }.start();
    }



    private AsyncTask<Void, Void, ArrayList<TabBannerDTO>> mBannerTask;

    public void loadBannerData() {
        System.out.println("loadBannerData");
        mBannerTask = new AsyncTask<Void, Void, ArrayList<TabBannerDTO>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (autoRefreshProgressBar != null) {
                    autoRefreshProgressBar.setVisibility(View.VISIBLE);
                }

            }

            @Override
            protected ArrayList<TabBannerDTO> doInBackground(Void... params) {


                ArrayList<TabBannerDTO> arrayListBanner = new ArrayList<TabBannerDTO>();
                Intent broadCastIntent = new Intent();
                try {
                    arrayListBanner.addAll(AppController.getInstance().getServiceManager().getVaultService().getAllTabBannerData());

                ArrayList<String> lstUrls = new ArrayList<>();

                File imageFile;
                for (TabBannerDTO bDTO : arrayListBanner) {
                    TabBannerDTO localBannerData = VaultDatabaseHelper.getInstance(getApplicationContext()).getLocalTabBannerDataByTabId(bDTO.getTabId());
                    if (localBannerData != null) {
                        if ((localBannerData.getBannerModified() != bDTO.getBannerModified()) || (localBannerData.getBannerCreated() != bDTO.getBannerCreated()))
                        {
                            VaultDatabaseHelper.getInstance(getApplicationContext()).updateBannerData(bDTO);
                        }
                        if (localBannerData.getTabDataModified() != bDTO.getTabDataModified()) {
                            VaultDatabaseHelper.getInstance(getApplicationContext()).updateTabData(bDTO);
                            if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.FEATURED).toLowerCase())) {
                                VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_FEATURED);
                                lstUrls.add(GlobalConstants.FEATURED_API_URL);
                                String url = GlobalConstants.FEATURED_API_URL + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);
                                broadCastIntent.setAction(FeaturedFragment.FeaturedResponseReceiver.ACTION_RESP);
                            } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.GAMES).toLowerCase())) {
                                VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_GAMES);
                                lstUrls.add(GlobalConstants.GAMES_API_URL);
                                String url = GlobalConstants.GAMES_API_URL + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);
                                broadCastIntent.setAction(GamesFragment.GamesResponseReceiver.ACTION_RESP);
                            } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.PLAYERS).toLowerCase())) {
                                VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_PLAYERS);

                                lstUrls.add(GlobalConstants.PLAYER_API_URL);
                                String url = GlobalConstants.PLAYER_API_URL + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);
                                broadCastIntent.setAction(PlayerFragment.PlayerResponseReceiver.ACTION_RESP);
                            } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.OPPONENTS).toLowerCase())) {
                                VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_OPPONENT);
                                lstUrls.add(GlobalConstants.OPPONENT_API_URL);
                                String url = GlobalConstants.OPPONENT_API_URL + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);
                                broadCastIntent.setAction(OpponentsFragment.OpponentsResponseReceiver.ACTION_RESP);
                            } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.COACHES_ERA).toLowerCase())) {
                                VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_COACH);
                                lstUrls.add(GlobalConstants.COACH_API_URL);
                                String url = GlobalConstants.COACH_API_URL + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);
                                broadCastIntent.setAction(CoachesEraFragment.CoachesResponseReceiver.ACTION_RESP);
                            }
                            imageFile = ImageLoader.getInstance().getDiscCache().get(localBannerData.getBannerURL());
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                            MemoryCacheUtils.removeFromCache(localBannerData.getBannerURL(), ImageLoader.getInstance().getMemoryCache());
                            broadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                            sendBroadcast(broadCastIntent);
                            arrayListVideos.clear();
                        }
                    } else {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).insertTabBannerData(bDTO);
                    }


                }
                if (lstUrls.size() == 0) {
                    int count = VaultDatabaseHelper.getInstance(getApplicationContext()).getTabBannerCount();
                    if (count > 0) {
                        lstUrls.add(GlobalConstants.FEATURED_API_URL);
                        lstUrls.add(GlobalConstants.GAMES_API_URL);
                        lstUrls.add(GlobalConstants.PLAYER_API_URL);
                        lstUrls.add(GlobalConstants.OPPONENT_API_URL);
                        lstUrls.add(GlobalConstants.COACH_API_URL);
                    }
                }
                AppController.getInstance().setAPI_URLS(lstUrls);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<TabBannerDTO> bannerDTOs) {
                super.onPostExecute(bannerDTOs);

                try {
                    VideoDataService.isServiceRunning = false;
                    stopService(new Intent(MainActivity.this, VideoDataService.class));
                    System.out.println("tabBannerDTO MainActivity ");
                    // new startAutoRefresh().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //  startService(new Intent(MainActivity.this, VideoDataService.class));

                            if (autoRefreshProgressBar != null) {
                                autoRefreshProgressBar.setVisibility(View.GONE);
                            }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mBannerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    ArrayList<VideoDTO> arrayListVideos = new ArrayList<VideoDTO>();

}
