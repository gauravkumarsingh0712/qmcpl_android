package com.ncsavault.floridavault;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.vault.app.activities.MainActivity;
import org.vault.app.activities.PermissionActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.VideoDTO;
import org.vault.app.fragments.CoachesEraFragment;
import org.vault.app.fragments.FeaturedFragment;
import org.vault.app.fragments.GamesFragment;
import org.vault.app.fragments.OpponentsFragment;
import org.vault.app.fragments.PlayerFragment;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by gauravkumar.singh on 11/24/2015.
 */
public class SplashActivity extends PermissionActivity {

    public static final String tag = SplashActivity.class.getSimpleName();
    Profile fbProfile;
    private AsyncTask<Void, Void, ArrayList<TabBannerDTO>> mBannerTask;
    private ProgressDialog pDialog;
    private ArrayList<TabBannerDTO> bannerDTOArrayList = new ArrayList<TabBannerDTO>();
    private Animation animation;
    private String videoUrlData;
    private boolean isBackToSplashScreen = false;
    private boolean askAgainForMustPermissions = false;
    private boolean goToSettingsScreen = false;
    private AlertDialog alertDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        hashKey();
        Uri uri = getIntent().getData();
        if (uri != null) {
            String videoUrl = String.valueOf(uri);

            if(videoUrl.toLowerCase().contains("floridavault")) {
                videoUrl = videoUrl.substring(11);
                String[] videoParams = videoUrl.split("=");
                videoUrlData = videoParams[1];
                System.out.println("videoUrl : " + videoParams[1]);
            }else if(videoUrl.toLowerCase().contains("vaultservices"))
            {
                videoUrl = videoUrl.substring(7);
                String[] videoParams = videoUrl.split("/");
                if(videoParams.length > 0) {
                    String str = videoParams[3].toString();
                    String[] values = str.split("\\.");
                    videoUrlData = values[0];
                }

            }else if(videoUrl.toLowerCase().contains("0b78b111a9d0410784caa8a634aa3b90"))
            {
                videoUrl = videoUrl.substring(7);
                String[] videoParams = videoUrl.split("/");
                if(videoParams.length > 0) {
                    String str = videoParams[3].toString();
                    String[] values = str.split("\\.");
                    videoUrlData = values[0];
                }
            }

        }


        setContentView(R.layout.splash_activity);
        File cacheDir = StorageUtils.getCacheDirectory(SplashActivity.this);
        ImageLoaderConfiguration config;
        config = new ImageLoaderConfiguration.Builder(SplashActivity.this)
                .threadPoolSize(3) // default
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .build();
        ImageLoader.getInstance().init(config);


        //Marshamallow permission
        if (haveAllMustPermissions()) {
            initViews();
            startApp();
        }
//        startLoading();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AppEventsLogger.activateApp(this);
    }

    public void initViews() {
        //  mShimmerViewContainer = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
//        mShimmerViewContainer.startShimmerAnimation();
        try {
            Point size = new Point();
            WindowManager w = getWindowManager();
            int screenWidth;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                w.getDefaultDisplay().getSize(size);
                screenWidth = size.y;
                // Measuredheight = size.y;
            } else {
                Display d = w.getDefaultDisplay();
                // Measuredheight = d.getHeight();
                screenWidth = d.getHeight();
            }

            int dimension = (int) (screenWidth * 0.45);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dimension, dimension);
            lp.setMargins(0, 30, 0, 0);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            //  mShimmerViewContainer.setLayoutParams(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionResult(int requestCode, boolean isGranted, Object extras) {
        switch (requestCode) {
            case PERMISSION_REQUEST_MUST:
                if (isGranted) {
                    //perform action here
                    initViews();
                    startApp();
                } else {
                    if (!askAgainForMustPermissions) {
                        askAgainForMustPermissions = true;
                        // Toast.makeText(this, "Please provide all the Permissions to Make the App work for you.", Toast.LENGTH_LONG).show();
                        haveAllMustPermissions();
                    } else if (!goToSettingsScreen) {
                        goToSettingsScreen = true;

                        showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_PERMISSION);

                    }else
                    {
                        showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_PERMISSION);
                    }

                }
                break;
        }
    }

    ArrayList<VideoDTO> arrayListVideos = new ArrayList<VideoDTO>();
    public void loadBannerData() {
        mBannerTask = new AsyncTask<Void, Void, ArrayList<TabBannerDTO>>() {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Profile fbProfile = Profile.getCurrentProfile();
                            SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                            long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

                            if (fbProfile != null || userId > 0) {
//                            Toast.makeText(SplashActivity.this, "Going to Main Activity", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (videoUrlData != null) {
                                    intent.putExtra("videoUrl", videoUrlData);
                                }
                                startActivity(intent);
                                overridePendingTransition(R.anim.slideup, R.anim.nochange);
                                finish();
                                if (!VideoDataService.isServiceRunning)
                                    startService(new Intent(SplashActivity.this, VideoDataService.class));
                            } else {
                                VaultDatabaseHelper.getInstance(SplashActivity.this).removeAllTabBannerData();
                                Intent intent = new Intent(SplashActivity.this, LoginEmailActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slideup, R.anim.nochange);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 3000);
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
                            if ((localBannerData.getBannerModified() != bDTO.getBannerModified()) || (localBannerData.getBannerCreated() != bDTO.getBannerCreated())) {
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

//                            if (!VideoDataService.isServiceRunning)
//                                startService(new Intent(SplashActivity.this, VideoDataService.class));
                        }
                    }
                    AppController.getInstance().setAPI_URLS(lstUrls);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return arrayListBanner;
            }

            @Override
            protected void onPostExecute(ArrayList<TabBannerDTO> bannerDTOs) {
                super.onPostExecute(bannerDTOs);
//                try {
//
//                    // getBannerAndTabData(bannerDTOs);
//
//                    Profile fbProfile = Profile.getCurrentProfile();
//                    SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
//                    long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
//
//                    if (fbProfile != null || userId > 0) {
////                        Toast.makeText(SplashActivity.this, "Going to Main Activity", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        if (videoUrlData != null) {
//                            intent.putExtra("videoUrl", videoUrlData);
//                        }
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.slideup, R.anim.nochange);
//                        finish();
////                        if (!VideoDataService.isServiceRunning )
////                            startService(new Intent(SplashActivity.this, VideoDataService.class));
//                    } else {
//                        VaultDatabaseHelper.getInstance(SplashActivity.this).removeAllTabBannerData();
//                        Intent intent = new Intent(SplashActivity.this, LoginEmailActivity.class);
//                        if (videoUrlData != null) {
//                            intent.putExtra("videoUrl", videoUrlData);
//                        }
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.slideup, R.anim.nochange);
//                        finish();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        };

        mBannerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void getBannerAndTabData(ArrayList<TabBannerDTO> arrayListBanner) {
        ArrayList<String> lstUrls = new ArrayList<>();

        File imageFile;
        for (TabBannerDTO bDTO : arrayListBanner) {
            TabBannerDTO localBannerData = VaultDatabaseHelper.getInstance(getApplicationContext()).getLocalTabBannerDataByTabId(bDTO.getTabId());
            if (localBannerData != null) {
                if ((localBannerData.getBannerModified() != bDTO.getBannerModified()) || (localBannerData.getBannerCreated() != bDTO.getBannerCreated())) {
                    VaultDatabaseHelper.getInstance(getApplicationContext()).updateBannerData(bDTO);
                }
                if (localBannerData.getTabDataModified() != bDTO.getTabDataModified()) {
                    VaultDatabaseHelper.getInstance(getApplicationContext()).updateTabData(bDTO);
                    if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.FEATURED).toLowerCase())) {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_FEATURED);
                        lstUrls.add(GlobalConstants.FEATURED_API_URL);
                    } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.GAMES).toLowerCase())) {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_GAMES);
                        lstUrls.add(GlobalConstants.GAMES_API_URL);
                    } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.PLAYERS).toLowerCase())) {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_PLAYERS);
                        lstUrls.add(GlobalConstants.PLAYER_API_URL);
                    } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.OPPONENTS).toLowerCase())) {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_OPPONENT);
                        lstUrls.add(GlobalConstants.OPPONENT_API_URL);
                    } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.COACHES_ERA).toLowerCase())) {
                        VaultDatabaseHelper.getInstance(getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_COACH);
                        lstUrls.add(GlobalConstants.COACH_API_URL);
                    }
                    imageFile = ImageLoader.getInstance().getDiscCache().get(localBannerData.getBannerURL());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    MemoryCacheUtils.removeFromCache(localBannerData.getBannerURL(), ImageLoader.getInstance().getMemoryCache());
                    System.out.println("gettting data end111");
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
        System.out.println("gettting data end");
    }

    private void startApp() {
        if (Utils.isInternetAvailable(AppController.getInstance().getApplicationContext())) {
            loadBannerData();
        } else {
            showToastMessage(GlobalConstants.MSG_NO_CONNECTION);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Profile fbProfile = Profile.getCurrentProfile();
                        SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

                        if (fbProfile != null || userId > 0) {
//                            Toast.makeText(SplashActivity.this, "Going to Main Activity", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (videoUrlData != null) {
                                intent.putExtra("videoUrl", videoUrlData);
                            }
                            startActivity(intent);
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                            finish();
                            if (!VideoDataService.isServiceRunning)
                                startService(new Intent(SplashActivity.this, VideoDataService.class));
                        } else {
                            VaultDatabaseHelper.getInstance(SplashActivity.this).removeAllTabBannerData();
                            Intent intent = new Intent(SplashActivity.this, LoginEmailActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);

        }
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
                animation = AnimationUtils.loadAnimation(SplashActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ncsavault.floridavault",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                // Log.d("My key Hash : ", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                System.out.println("My key Hash : " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        System.out.println("call on resume");

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isBackToSplashScreen) {
                isBackToSplashScreen = false;
                if (haveAllMustPermissions()) {
                    initViews();
                    startApp();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
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
                        showPermissionsConfirmationDialog(GlobalConstants.UGA_VAULT_PERMISSION);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 500) {
            isBackToSplashScreen = true;
        }
    }

    public void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, 500);
    }
}
