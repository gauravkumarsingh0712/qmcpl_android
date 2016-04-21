package org.vault.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ncsavault.floridavault.BaseActivity;
import com.ncsavault.floridavault.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.hockeyapp.android.CrashManager;

import org.json.JSONObject;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.APIResponse;
import org.vault.app.dto.User;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;


/**
 * Created by aqeeb.pathan on 13-04-2015.
 */
public class LoginEmailActivity extends BaseActivity {

    private EditText edEmailBox;
    private TextView tvSkipLogin, tvNextLogin;
    private LinearLayout ll_header_image, ll_facebook_login;
    private TextView tvFacebookLogin;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    User socialUser = new User();
    AlertDialog alertDialog;
    AsyncTask<Void, Void, String> mLoginTask = null;
    AsyncTask<Void, Void, Boolean> mFetchingTask = null;

    Profile fbProfile;
    ProgressDialog pDialog;
    private Animation animation;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            CrashManager.initialize(this, GlobalConstants.HOCKEY_APP_ID, null);
            FacebookSdk.sdkInitialize(this.getApplicationContext());

            callbackManager = CallbackManager.Factory.create();

            // The Dev key cab be set here or in the manifest.xml
            AppsFlyerLib.setAppsFlyerKey("i6ZusgQ8L8qW9ADfXbqgre");
            AppsFlyerLib.sendTracking(getApplicationContext());

            /*TwitterAuthConfig authConfig =
                    new TwitterAuthConfig("fqOPqtp50XP84ThpOcIQlol5L",
                            "fMMeRCDMHj4zcpsdy1Fq50H1eKkL4NTNTKvy5KqzJOmXZ3Kk6N");*/
            TwitterAuthConfig authConfig =
                    new TwitterAuthConfig(GlobalConstants.TWITTER_CONSUMER_KEY,
                            GlobalConstants.TWITTER_CONSUMER_SECRET);
            Fabric.with(this, new Twitter(authConfig));


            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            tvFacebookLogin.setText("Log Out");
                            getFacebookLoginStatus(loginResult);
                        }

                        @Override
                        public void onCancel() {
                            showAlert();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            exception.printStackTrace();
                            showAlert();
                        }

                        private void showAlert() {
                            showToastMessage(GlobalConstants.FACEBOOK_LOGIN_CANCEL);
                            /*new AlertDialog.Builder(LoginEmailActivity.this)
                                    .setTitle("Cancelled")
                                    .setMessage("Process was cancelled")
                                    .setPositiveButton("Ok", null)
                                    .show();*/
                        }
                    });

            setContentView(R.layout.login_email_activity);

            initViews();

            prefs = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);

            if (Utils.isInternetAvailable(this)) {
                boolean isConfirmed = prefs.getBoolean(GlobalConstants.PREF_IS_CONFIRMATION_DONE, false);
                if (!isConfirmed)
                    showNotificationConfirmationDialog(LoginEmailActivity.this);
            }

            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                }
            };

            fbProfile = Profile.getCurrentProfile();
            SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
            long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

            if (fbProfile != null || userId > 0) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            initData();
            initListener();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initData() {
        try {
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

            int dimension = (int) (screenWidth*0.45);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dimension, dimension);
            lp.setMargins(0, 30, 0, 0);
            ll_header_image.setLayoutParams(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initViews() {
        try {
            edEmailBox = (EditText) findViewById(R.id.ed_email);
            ll_header_image = (LinearLayout) findViewById(R.id.ll_header_image);
            tvFacebookLogin = (TextView) findViewById(R.id.tv_facebook_login);
            tvSkipLogin = (TextView) findViewById(R.id.tv_skip_login);
            tvNextLogin = (TextView) findViewById(R.id.tv_next_email);
            ll_facebook_login = (LinearLayout) findViewById(R.id.ll_facebook_login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initListener() {

        tvSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if(Utils.isInternetAvailable(LoginEmailActivity.this)) {
                    SharedPreferences prefs = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, GlobalConstants.DEFAULT_USER_ID).apply();
                    prefs.edit().putBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, true).apply();

                    fetchInitialRecordsForAll();
                }
                else{
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
                /*else {
                    makeDummyUser();
                }*/
            }
        });

        tvFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(LoginEmailActivity.this)) {
                    if (Profile.getCurrentProfile() == null)
                        LoginManager.getInstance().logInWithReadPermissions(LoginEmailActivity.this, Collections.singletonList("public_profile, email, user_birthday"));
                } else {
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        edEmailBox.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkEmailAndProceed();
                    return true;
                } else {
                    return false;
                }
            }
        });

        tvNextLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                checkEmailAndProceed();
            }
        });

        edEmailBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    ll_facebook_login.setVisibility(View.GONE);
                    tvNextLogin.setVisibility(View.VISIBLE);
                }else{
                    ll_facebook_login.setVisibility(View.VISIBLE);
                    tvNextLogin.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void getFacebookLoginStatus(final LoginResult loginResult){
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            URL image_path = null;
                            try {
                                image_path = new URL("http://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large");
                                System.out.println("Image Path : " + image_path.toString());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            Log.v("LoginActivity", response.toString());

                            socialUser.setEmailID(object.getString("email"));
                            if (image_path != null)
                                socialUser.setImageurl(image_path.toString());
                            socialUser.setUsername(object.getString("name"));
                            socialUser.setPasswd("vault_fb_" + object.getString("id"));
                            socialUser.setGender("gender");
                            socialUser.setAppID(GlobalConstants.APP_ID);
                            socialUser.setAppVersion(GlobalConstants.APP_VERSION);
                            socialUser.setDeviceType(GlobalConstants.DEVICE_TYPE);
                            socialUser.setFname(object.getString("first_name"));
                            socialUser.setLname(object.getString("last_name"));
                            socialUser.setFlagStatus("fb");

                            mLoginTask = new AsyncTask<Void, Void, String>() {

                                long userId = 0;

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    pDialog = new ProgressDialog(LoginEmailActivity.this, R.style.CustomDialogTheme);
                                    pDialog.show();
                                    pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginEmailActivity.this));
                                    pDialog.setCanceledOnTouchOutside(false);
                                    pDialog.setCancelable(false);
                                }

                                @Override
                                protected String doInBackground(Void... params) {
                                    String result = "";
                                    String validationResult = AppController.getInstance().getServiceManager().getVaultService().validateSocialLogin(socialUser.getEmailID(), "fb");
                                    try {
                                        Gson gson = new Gson();
                                        Type classType = new TypeToken<APIResponse>() {
                                        }.getType();
                                        APIResponse response = gson.fromJson(validationResult.trim(), classType);
                                        if (response.getReturnStatus().toLowerCase().contains("fail")) {
                                            result = AppController.getInstance().getServiceManager().getVaultService().postUserData(socialUser);
                                        } else if (response.getReturnStatus().toLowerCase().contains("success")) {
                                            result = "success";
                                            userId = response.getUserID();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    return result;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    System.out.println("Result of post user data : " + result);
                                    if (result.contains("success")) {
                                        pDialog.dismiss();
                                        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                                        pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, userId).apply();
                                        pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, socialUser.getUsername()).apply();
                                        pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, socialUser.getEmailID()).apply();
                                        pref.edit().putBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false).apply();
                                        fetchInitialRecordsForAll();
                                        /*Intent intent = new Intent(LoginEmailActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.rightin, R.anim.leftout);
                                        finish();*/
                                    } else {
                                        try {
                                            Gson gson = new Gson();
                                            Type classType = new TypeToken<APIResponse>() {
                                            }.getType();
                                            APIResponse response = gson.fromJson(result.trim(), classType);
                                            if (response.getReturnStatus().toLowerCase().contains("vt_exists") || response.getReturnStatus().toLowerCase().contains("false")) {
                                                pDialog.dismiss();
                                                showAlertDialog("Vault Account");
                                            } else if (response.getReturnStatus().toLowerCase().contains("fb_exists") || response.getReturnStatus().toLowerCase().contains("" +
                                                    "")) {
                                                pDialog.dismiss();
                                                SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                                                pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, response.getUserID()).apply();
                                                pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, socialUser.getUsername()).apply();
                                                pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, socialUser.getEmailID()).apply();
                                                pref.edit().putBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false).apply();
                                                fetchInitialRecordsForAll();
                                                /*Intent intent = new Intent(LoginEmailActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.rightin, R.anim.leftout);
                                                finish();*/
                                            } else if (response.getReturnStatus().toLowerCase().contains("tw_exists")) {
                                                pDialog.dismiss();
                                                showAlertDialog("Twitter");
                                            } else {
                                                pDialog.dismiss();
                                                LoginManager.getInstance().logOut();
                                                tvFacebookLogin.setText("Login with Facebook");
//                                                Toast.makeText(LoginEmailActivity.this, result, Toast.LENGTH_SHORT).show();
                                                showToastMessage(result);
                                            }

                                            mLoginTask = null;
                                        } catch (Exception e) {
                                            LoginManager.getInstance().logOut();
                                            e.printStackTrace();
                                            pDialog.dismiss();
                                            tvFacebookLogin.setText("Login with Facebook");
//                                            Toast.makeText(LoginEmailActivity.this, "We are unable to process your request", Toast.LENGTH_SHORT).show();
                                            showToastMessage("We are unable to process your request");
                                        }
                                    }

                                }
                            };
                            // execute AsyncTask
                            mLoginTask.execute();
                        } catch (Exception e) {
                            LoginManager.getInstance().logOut();
                            tvFacebookLogin.setText("Login with Facebook");
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday, first_name, last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    {//Twitter Email Fetching Method
    /*private void gettwitteremail() {
        twProgressDialog = new ProgressDialog(LoginEmailActivity.this, R.style.CustomDialogTheme);
        twProgressDialog.show();
        twProgressDialog.setContentView(AppController.getInstance().setViewToProgressDialog(this));
        twProgressDialog.setCanceledOnTouchOutside(false);
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();
        *//*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*//*
        TwitterAuthClient authClient = new TwitterAuthClient();

        authClient.requestEmail(session, new Callback() {
            @Override
            public void success(Result result) {
                // Do something with the result, which provides
                // the email address
                socialUser.setEmailID(result.data.toString());
                System.out.println("twitter email success" + result.data);
                getUserProfileData();
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                System.out.println("twitter sucess" + exception.getMessage());
                exception.printStackTrace();
                getUserProfileData();
            }
        });
    }

    public void getUserProfileData(){
        TwitterApiClient twitterApiClient =  Twitter.getApiClient();
        StatusesService twapiclient = twitterApiClient.getStatusesService();
        twapiclient.userTimeline(twitterUserId, null, null, null, null, null, null, null, null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                System.out.println("Email : " + socialUser.getEmailID());
                System.out.println("Name : " + listResult.data.get(0).user.name);
                System.out.println("Screen Name : " + listResult.data.get(0).user.screenName);
                System.out.println("Description : " + listResult.data.get(0).user.description);
                System.out.println("Profile Image Url : " + listResult.data.get(0).user.profileBackgroundImageUrl);
                if (twProgressDialog != null)
                    twProgressDialog.dismiss();
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }*/
    }


    public void fetchInitialRecordsForAll(){
        mFetchingTask = new AsyncTask<Void, Void, Boolean>() {

            String userJsonData = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pDialog = new ProgressDialog(LoginEmailActivity.this, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginEmailActivity.this));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);

                pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        tvFacebookLogin.setText("Login with Facebook");
                        LoginManager.getInstance().logOut();
                        if(mFetchingTask != null){
                            if(!mFetchingTask.isCancelled())
                                mFetchingTask.cancel(true);
                        }


                    }
                });
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean status = true;
                try {
                    SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                    final long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
                    final String email = pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");
                    userJsonData = AppController.getInstance().getServiceManager().getVaultService().getUserData(userId, email);

                    if(userJsonData != null) {
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
                    }

                    status = Utils.loadDataFromServer(LoginEmailActivity.this);

                }catch (Exception e){
                    e.printStackTrace();
                    status = false;
                }
                return status;
            }

            @Override
            protected void onPostExecute(Boolean isAllFetched) {
                super.onPostExecute(isAllFetched);
                try{
                    if (isAllFetched) {
                        Profile fbProfile = Profile.getCurrentProfile();
                        SharedPreferences pref = AppController.getInstance().getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

                        if (fbProfile != null || userId > 0) {
                            Intent intent = new Intent(LoginEmailActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                            finish();
                            if(!VideoDataService.isServiceRunning)
                                startService(new Intent(LoginEmailActivity.this, VideoDataService.class));
                        }
                    }
                    pDialog.dismiss();
                }catch(Exception e){
                    e.printStackTrace();
                    stopService(new Intent(LoginEmailActivity.this, VideoDataService.class));
                    VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();
                    pDialog.dismiss();
                }
                mFetchingTask = null;
            }
        };

        mFetchingTask.execute();
    }

    public void checkEmailAndProceed(){
        if(Utils.isInternetAvailable(this)) {
            if (isValidEmail(edEmailBox.getText().toString())) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (mLoginTask == null) {
                    mLoginTask = new AsyncTask<Void, Void, String>() {

                        String email = "";

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            pDialog = new ProgressDialog(LoginEmailActivity.this, R.style.CustomDialogTheme);
                            pDialog.show();
                            pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginEmailActivity.this));
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.setCancelable(false);
                            email = edEmailBox.getText().toString();
                        }

                        @Override
                        protected String doInBackground(Void... params) {
                            return AppController.getInstance().getServiceManager().getVaultService().validateEmail(email);
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            System.out.println("Result of email validation : " + result);
                            if (result != null) {
                                if (result.toLowerCase().contains("vt_exists") || result.toLowerCase().contains("false")) {
                                    Intent intent = new Intent(LoginEmailActivity.this, LoginPasswordActivity.class);
                                    intent.putExtra("status", result);
                                    intent.putExtra("email", edEmailBox.getText().toString());
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.rightin, R.anim.leftout);
                                } else if (result.toLowerCase().contains("fb_exists")) {
                                    showAlertDialog("Facebook");
                                } else if (result.toLowerCase().contains("tw_exists")) {
                                    showAlertDialog("Twitter");
                                } else {
                                    showToastMessage("Error in connection");
                                }
                                mLoginTask = null;
                            }
                            pDialog.dismiss();
                        }
                    };

                    // execute AsyncTask
                    mLoginTask.execute();
                }

            }
        }
        else{
            showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }
    }

    private boolean isValidEmail(String email) {
        if(email.length() == 0){
//            edEmailBox.setError("Email Not Entered!"); //Html.fromHtml("<font color='#CC0000'>Email Not Entered!</font>")
            showToastMessage("Email Not Entered!");
            return false;
        }
        else {
            String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(email);
            if(!matcher.matches())
            {
//                edEmailBox.setError("Invalid Email!");
                showToastMessage("Invalid Email");
                return false;
            }
            else
                return matcher.matches();
        }
    }

    public void showAlertDialog(String loginType) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Oops, you previously used this email for " + loginType + " login. Please, login through "+loginType+".");

        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        alertDialog.dismiss();
                        tvFacebookLogin.setText("Login with Facebook");
                        LoginManager.getInstance().logOut();
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        CrashManager.initialize(this, GlobalConstants.HOCKEY_APP_ID, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        if(pDialog != null)
            pDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    public void showToastMessage(String message){
        View includedLayout = findViewById(R.id.llToast);

        final TextView text = (TextView) includedLayout.findViewById(R.id.tv_toast_message);
        text.setText(message);

        animation = AnimationUtils.loadAnimation(this,
                R.anim.abc_fade_in);

        text.setAnimation(animation);
        text.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                animation = AnimationUtils.loadAnimation(LoginEmailActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }

    public void showNotificationConfirmationDialog(final Activity mActivity){

        prefs = mActivity.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder
                .setMessage(getResources().getString(R.string.notification_message));

        alertDialogBuilder.setPositiveButton("Allow",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Utils.getInstance().registerWithGCM(mActivity);
                        prefs.edit().putBoolean(GlobalConstants.PREF_IS_CONFIRMATION_DONE, true).apply();
                    }
                });
        alertDialogBuilder.setNegativeButton("Deny",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, false).apply();
                        prefs.edit().putBoolean(GlobalConstants.PREF_IS_CONFIRMATION_DONE, true).apply();
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
