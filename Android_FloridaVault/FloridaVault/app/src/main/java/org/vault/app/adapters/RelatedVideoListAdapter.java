package org.vault.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ncsavault.floridavault.R;

import org.vault.app.appcontroller.AppController;
import org.vault.app.dto.VideoDTO;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RelatedVideoListAdapter extends BaseAdapter{
    private ArrayList<VideoDTO> arrayListVideoDTOs;
    private Activity context;
    public static DisplayImageOptions options;
    private LayoutInflater layoutInflater;
    private String videoThumbnailURL;
    private String videoName;
    private long videoDuration;
    private ViewHolder viewHolder;
    private long videoId;
    private String playListRefId;
    AsyncTask<Void, Void, Void> mPostTask;
    String postResult;
    private int Measuredwidth = 0;
    ImageLoader imageLoader;
    ProgressDialog progressDialog;

    boolean isFavoriteChecked;

    public RelatedVideoListAdapter(ArrayList<VideoDTO> arrayListVideos,
                                   Activity context) {
        // TODO Auto-generated constructor stub
        this.arrayListVideoDTOs = arrayListVideos;
        this.context = context;
        if(this.context != null)
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
        if(context != null) {
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
        if (convertView == null && layoutInflater != null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater
                    .inflate(R.layout.item_related_list_view, null);

            viewHolder.networkedCacheableImageView = (ImageView) convertView
                    .findViewById(R.id.imgVideoThumbNail);
            viewHolder.textViewVideoName = (TextView) convertView
                    .findViewById(R.id.tv_video_name);
            viewHolder.tvVideoLength = (TextView) convertView
                    .findViewById(R.id.tv_video_length);
            viewHolder.tvVideoDescription = (TextView) convertView
                    .findViewById(R.id.tv_video_description);
            viewHolder.spinner = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            if(context != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.spinner.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.circle_progress_bar_lower));
                }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.spinner.setIndeterminateDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.progress_large_material, null));
                }
            }
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        videoId = arrayListVideoDTOs.get(pos).getVideoId();
        playListRefId = arrayListVideoDTOs.get(pos).getPlaylistReferenceId();

        // ------for VideoStillurl-------
        videoThumbnailURL = arrayListVideoDTOs.get(pos).getVideoStillUrl();
        videoName = arrayListVideoDTOs.get(pos).getVideoName();
        videoDuration = arrayListVideoDTOs.get(pos).getVideoDuration();

        viewHolder.tvVideoDescription.setText(arrayListVideoDTOs.get(pos).getVideoShortDescription());

        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(videoThumbnailURL,
                viewHolder.networkedCacheableImageView, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        viewHolder.spinner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        viewHolder.spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        viewHolder.spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        viewHolder.spinner.setVisibility(View.GONE);
                    }
                });

        viewHolder.textViewVideoName.setText(videoName);

        if (videoDuration != 0) {
            viewHolder.tvVideoLength
                    .setText(convertSecondsToHMmSs(videoDuration));
        }

        return convertView;
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
        ImageView networkedCacheableImageView;
        TextView textViewVideoName, tvVideoLength, tvVideoDescription;
        ProgressBar spinner;
    }


}
