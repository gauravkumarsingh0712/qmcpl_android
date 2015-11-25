package com.ncsavault.floridavault;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import org.vault.app.activities.MainActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.globalconstants.GlobalConstants;

import java.util.Calendar;

/**
 * @author aqeeb.pathan
 *
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";
    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
    SharedPreferences prefs;

    public GCMIntentService() {
        // Call extended class Constructor GCMBaseIntentService
        super(GlobalConstants.GOOGLE_SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(final Context context,
                                final String registrationId) {
        prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        Log.i(TAG, "Device registered: regId = " + registrationId);
        // Save the registrationID on your server
        mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.i("MainActivity", "Device Registration Id : = "
                            + registrationId);
                    String deviceId = Secure.getString(getContentResolver(),
                            Secure.ANDROID_ID);
				/*String result = Utils.doPost(GlobalConstants.PUSH_REGISTER_URL,
						registrationId, deviceId);*/
                    String result = AppController.getInstance().getServiceManager().getVaultService().sendPushNotificationRegistration(GlobalConstants.PUSH_REGISTER_URL,
                            registrationId, deviceId, true);
                    if (result != null) {
                        System.out
                                .println("Response from server after registration : "
                                        + result);
                        if (result.toLowerCase().contains("success")){
                            GCMRegistrar.setRegisteredOnServer(context, true);
                            prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, true).commit();
                            prefs.edit().putBoolean(GlobalConstants.PREF_IS_DEVICE_REGISTERED, true).commit();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    /**
     * Method called on device unregistred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(GlobalConstants.PREF_IS_NOTIFICATION_ALLOW, false).commit();
        System.out.println("Device unregistered");
    }

    /**
     * Method called on Receiving a new message from GCM server
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        // read message and take appropreate data from it
        String message = intent.getExtras().getString("message");
        String title = intent.getExtras().getString("title");
        // notifies user
        generateNotificationCustomView(context, message, title);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {

        Log.i(TAG, "Received deleted messages notification");
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * @author aqeeb.pathan
     * @param context
     * @param message
     * @param title
     *            This method generates a notification to be show in mobile
     *            notification bar.
     */
    @SuppressWarnings("unused")
    private static void generateNotification(Context context, String message,
                                             String title) {
        try {
            Bitmap remote_picture = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.logo);
            // Setup an explicit intent for an ResultActivity to receive.
            Intent resultIntent = new Intent(context, LoginEmailActivity.class);

            // TaskStackBuilder ensures that the back button follows the recommended
            // convention for the back key.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

            // Adds the back stack for the Intent (but not the Intent itself).
            stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack.
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification noti = new Notification();
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            noti = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo).setAutoCancel(true)
                    .setLargeIcon(remote_picture)
                    .setContentIntent(resultPendingIntent).setContentTitle(title)
                    .setContentText(message).build();

            noti.defaults |= Notification.DEFAULT_LIGHTS;
            noti.defaults |= Notification.DEFAULT_VIBRATE;
            noti.defaults |= Notification.DEFAULT_SOUND;

            noti.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            mNotificationManager.notify(0, noti);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateNotificationCustomView(Context context,
                                                       String message, String title) {

        try {
            Bitmap remote_picture = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.logo);
            Calendar c = Calendar.getInstance();
            String currTime = "";
            if (c.get(Calendar.HOUR_OF_DAY) < 10)
                currTime += "0" + c.get(Calendar.HOUR_OF_DAY) + ":";
            else
                currTime += c.get(Calendar.HOUR_OF_DAY) + ":";

            if (c.get(Calendar.MINUTE) < 10)
                currTime += "0" + c.get(Calendar.MINUTE);
            else
                currTime += c.get(Calendar.MINUTE);

            Intent resultIntent = new Intent(context, LoginEmailActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // The custom view
            RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                    R.layout.notification_custom_view);
            expandedView.setTextViewText(R.id.notification_title, title);
            expandedView.setTextViewText(R.id.notification_subtitle, message);
            expandedView.setTextViewText(R.id.tv_notification_time, currTime);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        context).setAutoCancel(true).setContentTitle(title)
                        .setSmallIcon(R.drawable.logo).setLargeIcon(remote_picture)
                        .setContentIntent(resultPendingIntent)
                        .setContentText(message);
                mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);

                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                bigText.bigText(message);
                bigText.setBigContentTitle(title);
                mBuilder.setStyle(bigText);

                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(100, mBuilder.build());
            } else {
                // this notification appears properly below 4.4.4
                Notification notificationBuilder = new NotificationCompat.Builder(
                        context)
                        .setSmallIcon(R.drawable.logo)
                        .setLargeIcon(remote_picture)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message.toString()))
                        .setContent(expandedView).build();
                notificationBuilder.defaults |= Notification.DEFAULT_LIGHTS;
                notificationBuilder.defaults |= Notification.DEFAULT_VIBRATE;
                notificationBuilder.defaults |= Notification.DEFAULT_SOUND;

                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(100, notificationBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
