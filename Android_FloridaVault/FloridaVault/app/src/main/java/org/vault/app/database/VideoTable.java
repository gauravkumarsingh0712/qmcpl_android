package org.vault.app.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by aqeeb.pathan on 24-06-2015.
 */
public class VideoTable {

    // --------- Table Name-----------------
    public static final String VIDEO_TABLE = "video_data";
    public static final String BANNER_TABLE = "banner_data";

    //------------ Video Table Columns ----------------
    public static final String KEY_ID = "id";
    public static final String KEY_VIDEO_ID = "video_id";
    public static final String KEY_VIDEO_NAME = "video_name";
    public static final String KEY_VIDEO_SHORT_DESC = "video_short_desc";
    public static final String KEY_VIDEO_LONG_DESC = "video_long_desc";
    public static final String KEY_VIDEO_SHORT_URL = "video_short_url";
    public static final String KEY_VIDEO_LONG_URL = "video_long_url";
    public static final String KEY_VIDEO_THUMB_URL = "video_thumbnail_url";
    public static final String KEY_VIDEO_STILL_URL = "video_still_url";
    public static final String KEY_VIDEO_COVER_URL = "video_cover_url";
    public static final String KEY_VIDEO_WIDE_STILL_URL = "video_wide_still_url";
    public static final String KEY_VIDEO_BADGE_URL = "video_badge_url";
    public static final String KEY_VIDEO_DURATION = "video_duration";
    public static final String KEY_VIDEO_TAGS = "video_tags";
    public static final String KEY_VIDEO_IS_FAVORITE = "video_is_favorite";
    public static final String KEY_VIDEO_INDEX = "video_index";
    public static final String KEY_PLAYLIST_NAME = "playlist_name";
    public static final String KEY_PLAYLIST_ID = "playlist_id";
    public static final String KEY_PLAYLIST_THUMB_URL = "playlist_thumbnail_url";
    public static final String KEY_PLAYLIST_SHORT_DESC = "playlist_short_desc";
    public static final String KEY_PLAYLIST_LONG_DESC = "playlist_long_desc";
    public static final String KEY_PLAYLIST_TAGS = "playlist_tags";
    public static final String KEY_PLAYLIST_REFERENCE_ID = "playlist_reference_id";
    public static final String KEY_VIDEO_SOCIAL_URL = "video_social_url";

    public static final String CREATE_VIDEO_TABLE = "CREATE TABLE "
            + VIDEO_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_VIDEO_ID + " INTEGER," + KEY_VIDEO_NAME
            + " TEXT," + KEY_VIDEO_SHORT_DESC + " TEXT," + KEY_VIDEO_LONG_DESC
            + " TEXT," + KEY_VIDEO_SHORT_URL + " TEXT," + KEY_VIDEO_LONG_URL + " TEXT,"
            + KEY_VIDEO_THUMB_URL + " TEXT," + KEY_VIDEO_STILL_URL + " TEXT," + KEY_VIDEO_COVER_URL + " TEXT," + KEY_VIDEO_WIDE_STILL_URL + " TEXT," + KEY_VIDEO_BADGE_URL + " TEXT," + KEY_VIDEO_DURATION
            + " INTEGER," + KEY_VIDEO_TAGS + " TEXT," + KEY_VIDEO_IS_FAVORITE + " INTEGER," + KEY_VIDEO_INDEX + " INTEGER,"
            + KEY_PLAYLIST_NAME + " TEXT," + KEY_PLAYLIST_ID + " INTEGER," + KEY_PLAYLIST_THUMB_URL
            + " TEXT," + KEY_PLAYLIST_SHORT_DESC + " TEXT," + KEY_PLAYLIST_LONG_DESC + " TEXT," + KEY_PLAYLIST_TAGS + " TEXT," + KEY_PLAYLIST_REFERENCE_ID + " TEXT," + KEY_VIDEO_SOCIAL_URL + " TEXT" + ")";

    // ------ Raw queries-----------

    //Query to get count of videos in videoTable
    public static final String selectAllVideos = "SELECT * FROM "
            + VIDEO_TABLE ;

    // Query to select featured video from database-----------
    public static final String selectOKFFeaturedQuery = "SELECT * FROM "
            + VIDEO_TABLE + " WHERE " + KEY_PLAYLIST_REFERENCE_ID
            + " LIKE 'OKFFeatured%'";

    // Query to select opponents video from database-----------
    public static final String selectOKFOpponentQuery = "SELECT * FROM "
            + VIDEO_TABLE + " WHERE " + KEY_PLAYLIST_REFERENCE_ID
            + " LIKE 'OKFOpponent%'";

    // ------Query to select CoachesEra video from database-----------
    public static final String selectOKFCoachesEraQuery = "SELECT * FROM "
            + VIDEO_TABLE + " WHERE " + KEY_PLAYLIST_REFERENCE_ID
            + " LIKE 'OKFCoach%'";

    // ------Query to select Player video from database-----------
    public static final String selectOKFPlayerQuery = "SELECT * FROM " + VIDEO_TABLE
            + " WHERE " + KEY_PLAYLIST_REFERENCE_ID + " LIKE 'OKFPlayer%'";


    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_VIDEO_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + VIDEO_TABLE);
        onCreate(database);
    }

    public static String getQueryByTab(String tabIdentifier){
        return "SELECT * FROM "
                + VIDEO_TABLE + " WHERE " + KEY_PLAYLIST_REFERENCE_ID
                + " LIKE '"+tabIdentifier+"%'";
    }
}
