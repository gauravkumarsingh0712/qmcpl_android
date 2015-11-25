package org.vault.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ncsavault.floridavault.BaseActivity;
import com.ncsavault.floridavault.R;

import org.vault.app.appcontroller.AppController;
import org.vault.app.dto.APIResponse;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.utils.Utils;

import java.lang.reflect.Type;

/**
 * Created by aqeeb.pathan on 22-04-2015.
 */
public class ChangePasswordActivity extends BaseActivity implements TextWatcher{

    private EditText edOldPassword;
    private EditText edNewPassword;
    private EditText edConfirmPassword;
    private TextView tvSavePassword;
    private TextView tvBack;
    private CheckBox chkShowPassword;
    private LinearLayout ll_header_image;
    private ScrollView scrollView;

    private AsyncTask<Void, Void, String> mChangeTask;
    private boolean isAllFieldsChecked = false;
    private boolean isEditing = false;
    private AlertDialog alertDialog;
    ProgressDialog pDialog;

    Animation animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_change_activity);

        initViews();
        initData();
        initListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pDialog != null)
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
    public void onBackPressed() {
        if(isEditing){
            showConfirmation();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void initViews() {
        edOldPassword = (EditText) findViewById(R.id.ed_old_password);
        edNewPassword = (EditText) findViewById(R.id.ed_new_password);
        edConfirmPassword = (EditText) findViewById(R.id.ed_confirm_password);

        edOldPassword.setTypeface(Typeface.DEFAULT);
        edNewPassword.setTypeface(Typeface.DEFAULT);
        edConfirmPassword.setTypeface(Typeface.DEFAULT);

        tvSavePassword = (TextView) findViewById(R.id.tv_save);
        tvBack = (TextView) findViewById(R.id.tv_back);
        chkShowPassword = (CheckBox) findViewById(R.id.chk_show_password);
        ll_header_image = (LinearLayout) findViewById(R.id.ll_header_image);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
    }

    @Override
    public void initData() {
        Point size = new Point();
        WindowManager w = getWindowManager();
        int screenWidth = 0;

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
        lp.setMargins(0, 20, 0, 0);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
//        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll_header_image.setLayoutParams(lp);
    }

    @Override
    public void initListener() {
        edOldPassword.addTextChangedListener(this);
        edNewPassword.addTextChangedListener(this);
        edConfirmPassword.addTextChangedListener(this);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });


        edOldPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(!isValidPass(edOldPassword.getText().toString(), edOldPassword, "Old Password"))
                        return true;
                }
                return false;
            }
        });

        edNewPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(!isValidPass(edNewPassword.getText().toString(), edNewPassword, "New Password"))
                        return true;
                }
                return false;
            }
        });

        edConfirmPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (!isValidPass(edConfirmPassword.getText().toString(), edConfirmPassword, "Confirm Password"))
                        return true;
                    else {
                        if (!isVaildNewPassword(edConfirmPassword.getText().toString()))
                            return true;
                        else {
                            if (checkPasswordEquality())
                                //Toast.makeText(ChangePasswordActivity.this, "New password cannot be same as old password", Toast.LENGTH_LONG).show();
                                showToastMessage("New and old password cannot be same");
                            else
                                changePasswordCall();
                        }
                    }
                }
                return false;
            }
        });

        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    edConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    edNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    edOldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                else{
                    edNewPassword.setInputType(129);
                    edConfirmPassword.setInputType(129);
                    edOldPassword.setInputType(129);
                }
                edOldPassword.setTypeface(Typeface.DEFAULT);
                edNewPassword.setTypeface(Typeface.DEFAULT);
                edConfirmPassword.setTypeface(Typeface.DEFAULT);
            }
        });

        tvSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if(Utils.isInternetAvailable(getApplicationContext())) {
                    isAllFieldsChecked = true;
                    if(!isValidPass(edOldPassword.getText().toString(), edOldPassword, "Old Password")){
                        isAllFieldsChecked = false;
                    }else if(!isValidPass(edNewPassword.getText().toString(), edNewPassword, "New Password")){
                        isAllFieldsChecked = false;
                    }else if(!isValidPass(edConfirmPassword.getText().toString(), edConfirmPassword, "Confirm Password")){
                        isAllFieldsChecked = false;
                    }
                    /*if (!isValidPass(edOldPassword.getText().toString(), edOldPassword) || !isValidPass(edNewPassword.getText().toString(), edNewPassword) || !isValidPass(edConfirmPassword.getText().toString(), edConfirmPassword))
                        isAllFieldsChecked = false;*/
                    else {
                        if (!isVaildNewPassword(edConfirmPassword.getText().toString())) {
                            isAllFieldsChecked = false;
                        }
                    }

                    if (isAllFieldsChecked) {
                        if (checkPasswordEquality())
                            //Toast.makeText(ChangePasswordActivity.this, "New password cannot be same as old password", Toast.LENGTH_LONG).show();
                            showToastMessage("New and old password cannot be same");
                        else
                            changePasswordCall();
                    }
                }else{
                    showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                }
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditing){
                    showConfirmation();
                }
                else{
                    finish();
                }
            }
        });
    }

    public void showConfirmation(){
        if(isEditing){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setMessage("Do you want to save changes you made?");
            alertDialogBuilder.setTitle("Alert");
            alertDialogBuilder.setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            isAllFieldsChecked = true;
                            if (!isValidPass(edOldPassword.getText().toString(), edOldPassword, "Old Password") || !isValidPass(edNewPassword.getText().toString(), edNewPassword, "New Password") || !isValidPass(edConfirmPassword.getText().toString(), edConfirmPassword, "Confirm Password"))
                                isAllFieldsChecked = false;
                            else {
                                if (!isVaildNewPassword(edConfirmPassword.getText().toString())) {
                                    isAllFieldsChecked = false;
                                }
                            }

                            if (isAllFieldsChecked) {
                                if(checkPasswordEquality())
                                    //Toast.makeText(ChangePasswordActivity.this, "New password cannot be same as old password", Toast.LENGTH_LONG).show();
                                    showToastMessage("New and old password cannot be same");
                                else
                                    changePasswordCall();
                            }
                            alertDialog.dismiss();
                        }
                    });
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            alertDialog.dismiss();
                            finish();
                        }
                    });

            alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }



    private boolean isValidPass(String str, EditText edCheck, String fieldName) {
        if (str != null && str.length() >= 6) {
            return true;
        } else {
            if (str.length() == 0)
//                edCheck.setError(fieldName+" should not be empty");
                showToastMessage(fieldName+" should not be empty");
            else if (str.length() < 6)
//                edCheck.setError(fieldName+" should have minimum 6 characters!");
                showToastMessage(fieldName + " should have minimum 6 characters!");
        }
        return false;
    }

    private boolean isVaildNewPassword(String confirmPass) {
        if (confirmPass != null) {
            if (confirmPass.equals(edNewPassword.getText().toString()))
                return true;
            else {
//                edConfirmPassword.setError("Password does not match!");
                showToastMessage("Password does not match");
            }
        }
        return false;
    }

    private boolean checkPasswordEquality(){
        if(edOldPassword.getText().toString().equals(edNewPassword.getText().toString())){
            return true;
        }
        return false;
    }

    public void changePasswordCall() {
        mChangeTask = new AsyncTask<Void, Void, String>() {

            String oldPassword = "";
            String newPassword = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(ChangePasswordActivity.this, R.style.CustomDialogTheme);
                pDialog.show();
                pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(ChangePasswordActivity.this));
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelable(false);
                oldPassword = edOldPassword.getText().toString();
                newPassword = edNewPassword.getText().toString();
            }


            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    result = AppController.getInstance().getServiceManager().getVaultService().changeUserPassword(AppController.getInstance().getUserId(), oldPassword, newPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                APIResponse response;
                try {
                    pDialog.dismiss();
                    Gson gson = new Gson();
                    Type classType = new TypeToken<APIResponse>() {
                    }.getType();
                    response = gson.fromJson(result.trim(), classType);
                    if (response != null) {
                        if (response.getReturnStatus() != null) {
                            if (response.getReturnStatus().toLowerCase().equals("true")) {
                                View view = getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                }
                                //Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_LONG).show();
                                showToastMessage("Password changed successfully");
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable(){
                                    @Override
                                    public void run(){
                                        finish();
                                    }
                                }, 2000);
                            } else {
//                                edOldPassword.setError("Incorrect Password");
                                showToastMessage("Old Password is incorrect");
                            }

                        }
                    }
                } catch (Exception e) {

                }
            }
        };
        mChangeTask.execute();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isEditing = true;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void showToastMessage(String message){
       /* LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,null);*/

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
                animation = AnimationUtils.loadAnimation(ChangePasswordActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
            }
        }, 2000);
    }
}
