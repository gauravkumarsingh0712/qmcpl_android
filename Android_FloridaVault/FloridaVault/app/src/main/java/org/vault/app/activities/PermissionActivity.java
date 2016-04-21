package org.vault.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by gauravkumar.singh on 2/11/2016.
 */
public abstract class PermissionActivity extends FragmentActivity {

    /**
     * This will check all must permission at the same time
     */
    public static final int PERMISSION_REQUEST_MUST = 101;


    /**
     * Add each permission here in a sequence of below array
     */
    public static final int PERMISSION_REQUEST_INTERNET = 0;
    public static final int PERMISSION_REQUEST_ACCESS_NETWORK_STATE = 1;
    public static final int PERMISSION_REQUEST_READ_PHONE_STATE = 2;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    public static final int PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 4;
    public static final int PERMISSION_REQUEST_WAKE_LOCK = 5;
    public static final int PERMISSION_REQUEST_ACCESS_WIFI_STATE = 6;


    /***
     * Permission strings
     */
    private String[] permissions =
            {
                    //internet we get by default
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.ACCESS_WIFI_STATE

            };

    /***
     * Permission strings
     */
    private String[] mustPermissions =
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.GET_ACCOUNTS

            };

    public String[] readPhonePermissions = {
            Manifest.permission.READ_PHONE_STATE
    };

    private int permissionRequestCode;
    private Object extras;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean havePermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public boolean checkPermission(int requestCode, Object extras) {
        this.permissionRequestCode = requestCode;
        this.extras = extras;
        //if we have permission then will procceed
//        if (havePermission(permissions[requestCode])) {
//            return true;
//        }
        //else we will take the permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[requestCode])) {
           // Toast.makeText(this, "Please provide this permission.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{permissions[requestCode]}, requestCode);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == permissionRequestCode) {

            if (grantResults.length >= 1) {
                boolean anyDenied = false;
                for (int i = 0; i < grantResults.length; i++) {
                    // Check if the only required permission has been granted
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //showPermissionsConfirmationDialog();
                      //  Toast.makeText(this, "PERMISSION OF " + permissions[i] + " IS GRANTED.", Toast.LENGTH_SHORT).show();
                    } else {
                        anyDenied = true;
                        System.out.println("i am here Gaurav123");
                       // Toast.makeText(this, "PERMISSION OF " + permissions[i] + " IS DENIED.", Toast.LENGTH_SHORT).show();
                      //  finish();
                      // goToSettings();
                        //System.exit(0);
                    }
                }

                if (anyDenied) {
                    onPermissionResult(requestCode, false, extras);
                } else {
                    onPermissionResult(requestCode, true, extras);
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * This method will check all necessary permission to get
     *
     * @return
     */
    protected boolean haveAllMustPermissions() {

        this.permissionRequestCode = PERMISSION_REQUEST_MUST;

        ArrayList<String> permissionsNotAvail = new ArrayList<String>();

        //collecting permission which are not given by user
        for (int i = 0; i < mustPermissions.length; i++) {
            if (!havePermission(mustPermissions[i])) {
                permissionsNotAvail.add(mustPermissions[i]);
            }
        }

        if (permissionsNotAvail.size() > 0) {
            //adding in string array
            String[] neededPermissions = new String[permissionsNotAvail.size()];
            for (int i = 0; i < permissionsNotAvail.size(); i++) {
                neededPermissions[i] = permissionsNotAvail.get(i);
                //System.out.println(">>permission needed of =" + neededPermissions[i]);
            }

            ActivityCompat.requestPermissions(this, neededPermissions, PERMISSION_REQUEST_MUST);
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method will check all necessary permission to get
     *
     * @return
     */
    public boolean haveAllMustPermissions(String[] permissions, int permissionReqCode) {
        this.permissionRequestCode = permissionReqCode;
        ArrayList<String> permissionsNotAvail = new ArrayList<String>();

        //collecting permission which are not given by user
        for (int i = 0; i < permissions.length; i++) {
            if (!havePermission(permissions[i])) {
                permissionsNotAvail.add(permissions[i]);
            }
        }

        if (permissionsNotAvail.size() > 0) {
            //adding in string array
            String[] neededPermissions = new String[permissionsNotAvail.size()];
            for (int i = 0; i < permissionsNotAvail.size(); i++) {
                neededPermissions[i] = permissionsNotAvail.get(i);
                System.out.println(">>permission needed of =" + neededPermissions[i]);
            }

            ActivityCompat.requestPermissions(this, neededPermissions, permissionReqCode);
            return false;
        } else {
            return true;
        }
    }

    public void showPermissionsConfirmationDialog(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Permission mesage");

        alertDialogBuilder.setPositiveButton("Allow",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Utils.getInstance().registerWithGCM(mActivity);

                    }
                });
        alertDialogBuilder.setNegativeButton("Deny",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public abstract void onPermissionResult(int requestCode, boolean isGranted, Object extras);



}
