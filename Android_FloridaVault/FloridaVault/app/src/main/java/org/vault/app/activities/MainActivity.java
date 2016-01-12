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
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
import android.widget.Button;
import android.widget.LinearLayout;
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
import org.vault.app.fragments.CoachesEraFragment;
import org.vault.app.fragments.FavoritesFragment;
import org.vault.app.fragments.FeaturedFragment;
import org.vault.app.fragments.GamesFragment;
import org.vault.app.fragments.OpponentsFragment;
import org.vault.app.fragments.PlayerFragment;
import org.vault.app.globalconstants.GlobalConstants;
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

    private static CallbackManager callbackManager;

    private static ShareDialog shareDialog;

    AlertDialog alertDialog = null;
    private static boolean canPresentShareDialog;

    TwitterLoginButton twitterLoginButton;
    public ProgressDialog progressDialog;


    ProfileTracker profileTracker;



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        setContentView(R.layout.main_activity);
        context = MainActivity.this;

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
                    shareVideoUrl(videourl, imageurl, description, name, context);
                }
            }
        };

    }

    /**
     * Method used for facebook install or not
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

    public void makeShareDialog(final String videoUrl, final String videoShortUrl, final String imageUrl, final String description, final String name, final Activity context) {
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
                progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(MainActivity.this)) {
                    if (videoShortUrl != null) {
                        if (videoShortUrl.length() == 0) {
                            //Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                        } else {
                            ((MainActivity) context).shareVideoUrl(videoShortUrl, imageUrl, description, name, context);
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

        flatButtonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
                if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                    showConfirmLoginDialog(GlobalConstants.SHARE_MESSAGE);
                } else if (Utils.isInternetAvailable(MainActivity.this)) {
                    if (videoShortUrl != null) {
                        if (videoShortUrl.length() == 0) {
//                        Toast.makeText(context, "Video Information Not Available To Share", Toast.LENGTH_SHORT).show();
                            showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE + " to share");
                        } else {
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
                                                .url(new URL(videoShortUrl));

                                        builder.show();
                                    } else if (name != null) {
                                    /*Intent intent = new TweetComposer.Builder(MainActivity.this)
                                            .text(name + "\n\n")
                                            .url(new URL(videoShortUrl))
                                            .createIntent();

                                    startActivityForResult(intent, 100);*/

                                        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                                                .text(name + "\n\n")
                                                .url(new URL(videoShortUrl));

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

        progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        progressDialog.setContentView(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode,
                data);

//        Toast.makeText(MainActivity.this,"Request Code : "+requestCode,Toast.LENGTH_SHORT).show();
    }

    public void shareVideoUrl(String videourl, String imageurl, String description, String name, final Activity context) {
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
                    .setContentUrl(Uri.parse(videourl))
                    .build();

            if (profile != null) {
                if (canPresentShareDialog) {
                    shareDialog.show(linkContent);
                } else if (profile != null && hasPublishPermission()) {
                    ShareApi.share(linkContent, shareCallback);
                }
            } else {
                ((MainActivity) context).loginWithFacebook(videourl, imageurl, description, name, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public void loginWithFacebook(String vidurl, String imgurl, String desc, String n, Activity ctx) {
        videourl = vidurl;
        imageurl = imgurl;
        description = desc;
        name = n;
//        Toast.makeText(ctx, "Your are not logged in, please login", Toast.LENGTH_LONG).show();
        LoginManager.getInstance().logInWithReadPermissions(ctx, Arrays.asList("public_profile, email, user_birthday"));
    }


    private void initViews() {
        mPager = (ViewPager) findViewById(R.id.pager);
        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
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

    /*public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }*/
}
