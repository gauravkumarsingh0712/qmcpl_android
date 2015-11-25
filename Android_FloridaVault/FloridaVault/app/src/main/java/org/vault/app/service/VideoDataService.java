package org.vault.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.vault.app.activities.SplashActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.fragments.CoachesEraFragment;
import org.vault.app.fragments.FeaturedFragment;
import org.vault.app.fragments.GamesFragment;
import org.vault.app.fragments.OpponentsFragment;
import org.vault.app.fragments.PlayerFragment;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by aqeeb.pathan on 24-06-2015.
 */
public class VideoDataService extends Service {

    public static boolean isServiceRunning = false;
    ArrayList<VideoDTO> arrayListVideos = new ArrayList<VideoDTO>();

    boolean status = true;
    String userJsonData = "";

    /*private static final String[] API_CALLS = {GlobalConstants.FEATURED_API_URL,
            GlobalConstants.PLAYER_API_URL, GlobalConstants.COACH_API_URL, GlobalConstants.OPPONENT_API_URL,
            GlobalConstants.FAVORITE_API_URL};*/
    private static final String[] API_CALLS = {GlobalConstants.FEATURED_API_URL, GlobalConstants.GAMES_API_URL,
            GlobalConstants.PLAYER_API_URL, GlobalConstants.COACH_API_URL, GlobalConstants.OPPONENT_API_URL};

    public static ArrayList<String> API_URLS = new ArrayList<>();


    Thread thread;

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        /*if(thread != null)
            thread.interrupt();*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;

        if (Utils.isInternetAvailable(AppController.getInstance().getApplicationContext())) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (final String apiUrl : API_URLS) {

                        if (Utils.isInternetAvailable(AppController.getInstance().getApplicationContext())) {
                            if (isServiceRunning) {
                                String url = apiUrl + "userid=" + AppController.getInstance().getUserId();
                                try {
                                    arrayListVideos.addAll(AppController.getInstance().getServiceManager().getVaultService().getVideosListFromServer(url));
                                    System.out.println("Size of list after calling " + apiUrl + " : " + arrayListVideos.size());
                                } catch (Exception e) {
                                    status = false;
                                    e.printStackTrace();
                                }
                                if (status)
                                    VaultDatabaseHelper.getInstance(getApplicationContext()).insertVideosInDatabase(arrayListVideos);

                                Intent broadCastIntent = new Intent();
                                if (url.toLowerCase().contains("featured"))
                                    broadCastIntent.setAction(FeaturedFragment.FeaturedResponseReceiver.ACTION_RESP);
                                else if (url.toLowerCase().contains("games"))
                                    broadCastIntent.setAction(GamesFragment.GamesResponseReceiver.ACTION_RESP);
                                else if (url.toLowerCase().contains("player"))
                                    broadCastIntent.setAction(PlayerFragment.PlayerResponseReceiver.ACTION_RESP);
                                else if (url.toLowerCase().contains("coach"))
                                    broadCastIntent.setAction(CoachesEraFragment.CoachesResponseReceiver.ACTION_RESP);
                                else if (url.toLowerCase().contains("opponent"))
                                    broadCastIntent.setAction(OpponentsFragment.OpponentsResponseReceiver.ACTION_RESP);

                                broadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                sendBroadcast(broadCastIntent);
                                arrayListVideos.clear();
                            }
                        } else {
                            isServiceRunning = false;
                            stopSelf();
                        }
                    }
                    isServiceRunning = false;
                    stopSelf();
                }
            };
            thread.start();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
