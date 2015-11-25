package org.vault.app.utils;

import android.app.Activity;
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
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author aqeeb.pathan
 */
public class Utils {


    private static Utils utilInstance = new Utils();

    private ProgressDialog progressDialog;

    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
    AsyncTask<Void, Void, Void> mPermissionChangeTask;
    SharedPreferences prefs;
    private String result;

    public static Utils getInstance() {
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

        if(!hasMenuKey && !hasBackKey) {                // Condition worked for Samsung Devices
            result = getNavigationBarHeight(context);
        }else if(id > 0 && context.getResources().getBoolean(id)){      // Condition will work for Micromax Canvas Nitro 2
            result = getNavigationBarHeight(context);
        }else if((!(hasBackKey && hasHomeKey))){        // Condition worked for all other devices
            result = getNavigationBarHeight(context);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context){
        //The device has a navigation bar
        Resources resources = context.getResources();

        int orientation = context.getResources().getConfiguration().orientation;
        int resourceId;
        if (context.getResources().getBoolean(R.bool.isTablet)){
            resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        }  else {
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

    public static void addBannerImageWithoutCaching(final ImageView bannerCacheableImageView, String url) {
        DisplayImageOptions imgLoadingOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false)
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
                        Animation anim = AnimationUtils.loadAnimation(AppController.getInstance().getApplicationContext(), R.anim.slidedown_header);
                        bannerCacheableImageView.setAnimation(anim);
                        bannerCacheableImageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
//                        bannerCacheableImageView.setVisibility(View.GONE);
                    }
                });
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
    public static void addPagerIndicatorBelowActionBar(Context context, View v) {
        int actionBarHeight = Utils.CalculateActionBar(context)
                + Utils.getStatusBarHeight(context);
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


    private static  AsyncTask<Void, Void, ArrayList<TabBannerDTO>> mBannerTask;
    public static ArrayList<TabBannerDTO> bannerDTOArrayList = new ArrayList<TabBannerDTO>();
    /**
     * This method used for fetching all data for banner from Server
     */
    public static void loadDataFromServer(final Activity context) {

        mBannerTask = new AsyncTask<Void, Void, ArrayList<TabBannerDTO>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ProgressDialog pDialog = new ProgressDialog(context, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(context));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);

                pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mBannerTask != null) {
                            if (!mBannerTask.isCancelled())
                                mBannerTask.cancel(true);
                        }
                    }
                });
            }

            @Override
            protected ArrayList<TabBannerDTO> doInBackground(Void... params) {

                ArrayList<TabBannerDTO> arrayListBanner = new ArrayList<TabBannerDTO>();


                String url = "" + "userid=" + AppController.getInstance().getUserId();
                try {
                    arrayListBanner.addAll(AppController.getInstance().getServiceManager().getVaultService().getBannerListFromServer(url));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                bannerDTOArrayList.clear();
                bannerDTOArrayList.addAll(arrayListBanner);

                for (TabBannerDTO bDTO : bannerDTOArrayList) {
                    TabBannerDTO localBannerData = VaultDatabaseHelper.getInstance(context.getApplicationContext()).getTabBannerDataById(bDTO.getBannerId());
                    if (localBannerData != null) {
                        if (localBannerData.getLastBannerUpdate() != bDTO.getLastBannerUpdate()) {
                            VaultDatabaseHelper.getInstance(context.getApplicationContext()).updateBannerData(bDTO);
                        }
                        if (localBannerData.getLastServerTabUpdate() != bDTO.getLastServerTabUpdate()) {

                            if (localBannerData.getTabName() == (GlobalConstants.FEATURED).toLowerCase(Locale.getDefault())) {
                                VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_FEATURED);
                                VideoDataService.API_URLS.add(GlobalConstants.FEATURED_API_URL);

                            } else if (localBannerData.getTabName() == (GlobalConstants.GAMES).toLowerCase(Locale.getDefault())) {
                                VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_GAMES);
                                VideoDataService.API_URLS.add(GlobalConstants.GAMES_API_URL);

                            } else if (localBannerData.getTabName() == (GlobalConstants.PLAYERS).toLowerCase(Locale.getDefault())) {

                                VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_PLAYERS);
                                VideoDataService.API_URLS.add(GlobalConstants.PLAYER_API_URL);

                            } else if (localBannerData.getTabName() == (GlobalConstants.OPPONENTS).toLowerCase(Locale.getDefault())) {

                                VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_OPPONENT);
                                VideoDataService.API_URLS.add(GlobalConstants.OPPONENT_API_URL);

                            } else if (localBannerData.getTabName() == (GlobalConstants.COACHES_ERA).toLowerCase(Locale.getDefault())) {

                                VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeRecordsByTab(GlobalConstants.OKF_COACH);
                                VideoDataService.API_URLS.add(GlobalConstants.COACH_API_URL);

                            }
                        }
                    } else {
                        VaultDatabaseHelper.getInstance(context.getApplicationContext()).insertTabBannerData(bDTO);
                    }
                }

                return null;
            }


            @Override
            protected void onPostExecute(ArrayList<TabBannerDTO> bannerDTOs) {
                super.onPostExecute(bannerDTOs);


            }
        };

    }


}
