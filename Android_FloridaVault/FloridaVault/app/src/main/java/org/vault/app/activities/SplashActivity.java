package org.vault.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;

import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.shimmer.ShimmerFrameLayout;
import org.vault.app.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gauravkumar.singh on 11/24/2015.
 */
public class SplashActivity extends Activity {

    private ShimmerFrameLayout mShimmerViewContainer;
    public static final String tag = SplashActivity.class.getSimpleName();
    Profile fbProfile;
    private AsyncTask<Void, Void, ArrayList<TabBannerDTO>> mBannerTask;
    private ProgressDialog pDialog;
    private ArrayList<TabBannerDTO> bannerDTOArrayList = new ArrayList<TabBannerDTO>();
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.splash_activity);
        initViews();
        startApp();
    }


    private void initViews() {
        mShimmerViewContainer = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.startShimmerAnimation();
    }

    private void startApp() {
        if (Utils.isInternetAvailable(AppController.getInstance().getApplicationContext())) {
            Utils.loadDataFromServer(SplashActivity.this);
            startMainActivity();
        } else {
            showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
        }

    }


    private void startMainActivity() {

        fbProfile = Profile.getCurrentProfile();
        SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);

        if (fbProfile != null || userId > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slideup, R.anim.nochange);
            finish();
            startService(new Intent(SplashActivity.this, VideoDataService.class));
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginEmailActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slideup, R.anim.nochange);
            finish();
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
}
