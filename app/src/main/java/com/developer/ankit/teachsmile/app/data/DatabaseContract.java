package com.developer.ankit.teachsmile.app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class DatabaseEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(DatabaseContract.CONTENT_URI,
                "images");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "photo";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "photo";

        public static final String TABLE_NAME = "entries";
        public static final String COLUMN_NAME_IMAGE_NAME = "name";
        public static final String COLUMN_NAME_EMOTION = "emotion";
        public static final String COLUMN_NAME_IMAGE_PATH = "path";

        public static final String[] PROJECTION_ALL = {_ID, COLUMN_NAME_IMAGE_NAME, COLUMN_NAME_EMOTION,
                COLUMN_NAME_IMAGE_PATH};

        public static final String SORT_ORDER = COLUMN_NAME_IMAGE_NAME + " ASC";
    }

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DatabaseEntry.TABLE_NAME +
            " (" + DatabaseEntry._ID + " INTEGER PRIMARY KEY," + DatabaseEntry.COLUMN_NAME_IMAGE_NAME
            + " TEXT," + DatabaseEntry.COLUMN_NAME_EMOTION + " TEXT," + DatabaseEntry.COLUMN_NAME_IMAGE_PATH
            + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DatabaseEntry.TABLE_NAME;

    public static final String AUTHORITY = "com.developer.ankit.teachsmile.app.data";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
}