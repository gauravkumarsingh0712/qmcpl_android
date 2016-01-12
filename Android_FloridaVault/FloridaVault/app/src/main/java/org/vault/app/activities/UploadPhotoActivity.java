package org.vault.app.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ncsavault.floridavault.BaseActivity;
import com.ncsavault.floridavault.R;

import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.APIResponse;
import org.vault.app.dto.User;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by aqeeb.pathan on 15-04-2015.
 */
public class UploadPhotoActivity extends BaseActivity {

    //Initialize UI Components
    private ImageView userProfilePic;
    private TextView tvFinish;
    private TextView tvBack;

    private boolean isImageProvided = false;

    private Uri selectedImageUri = null;
    private Uri outputFileUri;
    private int YOUR_SELECT_PICTURE_REQUEST_CODE = 100;
    File sdImageMainDirectory;
    AsyncTask<Void, Void, String> mLoginTask;
    User vaultUser = null;
    int screenWidth = 0;

    ProgressDialog pDialog;
    private Animation animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload_activity);
//        getScreenDimensions();
        initViews();
        initData();
        initListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null)
            pDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initViews() {
        userProfilePic = (ImageView) findViewById(R.id.img_userprofile);
        tvFinish = (TextView) findViewById(R.id.tv_finish);
        tvBack = (TextView) findViewById(R.id.tv_avatar_back);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initData() {

        try {
//            InputStream istr = getAssets().open("placeholder.jpg");
//            //set drawable from stream
//
//            userProfilePic.setImageDrawable(Drawable.createFromStream(istr, null));

            userProfilePic.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
            Point size = new Point();
            WindowManager w = getWindowManager();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                w.getDefaultDisplay().getSize(size);
                screenWidth = size.x;
                // Measuredheight = size.y;
            } else {
                Display d = w.getDefaultDisplay();
                // Measuredheight = d.getHeight();
                screenWidth = d.getWidth();
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (screenWidth / 1.5), (int) (screenWidth / 1.5));
            lp.gravity = Gravity.CENTER_HORIZONTAL;
//            lp.setMargins(20,20,20,20);
            userProfilePic.setLayoutParams(lp);
            vaultUser = (User) getIntent().getSerializableExtra("user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initListener() {

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

       /* btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnCaptureSelect.setVisibility(View.VISIBLE);
                llUploadButtons.setVisibility(View.GONE);
                try {
                    InputStream istr = getAssets().open("placeholder.jpg");
                    //set drawable from stream
                    userProfilePic.setImageDrawable(Drawable.createFromStream(istr, null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/

        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload the image on server
                if (Utils.isInternetAvailable(UploadPhotoActivity.this)) {
                    storeDataOnServer();
                } else showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
                if (root != null) {
                    if (root.listFiles() != null) {
                        for (File childFile : root.listFiles()) {
                            if (childFile != null) {
                                if (childFile.exists())
                                    childFile.delete();
                            }

                        }
                        if (root.exists())
                            root.delete();
                    }
                }
                onBackPressed();
                overridePendingTransition(R.anim.leftin, R.anim.rightout);


            }
        });
    }


    public void fetchInitialRecordsForAll() {
        AsyncTask<Void, Void, Boolean> mFetchingTask = new AsyncTask<Void, Void, Boolean>() {

            ArrayList<VideoDTO> videosList = new ArrayList<VideoDTO>();
            String userJsonData = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(UploadPhotoActivity.this, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(UploadPhotoActivity.this));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean status = true;
                try {
                    SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                    final long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
                    final String email = pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");
                    userJsonData = AppController.getInstance().getServiceManager().getVaultService().getUserData(userId, email);

                    if (!userJsonData.isEmpty()) {
                        Gson gson = new Gson();
                        Type classType = new TypeToken<User>() {
                        }.getType();
                        System.out.println("User Data : " + userJsonData);
                        User responseUser = gson.fromJson(userJsonData.trim(), classType);
                        if (responseUser != null) {
                            if (responseUser.getUserID() > 0) {
                                AppController.getInstance().storeUserDataInPreferences(responseUser);
                            }
                        }
                    }


                    status = Utils.loadDataFromServer(UploadPhotoActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    status = false;
                }
                return status;
            }

            @Override
            protected void onPostExecute(Boolean isAllFetched) {
                super.onPostExecute(isAllFetched);
                try {
                    if (isAllFetched) {
                        Profile fbProfile = Profile.getCurrentProfile();
                        SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

                        if (fbProfile != null || userId > 0) {
                            Intent intent = new Intent(UploadPhotoActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                            finish();
                            if(!VideoDataService.isServiceRunning)
                                startService(new Intent(UploadPhotoActivity.this, VideoDataService.class));
                        }
                    }
                    pDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    stopService(new Intent(UploadPhotoActivity.this, VideoDataService.class));
                    VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();
                    pDialog.dismiss();
                }
            }
        };

        mFetchingTask.execute();
    }


    public void storeDataOnServer() {
        mLoginTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(UploadPhotoActivity.this, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(UploadPhotoActivity.this));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    if (isImageProvided) {
                        Bitmap selectedBitmap = Utils.getInstance().decodeUri(selectedImageUri, UploadPhotoActivity.this);
                        selectedBitmap = Utils.getInstance().rotateImageDetails(selectedBitmap, selectedImageUri, UploadPhotoActivity.this, sdImageMainDirectory);
                        String convertedImage = ConvertBitmapToBase64Format(selectedBitmap);
                        vaultUser.setImageurl(convertedImage);
                    } else {
                        vaultUser.setImageurl("");
                    }
                    vaultUser.setAppID(GlobalConstants.APP_ID);
                    vaultUser.setAppVersion(GlobalConstants.APP_VERSION);
                    vaultUser.setDeviceType(GlobalConstants.DEVICE_TYPE);
                    result = AppController.getInstance().getServiceManager().getVaultService().postUserData(vaultUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                System.out.println("Result of post image data : " + result);
                try {
                    Gson gson = new Gson();
                    Type classType = new TypeToken<APIResponse>() {
                    }.getType();
                    APIResponse response = gson.fromJson(result.trim(), classType);
                    pDialog.dismiss();
                    if (response != null) {
                        if (response.getReturnStatus().toLowerCase().equals("true")) {
                            SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                            pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, response.getUserID()).apply();
                            pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, vaultUser.getUsername()).apply();
                            pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, vaultUser.getEmailID()).apply();

                            fetchInitialRecordsForAll();
                            /*Intent intent = new Intent(UploadPhotoActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.rightin, R.anim.leftout);*/

                            if (isImageProvided) {
                                final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
                                if (root != null) {
                                    if (root.listFiles() != null) {
                                        for (File childFile : root.listFiles()) {
                                            if (childFile != null) {
                                                if (childFile.exists())
                                                    childFile.delete();
                                            }

                                        }
                                        if (root.exists())
                                            root.delete();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(UploadPhotoActivity.this, response.getReturnStatus(), Toast.LENGTH_LONG).show();
                        }
                    }

                    mLoginTask = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        // execute AsyncTask
        mLoginTask.execute();
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
                animation = AnimationUtils.loadAnimation(UploadPhotoActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }

    public String ConvertBitmapToBase64Format(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);

    }

   /* @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void getScreenDimensions() {
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
            MeasuredHeight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            MeasuredHeight = d.getHeight();
            Measuredwidth = d.getWidth();
        }
    }
*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                isCamera = data == null || MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                /*final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                }*/

                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data.getData();
                }
                if (selectedImageUri != null) {
                    try {
                        Bitmap selectedBitmap = Utils.getInstance().decodeUri(selectedImageUri, UploadPhotoActivity.this);
                        selectedBitmap = Utils.getInstance().rotateImageDetails(selectedBitmap, selectedImageUri, UploadPhotoActivity.this, sdImageMainDirectory);
                        /*Drawable drawable = new BitmapDrawable(getResources(), selectedBitmap);
                        userProfilePic.setImageDrawable(drawable);*/

                        userProfilePic.setImageBitmap(selectedBitmap);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (screenWidth / 1.5), (int) (screenWidth / 1.5));
                        lp.gravity = Gravity.CENTER_HORIZONTAL;
//                        lp.setMargins(0,30,0,0);
                        userProfilePic.setLayoutParams(lp);
                        isImageProvided = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        userProfilePic.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
                    }
                }
            }
        }
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
        root.mkdirs();
        Random randomNumber = new Random();
        final String fname = GlobalConstants.PROFILE_PIC_DIRECTORY + "_" + randomNumber.nextInt(1000) + 1;
        sdImageMainDirectory = new File(root, fname);

        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, YOUR_SELECT_PICTURE_REQUEST_CODE);
    }

}
