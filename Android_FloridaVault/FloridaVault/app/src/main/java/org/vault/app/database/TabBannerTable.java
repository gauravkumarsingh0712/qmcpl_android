package org.vault.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.vault.app.dto.TabBannerDTO;

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
    public static final String KEY_TAB_POSITION = "tab_position";
    public static final String KEY_TAB_LAST_UPDATE = "tab_last_update";

    //Banner Columns
    public static final String KEY_BANNER_ID = "banner_id";
    public static final String KEY_BANNER_URL = "banner_url";
    public static final String KEY_IS_BANNER_ACTIVATED = "is_banner_activated";
    public static final String KEY_IS_HYPER_LINK_AVAILABLE = "is_hyperlink_available";
    public static final String KEY_ACTION_URL = "action_url";
    public static final String KEY_BANNER_LAST_UPDATE = "banner_last_update";

    public static final String CREATE_TAB_BANNER = "CREATE TABLE "
            + TAB_BANNER_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TAB_ID + " INTEGER," + KEY_TAB_NAME
            + " TEXT," + KEY_TAB_POSITION + " INTEGER," + KEY_TAB_LAST_UPDATE
            + " TEXT," + KEY_BANNER_ID + " INTEGER," + KEY_BANNER_URL + " TEXT,"
            + KEY_IS_BANNER_ACTIVATED + " INTEGER," + KEY_IS_HYPER_LINK_AVAILABLE + " INTEGER," + KEY_ACTION_URL + " TEXT," + KEY_BANNER_LAST_UPDATE + " TEXT )";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TAB_BANNER);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + CREATE_TAB_BANNER);
        onCreate(database);
    }

    private static TabBannerTable sInstance;

    public static synchronized TabBannerTable getInstance() {
        if (sInstance == null) {
            sInstance = new TabBannerTable();
        }
        return sInstance;
    }

    public void insertTabBannerData(TabBannerDTO tabBannerDTO, SQLiteDatabase database) {
        try {
            database.enableWriteAheadLogging();
            ContentValues tabBannerValues = new ContentValues();
            tabBannerValues.put(KEY_TAB_ID, tabBannerDTO.getTabId());
            tabBannerValues.put(KEY_TAB_NAME, tabBannerDTO.getTabName());
            tabBannerValues.put(KEY_TAB_POSITION, tabBannerDTO.getTabPosition());
            tabBannerValues.put(KEY_TAB_LAST_UPDATE, tabBannerDTO.getLastServerTabUpdate());
            tabBannerValues.put(KEY_BANNER_ID, tabBannerDTO.getBannerId());
            tabBannerValues.put(KEY_BANNER_URL, tabBannerDTO.getBannerUrl());
            tabBannerValues.put(KEY_IS_BANNER_ACTIVATED, tabBannerDTO.isBannerActivated());
            tabBannerValues.put(KEY_IS_HYPER_LINK_AVAILABLE, tabBannerDTO.isHyperLinkAvailable());
            tabBannerValues.put(KEY_ACTION_URL, tabBannerDTO.getActionUrl());
            tabBannerValues.put(KEY_BANNER_LAST_UPDATE, tabBannerDTO.getLastBannerUpdate());
            database.insert(TAB_BANNER_TABLE, null, tabBannerValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TabBannerDTO getTabBannerDataById(SQLiteDatabase database, long bannerId) {
        try {
            TabBannerDTO tabBannerDTO = null;
            database.enableWriteAheadLogging();
            String selectQuery = "SELECT * FROM " + TAB_BANNER_TABLE + " WHERE " + KEY_BANNER_ID + " = " + bannerId;
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor != null)
                if (cursor.moveToFirst()) {
                    do {
                        tabBannerDTO = new TabBannerDTO();
                        tabBannerDTO.setTabId(cursor.getLong(cursor.getColumnIndex(KEY_TAB_ID)));
                        tabBannerDTO.setTabName(cursor.getString(cursor.getColumnIndex(KEY_TAB_NAME)));
                        tabBannerDTO.setTabPosition(cursor.getLong(cursor.getColumnIndex(KEY_TAB_POSITION)));
                        tabBannerDTO.setLastServerTabUpdate(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_TAB_LAST_UPDATE))));
                        tabBannerDTO.setBannerId(cursor.getLong(cursor.getColumnIndex(KEY_BANNER_ID)));
                        tabBannerDTO.setBannerUrl(cursor.getString(cursor.getColumnIndex(KEY_BANNER_URL)));
                        if (cursor.getInt(cursor.getColumnIndex(KEY_IS_BANNER_ACTIVATED)) == 1)
                            tabBannerDTO.setIsBannerActivated(true);
                        else
                            tabBannerDTO.setIsBannerActivated(false);
                        if (cursor.getInt(cursor.getColumnIndex(KEY_IS_HYPER_LINK_AVAILABLE)) == 1)
                            tabBannerDTO.setIsHyperLinkAvailable(true);
                        else
                            tabBannerDTO.setIsHyperLinkAvailable(false);

                        tabBannerDTO.setLastBannerUpdate(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_BANNER_LAST_UPDATE))));
                        tabBannerDTO.setActionUrl(cursor.getString(cursor.getColumnIndex(KEY_ACTION_URL)));
                    } while (cursor.moveToNext());
                }

            cursor.close();
            return tabBannerDTO;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateBannerData(SQLiteDatabase database, TabBannerDTO tabBannerDTO) {
        try {
            database.enableWriteAheadLogging();
            ContentValues bannerValues = new ContentValues();
            bannerValues.put(KEY_BANNER_ID, tabBannerDTO.getBannerId());
            bannerValues.put(KEY_BANNER_URL, tabBannerDTO.getBannerUrl());
            bannerValues.put(KEY_IS_BANNER_ACTIVATED, tabBannerDTO.isBannerActivated());
            bannerValues.put(KEY_IS_HYPER_LINK_AVAILABLE, tabBannerDTO.isHyperLinkAvailable());
            bannerValues.put(KEY_ACTION_URL, tabBannerDTO.getActionUrl());
            bannerValues.put(KEY_BANNER_LAST_UPDATE, tabBannerDTO.getLastBannerUpdate());

            database.update(TAB_BANNER_TABLE, bannerValues, KEY_BANNER_ID + "=?", new String[]{"" + tabBannerDTO.getBannerId()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTabData(SQLiteDatabase database, TabBannerDTO tabBannerDTO) {
        try {
            database.enableWriteAheadLogging();
            ContentValues tabValues = new ContentValues();
            tabValues.put(KEY_TAB_ID, tabBannerDTO.getTabId());
            tabValues.put(KEY_TAB_NAME, tabBannerDTO.getTabName());
            tabValues.put(KEY_TAB_POSITION, tabBannerDTO.getTabPosition());
            tabValues.put(KEY_TAB_LAST_UPDATE, tabBannerDTO.getLastServerTabUpdate());

            database.update(TAB_BANNER_TABLE, tabValues, KEY_BANNER_ID + "=?", new String[]{"" + tabBannerDTO.getBannerId()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAllTabBannerData(SQLiteDatabase database) {
        try {
            database.enableWriteAheadLogging();
            database.execSQL("DELETE FROM " + TAB_BANNER_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
