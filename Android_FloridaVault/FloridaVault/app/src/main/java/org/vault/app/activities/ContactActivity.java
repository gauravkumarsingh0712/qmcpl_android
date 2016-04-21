package org.vault.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ncsavault.floridavault.BaseActivity;
import com.ncsavault.floridavault.R;

import org.json.JSONObject;
import org.vault.app.appcontroller.AppController;
import org.vault.app.globalconstants.GlobalConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aqeeb.pathan on 01-07-2015.
 */
public class ContactActivity extends BaseActivity {

    private TextView tvTitle, tvSubTitle, tvGuestText;
    private TextView tvClose;
    private TextView tvSubmit;
    private EditText edMessage, edName, edEmail;
    private Animation animation;

    private ProgressDialog pDialog;
    private String tagId = "";
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);

        initViews();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        View view = ContactActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) ContactActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        overridePendingTransition(R.anim.nochange, R.anim.slidedown);
    }

    @Override
    public void initViews(){
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSubTitle = (TextView) findViewById(R.id.tv_sub_title);
        tvGuestText = (TextView) findViewById(R.id.tv_guest_text);

        tvClose = (TextView) findViewById(R.id.tv_close);
        tvSubmit = (TextView) findViewById(R.id.tv_submit);

        edMessage = (EditText) findViewById(R.id.ed_message);
        edName = (EditText) findViewById(R.id.ed_name);
        edEmail = (EditText) findViewById(R.id.ed_email);
    }

    @Override
    public void initData() {
        long userID = AppController.getInstance().getUserId();
        if(userID == GlobalConstants.DEFAULT_USER_ID){
            tvTitle.setVisibility(View.GONE);
            tvSubTitle.setVisibility(View.GONE);

            tvGuestText.setVisibility(View.VISIBLE);
            edName.setVisibility(View.VISIBLE);
            edEmail.setVisibility(View.VISIBLE);
        }else {
            String title = getIntent().getStringExtra("title");
            String subTitle = getIntent().getStringExtra("subtitle");

            tvTitle.setText(title);
            tvSubTitle.setText(subTitle);

            tvTitle.setVisibility(View.VISIBLE);
            tvSubTitle.setVisibility(View.VISIBLE);

            tvGuestText.setVisibility(View.GONE);
            edName.setVisibility(View.GONE);
            edEmail.setVisibility(View.GONE);
        }

        tagId = getIntent().getStringExtra("tagId");
    }

    @Override
    public void initListener() {
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if(!edMessage.getText().toString().isEmpty()){
                    showConfirmationDialog();
                }else {
                    onBackPressed();
                }
            }
        });

        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                boolean isChecked = true;
                long userId = AppController.getInstance().getUserId();
                String nameAndEmail = "";
                if(userId == GlobalConstants.DEFAULT_USER_ID){
                    if(!isValidEmail(edEmail.getText().toString()))
                        isChecked = false;
                    if(!isValidText(edName.getText().toString())) {
                        isChecked = false;
                        edName.setError("Minimum 3 characters required");
                    }
                    if(isChecked){
                        nameAndEmail = edName.getText().toString()+" , "+edEmail.getText().toString();
                    }
                }else{
                    nameAndEmail = AppController.getInstance().getFirstName().toString()+" "+ AppController.getInstance().getLastName().toString()+" , "+ AppController.getInstance().getEmailAddress().toString();
                }
                if(isChecked) {
                    edMessage.setText(edMessage.getText().toString().trim());
                    if (!edMessage.getText().toString().isEmpty()) {
                        new CreateTaskOnAsana().execute(nameAndEmail, edMessage.getText().toString());
                    } else {
                        showToastMessage("Please provide message", false);
//                        edMessage.setError("Please provide message");
                    }
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        if(email.length() == 0){
            edEmail.setError("Email Not Entered!");
            return false;
        }
        else {
            String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(email);
            if(!matcher.matches())
            {
                edEmail.setError("Invalid Email!");
                return false;
            }
            else
                return matcher.matches();
        }
    }

    private boolean isValidText(String text) {
        if (text != null && text.length() >= 3) {
            return true;
        }
        return false;
    }

    public void showConfirmationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Do you want to discard this message?");
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Keep",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        alertDialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton("Discard",
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
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setAllCaps(false);
        nbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        pbutton.setTextColor(getResources().getColor(R.color.apptheme_color));
        pbutton.setAllCaps(false);
    }

    class CreateTaskOnAsana extends AsyncTask<String, Void, Void>{

        String task_id = "";
        boolean status = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ContactActivity.this, R.style.CustomDialogTheme);
            pDialog.show();
            pDialog.setContentView(AppController.getInstance().setViewToProgressDialog(ContactActivity.this));
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                /*String taskType = "";
                if(tagId.equalsIgnoreCase(GlobalConstants.FEEDBACK_TAG_ID))
                    taskType = "feedback";
                else if(tagId.equalsIgnoreCase(GlobalConstants.CLIP_REQUEST_TAG_ID))
                    taskType = "clip_request";
                else if(tagId.equalsIgnoreCase(GlobalConstants.SUPPORT_TAG_ID))
                    taskType = "support";
                else if(tagId.equalsIgnoreCase(GlobalConstants.NO_LOGIN_TAG_ID))
                    taskType = "no_login";*/

                String result = AppController.getInstance().getServiceManager().getVaultService().createTaskOnAsana(params[0], params[1], tagId);

                if (result != null) {
                    if (!result.isEmpty()) {
                        JSONObject jsonResult = new JSONObject(result);
                        if(jsonResult != null){
                            JSONObject jsonData = (JSONObject) jsonResult.get("data");
                            if(jsonData != null){
                                if(jsonData.getString("id") != null) {
                                    task_id = jsonData.getString("id").toString();
                                    //Create tag for task type
                                    String tagResult;
                                    if(!tagId.isEmpty()) {
                                        tagResult = AppController.getInstance().getServiceManager().getVaultService().createTagForAsanaTask(tagId, task_id);
                                        if (tagResult.contains("\"data\":"))
                                            status = true;
                                        else
                                            status = false;
                                    }

                                    //create tag for Platform Name
                                    tagResult = AppController.getInstance().getServiceManager().getVaultService().createTagForAsanaTask(GlobalConstants.ANDROID_TAG_ID, task_id);
                                    if(tagResult.contains("\"data\":"))
                                        status = true;
                                    else
                                        status = false;
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                status = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(status){
//                showToastMessage(GlobalConstants.EMAIL_SUCCESS_MESSAGE, true);
                showTicketSuccess(task_id);
            }else{
                showToastMessage(GlobalConstants.EMAIL_FAILURE_MESSAGE, false);
            }

            pDialog.dismiss();
        }
    }

    public void showTicketSuccess(String taskId){
        String successMessage = "";
        long userID = AppController.getInstance().getUserId();
        if(userID == GlobalConstants.DEFAULT_USER_ID)
            successMessage = "Thank you. Ticket #" + taskId + " has been created. Someone from " + GlobalConstants.APP_FULL_NAME + " will reply to you via your registered email,  " + edEmail.getText().toString() + ". We appreciate you taking the time to contact us. -The " + GlobalConstants.APP_FULL_NAME;
        else
            successMessage = "Thank you. Ticket #" + taskId + " has been created. Someone from " + GlobalConstants.APP_FULL_NAME + " will reply to you via your registered email,  " + AppController.getInstance().getEmailAddress() + ". We appreciate you taking the time to contact us. -The " + GlobalConstants.APP_FULL_NAME;
        AlertDialog alertDialog = null;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactActivity.this);
        alertDialogBuilder
                .setMessage(successMessage);
//        alertDialogBuilder.setTitle("Success");

        alertDialogBuilder.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
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

    public void showToastMessage(String message, final boolean closeContact){

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
                animation = AnimationUtils.loadAnimation(ContactActivity.this,
                        R.anim.abc_fade_out);

                text.setAnimation(animation);
                text.setVisibility(View.GONE);
                if (closeContact)
                    finish();
            }
        }, 3000);
    }
}
