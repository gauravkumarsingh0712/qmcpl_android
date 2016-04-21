package org.vault.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.VideoDTO;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by aqeeb.pathan on 17-06-2015.
 */
public class VaultDatabaseHelper extends SQLiteOpenHelper {

    private static VaultDatabaseHelper sInstance;

    // ------ Database Version----------
    public static final int DATABASE_VERSION = 3;
    private static String DATABASE_PATH = "";

    // ----- Database Name------------
    public static final String DATABASE_NAME = "FloridaVault_database";

    private static final String USER_TABLE = "user_data";
    private static final String OLD_VIDEO_TABLE = "VIDEO_TABLE";

    public Context context;

    public VaultDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        context = ctx;
    }

    public static synchronized VaultDatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new VaultDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        VideoTable.onCreate(db);
        TabBannerTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OLD_VIDEO_TABLE);
        VideoTable.onUpgrade(db, oldVersion, newVersion);
        TabBannerTable.onUpgrade(db, oldVersion, newVersion);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public boolean isOpen() {
        if (this.getWritableDatabase() != null && this.getWritableDatabase().isOpen())
            return true;
        return false;
    }

    //DATABASE OPERATION QUERIES AND METHODS
    /***********************************************************************************************
     *  All methods related to storage of videos in the table
     *  All methods related to retrieval of video records from DB
     **********************************************************************************************/

    /**
     * Method to check number of videos from the VideoTable in database
     * @return
     */
    public int getVideoCount(){
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            Cursor cursor = database.rawQuery(VideoTable.selectAllVideos, null);

            if (cursor != null) {
                return cursor.getCount();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public ArrayList<VideoDTO> getVideoListByTab(String tabIdentifier){
        try {
            ArrayList<VideoDTO> videoDTOsArrayList = new ArrayList<>();
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            Cursor cursor = database.rawQuery(VideoTable.getQueryByTab(tabIdentifier), null);

            if (cursor.moveToFirst()) {
                do {
                    VideoDTO videoDTO = new VideoDTO();
                    videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                    videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                    videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                    videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                    videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                    videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                    videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                    videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                    videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                    videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                    videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                    videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                    videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                    if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                        videoDTO.setVideoIsFavorite(false);
                    else
                        videoDTO.setVideoIsFavorite(true);

                    videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                    videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                    videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                    videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                    videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                    videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                    videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                    videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                    videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));


                    videoDTOsArrayList.add(videoDTO);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return videoDTOsArrayList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }
    }

    public VideoDTO getVideoDataByVideoId(String videoId){
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            String query = "SELECT * FROM "
                    + VideoTable.VIDEO_TABLE + " WHERE " + VideoTable.KEY_VIDEO_ID
                    + " = "+videoId+" GROUP BY "+ VideoTable.KEY_VIDEO_ID;
            Cursor cursor = database.rawQuery(query, null);
            VideoDTO videoDTO = null;
            if (cursor.moveToFirst()) {
                do {
                    videoDTO = new VideoDTO();
                    videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                    videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                    videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                    videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                    videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                    videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                    videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                    videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                    videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                    videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                    videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                    videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                    videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                    if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                        videoDTO.setVideoIsFavorite(false);
                    else
                        videoDTO.setVideoIsFavorite(true);

                    videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                    videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                    videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                    videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                    videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                    videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                    videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                    videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                    videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                } while (cursor.moveToNext());
            }
            cursor.close();
            return videoDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<VideoDTO> getAllVideoList(){
        String selectOKFQuery = "SELECT * FROM " + VideoTable.VIDEO_TABLE;
        try {
            ArrayList<VideoDTO> videoDTOsArrayList = new ArrayList<VideoDTO>();
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            Cursor cursor = database.rawQuery(selectOKFQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    VideoDTO videoDTO = new VideoDTO();
                    videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                    videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                    videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                    videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                    videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                    videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                    videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                    videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                    videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                    videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                    videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                    videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                    videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                    if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                        videoDTO.setVideoIsFavorite(false);
                    else
                        videoDTO.setVideoIsFavorite(true);

                    videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                    videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                    videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                    videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                    videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                    videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                    videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                    videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                    videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                    videoDTOsArrayList.add(videoDTO);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return videoDTOsArrayList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }
    }

    public ArrayList<VideoDTO> getVideoList(String referenceId){
        String selectOKFQuery = "SELECT * FROM " + VideoTable.VIDEO_TABLE
                + " WHERE " + VideoTable.KEY_PLAYLIST_REFERENCE_ID + " LIKE '"+referenceId+"%'"+" GROUP BY "+ VideoTable.KEY_VIDEO_ID;
        try {
            ArrayList<VideoDTO> videoDTOsArrayList = new ArrayList<VideoDTO>();
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            Cursor cursor = database.rawQuery(selectOKFQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    VideoDTO videoDTO = new VideoDTO();
                    videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                    videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                    videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                    videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                    videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                    videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                    videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                    videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                    videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                    videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                    videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                    videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                    videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                    if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                        videoDTO.setVideoIsFavorite(false);
                    else
                        videoDTO.setVideoIsFavorite(true);

                    videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                    videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                    videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                    videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                    videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                    videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                    videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                    videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                    videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                    videoDTOsArrayList.add(videoDTO);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return videoDTOsArrayList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }
    }

    /**
     * This method is used to get the favorites videos
     *
     * @return
     */
    public ArrayList<VideoDTO> getFavouriteVideosArrayList() {
        try {
            ArrayList<VideoDTO> arrayListNewVideoDTOs = new ArrayList<VideoDTO>();
            SQLiteDatabase database = this.getWritableDatabase();
            database.enableWriteAheadLogging();
            String getFavoutiteVideosListQuery = "SELECT * FROM " + VideoTable.VIDEO_TABLE + " WHERE " + VideoTable.KEY_VIDEO_IS_FAVORITE + "=1 " + " GROUP BY " + VideoTable.KEY_VIDEO_ID + " ORDER BY " + VideoTable.KEY_VIDEO_NAME + " ASC";
            Cursor cursor = database.rawQuery(getFavoutiteVideosListQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    VideoDTO videoDTO = new VideoDTO();
                    videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                    videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                    videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                    videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                    videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                    videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                    videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                    videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                    videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                    videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                    videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                    videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                    videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                    if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                        videoDTO.setVideoIsFavorite(false);
                    else
                        videoDTO.setVideoIsFavorite(true);

                    videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                    videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                    videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                    videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                    videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                    videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                    videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                    videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                    videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                    arrayListNewVideoDTOs.add(videoDTO);
                } while (cursor.moveToNext());
            }

            cursor.close();
            return arrayListNewVideoDTOs;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }

    }

    /**
     * This method is used to set the video favorite
     *
     * @param flag
     * @param videoID
     */
    public void setFavoriteFlag(int flag, long videoID) {
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            ContentValues contents = new ContentValues();
            contents.put(VideoTable.KEY_VIDEO_IS_FAVORITE, flag);
            database.update(VideoTable.VIDEO_TABLE, contents, VideoTable.KEY_VIDEO_ID + "=?", new String[]{"" + videoID});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to check the video is favorite or not
     *
     * @param videoId
     * @return
     */
    public boolean isFavorite(long videoId) {
        try {
            int flag = 0;
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            String getFlagQuery = "SELECT " + VideoTable.KEY_VIDEO_IS_FAVORITE + "  FROM " + VideoTable.VIDEO_TABLE + " WHERE " + VideoTable.KEY_VIDEO_ID + " = "
                    + videoId;
            Cursor cursor = database.rawQuery(getFlagQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    flag = cursor.getInt(0);
                } while (cursor.moveToNext());

            }
            cursor.close();
            if (flag == 1) {
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setAllFavoriteStatusToFalse() {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            database.enableWriteAheadLogging();
            database.execSQL("UPDATE " + VideoTable.VIDEO_TABLE + " SET " + VideoTable.KEY_VIDEO_IS_FAVORITE + " = 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getFavoriteCount() {
        SQLiteDatabase database = this.getWritableDatabase();
        int count = 0 ;
        try {
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM "+VideoTable.VIDEO_TABLE + " WHERE " + VideoTable.KEY_VIDEO_IS_FAVORITE + "= 1" ;
            Cursor cursor = database.rawQuery(selectQuery, null);
            if(cursor != null)
                count = cursor.getCount();
            cursor.close();
            return count;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return count;
        }
    }

    /**
     * This method is used to check the video is available in database or
     * not----------
     *
     * @param videoId
     * @param playlistRefernceId
     * @return
     */
    public boolean isVideoAvailableInDB(long videoId, String playlistRefernceId) {
        // TODO Auto-generated method stub
        int count = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        database.enableWriteAheadLogging();
        String query = "select * from " + VideoTable.VIDEO_TABLE + " where " + VideoTable.KEY_VIDEO_ID + " = " + videoId
                + " and " + VideoTable.KEY_PLAYLIST_REFERENCE_ID + " = ? ";
        Cursor cursor = database.rawQuery(query,
                new String[]{playlistRefernceId});
        count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean isVideoAvailableInDB(String videoId) {
        // TODO Auto-generated method stub
        int count = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        database.enableWriteAheadLogging();
        String query = "select * from " + VideoTable.VIDEO_TABLE + " where " + VideoTable.KEY_VIDEO_ID + " = " + videoId;
        Cursor cursor = database.rawQuery(query, null);
        count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean checkVideoAvailability(long videoId) {
        // TODO Auto-generated method stub
        int count = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        database.enableWriteAheadLogging();
        String query = "select * from " + VideoTable.VIDEO_TABLE + " where " + VideoTable.KEY_VIDEO_ID + " = " + videoId;
        Cursor cursor = database.rawQuery(query, null);
        count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean isChangeInData(VideoDTO newVideoObject){
        SQLiteDatabase database = this.getReadableDatabase();
        database.enableWriteAheadLogging();
        //Fetch Old Video Metadata from local database
        String query = "select * from " + VideoTable.VIDEO_TABLE + " where " + VideoTable.KEY_VIDEO_ID + " = " + newVideoObject.getVideoId();
        Cursor cursor = database.rawQuery(query,null);
        int count = cursor.getCount();
        if(count > 0){
            if (cursor.moveToFirst()) {
                do {
                    if (!cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)).equals(newVideoObject.getVideoName()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)).equals(newVideoObject.getVideoShortDescription()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)).equals(newVideoObject.getVideoLongDescription()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)).equals(newVideoObject.getVideoShortUrl()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)).equals(newVideoObject.getVideoLongUrl()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)).equals(newVideoObject.getVideoThumbnailUrl()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)).equals(newVideoObject.getVideoStillUrl()) || !cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)).equals(newVideoObject.getVideoTags()) || cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)) != newVideoObject.getVideoDuration())
                        return true;
                }while(cursor.moveToNext());
            }
        }
        cursor.close();
        return false;
    }

    public ArrayList<VideoDTO> getRelatedVideosArrayList(String videoTags, long videoId) {
        try {
            if(videoTags != null) {
                String getRelatedVideos = "SELECT * FROM " + VideoTable.VIDEO_TABLE + " WHERE (";
                String[] arrTags = videoTags.split(",");
                for(int i=0; i<arrTags.length; i++){
                    if(i==0)
                        getRelatedVideos = getRelatedVideos + VideoTable.KEY_VIDEO_TAGS + " like '%"+arrTags[i].trim()+"%' ";
                    else if(i > 0 && i < arrTags.length)
                        getRelatedVideos = getRelatedVideos + " or " + VideoTable.KEY_VIDEO_TAGS + " like '%"+arrTags[i].trim()+"%' ";
                }
                getRelatedVideos = getRelatedVideos + ") and "+VideoTable.KEY_VIDEO_ID+" != " + videoId + " GROUP BY " + VideoTable.KEY_VIDEO_ID + " ORDER BY " + VideoTable.KEY_VIDEO_NAME + " ASC";
                ArrayList<VideoDTO> arrayListNewVideoDTOs = new ArrayList<VideoDTO>();
                SQLiteDatabase database = this.getWritableDatabase();
                database.enableWriteAheadLogging();

                Cursor cursor = database.rawQuery(getRelatedVideos, null);
                if (cursor.moveToFirst()) {
                    do {
                        VideoDTO videoDTO = new VideoDTO();
                        videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                        videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                        videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                        videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                        videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                        videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                        videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                        videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                        videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                        videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                        videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                        videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                        videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                        if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                            videoDTO.setVideoIsFavorite(false);
                        else
                            videoDTO.setVideoIsFavorite(true);

                        videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                        videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                        videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                        videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                        videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                        videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                        videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                        videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                        videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                        arrayListNewVideoDTOs.add(videoDTO);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                return arrayListNewVideoDTOs;
            }else{
                return new ArrayList<VideoDTO>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }

    }

    public String trimAndRemoveSpecialCharacters(String videoAttribute){
        videoAttribute = videoAttribute.replace("'","");
        videoAttribute = videoAttribute.replace(":","");
        videoAttribute = videoAttribute.replace(";","");
        videoAttribute = videoAttribute.replace("-","");
        videoAttribute = videoAttribute.replace("_","");
        videoAttribute = videoAttribute.replace("#","");

        return videoAttribute.trim();
    }

    public ArrayList<VideoDTO> getRelatedVideosArrayListByNameAndTag(String videoTags, String videoName, long videoId) {
        try {
            if(videoTags != null) {
                String getRelatedVideos = "SELECT * FROM " + VideoTable.VIDEO_TABLE + " WHERE (";
                String[] arrTags = videoTags.split(",");
                String[] arrName = videoName.split(" ");
                ArrayList<String> fetchedNames = new ArrayList<>();
                for(int i=0; i<arrName.length; i++){
                    if(arrName[i].length() >= 4)
                        fetchedNames.add(arrName[i]);
                }
                if(arrTags.length > 0) {
                    for (int i = 0; i < arrTags.length; i++) {
                        if (i == 0)
                            getRelatedVideos = getRelatedVideos + VideoTable.KEY_VIDEO_TAGS + " like '%" + arrTags[i].trim() + "%' ";
                        else if (i > 0 && i < arrTags.length)
                            getRelatedVideos = getRelatedVideos + " or " + VideoTable.KEY_VIDEO_TAGS + " like '%" + arrTags[i].trim() + "%' ";
                    }
                    getRelatedVideos = getRelatedVideos + ") and (";
                    for (int i = 0; i < fetchedNames.size(); i++) {
                        if (i == 0)
                            getRelatedVideos = getRelatedVideos + VideoTable.KEY_VIDEO_NAME + " like '%" + trimAndRemoveSpecialCharacters(fetchedNames.get(i)) + "%' ";
                        else if (i > 0 && i < fetchedNames.size())
                            getRelatedVideos = getRelatedVideos + " or " + VideoTable.KEY_VIDEO_NAME + " like '%" + trimAndRemoveSpecialCharacters(fetchedNames.get(i)) + "%' ";
                    }
                    getRelatedVideos = getRelatedVideos + ") and " + VideoTable.KEY_VIDEO_ID + " != " + videoId + " GROUP BY " + VideoTable.KEY_VIDEO_ID + " ORDER BY " + VideoTable.KEY_VIDEO_NAME + " ASC";
                    ArrayList<VideoDTO> arrayListNewVideoDTOs = new ArrayList<VideoDTO>();
                    SQLiteDatabase database = this.getWritableDatabase();
                    database.enableWriteAheadLogging();

                    Cursor cursor = database.rawQuery(getRelatedVideos, null);
                    if (cursor.moveToFirst()) {
                        do {
                            VideoDTO videoDTO = new VideoDTO();
                            videoDTO.setVideoId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_ID)));
                            videoDTO.setVideoName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_NAME)));
                            videoDTO.setVideoShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_DESC)));
                            videoDTO.setVideoLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_DESC)));
                            videoDTO.setVideoShortUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SHORT_URL)));
                            videoDTO.setVideoLongUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_LONG_URL)));
                            videoDTO.setVideoThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_THUMB_URL)));
                            videoDTO.setVideoStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_STILL_URL)));
                            videoDTO.setVideoCoverUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_COVER_URL)));
                            videoDTO.setVideoWideStillUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_WIDE_STILL_URL)));
                            videoDTO.setVideoBadgeUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_BADGE_URL)));
                            videoDTO.setVideoDuration(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_VIDEO_DURATION)));
                            videoDTO.setVideoTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_TAGS)));
                            if (cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_IS_FAVORITE)) == 0)
                                videoDTO.setVideoIsFavorite(false);
                            else
                                videoDTO.setVideoIsFavorite(true);

                            videoDTO.setVideoIndex(cursor.getInt(cursor.getColumnIndex(VideoTable.KEY_VIDEO_INDEX)));
                            videoDTO.setPlaylistName(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_NAME)));
                            videoDTO.setPlaylistId(cursor.getLong(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_ID)));
                            videoDTO.setPlaylistThumbnailUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_THUMB_URL)));
                            videoDTO.setPlaylistShortDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_SHORT_DESC)));
                            videoDTO.setPlaylistLongDescription(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_LONG_DESC)));
                            videoDTO.setPlaylistTags(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_TAGS)));
                            videoDTO.setPlaylistReferenceId(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_PLAYLIST_REFERENCE_ID)));
                            videoDTO.setVideoSocialUrl(cursor.getString(cursor.getColumnIndex(VideoTable.KEY_VIDEO_SOCIAL_URL)));

                            arrayListNewVideoDTOs.add(videoDTO);
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                    return arrayListNewVideoDTOs;
                }else{
                    return new ArrayList<VideoDTO>();
                }
            }else{
                return new ArrayList<VideoDTO>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<VideoDTO>();
        }

    }


    /**
     * Adding videos to the local database
     */
    public void insertVideosInDatabase(ArrayList<VideoDTO> listVideos) {
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            ContentValues initialValues;
            for (VideoDTO videoDTO : listVideos) {
                //if video is not available in database, execute INSERT
                if (!isVideoAvailableInDB(videoDTO.getVideoId(), videoDTO.getPlaylistReferenceId())) {
                    if(videoDTO.getVideoShortDescription() != null && videoDTO.getVideoName() != null) {
                        initialValues = new ContentValues();
                        initialValues.put(VideoTable.KEY_VIDEO_ID, videoDTO.getVideoId());
                        initialValues.put(VideoTable.KEY_VIDEO_NAME, videoDTO.getVideoName());
                        initialValues.put(VideoTable.KEY_VIDEO_SHORT_DESC, videoDTO.getVideoShortDescription());
                        initialValues.put(VideoTable.KEY_VIDEO_LONG_DESC, videoDTO.getVideoLongDescription());
                        initialValues.put(VideoTable.KEY_VIDEO_SHORT_URL, videoDTO.getVideoShortUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_LONG_URL, videoDTO.getVideoLongUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_THUMB_URL, videoDTO.getVideoThumbnailUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_STILL_URL, videoDTO.getVideoStillUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_COVER_URL, videoDTO.getVideoCoverUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_WIDE_STILL_URL, videoDTO.getVideoWideStillUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_BADGE_URL, videoDTO.getVideoBadgeUrl());
                        initialValues.put(VideoTable.KEY_VIDEO_DURATION, videoDTO.getVideoDuration());
                        initialValues.put(VideoTable.KEY_VIDEO_TAGS, videoDTO.getVideoTags());
                        if (videoDTO.isVideoIsFavorite())
                            initialValues.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 1);
                        else
                            initialValues.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 0);
                        initialValues.put(VideoTable.KEY_VIDEO_INDEX, videoDTO.getVideoIndex());

                        initialValues.put(VideoTable.KEY_PLAYLIST_NAME, videoDTO.getPlaylistName());
                        initialValues.put(VideoTable.KEY_PLAYLIST_ID, videoDTO.getPlaylistId());
                        initialValues.put(VideoTable.KEY_PLAYLIST_THUMB_URL, videoDTO.getPlaylistThumbnailUrl());
                        initialValues.put(VideoTable.KEY_PLAYLIST_SHORT_DESC, videoDTO.getPlaylistShortDescription());
                        initialValues.put(VideoTable.KEY_PLAYLIST_LONG_DESC, videoDTO.getPlaylistLongDescription());
                        initialValues.put(VideoTable.KEY_PLAYLIST_TAGS, videoDTO.getPlaylistTags());
                        initialValues.put(VideoTable.KEY_PLAYLIST_REFERENCE_ID, videoDTO.getPlaylistReferenceId());
                        initialValues.put(VideoTable.KEY_VIDEO_SOCIAL_URL, videoDTO.getVideoSocialUrl());
                        database.insert(VideoTable.VIDEO_TABLE, null, initialValues);

                        checkVideoAvailabilityInOtherPlaylistAndUpdate(videoDTO);
                    }
                }else{      // Perform UPDATE query on available record
                    ContentValues updateExistingVideo = new ContentValues();
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_ID, videoDTO.getVideoId());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_NAME, videoDTO.getVideoName());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SHORT_DESC, videoDTO.getVideoShortDescription());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_LONG_DESC, videoDTO.getVideoLongDescription());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SHORT_URL, videoDTO.getVideoShortUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_LONG_URL, videoDTO.getVideoLongUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_THUMB_URL, videoDTO.getVideoThumbnailUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_STILL_URL, videoDTO.getVideoStillUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_COVER_URL, videoDTO.getVideoCoverUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_WIDE_STILL_URL, videoDTO.getVideoWideStillUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_BADGE_URL, videoDTO.getVideoBadgeUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_DURATION, videoDTO.getVideoDuration());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_TAGS, videoDTO.getVideoTags());
                    if (videoDTO.isVideoIsFavorite())
                        updateExistingVideo.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 1);
                    else
                        updateExistingVideo.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 0);
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_INDEX, videoDTO.getVideoIndex());

                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_NAME, videoDTO.getPlaylistName());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_ID, videoDTO.getPlaylistId());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_THUMB_URL, videoDTO.getPlaylistThumbnailUrl());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_SHORT_DESC, videoDTO.getPlaylistShortDescription());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_LONG_DESC, videoDTO.getPlaylistLongDescription());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_TAGS, videoDTO.getPlaylistTags());
                    updateExistingVideo.put(VideoTable.KEY_PLAYLIST_REFERENCE_ID, videoDTO.getPlaylistReferenceId());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SOCIAL_URL, videoDTO.getVideoSocialUrl());

                    database.update(VideoTable.VIDEO_TABLE, updateExistingVideo, VideoTable.KEY_VIDEO_ID + "=?", new String[]{"" + videoDTO.getVideoId()});
                    checkVideoAvailabilityInOtherPlaylistAndUpdate(videoDTO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void checkVideoAvailabilityInOtherPlaylistAndUpdate(VideoDTO videoDTO){
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            database.enableWriteAheadLogging();
            //Update the video metadata if exists in some other playlist
            if (checkVideoAvailability(videoDTO.getVideoId())) {
                if (isChangeInData(videoDTO)) {
                    //Remove video image assets from Disc Cache
                    File imageFile = ImageLoader.getInstance().getDiscCache().get(videoDTO.getVideoStillUrl());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    imageFile = ImageLoader.getInstance().getDiscCache().get(videoDTO.getVideoThumbnailUrl());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    imageFile = ImageLoader.getInstance().getDiscCache().get(videoDTO.getVideoCoverUrl());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    imageFile = ImageLoader.getInstance().getDiscCache().get(videoDTO.getVideoBadgeUrl());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    imageFile = ImageLoader.getInstance().getDiscCache().get(videoDTO.getVideoWideStillUrl());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    //Remove video image assets from Memory Cache
                    MemoryCacheUtils.removeFromCache(videoDTO.getVideoStillUrl(), ImageLoader.getInstance().getMemoryCache());
                    MemoryCacheUtils.removeFromCache(videoDTO.getVideoThumbnailUrl(), ImageLoader.getInstance().getMemoryCache());
                    MemoryCacheUtils.removeFromCache(videoDTO.getVideoCoverUrl(), ImageLoader.getInstance().getMemoryCache());
                    MemoryCacheUtils.removeFromCache(videoDTO.getVideoBadgeUrl(), ImageLoader.getInstance().getMemoryCache());
                    MemoryCacheUtils.removeFromCache(videoDTO.getVideoWideStillUrl(), ImageLoader.getInstance().getMemoryCache());

                    ContentValues updateExistingVideo = new ContentValues();
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_ID, videoDTO.getVideoId());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_NAME, videoDTO.getVideoName());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SHORT_DESC, videoDTO.getVideoShortDescription());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_LONG_DESC, videoDTO.getVideoLongDescription());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SHORT_URL, videoDTO.getVideoShortUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_LONG_URL, videoDTO.getVideoLongUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_THUMB_URL, videoDTO.getVideoThumbnailUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_STILL_URL, videoDTO.getVideoStillUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_COVER_URL, videoDTO.getVideoCoverUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_WIDE_STILL_URL, videoDTO.getVideoWideStillUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_BADGE_URL, videoDTO.getVideoBadgeUrl());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_DURATION, videoDTO.getVideoDuration());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_TAGS, videoDTO.getVideoTags());
                    updateExistingVideo.put(VideoTable.KEY_VIDEO_SOCIAL_URL, videoDTO.getVideoSocialUrl());
                    if (videoDTO.isVideoIsFavorite())
                        updateExistingVideo.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 1);
                    else
                        updateExistingVideo.put(VideoTable.KEY_VIDEO_IS_FAVORITE, 0);
//                                updateExistingVideo.put(VideoTable.KEY_VIDEO_INDEX, videoDTO.getVideoIndex());
                    database.update(VideoTable.VIDEO_TABLE, updateExistingVideo, VideoTable.KEY_VIDEO_ID + "=?", new String[]{"" + videoDTO.getVideoId()});
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeAllRecords() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.enableWriteAheadLogging();
            db.execSQL("DELETE FROM " + VideoTable.VIDEO_TABLE);
            removeAllTabBannerData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeRecordsByTab(String referenceId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.enableWriteAheadLogging();
            db.execSQL("DELETE FROM " + VideoTable.VIDEO_TABLE + " WHERE " + VideoTable.KEY_PLAYLIST_REFERENCE_ID + " LIKE '" + referenceId + "%'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //***********************************************************************//
    //*******************TAB BANNER METHODS**********************************//
    //***********************************************************************//
    public void insertTabBannerData(TabBannerDTO tabBannerDTO){
        TabBannerTable.getInstance().insertTabBannerData(tabBannerDTO, this.getWritableDatabase());
    }

    public ArrayList<TabBannerDTO> getAllLocalTabBannerData(){
        return TabBannerTable.getInstance().getAllLocalTabBannerData(this.getReadableDatabase());
    }

    public TabBannerDTO getLocalTabBannerDataByBannerId(long bannerId){
        return TabBannerTable.getInstance().getLocalTabBannerDataByBannerId(this.getReadableDatabase(), bannerId);
    }

    public int getTabBannerCount(){
        return TabBannerTable.getInstance().getTabBannerCount(this.getWritableDatabase());
    }

    public TabBannerDTO getLocalTabBannerDataByTabId(long tabId){
        return TabBannerTable.getInstance().getLocalTabBannerDataByTabId(this.getReadableDatabase(), tabId);
    }

    public void updateBannerData(TabBannerDTO tabBannerDTO){
        TabBannerTable.getInstance().updateBannerData(this.getWritableDatabase(), tabBannerDTO);
    }

    public void updateTabData(TabBannerDTO tabBannerDTO){
        TabBannerTable.getInstance().updateTabData(this.getWritableDatabase(), tabBannerDTO);
    }

    public void updateTabBannerData(TabBannerDTO tabBannerDTO){
        TabBannerTable.getInstance().updateTabBannerData(this.getWritableDatabase(), tabBannerDTO);
    }

    public void removeAllTabBannerData(){
        TabBannerTable.getInstance().removeAllTabBannerData(this.getWritableDatabase());
    }

    //***********************************************************************//
    //*********************END***********************************************//
    //***********************************************************************//
}
