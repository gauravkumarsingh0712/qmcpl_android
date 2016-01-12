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
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ncsavault.floridavault.LoginEmailActivity;
import com.ncsavault.floridavault.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.vault.app.activities.MainActivity;
import org.vault.app.appcontroller.AppController;
import org.vault.app.database.VaultDatabaseHelper;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.VideoDataService;
import org.vault.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class VideoContentHeaderListAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {

    private ArrayList<VideoDTO> arrayListVideoDTOs;
    public ArrayList<VideoDTO> listSearch;
    private Activity context;
    public static DisplayImageOptions options;
    private LayoutInflater layoutInflater;
    private String videoImageURL;
    private String videoName;
    private long videoDuration;
    private ViewHolder viewHolder;
    AsyncTask<Void, Void, Void> mPostTask;
    String postResult;
    private int Measuredwidth;
    public boolean isPullRefreshInProgress = false;
    int viewType;

    boolean isFavoriteChecked;
    private String videoDescription;
    char lastFirstChar;

    private int[] mSectionIndices;
    private Character[] mSectionLetters;
    private boolean showLetters, isGames;

    public VideoContentHeaderListAdapter(ArrayList<VideoDTO> arrayListVideos,
                                         Activity context, int type, boolean showLetters, boolean isGames) {
        // TODO Auto-generated constructor stub
        this.arrayListVideoDTOs = arrayListVideos;
        this.context = context;
        this.listSearch = new ArrayList<VideoDTO>();
        viewType = type;
        this.listSearch.addAll(arrayListVideos);
        layoutInflater = LayoutInflater.from(this.context);
        getScreenDimensions();
        this.showLetters = showLetters;
        this.isGames = isGames;
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
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
        return arrayListVideoDTOs.get(pos);
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

            /*viewHolder.toggleButtonStar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    isFavoriteChecked = isChecked;
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    arrayListVideoDTOs.get(getPosition).setVideoIsFavorite(buttonView.isChecked());
                }
            });*/

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

    @Override
    public int getPositionForSection(int section) {
        // TODO Auto-generated method stub
        if(showLetters) {
            if (mSectionIndices.length == 0) {
                return 0;
            }

            if (section >= mSectionIndices.length) {
                section = mSectionIndices.length - 1;
            } else if (section < 0) {
                section = 0;
            }
            return mSectionIndices[section];
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        // TODO Auto-generated method stub
        if(showLetters) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (position < mSectionIndices[i]) {
                    return i - 1;
                }
            }
            return mSectionIndices.length - 1;
        }
        return 0;
    }

    @Override
    public Object[] getSections() {
        // TODO Auto-generated method stub
        if(showLetters)
            return mSectionLetters;
        return null;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.headerview_stickyheaderlistview, null);
            holder = new ViewHolder();
            holder.textViewHeader = (TextView) convertView
                    .findViewById(R.id.tv_header);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String headerName = String.valueOf(arrayListVideoDTOs.get(position)
                .getPlaylistName());

        holder.textViewHeader.setText(headerName);
        return convertView;
    }



    public void initData(final int pos){
        if(arrayListVideoDTOs.get(pos).isVideoIsFavorite())
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
        else
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);

        if(VaultDatabaseHelper.getInstance(context.getApplicationContext()).isFavorite(arrayListVideoDTOs.get(pos).getVideoId()))
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargold);
        else
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);

//        videoImageURL = arrayListVideoDTOs.get(pos).getVideoStillUrl();
        if(isGames)
            videoImageURL = arrayListVideoDTOs.get(pos).getVideoCoverUrl();
        else
            videoImageURL = arrayListVideoDTOs.get(pos).getVideoStillUrl();
        videoName = arrayListVideoDTOs.get(pos).getVideoName();
        videoDescription = arrayListVideoDTOs.get(pos).getVideoShortDescription();
        videoDuration = arrayListVideoDTOs.get(pos).getVideoDuration();

        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(videoImageURL,
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

        if(viewType == 1){
            int aspectHeight;    //Edge To Edge view for tab
            if(!isGames) {
                aspectHeight = (Measuredwidth * 9) / 16;    //Edge To Edge view for tab

            }else {
                aspectHeight = (int) ((Measuredwidth * 4.5) / 16);    //Wide Still View specially for Games tab which does not show section indexer letters
                viewHolder.imgToggleButton.setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewHolder.tvVideoDuration.setLayoutParams(lp);
            }
            viewHolder.frmVideoItem.setMinimumHeight(aspectHeight);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, aspectHeight);
            viewHolder.thumbnailImageView.setLayoutParams(lp);
        }else if(viewType == 2) {
            if (videoDescription != null)
                viewHolder.tvVideoDescription.setText(videoDescription);
            else
                viewHolder.tvVideoDescription.setText("");
        }
        viewHolder.tvVideoName.setText(videoName);
        if (videoDuration != 0) {
            viewHolder.tvVideoDuration
                    .setText(convertSecondsToHMmSs(videoDuration));
        }

    }

    public void initListener(final int pos) {

        viewHolder.imgToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayListVideoDTOs.get(pos).isVideoIsFavorite() && ((arrayListVideoDTOs.get(pos).getVideoLongUrl().length() == 0 || arrayListVideoDTOs.get(pos).getVideoLongUrl().toLowerCase().equals("none")))) {
                    markFavoriteStatus(pos);
                } else {
                    if (arrayListVideoDTOs.get(pos).getVideoLongUrl().length() > 0 && !arrayListVideoDTOs.get(pos).getVideoLongUrl().toLowerCase().equals("none")) {
                        markFavoriteStatus(pos);
                    } else {
                        ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_INFO_AVAILABLE);
                        viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
                    }
                }
                notifyDataSetChanged();
            }
        });

    }

    public void markFavoriteStatus(final int pos){
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
                } else {
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

//                        mPostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mPostTask.execute();
            }
        } else {
            ((MainActivity) context).showToastMessage(GlobalConstants.MSG_NO_CONNECTION);
            viewHolder.imgToggleButton.setBackgroundResource(R.drawable.stargreyicon);
        }
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

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        if (arrayListVideoDTOs.size() > 0) {
            if (arrayListVideoDTOs.get(0).getPlaylistName() != "") {
                lastFirstChar = arrayListVideoDTOs.get(0).getPlaylistName().charAt(0);
            }
            sectionIndices.add(0);
            for (int i = 1; i < arrayListVideoDTOs.size(); i++) {
                if (arrayListVideoDTOs.get(i) != null) {
                    if (arrayListVideoDTOs.get(i).getPlaylistName() != "")
                        if (arrayListVideoDTOs.get(i).getPlaylistName().charAt(0) != lastFirstChar) {
                            lastFirstChar = arrayListVideoDTOs.get(i).getPlaylistName().charAt(0);
                            sectionIndices.add(i);
                        }
                }
            }
            int[] sections = new int[sectionIndices.size()];
            for (int i = 0; i < sectionIndices.size(); i++) {
                sections[i] = sectionIndices.get(i);
            }
            return sections;
        }
        return null;
    }

    private Character[] getSectionLetters() {
        if (mSectionIndices != null) {
            Character[] letters = new Character[mSectionIndices.length];
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (arrayListVideoDTOs.get(mSectionIndices[i]) != null)
                    if (arrayListVideoDTOs.get(mSectionIndices[i]).getPlaylistName() != "")
                        letters[i] = arrayListVideoDTOs.get(mSectionIndices[i]).getPlaylistName().charAt(0);
            }
            return letters;
        }
        return null;
    }

    public void updateIndexer() {
        mSectionIndices = null;
        mSectionLetters = null;

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        notifyDataSetChanged();
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
        Collections.sort(arrayListVideoDTOs, new Comparator<VideoDTO>() {

            @Override
            public int compare(VideoDTO lhs, VideoDTO rhs) {
                // TODO Auto-generated method stub
                if(isGames)
                    return rhs.getPlaylistName().toLowerCase()
                            .compareTo(lhs.getPlaylistName().toLowerCase());
                else
                    return lhs.getPlaylistName().toLowerCase()
                            .compareTo(rhs.getPlaylistName().toLowerCase());
            }
        });
        if (arrayListVideoDTOs.size() > 0)
            updateIndexer();
        notifyDataSetChanged();


    }

    @Override
    public long getHeaderId(int position) {
        if (arrayListVideoDTOs.size() > 0)
            return arrayListVideoDTOs.get(position).getPlaylistName()
                    .hashCode();
        return 0;
    }

    public static class ViewHolder {
        ImageView thumbnailImageView, imgToggleButton;
        TextView tvVideoName, tvVideoDuration, tvVideoDescription;
        FrameLayout frmVideoItem;
        ProgressBar progressBar;
        TextView textViewHeader;
    }


}
