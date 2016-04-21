package org.vault.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.ncsavault.floridavault.BaseActivity;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;

import org.json.JSONObject;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.APIResponse;
import org.vault.app.dto.User;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by aqeeb.pathan on 17-04-2015.
 */
public class UserProfileActivity extends BaseActivity implements TextWatcher {

    private TextView tvEditHeader;
    private TextView tvCloseHeader;
    private TextView tvFirstName;
    private TextView tvLastName;
    private TextView tvBio;
    private TextView tvFacebookStatus;
    private TextView tvTwitterStatus;
    private TextView tvChangePassword;
    private EditText edFirstName;
    private EditText edLastName;
    private EditText edBio;
    private TextView tvLogout;
    private TwitterLoginButton twitterLoginButton;

    ImageView imgUserProfile;
    ProgressBar pBar;
    ScrollView scrollView;

    String username = "";

    private boolean isValidFields = true;

    AsyncTask<Void, Void, String> mFetchingTask = null;
    AsyncTask<Void, Void, Boolean> mUpdatingTask = null;
    private DisplayImageOptions options;

    private User responseUser = null;

    private Uri selectedImageUri = null;
    private Uri outputFileUri = null;
    private int YOUR_SELECT_PICTURE_REQUEST_CODE = 100;
    File sdImageMainDirectory;

    private boolean isEditing = false;
    AlertDialog alertDialog = null;
    ProgressDialog pDialog;

    private static CallbackManager callbackManager;
    Animation animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        initViews();
        initData();
        initializeFacebookUtils();
        initListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null)
            pDialog.dismiss();
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initViews() {
        edFirstName = (EditText) findViewById(R.id.ed_first_name);
        edLastName = (EditText) findViewById(R.id.ed_last_name);
        edBio = (EditText) findViewById(R.id.ed_bio);

        imgUserProfile = (ImageView) findViewById(R.id.imgUserProfile);

        tvCloseHeader = (TextView) findViewById(R.id.tv_close);
        tvEditHeader = (TextView) findViewById(R.id.tv_edit);

        tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        tvLastName = (TextView) findViewById(R.id.tvLastName);
        tvBio = (TextView) findViewById(R.id.tvBioText);

        tvFacebookStatus = (TextView) findViewById(R.id.tv_facebook_status);
        tvTwitterStatus = (TextView) findViewById(R.id.tv_twitter_status);
        tvChangePassword = (TextView) findViewById(R.id.tv_change_password);

        tvLogout = (TextView) findViewById(R.id.tv_logout);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        pBar = (ProgressBar) findViewById(R.id.progressbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            pBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            pBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_large_material, null));
        }
//        edBio.setScroller(new Scroller(this));
        edBio.setMaxLines(2);
        edBio.setVerticalScrollBarEnabled(true);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        edFirstName.setTag(false);
        edLastName.setTag(false);

    }

    public void initializeFacebookUtils() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        System.out.println("Facebook login successful");
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        try {
                                            /*URL image_path;
                                            try {
                                                image_path = new URL("http://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large");
                                                System.out.println("Image Path : " + image_path.toString());
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            }*/
                                            Log.v("LoginActivity", response.toString());

                                            tvFacebookStatus.setText(object.getString("name"));
                                        } catch (Exception e) {
                                            LoginManager.getInstance().logOut();
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday, first_name, last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
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
                       /* new AlertDialog.Builder(ProfileUpdateActivity.this)
                                .setTitle("Cancelled")
                                .setMessage("Process was cancelled")
                                .setPositiveButton("Ok", null)
                                .show();*/
                    }
                });

       /* profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

            }
        };*/
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initData() {
        try {
            //set drawable from stream
            imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));

            Point size = new Point();
            WindowManager w = getWindowManager();
            int screenWidth;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                w.getDefaultDisplay().getSize(size);
                screenWidth = size.x;
                // Measuredheight = size.y;
            } else {
                Display d = w.getDefaultDisplay();
                // Measuredheight = d.getHeight();
                screenWidth = d.getWidth();
            }

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth / 2, screenWidth / 2);
            imgUserProfile.setLayoutParams(lp);

            if (Utils.isInternetAvailable(this))
                loadUserDataFromServer();
            else
                loadUserDataFromLocal();

            Profile fbProfile = Profile.getCurrentProfile();
            if (fbProfile != null) {
                tvFacebookStatus.setText(fbProfile.getName());
            }

            TwitterSession session =
                    Twitter.getSessionManager().getActiveSession();

            if (session != null) {
//                TwitterAuthToken authToken = session.getAuthToken();
//                String token = authToken.token;
//                String secret = authToken.secret;
                tvTwitterStatus.setText("@" + session.getUserName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initListener() {

        tvTwitterStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(getApplicationContext())) {
                    TwitterSession session =
                            Twitter.getSessionManager().getActiveSession();
                    if (session == null) {
                        twitterLoginButton.performClick();
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
//                Toast.makeText(ProfileUpdateActivity.this,"Twitter Login Done",Toast.LENGTH_SHORT).show();
                tvTwitterStatus.setText("@" + twitterSessionResult.data.getUserName());
            }

            @Override
            public void failure(TwitterException e) {

            }
        });

        tvFacebookStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(getApplicationContext())) {
                    if (Profile.getCurrentProfile() == null) {
                        LoginManager.getInstance().logInWithReadPermissions(UserProfileActivity.this, Collections.singletonList("public_profile, email, user_birthday"));
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        edBio.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v("TAG", "CHILD TOUCH");

                if (v.getId() == R.id.ed_bio) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Twitter.logOut();

                stopService(new Intent(UserProfileActivity.this, VideoDataService.class));
//                VideoDataFetchingService.isServiceRunning = false;
                // VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();
                if (LoginManager.getInstance() != null)
                    LoginManager.getInstance().logOut();
                SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).apply();
                pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, "").apply();
                pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, "").apply();
//                pref.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).apply();

                Intent intent = new Intent(UserProfileActivity.this, LoginEmailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });

        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });


        tvEditHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(getApplicationContext())) {
                    if (isEditing) {
                        if (!isValidText(edFirstName.getText().toString())) {
                            isValidFields = false;
//                            edFirstName.setError("Invalid! Minimum 3 characters");
                            showToastMessage("First Name should have minimum 3 characters");
                        }
                        else if (!isValidText(edLastName.getText().toString())) {
                            isValidFields = false;
//                            edLastName.setError("Invalid! Minimum 3 characters");
                            showToastMessage("Last Name should have minimum 3 characters");
                        }


                        if (isValidFields) {
                            //make a server call for updating the data along with video
                            hideKeyborad();
                            tvEditHeader.setText("Edit");

                            tvFirstName.setText(edFirstName.getText().toString());
                            tvLastName.setText(edLastName.getText().toString());
                            tvBio.setHint("bio");
                            tvBio.setHintTextColor(Color.WHITE);
                            tvBio.setText(edBio.getText().toString().trim());

                            edFirstName.setVisibility(View.GONE);
                            edLastName.setVisibility(View.GONE);
                            edBio.setVisibility(View.GONE);
                            isEditing = false;
                            edFirstName.setBackground(null);
                            edLastName.setBackground(null);
                            edBio.setBackground(null);

                            tvFirstName.setVisibility(View.VISIBLE);
                            tvLastName.setVisibility(View.VISIBLE);
                            tvBio.setVisibility(View.VISIBLE);
                            updateUserData();
                        }
                        isValidFields = true;
                    } else {
                        isEditing = true;
                        edFirstName.setFocusable(true);
                        tvEditHeader.setText("Done");
                        edFirstName.setVisibility(View.VISIBLE);
                        edLastName.setVisibility(View.VISIBLE);
                        edBio.setVisibility(View.VISIBLE);

                        tvFirstName.setVisibility(View.GONE);
                        tvLastName.setVisibility(View.GONE);
                        tvBio.setVisibility(View.GONE);

                        edFirstName.setBackgroundResource(R.drawable.edittext_selector);
                        edLastName.setBackgroundResource(R.drawable.edittext_selector);
                        edBio.setBackgroundResource(R.drawable.edittext_selector);
                    }
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        imgUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(getApplicationContext()))
                    openImageIntent();
                else
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
            }
        });

        tvCloseHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (isEditing) {
                    showConfirmationDialog();
                } else {
                    finish();
                }
            }
        });

        edFirstName.addTextChangedListener(this);
        edLastName.addTextChangedListener(this);
        edBio.addTextChangedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (isEditing) {
            showConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void showConfirmationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Do you want to save changes you made?");
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (!isValidText(edFirstName.getText().toString())) {
                            isValidFields = false;
//                            edFirstName.setError("Invalid! Minimum 3 characters");
                            showToastMessage("First Name should have minimum 3 characters");
                        }
                        else if (!isValidText(edLastName.getText().toString())) {
                            isValidFields = false;
//                            edLastName.setError("Invalid! Minimum 3 characters");
                            showToastMessage("Last Name should have minimum 3 characters");
                        }

                        if (isValidFields) {
                            hideKeyborad();
                            tvEditHeader.setText("Edit");

                            tvFirstName.setText(edFirstName.getText().toString());
                            tvLastName.setText(edLastName.getText().toString());
                            tvBio.setText(edBio.getText().toString());

                            edFirstName.setVisibility(View.GONE);
                            edLastName.setVisibility(View.GONE);
                            edBio.setVisibility(View.GONE);
                            isEditing = false;
                            edFirstName.setBackground(null);
                            edLastName.setBackground(null);
                            edBio.setBackground(null);

                            tvFirstName.setVisibility(View.VISIBLE);
                            tvLastName.setVisibility(View.VISIBLE);
                            tvBio.setVisibility(View.VISIBLE);
//                            isEdited = false;
                            //make a server call for updating the data along with video
                            updateUserData();
                        }
                        isValidFields = true;
                        alertDialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
                        if(root != null) {
                            if(root.listFiles() != null) {
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
                        finish();
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

    public void hideKeyborad() {
        View view = UserProfileActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) UserProfileActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public void loadUserDataFromLocal() {
        try {
            responseUser = AppController.getInstance().getUserData();
            if (responseUser != null) {
                if (responseUser.getUserID() > 0) {
                    tvFirstName.setText(responseUser.getFname());
                    tvLastName.setText(responseUser.getLname());
                    if (responseUser.getBiotext() != null)
                        if (responseUser.getBiotext().length() > 0)
                            tvBio.setText(responseUser.getBiotext());

                    username = responseUser.getUsername();

                    edFirstName.setText(responseUser.getFname());
                    edLastName.setText(responseUser.getLname());
                    if (responseUser.getBiotext() != null)
                        if (responseUser.getBiotext().length() > 0)
                            edBio.setText(responseUser.getBiotext());

                    if (!responseUser.getFlagStatus().toLowerCase().equals("vt"))
                        tvChangePassword.setVisibility(View.GONE);
                    if (responseUser.getImageurl().length() > 0) {
                        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(responseUser.getImageurl(), imgUserProfile, options, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                pBar.setVisibility(View.VISIBLE);
                                try {
                                    /*InputStream istr = getAssets().open("placeholder.jpg");
                                    //set drawable from stream
                                    imgUserProfile.setImageDrawable(Drawable.createFromStream(istr, null));*/

                                    imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                pBar.setVisibility(View.GONE);
                                try {
                                    /*InputStream istr = getAssets().open("placeholder.jpg");
                                    //set drawable from stream
                                    imgUserProfile.setImageDrawable(Drawable.createFromStream(istr, null));*/
                                    imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                pBar.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
//                                Toast.makeText(ProfileUpdateActivity.this, "Error loading information!!! Please try again later.", Toast.LENGTH_LONG).show();
                    showToastMessage("Error loading information");
                }
            } else {
//                            Toast.makeText(ProfileUpdateActivity.this, "Error loading information!!! Please try again later.", Toast.LENGTH_LONG).show();
                showToastMessage("Error loading information");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadUserDataFromServer() {
        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        final long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
        final String email = pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");

        if (mFetchingTask == null) {
            mFetchingTask = new AsyncTask<Void, Void, String>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(UserProfileActivity.this, R.style.CustomDialogTheme);
                    pDialog.show();
                    pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(UserProfileActivity.this));
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setCancelable(false);
                }

                @Override
                protected String doInBackground(Void... params) {
                    return AppController.getInstance().getServiceManager().getVaultService().getUserData(userId, email);
                }

                @Override
                protected void onPostExecute(String result) {
                    System.out.println("Result of user data fetching : " + result);
                    try {
                        pDialog.dismiss();
                        Gson gson = new Gson();
                        Type classType = new TypeToken<User>() {
                        }.getType();
                        responseUser = gson.fromJson(result.trim(), classType);
                        if (responseUser != null) {
                            if (responseUser.getUserID() > 0) {

                                AppController.getInstance().storeUserDataInPreferences(responseUser);
                                username = responseUser.getUsername();
                                tvFirstName.setText(responseUser.getFname());
                                tvLastName.setText(responseUser.getLname());

                                if (responseUser.getBiotext() != null)
                                    if (responseUser.getBiotext().length() > 0)
                                        tvBio.setText(responseUser.getBiotext());

                                edFirstName.setText(responseUser.getFname());
                                edLastName.setText(responseUser.getLname());
                                if (responseUser.getBiotext() != null)
                                    if (responseUser.getBiotext().length() > 0)
                                        edBio.setText(responseUser.getBiotext());

                                if (!responseUser.getFlagStatus().toLowerCase().equals("vt"))
                                    tvChangePassword.setVisibility(View.GONE);

//                                btnLogOut.setText("Log Out " + responseUser.getUsername());
                                if (responseUser.getImageurl().length() > 0) {
                                    com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(responseUser.getImageurl(), imgUserProfile, options, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingStarted(String imageUri, View view) {
                                            pBar.setVisibility(View.VISIBLE);
                                            try {
                                                /*InputStream istr = getAssets().open("placeholder.jpg");
                                                //set drawable from stream
                                                imgUserProfile.setImageDrawable(Drawable.createFromStream(istr, null));*/
                                                imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                            pBar.setVisibility(View.GONE);
                                            try {
                                                /*InputStream istr = getAssets().open("placeholder.jpg");
                                                //set drawable from stream
                                                imgUserProfile.setImageDrawable(Drawable.createFromStream(istr, null));*/
                                                imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.camera_background));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                            pBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            } else {
//                                Toast.makeText(ProfileUpdateActivity.this, "Error loading information!!! Please try again later.", Toast.LENGTH_LONG).show();
                                showToastMessage("Error loading information");
                            }
                        } else {
//                            Toast.makeText(ProfileUpdateActivity.this, "Error loading information!!! Please try again later.", Toast.LENGTH_LONG).show();
                            showToastMessage("Error loading information");
                        }

                        mFetchingTask = null;
                    } catch (Exception e) {
                        mFetchingTask = null;
                        e.printStackTrace();
                    }
                }
            };

            // execute AsyncTask
            mFetchingTask.execute();
        }
    }

    public void updateUserData() {
        if (mUpdatingTask == null) {
            mUpdatingTask = new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(UserProfileActivity.this, R.style.CustomDialogTheme);
                    pDialog.show();
                    pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(UserProfileActivity.this));
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setCancelable(false);
                    if (responseUser != null) {
                        responseUser.setUsername(username);
                        responseUser.setFname(tvFirstName.getText().toString());
                        responseUser.setLname(tvLastName.getText().toString());
                        responseUser.setBiotext(tvBio.getText().toString());
                    }
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        final long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
                        final String email = pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");
                        if (responseUser != null) {
                            if (selectedImageUri != null) {
                                Bitmap selectedBitmap = Utils.getInstance().decodeUri(selectedImageUri, UserProfileActivity.this);
                                selectedBitmap = Utils.getInstance().rotateImageDetails(selectedBitmap, selectedImageUri, UserProfileActivity.this, sdImageMainDirectory);
                                String convertedImage = ConvertBitmapToBase64Format(selectedBitmap);
                                responseUser.setImageurl(convertedImage);
                            }
                        }

                        String result = AppController.getInstance().getServiceManager().getVaultService().updateUserData(responseUser);
                        System.out.println("Result of user data updating : " + result);
                        Gson gson = new Gson();
                        Type classType = new TypeToken<APIResponse>() {
                        }.getType();
                        APIResponse response = gson.fromJson(result.trim(), classType);
                        if (response != null) {
                            if (response.getReturnStatus().toLowerCase().equals("success")) {
                                String userJsonData = AppController.getInstance().getServiceManager().getVaultService().getUserData(userId, email);
                                if (!userJsonData.isEmpty()) {
                                    Type classUserType = new TypeToken<User>() {
                                    }.getType();
                                    System.out.println("User Data : " + userJsonData);
                                    User responseUser = gson.fromJson(userJsonData.trim(), classUserType);
                                    if (responseUser != null) {
                                        if (responseUser.getUserID() > 0) {
                                            AppController.getInstance().storeUserDataInPreferences(responseUser);
                                            return true;
                                        }
                                    } else
                                        return false;
                                } else
                                    return false;
                            } else
                                return false;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }

                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                protected void onPostExecute(Boolean isFetched) {
                    try {
                        pDialog.dismiss();
                        if (isFetched) {
                            showToastMessage("Profile updated successfully");
                        } else {
                            showToastMessage("Error updating information");
                            loadUserDataFromLocal();
                        }
                        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
                        if(root != null) {
                            if(root.listFiles() != null) {
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

                        mUpdatingTask = null;
                    } catch (Exception e) {
                        mUpdatingTask = null;
                        e.printStackTrace();
                    }
                }
            };

            // execute AsyncTask
            mUpdatingTask.execute();
        }
    }

    private boolean isValidText(String str) {
        return str != null && str.length() >= 3;
    }


    public String ConvertBitmapToBase64Format(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + GlobalConstants.PROFILE_PIC_DIRECTORY + File.separator);
        root.mkdirs();
        Random randomNumber = new Random();
        final String fname = "vault_picture_" + randomNumber.nextInt(1000) + 1;
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
                        Bitmap selectedBitmap = Utils.getInstance().decodeUri(selectedImageUri, UserProfileActivity.this);
                        selectedBitmap = Utils.getInstance().rotateImageDetails(selectedBitmap, selectedImageUri, UserProfileActivity.this, sdImageMainDirectory);

                        /*Drawable drawable = new BitmapDrawable(getResources(), selectedBitmap);
                        imgUserProfile.setImageDrawable(drawable);*/

                        imgUserProfile.setImageBitmap(selectedBitmap);

                        isEditing = true;
                        edFirstName.setFocusable(true);
                        tvEditHeader.setText("Done");
                        edFirstName.setVisibility(View.VISIBLE);
                        edLastName.setVisibility(View.VISIBLE);
                        edBio.setVisibility(View.VISIBLE);

                        tvFirstName.setVisibility(View.GONE);
                        tvLastName.setVisibility(View.GONE);
                        tvBio.setVisibility(View.GONE);

                        edFirstName.setBackgroundResource(R.drawable.edittext_selector);
                        edLastName.setBackgroundResource(R.drawable.edittext_selector);
                        edBio.setBackgroundResource(R.drawable.edittext_selector);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode,
                data);
    }

    /*private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

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
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    private Bitmap rotateImageDetails(Bitmap bitmap) {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(
                    getRealPathFromURI(selectedImageUri, this));
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

    public static String getRealPathFromURI(Uri contentURI,Context context) {
        String path= contentURI.getPath();
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        catch(Exception e)
        {
            return path;
        }
        return path;
    }
*/
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

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
                animation = AnimationUtils.loadAnimation(UserProfileActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }
}
