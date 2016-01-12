package org.vault.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.vault.app.dto.TabBannerDTO;

import java.util.ArrayList;

/**
 * Created by aqeeb.pathan on 24-11-2015.
 */
public class TabBannerTable {
    // --------- Table Name-----------------
    public static final String TAB_BANNER_TABLE = "tab_banner_data";

    //Primary Key Column
    public static final String KEY_ID = "id";

    //Tab Columns
    public static final String KEY_TAB_ID = "tab_id";
    public static final String KEY_TAB_NAME = "tab_name";
    public static final String KEY_TAB_POSITION = "tab_index_position";
    public static final String KEY_TAB_MODIFIED = "tab_modified";
    public static final String KEY_TAB_CREATED = "tab_created";
    public static final String KEY_TAB_DATA_MODIFIED = "tab_data_modified";
    public static final String KEY_TAB_DATA_CREATED = "tab_data_created";
    public static final String KEY_TAB_KEYWORD = "tab_keyword";
    public static final String KEY_TAB_DISPLAY_TYPE = "tab_display_type";

    //Banner Columns
    public static final String KEY_BANNER_ID = "banner_id";
    public static final String KEY_BANNER_NAME = "banner_name";
    public static final String KEY_BANNER_URL = "banner_url";
    public static final String KEY_IS_BANNER_ACTIVATED = "is_banner_activated";
    public static final String KEY_IS_HYPER_LINK_ACTIVATED = "is_hyperlink_activated";
    public static final String KEY_ACTION_URL = "banner_action_url";
    public static final String KEY_BANNER_CREATED = "banner_created";
    public static final String KEY_BANNER_MODIFIED = "banner_modified";

    public static final String CREATE_TAB_BANNER = "CREATE TABLE "
            + TAB_BANNER_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TAB_ID + " INTEGER," + KEY_TAB_NAME
            + " TEXT," + KEY_TAB_POSITION + " INTEGER,"
            + KEY_TAB_CREATED  + " TEXT," + KEY_TAB_MODIFIED + " TEXT,"
            + KEY_TAB_DATA_CREATED + " TEXT," + KEY_TAB_DATA_MODIFIED + " TEXT,"
            + KEY_TAB_KEYWORD + " TEXT," + KEY_TAB_DISPLAY_TYPE + " TEXT,"
            + KEY_BANNER_ID + " INTEGER," + KEY_BANNER_URL + " TEXT," + KEY_BANNER_NAME + " TEXT," + KEY_BANNER_CREATED + " TEXT," + KEY_BANNER_MODIFIED + " TEXT,"
            + KEY_IS_BANNER_ACTIVATED + " INTEGER," + KEY_IS_HYPER_LINK_ACTIVATED + " INTEGER," + KEY_ACTION_URL + " TEXT )";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TAB_BANNER);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TAB_BANNER_TABLE);
        onCreate(database);
    }

    private static TabBannerTable sInstance;

    public static synchronized TabBannerTable getInstance() {
        if (sInstance == null) {
            sInstance = new TabBannerTable();
        }
        return sInstance;
    }

    public void insertTabBannerData(TabBannerDTO tabBannerDTO, SQLiteDatabase database){
        try {
            database.enableWriteAheadLogging();
            ContentValues tabBannerValues = new ContentValues();
            tabBannerValues.put(KEY_TAB_ID, tabBannerDTO.getTabId());
            tabBannerValues.put(KEY_TAB_NAME, tabBannerDTO.getTabName());
            tabBannerValues.put(KEY_TAB_POSITION, tabBannerDTO.getTabIndexPosition());
            tabBannerValues.put(KEY_TAB_CREATED, tabBannerDTO.getTabCreated());
            tabBannerValues.put(KEY_TAB_MODIFIED, tabBannerDTO.getTabModified());
            tabBannerValues.put(KEY_TAB_DATA_CREATED, tabBannerDTO.getTabDataCreated());
            tabBannerValues.put(KEY_TAB_DATA_MODIFIED, tabBannerDTO.getTabDataModified());
            tabBannerValues.put(KEY_TAB_KEYWORD, tabBannerDTO.getTabKeyword());
            tabBannerValues.put(KEY_TAB_DISPLAY_TYPE, tabBannerDTO.getTabDisplayType());

            tabBannerValues.put(KEY_BANNER_ID, tabBannerDTO.getTabBannerId());
            tabBannerValues.put(KEY_BANNER_URL, tabBannerDTO.getBannerURL());
            tabBannerValues.put(KEY_BANNER_NAME, tabBannerDTO.getTabBannerName());
            tabBannerValues.put(KEY_BANNER_CREATED, tabBannerDTO.getBannerCreated());
            tabBannerValues.put(KEY_BANNER_MODIFIED, tabBannerDTO.getBannerModified());
            if(tabBannerDTO.isBannerActive())
                tabBannerValues.put(KEY_IS_BANNER_ACTIVATED, 1);
            else
                tabBannerValues.put(KEY_IS_BANNER_ACTIVATED, 0);
            if(tabBannerDTO.isHyperlinkActive())
                tabBannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 1);
            else
                tabBannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 0);
            tabBannerValues.put(KEY_ACTION_URL, tabBannerDTO.getBannerActionURL());
            database.insert(TAB_BANNER_TABLE, null, tabBannerValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TabBannerDTO getLocalTabBannerDataByBannerId(SQLiteDatabase database, long bannerId){
        try {
            TabBannerDTO tabBannerDTO = null;
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM "+TAB_BANNER_TABLE+" WHERE "+KEY_BANNER_ID+" = "+bannerId;
            Cursor cursor = database.rawQuery(selectQuery, null);
            if(cursor != null)
                if (cursor.moveToFirst()) {
                    do {
                        tabBannerDTO = new TabBannerDTO();
                        tabBannerDTO.setTabId(cursor.getLong(cursor.getColumnIndex(KEY_TAB_ID)));
                        tabBannerDTO.setTabName(cursor.getString(cursor.getColumnIndex(KEY_TAB_NAME)));
                        tabBannerDTO.setTabIndexPosition(cursor.getLong(cursor.getColumnIndex(KEY_TAB_POSITION)));
                        tabBannerDTO.setTabCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_CREATED))));
                        tabBannerDTO.setTabModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_MODIFIED))));
                        tabBannerDTO.setTabDataCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_CREATED))));
                        tabBannerDTO.setTabDataModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_MODIFIED))));
                        tabBannerDTO.setTabKeyword(cursor.getString(cursor.getColumnIndex(KEY_TAB_KEYWORD)));
                        tabBannerDTO.setTabDisplayType(cursor.getString(cursor.getColumnIndex(KEY_TAB_DISPLAY_TYPE)));


                        tabBannerDTO.setTabBannerId(cursor.getLong(cursor.getColumnIndex(KEY_BANNER_ID)));
                        tabBannerDTO.setTabBannerName(cursor.getString(cursor.getColumnIndex(KEY_BANNER_NAME)));
                        tabBannerDTO.setBannerCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_CREATED))));
                        tabBannerDTO.setBannerModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_MODIFIED))));
                        tabBannerDTO.setBannerURL(cursor.getString(cursor.getColumnIndex(KEY_BANNER_URL)));
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_BANNER_ACTIVATED)) == 1)
                            tabBannerDTO.setIsBannerActive(true);
                        else
                            tabBannerDTO.setIsBannerActive(false);
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_HYPER_LINK_ACTIVATED)) == 1)
                            tabBannerDTO.setIsHyperlinkActive(true);
                        else
                            tabBannerDTO.setIsHyperlinkActive(false);

                        tabBannerDTO.setBannerActionURL(cursor.getString(cursor.getColumnIndex(KEY_ACTION_URL)));
                    }while (cursor.moveToNext());
                }

            cursor.close();
            return tabBannerDTO;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public TabBannerDTO getLocalTabBannerDataByTabId(SQLiteDatabase database, long tabId){
        try {
            TabBannerDTO tabBannerDTO = null;
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM "+TAB_BANNER_TABLE+" WHERE "+KEY_TAB_ID+" = "+tabId;
            Cursor cursor = database.rawQuery(selectQuery, null);
            if(cursor != null)
                if (cursor.moveToFirst()) {
                    do {
                        tabBannerDTO = new TabBannerDTO();
                        tabBannerDTO.setTabId(cursor.getLong(cursor.getColumnIndex(KEY_TAB_ID)));
                        tabBannerDTO.setTabName(cursor.getString(cursor.getColumnIndex(KEY_TAB_NAME)));
                        tabBannerDTO.setTabIndexPosition(cursor.getLong(cursor.getColumnIndex(KEY_TAB_POSITION)));
                        tabBannerDTO.setTabCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_CREATED))));
                        tabBannerDTO.setTabModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_MODIFIED))));
                        tabBannerDTO.setTabDataCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_CREATED))));
                        tabBannerDTO.setTabDataModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_MODIFIED))));
                        tabBannerDTO.setTabKeyword(cursor.getString(cursor.getColumnIndex(KEY_TAB_KEYWORD)));
                        tabBannerDTO.setTabDisplayType(cursor.getString(cursor.getColumnIndex(KEY_TAB_DISPLAY_TYPE)));


                        tabBannerDTO.setTabBannerId(cursor.getLong(cursor.getColumnIndex(KEY_BANNER_ID)));
                        tabBannerDTO.setTabBannerName(cursor.getString(cursor.getColumnIndex(KEY_BANNER_NAME)));
                        tabBannerDTO.setBannerCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_CREATED))));
                        tabBannerDTO.setBannerModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_MODIFIED))));
                        tabBannerDTO.setBannerURL(cursor.getString(cursor.getColumnIndex(KEY_BANNER_URL)));
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_BANNER_ACTIVATED)) == 1)
                            tabBannerDTO.setIsBannerActive(true);
                        else
                            tabBannerDTO.setIsBannerActive(false);
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_HYPER_LINK_ACTIVATED)) == 1)
                            tabBannerDTO.setIsHyperlinkActive(true);
                        else
                            tabBannerDTO.setIsHyperlinkActive(false);

                        tabBannerDTO.setBannerActionURL(cursor.getString(cursor.getColumnIndex(KEY_ACTION_URL)));
                    }while (cursor.moveToNext());
                }

            cursor.close();
            return tabBannerDTO;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<TabBannerDTO> getAllLocalTabBannerData(SQLiteDatabase database){
        try {
            ArrayList<TabBannerDTO> listTabBannerDTO = new ArrayList<>();
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM "+TAB_BANNER_TABLE;
            Cursor cursor = database.rawQuery(selectQuery, null);
            TabBannerDTO tabBannerDTO = null;
            if(cursor != null)
                if (cursor.moveToFirst()) {
                    do {
                        tabBannerDTO = new TabBannerDTO();
                        tabBannerDTO.setTabId(cursor.getLong(cursor.getColumnIndex(KEY_TAB_ID)));
                        tabBannerDTO.setTabName(cursor.getString(cursor.getColumnIndex(KEY_TAB_NAME)));
                        tabBannerDTO.setTabIndexPosition(cursor.getLong(cursor.getColumnIndex(KEY_TAB_POSITION)));
                        tabBannerDTO.setTabCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_CREATED))));
                        tabBannerDTO.setTabModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_MODIFIED))));
                        tabBannerDTO.setTabDataCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_CREATED))));
                        tabBannerDTO.setTabDataModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_DATA_MODIFIED))));
                        tabBannerDTO.setTabKeyword(cursor.getString(cursor.getColumnIndex(KEY_TAB_KEYWORD)));
                        tabBannerDTO.setTabDisplayType(cursor.getString(cursor.getColumnIndex(KEY_TAB_DISPLAY_TYPE)));


                        tabBannerDTO.setTabBannerId(cursor.getLong(cursor.getColumnIndex(KEY_BANNER_ID)));
                        tabBannerDTO.setTabBannerName(cursor.getString(cursor.getColumnIndex(KEY_BANNER_NAME)));
                        tabBannerDTO.setBannerCreated(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_CREATED))));
                        tabBannerDTO.setBannerModified(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_MODIFIED))));
                        tabBannerDTO.setBannerURL(cursor.getString(cursor.getColumnIndex(KEY_BANNER_URL)));
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_BANNER_ACTIVATED)) == 1)
                            tabBannerDTO.setIsBannerActive(true);
                        else
                            tabBannerDTO.setIsBannerActive(false);
                        if(cursor.getInt(cursor.getColumnIndex(KEY_IS_HYPER_LINK_ACTIVATED)) == 1)
                            tabBannerDTO.setIsHyperlinkActive(true);
                        else
                            tabBannerDTO.setIsHyperlinkActive(false);

                        tabBannerDTO.setBannerActionURL(cursor.getString(cursor.getColumnIndex(KEY_ACTION_URL)));
                        listTabBannerDTO.add(tabBannerDTO);
                    }while (cursor.moveToNext());
                }

            cursor.close();
            return listTabBannerDTO;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTabBannerCount(SQLiteDatabase database) {
        int count = 0 ;
        try {
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM "+TAB_BANNER_TABLE;
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

    public void updateBannerData(SQLiteDatabase database, TabBannerDTO tabBannerDTO){
        try {
            database.enableWriteAheadLogging();
            ContentValues bannerValues = new ContentValues();
            bannerValues.put(KEY_BANNER_ID, tabBannerDTO.getTabBannerId());
            bannerValues.put(KEY_BANNER_URL, tabBannerDTO.getBannerURL());
            if(tabBannerDTO.isBannerActive())
                bannerValues.put(KEY_IS_BANNER_ACTIVATED, 1);
            else
                bannerValues.put(KEY_IS_BANNER_ACTIVATED, 0);
            if(tabBannerDTO.isHyperlinkActive())
                bannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 1);
            else
                bannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 0);
            bannerValues.put(KEY_ACTION_URL, tabBannerDTO.getBannerActionURL());
            bannerValues.put(KEY_BANNER_CREATED, tabBannerDTO.getBannerCreated());
            bannerValues.put(KEY_BANNER_MODIFIED, tabBannerDTO.getBannerModified());
            bannerValues.put(KEY_BANNER_NAME, tabBannerDTO.getTabBannerName());

            database.update(TAB_BANNER_TABLE, bannerValues, KEY_TAB_ID + "=?", new String[]{"" + tabBannerDTO.getTabId()});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateTabData(SQLiteDatabase database, TabBannerDTO tabBannerDTO){
        try {
            database.enableWriteAheadLogging();
            ContentValues tabValues = new ContentValues();
            tabValues.put(KEY_TAB_ID, tabBannerDTO.getTabId());
            tabValues.put(KEY_TAB_NAME, tabBannerDTO.getTabName());
            tabValues.put(KEY_TAB_POSITION, tabBannerDTO.getTabIndexPosition());
            tabValues.put(KEY_TAB_CREATED, tabBannerDTO.getTabCreated());
            tabValues.put(KEY_TAB_MODIFIED, tabBannerDTO.getTabModified());
            tabValues.put(KEY_TAB_DATA_CREATED, tabBannerDTO.getTabDataCreated());
            tabValues.put(KEY_TAB_DATA_MODIFIED, tabBannerDTO.getTabDataModified());
            tabValues.put(KEY_TAB_KEYWORD, tabBannerDTO.getTabKeyword());
            tabValues.put(KEY_TAB_DISPLAY_TYPE, tabBannerDTO.getTabDisplayType());

            database.update(TAB_BANNER_TABLE, tabValues, KEY_TAB_ID + "=?", new String[]{"" + tabBannerDTO.getTabId()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTabBannerData(SQLiteDatabase database, TabBannerDTO tabBannerDTO){
        try {
            database.enableWriteAheadLogging();
            ContentValues tabBannerValues = new ContentValues();
            tabBannerValues.put(KEY_TAB_ID, tabBannerDTO.getTabId());
            tabBannerValues.put(KEY_TAB_NAME, tabBannerDTO.getTabName());
            tabBannerValues.put(KEY_TAB_POSITION, tabBannerDTO.getTabIndexPosition());
            tabBannerValues.put(KEY_TAB_CREATED, tabBannerDTO.getTabCreated());
            tabBannerValues.put(KEY_TAB_MODIFIED, tabBannerDTO.getTabModified());
            tabBannerValues.put(KEY_TAB_DATA_CREATED, tabBannerDTO.getTabDataCreated());
            tabBannerValues.put(KEY_TAB_DATA_MODIFIED, tabBannerDTO.getTabDataModified());
            tabBannerValues.put(KEY_TAB_KEYWORD, tabBannerDTO.getTabKeyword());
            tabBannerValues.put(KEY_TAB_DISPLAY_TYPE, tabBannerDTO.getTabDisplayType());

            tabBannerValues.put(KEY_BANNER_ID, tabBannerDTO.getTabBannerId());
            tabBannerValues.put(KEY_BANNER_URL, tabBannerDTO.getBannerURL());
            tabBannerValues.put(KEY_BANNER_NAME, tabBannerDTO.getTabBannerName());
            tabBannerValues.put(KEY_BANNER_CREATED, tabBannerDTO.getBannerCreated());
            tabBannerValues.put(KEY_BANNER_MODIFIED, tabBannerDTO.getBannerModified());
            if(tabBannerDTO.isBannerActive())
                tabBannerValues.put(KEY_IS_BANNER_ACTIVATED, 1);
            else
                tabBannerValues.put(KEY_IS_BANNER_ACTIVATED, 0);
            if(tabBannerDTO.isHyperlinkActive())
                tabBannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 1);
            else
                tabBannerValues.put(KEY_IS_HYPER_LINK_ACTIVATED, 0);
            tabBannerValues.put(KEY_ACTION_URL, tabBannerDTO.getBannerActionURL());

            database.update(TAB_BANNER_TABLE, tabBannerValues, KEY_TAB_ID + "=?", new String[]{"" + tabBannerDTO.getTabId()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAllTabBannerData(SQLiteDatabase database){
        try {
            database.enableWriteAheadLogging();
            database.execSQL("DELETE FROM " + TAB_BANNER_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
