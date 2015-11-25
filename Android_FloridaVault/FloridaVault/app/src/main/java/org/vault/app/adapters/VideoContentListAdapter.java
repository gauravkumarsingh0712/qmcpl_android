package org.vault.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.toolbox.ImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;

import org.vault.app.activities.MainActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoContentListAdapter extends BaseAdapter {

    private ArrayList<VideoDTO> arrayListVideoDTOs;
    public ArrayList<VideoDTO> listSearch;
    private Activity context;
    public static DisplayImageOptions options;
    private LayoutInflater layoutInflater;
    private String videoThumbnailURL;
    private String videoName;
    private long videoDuration;
    private ViewHolder viewHolder;
    AsyncTask<Void, Void, Void> mPostTask;
    String postResult;
    private int Measuredwidth;
    public boolean isPullRefreshInProgress;
    ImageLoader imageLoader;
    int viewType;

    boolean isFavoriteChecked, isFavoriteTab;
    private String videoDescription;

    public VideoContentListAdapter(ArrayList<VideoDTO> arrayListVideos,
                                   Activity context, int type, boolean isFavoriteTab) {
        // TODO Auto-generated constructor stub
        this.arrayListVideoDTOs = arrayListVideos;
        this.context = context;
        this.isFavoriteTab = isFavoriteTab;
        this.listSearch = new ArrayList<VideoDTO>();
        viewType = type;
        this.listSearch.addAll(arrayListVideos);
        layoutInflater = LayoutInflater.from(this.context);
        getScreenDimensions();
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        imageLoader = AppController.getInstance().getImageLoader();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void getScreenDimensions() {
        Point size = new Point();
        WindowManager w = context.getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
            // Measuredheight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            // Measuredheight = d.getHeight();
            Measuredwidth = d.getWidth();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return arrayListVideoDTOs.size();
    }

    @Override
    public Object getItem(int pos) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int pos, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (viewType == 1) {
                convertView = layoutInflater
                        .inflate(R.layout.item_edge_to_edge_view_list, null);
                viewHolder.frmVideoItem = (FrameLayout) convertView
                        .findViewById(R.id.frame_video_item);
            } else {
                convertView = layoutInflater
                        .inflate(R.layout.item_list_view, null);
                viewHolder.tvVideoDescription = (TextView) convertView
                        .findViewById(R.id.tv_video_description);
            }
            viewHolder.thumbnailImageView = (ImageView) convertView
                    .findViewById(R.id.imgVideoThumbNail);
            viewHolder.tvVideoName = (TextView) convertView
                    .findViewById(R.id.tv_video_name);
            viewHolder.tvVideoDuration = (TextView) convertView
                    .findViewById(R.id.tv_video_length);
            viewHolder.imgToggleButton = (ImageView) convertView.findViewById(R.id.imgToggleButton);

            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                viewHolder.progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.circle_progress_bar_lower));
            else
                viewHolder.progressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.progress_large_material, null));

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(isPullRefreshInProgress)
            viewHolder.imgToggleButton.setEnabled(false);
        else
            viewHolder.imgToggleButton.setEnabled(true);

        initData(pos);
        initListener(pos);

        return convertView;
    }

    public void initData(final int pos){
        if(arrayListVideoDTOs.get(pos).isVideoIsFavorite())
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
        else
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);

        if (VaultDatabaseHelper.getInstance(context.getApplicationContext()).isFavorite(arrayListVideoDTOs.get(pos).getVideoId()))
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
        else
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);



        videoThumbnailURL = arrayListVideoDTOs.get(pos).getVideoStillUrl();
        videoName = arrayListVideoDTOs.get(pos).getVideoName();
        videoDescription = arrayListVideoDTOs.get(pos).getVideoShortDescription();
        videoDuration = arrayListVideoDTOs.get(pos).getVideoDuration();

        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(videoThumbnailURL,
                viewHolder.thumbnailImageView, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }
                });

        viewHolder.tvVideoName.setText(videoName);
        if(viewType == 1){
            int aspectHeight = (Measuredwidth * 9) / 16;

            viewHolder.frmVideoItem.setMinimumHeight(aspectHeight);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, aspectHeight);
            viewHolder.thumbnailImageView.setLayoutParams(lp);
        }else if(viewType == 2) {
            if (videoDescription != null)
                viewHolder.tvVideoDescription.setText(videoDescription);
            else
                viewHolder.tvVideoDescription.setText("");
        }

        if(isFavoriteTab){
            viewHolder.imgToggleButton.setEnabled(false);
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
        }

        if (videoDuration != 0) {
            viewHolder.tvVideoDuration
                    .setText(convertSecondsToHMmSs(videoDuration));
        }

    }

    public void initListener(final int pos) {

        viewHolder.imgToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetAvailable(context)) {
                    if (AppController.getInstance().getUserId() == GlobalConstants.DEFAULT_USER_ID) {
                        viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                        showConfirmLoginDialog(GlobalConstants.LOGIN_MESSAGE);
                    } else {
                        if (arrayListVideoDTOs.get(pos).isVideoIsFavorite()) {
                            isFavoriteChecked = false;
                            VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(0, arrayListVideoDTOs.get(pos).getVideoId());
                            arrayListVideoDTOs.get(pos).setVideoIsFavorite(false);
                            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                        }else {
                            isFavoriteChecked = true;
                            VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(1, arrayListVideoDTOs.get(pos).getVideoId());
                            arrayListVideoDTOs.get(pos).setVideoIsFavorite(true);
                            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
                        }

                        mPostTask = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    postResult = AppController.getInstance().getServiceManager().getVaultService().postFavoriteStatus(AppController.getInstance().getUserId(), arrayListVideoDTOs.get(pos).getVideoId(), arrayListVideoDTOs.get(pos).getPlaylistId(), isFavoriteChecked);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                System.out.println("Result of POST request : " + postResult);
                                if (isFavoriteChecked)
                                    VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(1, arrayListVideoDTOs.get(pos).getVideoId());
                                else
                                    VaultDatabaseHelper.getInstance(context.getApplicationContext()).setFavoriteFlag(0, arrayListVideoDTOs.get(pos).getVideoId());
                            }
                        };

                        mPostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } else {
                    ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
                    viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                }
                notifyDataSetChanged();
            }
        });
    }

    public void showConfirmLoginDialog(String message) {
        AlertDialog alertDialog = null;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(message);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        context.stopService(new Intent(context, VideoDataService.class));

                        VaultDatabaseHelper.getInstance(context.getApplicationContext()).removeAllRecords();

                        SharedPreferences prefs = context.getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0).commit();
//                        prefs.edit().putBoolean(GlobalConstants.PREF_PULL_OPTION_HEADER, false).commit();

                        Intent intent = new Intent(context, LoginEmailActivity.class);
                        context.startActivity(intent);
                        context.finish();
//                        context.finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public String convertSecondsToHMmSs(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        String duration = "";
        if (minutes > 60) {
            // convert the minutes to Hours:Minutes:Seconds
        } else {
            /*
             * if (minutes < 10) duration += "0" + minutes; else duration +=
			 * minutes;
			 */
            duration += minutes;
            if (seconds < 10)
                duration += ":0" + seconds;
            else
                duration += ":" + seconds;
        }
        return duration;
    }

    public static class ViewHolder {
        ImageView thumbnailImageView, imgToggleButton;
        TextView tvVideoName, tvVideoDuration, tvVideoDescription;
        FrameLayout frmVideoItem;
        ProgressBar progressBar;
    }

    /**
     * This methos is use to filter the lisview according to text-------
     *
     * @param charText
     */
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arrayListVideoDTOs.clear();
        if (charText.length() == 0) {
            arrayListVideoDTOs.addAll(listSearch);
        } else {
            for (VideoDTO dto : listSearch) {
                if (dto.getVideoShortDescription().toLowerCase(Locale.getDefault())
                        .contains(charText) || dto.getVideoName().toLowerCase(Locale.getDefault()).contains(charText) || dto.getVideoTags().toLowerCase(Locale.getDefault()).contains(charText)) {
                    arrayListVideoDTOs.add(dto);
                }
            }
        }
        notifyDataSetChanged();
    }

}
