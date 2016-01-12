package org.vault.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;
import org.vault.app.wheeladapters.NumericWheelAdapter;
import org.vault.app.wheelwidget.OnWheelChangedListener;
import org.vault.app.wheelwidget.OnWheelScrollListener;
import org.vault.app.wheelwidget.WheelView;

import java.lang.reflect.Type;
import java.util.Calendar;

/**
 * Created by aqeeb.pathan on 13-04-2015.
 */
public class LoginPasswordActivity extends BaseActivity {

    //Initialize UI components
    private EditText edPassword;
    private EditText edConfirmPassword;
    private EditText edUsername;
    private EditText edFirstName;
    private EditText edLastName;
    private EditText edAgeOptional;
    private CheckBox chkChangePassword;
    private RadioGroup radGroupGenderOptional;
    private RadioButton radMaleOptional, radFemaleOptional;
    private LinearLayout llPasswordBlock, llUsernameBlock, llUserDetailBlock, llUserOptionalDetailBlock, llHeaderImage, llConfirmPasswordLine, llAgeBox;
    private TextView tvHeader;
    private TextView tvPasswordNext, tvUserNameNext, tvUserRegisterNext, tvOptionalSkipNext;
    private TextView tvBack;
    private ScrollView scrollView;
    private WheelView yearWheel;

    private Animation leftOutAnimation, leftInAnimation;
    private Animation rightInAnimation, rightOutAnimation;

    private String loginStatus;
    private String email;
    private boolean isSignUpFieldsValid = true;

    AsyncTask<Void, Void, String> mValidateTask;
    AsyncTask<Void, Void, String> mUserNameTask;
    AsyncTask<Void, Void, Boolean> mFetchingTask = null;

    private String[] yearArray;
    boolean isErrorPassword = false;
    ProgressDialog pDialog;
    private Animation animation;
    private int Measuredheight = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_password_activity);

        getScreenDimensions();
        initViews();
        initData();
        initListener();
    }

    @Override
    public void initViews() {
        edPassword = (EditText) findViewById(R.id.ed_password);
        edConfirmPassword = (EditText) findViewById(R.id.ed_confirm_password);

        edPassword.setTypeface(Typeface.DEFAULT);
        edConfirmPassword.setTypeface(Typeface.DEFAULT);

        edUsername = (EditText) findViewById(R.id.ed_username);
        edFirstName = (EditText) findViewById(R.id.ed_first_name);
        edLastName = (EditText) findViewById(R.id.ed_last_name);
        edAgeOptional = (EditText) findViewById(R.id.ed_age_optional);
        edAgeOptional.setInputType(InputType.TYPE_NULL);

        llUsernameBlock = (LinearLayout) findViewById(R.id.ll_username);
        llHeaderImage = (LinearLayout) findViewById(R.id.ll_header_image);
        llUserDetailBlock = (LinearLayout) findViewById(R.id.ll_user_detail);
        llUserOptionalDetailBlock = (LinearLayout) findViewById(R.id.ll_optional_layout);
        llPasswordBlock = (LinearLayout) findViewById(R.id.ll_password_block);
        llConfirmPasswordLine = (LinearLayout) findViewById(R.id.ll_confirm_password_line);
        llAgeBox = (LinearLayout) findViewById(R.id.ll_age_box);

        tvHeader = (TextView) findViewById(R.id.tv_header_text);

        tvPasswordNext = (TextView) findViewById(R.id.tv_password_next);
        tvUserNameNext = (TextView) findViewById(R.id.tv_username_next);
        tvUserRegisterNext = (TextView) findViewById(R.id.tv_register_data_next);
        tvOptionalSkipNext = (TextView) findViewById(R.id.tv_user_optional_skip);

        tvBack = (TextView) findViewById(R.id.tv_back);

        radGroupGenderOptional = (RadioGroup) findViewById(R.id.radGroupOptional);
        radMaleOptional = (RadioButton) findViewById(R.id.radMaleOptional);
        radFemaleOptional = (RadioButton) findViewById(R.id.radFemaleOptional);

        yearWheel = (WheelView) findViewById(R.id.year_wheel);
        initWheel();
        yearWheel.setBackgroundColor(Color.parseColor("#797979"));
        chkChangePassword = (CheckBox) findViewById(R.id.chk_show_password);

        edFirstName.setTag(false);
        edLastName.setTag(false);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);


    }

    @SuppressWarnings("deprecation")
    @Override
    public void initData() {
        leftOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftout);
        rightInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightin);

        leftInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftin);
        rightOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightout);

        email = getIntent().getStringExtra("email");
        loginStatus = getIntent().getStringExtra("status");

        if (loginStatus.toLowerCase().contains("vt_exists")) {
            edConfirmPassword.setVisibility(View.GONE);
            llConfirmPasswordLine.setVisibility(View.GONE);
            tvHeader.setText("Login");
            edPassword.setImeOptions(EditorInfo.IME_ACTION_GO);
        }

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

        int dimension = (int) (screenWidth * 0.45);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dimension, dimension);
        lp.setMargins(0, 20, 0, 0);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
//        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llHeaderImage.setLayoutParams(lp);
    }

    // Wheel scrolled flag
    private boolean wheelScrolled = false;

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;
        }

        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            edAgeOptional.setText(yearArray[yearWheel.getCurrentItem()]);
        }
    };

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!wheelScrolled) {
                edAgeOptional.setText(String.valueOf(yearArray[newValue]));
            }
        }
    };

    private void initWheel() {
        int startingYear = 1901;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int numberOfYears = currentYear - startingYear;
        yearArray = new String[numberOfYears + 1];
        int yearCheck = startingYear;
        for (int i = 0; i <= numberOfYears; i++) {
            yearArray[i] = String.valueOf(yearCheck);
            yearCheck++;
        }

        yearWheel.setViewAdapter(new NumericWheelAdapter(this, startingYear, currentYear));
        yearWheel.setCurrentItem(numberOfYears / 2);

        yearWheel.addChangingListener(changedListener);
        yearWheel.addScrollingListener(scrolledListener);
        yearWheel.setCyclic(false);
//        yearWheel.setInterpolator(new AnticipateOvershootInterpolator());
    }

    public void getScreenDimensions() {
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            Measuredheight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            Measuredheight = d.getHeight();
        }
    }

    @Override
    public void initListener() {

       /* scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });*/

        tvPasswordNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (llPasswordBlock.isShown()) {
                    if (loginStatus.toLowerCase().contains("vt_exists")) {
                        loginVaultUser();
                    } else {
                        checkPasswordAndProceed();
                    }
                }
            }
        });

        tvUserNameNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (llUsernameBlock.isShown()) {
                    checkUsernameAndProceed();
                }
            }
        });

        tvUserRegisterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (llUserDetailBlock.isShown()) {
                    checkSignUpFieldAndProceed();
                }
            }
        });

        tvOptionalSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (llUserOptionalDetailBlock.isShown()) {
                    checkOptionalValuesAndProceed();
                }
            }
        });

        edAgeOptional.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    tvOptionalSkipNext.setText("Next");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edAgeOptional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (Measuredheight * 0.30));
                lp.setMargins(0, 10, 0, 0);
                lp.gravity = Gravity.BOTTOM;
                yearWheel.setLayoutParams(lp);
//                yearWheel.setMinimumHeight((int) (Measuredheight*0.30));

                Animation anim = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.slideup);
                yearWheel.setAnimation(anim);
                yearWheel.setVisibility(View.VISIBLE);
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (yearWheel.isShown()) {
                    Animation anim = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.slidedown);
                    yearWheel.setAnimation(anim);
                    yearWheel.setVisibility(View.GONE);
                }
                return false;
            }
        });

        radGroupGenderOptional.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                tvOptionalSkipNext.setText("Next");
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        chkChangePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    edConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    edPassword.setInputType(129);
                    edConfirmPassword.setInputType(129);
                }

                edPassword.setTypeface(Typeface.DEFAULT);
                edConfirmPassword.setTypeface(Typeface.DEFAULT);
            }
        });

        edPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    isErrorPassword = !isValidPassword(edPassword.getText().toString());
                    return isErrorPassword;
                } else {
                    if (loginStatus.toLowerCase().contains("vt_exists")) {
                        loginVaultUser();
                    }
                }
                return false;
            }
        });

        edConfirmPassword.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkPasswordAndProceed();
                    return true;
                } else {
                    return false;
                }
            }
        });

        edUsername.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkUsernameAndProceed();
                    return true;
                } else {
                    return false;
                }
            }
        });

        edFirstName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (edFirstName.getText().length() == 0) {
//                    edFirstName.setError("Cannot be empty!");
                    showToastMessage("First name cannot be empty!");
                    return true;
                } else if (edFirstName.getText().length() < 3) {
//                    edFirstName.setError("Minimum 3 characters!");
                    showToastMessage("First name should contain minimum 3 characters!");
                    return true;
                }
                return false;
            }
        });

        edLastName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (edLastName.getText().length() == 0) {
//                    edLastName.setError("Cannot be empty!");
                    showToastMessage("Last name cannot be empty!");
                    return true;
                } else if (edLastName.getText().length() < 3) {
//                    edLastName.setError("Minimum 3 characters!");
                    showToastMessage("Last name should contain minimum 3 characters!");
                    return true;
                }
                return false;
            }
        });

        edLastName.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkSignUpFieldAndProceed();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private boolean isValidPassword(String pass) {
        if (pass != null && pass.length() >= 6) {
            return true;
        }
        if (pass != null)
            if (pass.length() == 0)
//                    edPassword.setError("Password not entered");
                showToastMessage("Password not entered");
            else if (pass.length() < 6)
//                    edPassword.setError("Minimum 6 characters required!");
                showToastMessage("Password should contain minimum 6 characters!");
        return false;
    }

    private boolean isValidText(String text) {
        return text != null && text.length() >= 3;
    }

    private boolean isConfirmPasswordValid(String confirmPass) {
        return confirmPass != null && (confirmPass.equals(edPassword.getText().toString()));
    }

    public void loginVaultUser() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        if (Utils.isInternetAvailable(this)) {
            if (mValidateTask == null) {
                mValidateTask = new AsyncTask<Void, Void, String>() {

                    String password = "";

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        pDialog = new ProgressDialog(LoginPasswordActivity.this, R.style.CustomDialogTheme);
                        pDialog.show();
                        pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginPasswordActivity.this));
                        pDialog.setCanceledOnTouchOutside(false);
                        password = edPassword.getText().toString();
                    }


                    @Override
                    protected String doInBackground(Void... params) {
                        return AppController.getInstance().getServiceManager().getVaultService().validateUserCredentials(email, password);
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        System.out.println("Result of email validation : " + result);
                        try {
                            Gson gson = new Gson();
                            Type classType = new TypeToken<APIResponse>() {
                            }.getType();
                            if (result != null) {
                                APIResponse response = gson.fromJson(result.trim(), classType);
                                pDialog.dismiss();
                                if (response != null) {
                                    if (response.getReturnStatus().toLowerCase().equals("true")) {
                                        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                                        pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, response.getUserID()).apply();
                                        pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, email).apply();
                                        pref.edit().putBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false).apply();

                                        fetchInitialRecordsForAll();
                                        isErrorPassword = false;
                                    } else {
//                                        edPassword.setError("Password is incorrect!");
                                        showToastMessage("Password is incorrect!");
                                        isErrorPassword = true;
                                    }
                                }
                            }
                            mValidateTask = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                // execute AsyncTask
                mValidateTask.execute();
            }
        } else {
            showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }
    }


    public void fetchInitialRecordsForAll() {
        mFetchingTask = new AsyncTask<Void, Void, Boolean>() {

            String userJsonData = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pDialog = new ProgressDialog(LoginPasswordActivity.this, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginPasswordActivity.this));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);

                pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mFetchingTask != null) {
                            if (!mFetchingTask.isCancelled())
                                mFetchingTask.cancel(true);
                        }
                    }
                });
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean status = true;
                /*String featuredUrl = GlobalConstants.FEATURED_API_URL + "userid=" + AppController.getInstance().getUserId();
                String playerUrl = GlobalConstants.PLAYER_API_URL + "userid=" + AppController.getInstance().getUserId();
                String coachesUrl = GlobalConstants.COACH_API_URL + "userid=" + AppController.getInstance().getUserId();
                String opponentUrl = GlobalConstants.OPPONENT_API_URL + "userid=" + AppController.getInstance().getUserId();
                String favoriteUrl = GlobalConstants.FAVORITE_API_URL + "userid=" + AppController.getInstance().getUserId();
                try{
                    videosList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(featuredUrl));
                    videosList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(playerUrl));
                    videosList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(coachesUrl));
                    videosList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(opponentUrl));
                    videosList.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(favoriteUrl));

                    SharedPreferences prefs = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                    if(!prefs.getBoolean(GlobalConstants.PREF_VAULT_SKIP_LOGIN, false)){
                        long userId = prefs.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
                        String email = prefs.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");
                        if(userId > 0 && !email.isEmpty())
                            userJsonData = AppController.getInstance().getServiceManager().getVaultService().getUserData(userId, email);
                    }

                    status = true;
                }catch(Exception e){
                    e.printStackTrace();
                    status = false;
                }*/
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
                    status = Utils.loadDataFromServer(LoginPasswordActivity.this);
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
                            Intent intent = new Intent(LoginPasswordActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slideup, R.anim.nochange);
                            finish();
                            if (!VideoDataService.isServiceRunning)
                                startService(new Intent(LoginPasswordActivity.this, VideoDataService.class));
                        }
                    }
                    pDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    stopService(new Intent(LoginPasswordActivity.this, VideoDataService.class));
                    VaultDatabaseHelper.getInstance(getApplicationContext()).removeAllRecords();
                    pDialog.dismiss();
                }
                mFetchingTask = null;
            }
        };

        mFetchingTask.execute();
    }

    public void checkPasswordAndProceed() {
        if (isValidPassword(edPassword.getText().toString())) {
            if (isConfirmPasswordValid(edConfirmPassword.getText().toString())) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                leftOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftout);
                rightInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightin);

                llPasswordBlock.setAnimation(leftOutAnimation);
                llHeaderImage.setAnimation(leftOutAnimation);
                llPasswordBlock.setVisibility(View.GONE);
                llHeaderImage.setVisibility(View.GONE);

                tvHeader.setText("Register");
                llHeaderImage.setAnimation(rightInAnimation);
                llUsernameBlock.setAnimation(rightInAnimation);
                llUsernameBlock.setVisibility(View.VISIBLE);
                llHeaderImage.setVisibility(View.VISIBLE);
            } else {
//                edConfirmPassword.setError("Password does not match!");
                showToastMessage("Password does not match!");
            }
        }
    }

    public void checkUsernameAndProceed() {
        if (Utils.isInternetAvailable(this)) if (isValidText(edUsername.getText().toString())) {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            if (mUserNameTask == null) {
                mUserNameTask = new AsyncTask<Void, Void, String>() {

                    String username = "";

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        pDialog = new ProgressDialog(LoginPasswordActivity.this, R.style.CustomDialogTheme);
                        pDialog.show();
                        pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(LoginPasswordActivity.this));
                        pDialog.setCanceledOnTouchOutside(false);
                        username = edUsername.getText().toString();
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        return AppController.getInstance().getServiceManager().getVaultService().validateUsername(username);
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        System.out.println("Result of username validation : " + result);
                        if (result != null) {
                            if (result.toLowerCase().contains("true")) {
                                leftOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftout);
                                rightInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightin);

                                llUsernameBlock.setAnimation(leftOutAnimation);
                                llHeaderImage.setAnimation(leftOutAnimation);
                                llUsernameBlock.setVisibility(View.GONE);
                                llHeaderImage.setVisibility(View.GONE);

                                llHeaderImage.setAnimation(rightInAnimation);
                                llUserDetailBlock.setAnimation(rightInAnimation);
                                tvHeader.setText("Register");
                                llUserDetailBlock.setVisibility(View.VISIBLE);
                                llHeaderImage.setVisibility(View.VISIBLE);
                            } else if (result.toLowerCase().contains("false")) {
//                                edUsername.setError("Username already exists!");
                                showToastMessage("Username already exists!");
                            } else {
                                Toast.makeText(LoginPasswordActivity.this, result, Toast.LENGTH_SHORT).show();
                            }
                        }
                        mUserNameTask = null;
                        pDialog.dismiss();
                    }
                };

                // execute AsyncTask
                mUserNameTask.execute();
            }

        } else {
//            edUsername.setError("Minimum 3 characters");
            showToastMessage("Username should contain minimum 3 characters");
        }
        else {
            showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }
    }

    public void checkSignUpFieldAndProceed() {
        if (edFirstName.getText().toString().length() == 0) {
//            edFirstName.setError("First name required");
            showToastMessage("First name required");
            isSignUpFieldsValid = false;
        }
        if (isSignUpFieldsValid)
            if (edFirstName.getText().toString().length() < 3) {
//                edFirstName.setError("Minimum 3 characters ");
                showToastMessage("First name should contain minimum 3 characters");
                isSignUpFieldsValid = false;
            }
        if (isSignUpFieldsValid)
            if (edLastName.getText().toString().length() == 0) {
                //edLastName.setError("Last name required");
                showToastMessage("Last name required");
                isSignUpFieldsValid = false;
            }
        if (isSignUpFieldsValid)
            if (edLastName.getText().toString().length() < 3) {
//                edLastName.setError("Minimum 3 characters ");
                showToastMessage("Last name should contain minimum 3 characters ");
                isSignUpFieldsValid = false;
            }
        if (isSignUpFieldsValid) {
            leftOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftout);
            rightInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightin);

            llUserDetailBlock.setAnimation(leftOutAnimation);
            llHeaderImage.setAnimation(leftOutAnimation);
            llUserDetailBlock.setVisibility(View.GONE);
            llHeaderImage.setVisibility(View.GONE);

            llHeaderImage.setAnimation(rightInAnimation);
            llUserOptionalDetailBlock.setAnimation(rightInAnimation);
            tvHeader.setText("Register");
            llUserOptionalDetailBlock.setVisibility(View.VISIBLE);
            llHeaderImage.setVisibility(View.VISIBLE);
            radGroupGenderOptional.setVisibility(View.GONE);
            if (edAgeOptional.getText().length() > 0)
                tvOptionalSkipNext.setText("Next");
        }
        isSignUpFieldsValid = true;
    }

    public void checkOptionalValuesAndProceed() {
        boolean isValidated = false;
        if (llAgeBox.isShown()) {
            if (edAgeOptional.getText().toString().length() > 0 && tvOptionalSkipNext.getText().toString().toLowerCase().equals("next")) {
                isValidated = true;
            } else if (tvOptionalSkipNext.getText().toString().toLowerCase().equals("skip") && edAgeOptional.getText().toString().length() == 0) {
                isValidated = true;
            } else {
                if (edAgeOptional.getText().toString().length() == 0)
                    showToastMessage("Please provide proper age");
            }
            if (isValidated) {
                if (yearWheel.isShown()) {
                    Animation anim = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.slidedown);
                    yearWheel.setAnimation(anim);
                    yearWheel.setVisibility(View.GONE);
                }
                leftOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftout);
                rightInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightin);

                llUserOptionalDetailBlock.setAnimation(leftOutAnimation);
                llHeaderImage.setAnimation(leftOutAnimation);
                llUserOptionalDetailBlock.setVisibility(View.GONE);
                llHeaderImage.setVisibility(View.GONE);


                radGroupGenderOptional.setVisibility(View.VISIBLE);
                llAgeBox.setVisibility(View.GONE);
                llHeaderImage.setAnimation(rightInAnimation);
                llUserOptionalDetailBlock.setAnimation(rightInAnimation);
                tvHeader.setText("Register");
                llUserOptionalDetailBlock.setVisibility(View.VISIBLE);
                llHeaderImage.setVisibility(View.VISIBLE);
                tvOptionalSkipNext.setText("Skip");

                return;
            }

        } else if (radGroupGenderOptional.isShown()) {
            if ((radFemaleOptional.isChecked() || radMaleOptional.isChecked()) && tvOptionalSkipNext.getText().toString().toLowerCase().equals("next")) {
                isValidated = true;
            } else if (tvOptionalSkipNext.getText().toString().toLowerCase().equals("skip") && (!radFemaleOptional.isChecked() && !radMaleOptional.isChecked())) {
                isValidated = true;
            }
            if (isValidated) {
                User usr = new User();
                usr.setFname(edFirstName.getText().toString());
                usr.setLname(edLastName.getText().toString());
                usr.setUsername(edUsername.getText().toString());
                usr.setEmailID(email);
                usr.setPasswd(edPassword.getText().toString());
                if (edAgeOptional.getText().toString().length() > 0)
                    usr.setAge(Integer.parseInt(edAgeOptional.getText().toString()));
                usr.setAppID(1);
                if (radMaleOptional.isChecked())
                    usr.setGender(radMaleOptional.getText().toString());
                else if (radFemaleOptional.isChecked())
                    usr.setGender(radFemaleOptional.getText().toString());
                else
                    usr.setGender("");
                usr.setFlagStatus("vt");

                //set ImageUrl on the next screen when user selects image
                Intent intent = new Intent(LoginPasswordActivity.this, UploadPhotoActivity.class);
                intent.putExtra("user", usr);
                startActivity(intent);
                overridePendingTransition(R.anim.rightin, R.anim.leftout);
            } else {
                if (!radGroupGenderOptional.isSelected())
                    showToastMessage("Please provide proper gender");
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null)
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (llUserOptionalDetailBlock.isShown()) {
            if (radGroupGenderOptional.isShown()) {
                leftInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftin);
                rightOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightout);

                llUserOptionalDetailBlock.setAnimation(rightOutAnimation);
                llHeaderImage.setAnimation(rightOutAnimation);
                llUserOptionalDetailBlock.setVisibility(View.GONE);
                llHeaderImage.setVisibility(View.GONE);
                radGroupGenderOptional.clearCheck();
                radGroupGenderOptional.setVisibility(View.GONE);

                llAgeBox.setVisibility(View.VISIBLE);
                llUserOptionalDetailBlock.setAnimation(leftInAnimation);
                llHeaderImage.setAnimation(leftInAnimation);
                tvHeader.setText("Register");
                llUserOptionalDetailBlock.setVisibility(View.VISIBLE);
                llHeaderImage.setVisibility(View.VISIBLE);
                if (edAgeOptional.getText().length() > 0)
                    tvOptionalSkipNext.setText("Next");
                else
                    tvOptionalSkipNext.setText("Skip");

            } else if (llAgeBox.isShown()) {
                if (yearWheel.isShown()) {
                    Animation anim = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.slidedown);
                    yearWheel.setAnimation(anim);
                    yearWheel.setVisibility(View.GONE);
                }
                leftInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftin);
                rightOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightout);

                llUserOptionalDetailBlock.setAnimation(rightOutAnimation);
                llHeaderImage.setAnimation(rightOutAnimation);
                llUserOptionalDetailBlock.setVisibility(View.GONE);
                llHeaderImage.setVisibility(View.GONE);
                radGroupGenderOptional.clearCheck();

                edAgeOptional.setText("");
                tvOptionalSkipNext.setText("Skip");
                llUserDetailBlock.setAnimation(leftInAnimation);
                llHeaderImage.setAnimation(leftInAnimation);
                tvHeader.setText("Register");
                llUserDetailBlock.setVisibility(View.VISIBLE);
                llHeaderImage.setVisibility(View.VISIBLE);
            }
            return;
        } else if (llUserDetailBlock.isShown()) {
            leftInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftin);
            rightOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightout);

            llUserDetailBlock.setAnimation(rightOutAnimation);
            llHeaderImage.setAnimation(rightOutAnimation);
            llUserDetailBlock.setVisibility(View.GONE);
            llHeaderImage.setVisibility(View.GONE);

            llUsernameBlock.setAnimation(leftInAnimation);
            llHeaderImage.setAnimation(leftInAnimation);
            tvHeader.setText("Register");
            llUsernameBlock.setVisibility(View.VISIBLE);
            llHeaderImage.setVisibility(View.VISIBLE);
            return;
        } else if (llUsernameBlock.isShown()) {
            leftInAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.leftin);
            rightOutAnimation = AnimationUtils.loadAnimation(LoginPasswordActivity.this, R.anim.rightout);

            llUsernameBlock.setAnimation(rightOutAnimation);
            llHeaderImage.setAnimation(rightOutAnimation);
            llUsernameBlock.setVisibility(View.GONE);
            llHeaderImage.setVisibility(View.GONE);

            llPasswordBlock.setAnimation(leftInAnimation);
            tvHeader.setText("Register");
            llHeaderImage.setAnimation(leftInAnimation);
            llPasswordBlock.setVisibility(View.VISIBLE);
            llHeaderImage.setVisibility(View.VISIBLE);
            return;
        }/*else if(llPasswordBlock.isShown()){

            llPasswordBlock.setAnimation(rightOutAnimation);
            llHeaderImage.setAnimation(rightOutAnimation);
            llPasswordBlock.setVisibility(View.GONE);
            llHeaderImage.setVisibility(View.GONE);

            llPasswordBlock.setAnimation(leftInAnimation);
            tvHeader.setText("Login");
            llHeaderImage.setAnimation(leftInAnimation);
            llPasswordBlock.setVisibility(View.VISIBLE);
            llHeaderImage.setVisibility(View.VISIBLE);
            return;
        }*/
        super.onBackPressed();

        overridePendingTransition(R.anim.leftin, R.anim.rightout);
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
                animation = AnimationUtils.loadAnimation(LoginPasswordActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }
}
