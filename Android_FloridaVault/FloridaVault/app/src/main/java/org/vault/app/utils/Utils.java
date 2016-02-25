package org.vault.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.mailchimp.org.xmlrpc.android.XMLRPCException;
import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.lists.ListMethods;
import org.vault.app.mailchimp.rsg.mailchimp.api.lists.MergeFieldListUtil;
import org.vault.app.model.LocalModel;
import org.vault.app.service.VideoDataService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * @author aqeeb.pathan
 */
public class Utils {


    private static Utils utilInstance;

    private ProgressDialog progressDialog;

    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
    AsyncTask<Void, Void, Void> mPermissionChangeTask;
    SharedPreferences prefs;
    private String result;
    //variable used in mail chimp intregation
    private AsyncTask<Void, Void, Boolean> mMailChimpTask;
    private boolean mIsSignUpSuccessfull;
    private Animation animation;


    public static Utils getInstance() {

        if (utilInstance == null) {
            utilInstance = new Utils();
        }

        return utilInstance;
    }

    /**
     * Check for any type of internet connection
     *
     * @param ctx
     * @return
     */
    public static boolean isInternetAvailable(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static void showNoConnectionMessage(Context ctx) {
        Toast.makeText(ctx, GlobalConstants.MSG_NO_CONNECTION, Toast.LENGTH_SHORT).show();
    }

    //method to check whether the bottom navigation bar exists
    public static boolean hasNavBar(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0)
            return resources.getBoolean(id);
        else
            return false;
    }

    public static int getNavBarStatusAndHeight(Context context) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");

        if (!hasMenuKey && !hasBackKey) {                // Condition worked for Samsung Devices
            result = getNavigationBarHeight(context);
        } else if (id > 0 && context.getResources().getBoolean(id)) {      // Condition will work for Micromax Canvas Nitro 2
            result = getNavigationBarHeight(context);
        } else if ((!(hasBackKey && hasHomeKey))) {        // Condition worked for all other devices
            result = getNavigationBarHeight(context);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        //The device has a navigation bar
        Resources resources = context.getResources();

        int orientation = context.getResources().getConfiguration().orientation;
        int resourceId;
        if (context.getResources().getBoolean(R.bool.isTablet)) {
            resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        } else {
            resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
        }

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * This methoos is slide up the activity
     *
     * @param activity
     */
    public static void slideUpAnimation(Activity activity) {
        // TODO Auto-generated method stub
        activity.overridePendingTransition(R.anim.slideup, R.anim.nochange);
    }

    /**
     * @param context This method is used to show overflow menu forcefully on device
     */
    public static void forcingToShowOverflowMenu(Context context) {
        // forcing to show overflow menu
        try {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addBannerImage(final ImageView bannerCacheableImageView, final LinearLayout layout, TabBannerDTO tabBannerDTO, final Activity context) {
        if (tabBannerDTO != null)
            if (tabBannerDTO.isBannerActive()) {
                DisplayImageOptions imgLoadingOptions = new DisplayImageOptions.Builder()
                        .cacheOnDisk(true).resetViewBeforeLoading(true)
                        .cacheInMemory(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
                com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(tabBannerDTO.getBannerURL(),
                        bannerCacheableImageView, imgLoadingOptions, new ImageLoadingListener() {

                            @Override
                            public void onLoadingStarted(String s, View view) {
                            }

                            @Override
                            public void onLoadingFailed(String s, View view, FailReason failReason) {
                                bannerCacheableImageView.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                Point size = new Point();
                                WindowManager w = context.getWindowManager();
                                int measuredWidth = 0;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    w.getDefaultDisplay().getSize(size);
                                    measuredWidth = size.x;
                                } else {
                                    Display d = w.getDefaultDisplay();
                                    measuredWidth = d.getWidth();
                                }
                                int aspectHeight = (measuredWidth * 3) / 16;

                                FrameLayout.LayoutParams rLp = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, aspectHeight);
                                layout.setLayoutParams(rLp);

                                bannerCacheableImageView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingCancelled(String s, View view) {
                                //                        bannerCacheableImageView.setVisibility(View.GONE);
                            }
                        });
            } else {
                bannerCacheableImageView.setVisibility(View.GONE);
            }
    }

    public static void addBannerImagePullToRefresh(final ImageView bannerCacheableImageView, final LinearLayout llBlock, TabBannerDTO tabBannerDTO, final Activity context, final ProgressBar progressBar) {
        if (tabBannerDTO != null)
            if (tabBannerDTO.isBannerActive()) {
                DisplayImageOptions imgLoadingOptions = new DisplayImageOptions.Builder()
                        .cacheOnDisk(true).resetViewBeforeLoading(true)
                        .cacheInMemory(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
                com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(tabBannerDTO.getBannerURL(),
                        bannerCacheableImageView, imgLoadingOptions, new ImageLoadingListener() {

                            @Override
                            public void onLoadingStarted(String s, View view) {
                                progressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String s, View view, FailReason failReason) {
                                bannerCacheableImageView.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                               progressBar.setVisibility(View.GONE);
                                Point size = new Point();
                                WindowManager w = context.getWindowManager();
                                int measuredWidth = 0;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    w.getDefaultDisplay().getSize(size);
                                    measuredWidth = size.x;
                                } else {
                                    Display d = w.getDefaultDisplay();
                                    measuredWidth = d.getWidth();
                                }
                                int aspectHeight = (measuredWidth * 3) / 16;

                                FrameLayout.LayoutParams rLp = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, aspectHeight);
                                llBlock.setLayoutParams(rLp);
                                bannerCacheableImageView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingCancelled(String s, View view) {
                                progressBar.setVisibility(View.GONE);
                                //                        bannerCacheableImageView.setVisibility(View.GONE);
                            }
                        });
            } else {
                bannerCacheableImageView.setVisibility(View.GONE);
            }
    }


    public static void addImageByCaching(final ImageView bannerCacheableImageView, String url) {
        DisplayImageOptions imgLoadingOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(url,
                bannerCacheableImageView, imgLoadingOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        bannerCacheableImageView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        bannerCacheableImageView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * This method is used to calculate the action bar height
     *
     * @param context
     * @return
     */
    public static int CalculateActionBar(Context context) {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,
                tv, true)) {

            int mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
            return mActionBarHeight;
        }
        return 0;
    }

    /**
     * This method is used to get the status bar height
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * This methos is used to place the view below the action bar
     *
     * @param context
     * @param v
     */
    public static void addProgressBarBelowActionBar(Context context, View v) {
        int actionBarHeight = Utils.CalculateActionBar(context)
                + Utils.getStatusBarHeight(context);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v
                .getLayoutParams();
        layoutParams.setMargins(0, actionBarHeight, 0, 0);
        v.setLayoutParams(layoutParams);
    }

    /**
     * This methos is used to place the view below the action bar
     *
     * @param context
     * @param v
     */
    public static void addPagerIndicatorBelowActionBar(Context context, View v) {
        int actionBarHeight = Utils.CalculateActionBar(context)
                + Utils.getStatusBarHeight(context) ;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v
                .getLayoutParams();
        layoutParams.setMargins(0, actionBarHeight, 0, 0);
        v.setLayoutParams(layoutParams);
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public void registerWithGCM(final Activity mActivity) {
        prefs = mActivity.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        GCMRegistrar.checkDevice(mActivity.getApplicationContext());
        // Make sure the manifest permissions was properly set
        GCMRegistrar.checkManifest(mActivity.getApplicationContext());

        // Register custom Broadcast receiver to show messages on activity
        /*registerReceiver(mHandleMessageReceiver, new IntentFilter(
                GlobalConstants.DISPLAY_MESSAGE_ACTION));*/

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(mActivity.getApplicationContext());

        // Check if regid already presents
        if (regId.equals("")) {
            // Register with GCM
            GCMRegistrar.register(mActivity.getApplicationContext(), GlobalConstants.GOOGLE_SENDER_ID);
        } else {
            // Device is already registered on GCM Server
            mRegisterTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    Log.i("MainActivity", "Device Registration Id : = " + regId);
                    String deviceId = Settings.Secure.getString(mActivity.getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String result = AppController.getInstance().getServiceManager().getVaultService().sendPushNotificationRegistration(GlobalConstants.PUSH_REGISTER_URL,
                            regId, deviceId, true);
                    if (result != null) {
                        System.out
                                .println("Response from server after registration : "
                                        + result);
                        if (result.toLowerCase().contains("success")) {
                            GCMRegistrar.setRegisteredOnServer(mActivity.getApplicationContext(),
                                    true);
                            prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, true).commit();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    mRegisterTask = null;
                }
            };

            // execute AsyncTask
            mRegisterTask.execute();
        }
    }

    public void showNotificationToggleSetting(final MainActivity mActivity) {
        View view = mActivity.getLayoutInflater().inflate(R.layout.notification_toggle, null);
        TextView tv_notification_text = (TextView) view.findViewById(R.id.tv_notification_text);
        Switch switch_notification = (Switch) view.findViewById(R.id.toggle_notification);

        tv_notification_text.setText(mActivity.getResources().getString(R.string.notification_question));

        prefs = mActivity.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);

        boolean isAllowed = prefs.getBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, false);

        switch_notification.setChecked(isAllowed);
        GCMRegistrar.checkDevice(mActivity.getApplicationContext());
        // Make sure the manifest permissions was properly set
        GCMRegistrar.checkManifest(mActivity.getApplicationContext());

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(mActivity.getApplicationContext());

        final String deviceId = Settings.Secure.getString(mActivity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        switch_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                // TODO Auto-generated method stub
                mPermissionChangeTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        Log.i("Sync Dialog", "Device Id : " + deviceId);
                        System.out.println("Registration Id in Toggle Setting Dialog : " + regId);
                        if (isChecked) {
                            if (regId != "") {
                                result = AppController.getInstance().getServiceManager().getVaultService().sendPushNotificationRegistration(GlobalConstants.PUSH_REGISTER_URL, regId, deviceId, isChecked);
                                if (result != null) {
                                    if (result.toLowerCase().contains("success")) {
                                        GCMRegistrar.setRegisteredOnServer(mActivity.getApplicationContext(),
                                                true);
                                        prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, true).commit();
                                    }
                                }
                            } else
                                registerWithGCM(mActivity);
                        } else {
                            result = AppController.getInstance().getServiceManager().getVaultService().sendPushNotificationRegistration(GlobalConstants.PUSH_REGISTER_URL, regId, deviceId, isChecked);
                            if (result != null) {
                                if (result.toLowerCase().contains("success")) {
                                    prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, false).commit();
                                }
                            }
                        }
                        System.out.println("Result of Push Registration Url : " + result);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }
                };

                mPermissionChangeTask.execute();
            }
        });

        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        progressDialog.setContentView(view);
    }

    public Bitmap decodeUri(Uri selectedImage, Activity context) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 340;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);

    }

    public Bitmap rotateImageDetails(Bitmap bitmap, Uri selectedImageUri, Activity context, File sdImageMainDirectory) {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(
                    getRealPathFromURI(selectedImageUri, context));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String orientString = null;
        if (exif != null) {
            orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        }
        int orientation = orientString != null ? Integer.parseInt(orientString)
                : ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            rotationAngle = 90;
        }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            rotationAngle = 180;
        }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotationAngle = 270;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Log.i("Image Details",
                "Image Camera Width: " + rotatedBitmap.getWidth() + " Height: "
                        + rotatedBitmap.getHeight());
        File f = new File(sdImageMainDirectory.getPath());

        try {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.gc();
        return rotatedBitmap;
    }

    public static String getRealPathFromURI(Uri contentURI, Context context) {
        String path = contentURI.getPath();
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        } catch (Exception e) {
            return path;
        }
        return path;
    }

    public static boolean loadDataFromServer(final Activity context) {

        /**
         * This method used for fetching all data for banner from Server
         */


        try {
            ArrayList<TabBannerDTO> arrayListBanner = new ArrayList<TabBannerDTO>();

            arrayListBanner.addAll(AppController.getInstance().getServiceManager().getVaultService().getAllTabBannerData());

            ArrayList<String> lstUrls = new ArrayList<>();
            File imageFile;
            for (TabBannerDTO bDTO : arrayListBanner) {
                TabBannerDTO localBannerData = VaultDatabaseHelper.getInstance(context).getLocalTabBannerDataByTabId(bDTO.getTabId());
                if (localBannerData != null) {
                    if ((localBannerData.getBannerModified() != bDTO.getBannerModified()) || (localBannerData.getBannerCreated() != bDTO.getBannerCreated())) {
                        VaultDatabaseHelper.getInstance(context).updateBannerData(bDTO);
                    }
                    if (localBannerData.getTabDataModified() != bDTO.getTabDataModified()) {
                        VaultDatabaseHelper.getInstance(context).updateTabData(bDTO);
                        if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.FEATURED).toLowerCase())) {
                            VaultDatabaseHelper.getInstance(context).removeRecordsByTab(GlobalConstants.OKF_FEATURED);
                            lstUrls.add(GlobalConstants.FEATURED_API_URL);
                        } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.GAMES).toLowerCase())) {
                            VaultDatabaseHelper.getInstance(context).removeRecordsByTab(GlobalConstants.OKF_GAMES);
                            lstUrls.add(GlobalConstants.GAMES_API_URL);
                        } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.PLAYERS).toLowerCase())) {
                            VaultDatabaseHelper.getInstance(context).removeRecordsByTab(GlobalConstants.OKF_PLAYERS);
                            lstUrls.add(GlobalConstants.PLAYER_API_URL);
                        } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.OPPONENTS).toLowerCase())) {
                            VaultDatabaseHelper.getInstance(context).removeRecordsByTab(GlobalConstants.OKF_OPPONENT);
                            lstUrls.add(GlobalConstants.OPPONENT_API_URL);
                        } else if (localBannerData.getTabName().toLowerCase().contains((GlobalConstants.COACHES_ERA).toLowerCase())) {
                            VaultDatabaseHelper.getInstance(context).removeRecordsByTab(GlobalConstants.OKF_COACH);
                            lstUrls.add(GlobalConstants.COACH_API_URL);
                        }
                        imageFile = ImageLoader.getInstance().getDiscCache().get(localBannerData.getBannerURL());
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                        MemoryCacheUtils.removeFromCache(localBannerData.getBannerURL(), ImageLoader.getInstance().getMemoryCache());
                    }
                } else {
                    VaultDatabaseHelper.getInstance(context).insertTabBannerData(bDTO);
                }
            }
            if (lstUrls.size() == 0) {
                int count = VaultDatabaseHelper.getInstance(context).getTabBannerCount();
                if (count > 0) {
                    lstUrls.add(GlobalConstants.FEATURED_API_URL);
                    lstUrls.add(GlobalConstants.GAMES_API_URL);
                    lstUrls.add(GlobalConstants.PLAYER_API_URL);
                    lstUrls.add(GlobalConstants.OPPONENT_API_URL);
                    lstUrls.add(GlobalConstants.COACH_API_URL);
                }
            }
            AppController.getInstance().setAPI_URLS(lstUrls);

            /*Thread thread = new Thread();
            thread.sleep(3000);*/

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public HashMap getVideoInfoFromBanner(String actionUrl) {
//        actionUrl =  "uga:\\tabId=8;tabKeyword=OKFFeatured;videoId=4124519102076;videoName=Test featured;playlistId=0;playlistName=";
        HashMap videoMap = new HashMap();
        actionUrl = actionUrl.substring(5);
        String[] videoParams = actionUrl.split(";");
        if (videoParams.length > 0) {
            for (int i = 0; i < videoParams.length; i++) {

                String[] values = videoParams[i].split("=");
                if (values[0].toString().toLowerCase().contains("tabid"))
                    videoMap.put("TabId", values[1]);
                else if (values[0].toString().toLowerCase().contains("tabkeyword"))
                    videoMap.put("TabKeyword", values[1]);
                else if (values[0].toString().toLowerCase().contains("videoid"))
                    videoMap.put("VideoId", values[1]);
                else if (values[0].toString().toLowerCase().contains("videoname"))
                    videoMap.put("VideoName", values[1]);
                else if (values[0].toString().toLowerCase().contains("playlistid"))
                    videoMap.put("PlaylistId", values[1]);

            }
        }
        System.out.println("Video Hash Map Length : " + videoMap.size());
        return videoMap;
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

    public static void setVisibilityOfScrollBarHeightForNormalList(Activity activity,String newText, ListView listView) {
//        LocalModel localModel = LocalModel.getInstance();
//        localModel.setmListViewHeight(getTotalHeightofNormalListView(listView));
        int count = VaultDatabaseHelper.getInstance(activity.getApplicationContext()).getFavoriteCount();
      //  System.out.println("getmListViewHeight : "+localModel.getmListViewHeight()+" : "+localModel.getmDisplayHeight());
        if (count < 6) {
            listView.setFastScrollAlwaysVisible(false);
            listView.setVerticalScrollBarEnabled(false);
            listView.setFastScrollEnabled(false);

        } else {
            listView.setFastScrollAlwaysVisible(true);
            listView.setVerticalScrollBarEnabled(true);
            listView.setFastScrollEnabled(true);
        }

        if (newText.equals("")) {
            listView.setFastScrollAlwaysVisible(true);
            listView.setVerticalScrollBarEnabled(true);
            listView.setFastScrollEnabled(true);
        }
    }

    public static void setDisabledStickyListHeadersListViewScrolling(StickyListHeadersListView stickyListHeadersListView) {
        stickyListHeadersListView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true; // Indicates that this has been handled by you and will not be forwarded further.
                }
                return false;
            }
        });

        if (stickyListHeadersListView != null) {
            stickyListHeadersListView.setFastScrollEnabled(false);
            stickyListHeadersListView.setVerticalScrollBarEnabled(false);
            stickyListHeadersListView.setFastScrollAlwaysVisible(false);
        }

    }

    public static void setEnabledStickyListHeadersListViewScrolling(StickyListHeadersListView stickyListHeadersListView) {
        stickyListHeadersListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.setEnabled(true);
                return false;
            }
        });

        if (stickyListHeadersListView != null) {
            stickyListHeadersListView.setFastScrollEnabled(true);
            stickyListHeadersListView.setVerticalScrollBarEnabled(true);
            stickyListHeadersListView.setFastScrollAlwaysVisible(true);
        }

    }

    /**
     * Method used for get list view height dynamically
     *
     * @param listView
     * @return
     */
    public static int getTotalHeightofListViewForHeaderList(StickyListHeadersListView listView) {

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

    public static void setVisibilityOfScrollBarHeightForHeader(String newText, StickyListHeadersListView stickyListHeadersListView) {

        LocalModel localModel = LocalModel.getInstance();
        localModel.setmListViewHeight(getTotalHeightofListViewForHeaderList(stickyListHeadersListView));

        if (localModel.getmListViewHeight() < localModel.getmDisplayHeight()) {
            stickyListHeadersListView.setFastScrollAlwaysVisible(false);
            stickyListHeadersListView.setVerticalScrollBarEnabled(false);
            stickyListHeadersListView.setFastScrollEnabled(false);

        } else {
            stickyListHeadersListView.setFastScrollAlwaysVisible(true);
            stickyListHeadersListView.setVerticalScrollBarEnabled(true);
            stickyListHeadersListView.setFastScrollEnabled(true);

        }

        if (newText.equals("")) {
            stickyListHeadersListView.setFastScrollAlwaysVisible(true);
            stickyListHeadersListView.setVerticalScrollBarEnabled(true);
            stickyListHeadersListView.setFastScrollEnabled(true);
        }
    }

    public void showConfirmLoginDialog(final Activity context, String message, final String firstName, final String lastName, final String emailId) {
        AlertDialog alertDialog = null;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(message);
        alertDialogBuilder.setTitle("Join our Mailing list?");
        alertDialogBuilder.setPositiveButton("YES, Keep me Updated",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        pref.edit().putBoolean(GlobalConstants.PREF_JOIN_MAIL_CHIMP, true).commit();
                        if (Utils.isInternetAvailable(context)) {
                            if (mMailChimpTask == null) {

                                mMailChimpTask = new AsyncTask<Void, Void, Boolean>() {

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        progressDialog = new ProgressDialog(context, R.style.CustomDialogTheme);
                                        progressDialog.show();
                                        progressDialog.setContentView(AppController.getInstance().setViewToProgressDialog(context));
                                        progressDialog.setCanceledOnTouchOutside(false);
                                    }

                                    @Override
                                    protected Boolean doInBackground(Void... params) {

                                        return addToList(context, emailId, firstName, lastName);
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean aBoolean) {
                                        super.onPostExecute(aBoolean);
                                        Intent intent = new Intent(context, MainActivity.class);
                                        if (!aBoolean) {
                                            intent.putExtra("is_success", false);
                                        } else {
                                            intent.putExtra("is_success", true);
                                        }
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                        context.overridePendingTransition(R.anim.slideup, R.anim.nochange);
                                        context.finish();
                                        if (!VideoDataService.isServiceRunning)
                                            context.startService(new Intent(context, VideoDataService.class));
                                        progressDialog.dismiss();
                                        mMailChimpTask = null;
                                    }
                                };

                                // execute AsyncTask
                                mMailChimpTask.execute();
                            }
                        } else {
                            showToastMessage(context, GlobalConstants.MSG_NO_CONNECTION);
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("No Thanks",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        context.overridePendingTransition(R.anim.slideup, R.anim.nochange);
                        context.finish();
                        if (!VideoDataService.isServiceRunning)
                            context.startService(new Intent(context, VideoDataService.class));
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private boolean addToList(final Activity context, String emailId, String firstName, String lastName) {

        MergeFieldListUtil mergeFields = new MergeFieldListUtil();
        mergeFields.addEmail(emailId);
        try {
            mergeFields.addDateField("BIRFDAY", (new SimpleDateFormat("MM/dd/yyyy")).parse("07/30/2007"));
        } catch (ParseException e1) {
        }
        mergeFields.addField("FNAME", firstName);
        mergeFields.addField("LNAME", lastName);
        mergeFields.addField("PLATEFORM", "Android");
        mergeFields.addField("SCHOOL", "Florida");

        ListMethods listMethods = new ListMethods(context.getResources().getText(R.string.mc_api_key));

        try {
            try {
                mIsSignUpSuccessfull = listMethods.listSubscribe(context.getText(R.string.mc_list_id).toString(), emailId, mergeFields);
            } catch (XMLRPCException e) {
                e.printStackTrace();
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showToastMessage(context, "This email id has been registered already on Mail Chimp.");
//                    }
//                });

                mIsSignUpSuccessfull = false;

                return mIsSignUpSuccessfull;
            }
        } catch (MailChimpApiException e) {
            Log.e("MailChimp", "Exception subscribing person: " + e.getMessage());
            e.getMessage();
//            context.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showToastMessage(context, "This email id has been registered already on Mail Chimp.");
//                }
//            });
            mIsSignUpSuccessfull = false;
            return mIsSignUpSuccessfull;
        }

        return mIsSignUpSuccessfull;

    }


    public void showToastMessage(final Activity context, String message) {
        View includedLayout = context.findViewById(R.id.llToast);

        final TextView text = (TextView) includedLayout.findViewById(R.id.tv_toast_message);
        text.setText(message);

        animation = AnimationUtils.loadAnimation(context,
                R.anim.abc_fade_in);

        text.setAnimation(animation);
        text.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(context,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }
}
