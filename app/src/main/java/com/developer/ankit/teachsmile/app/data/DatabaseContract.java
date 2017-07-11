package com.developer.ankit.teachsmile.app.data;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class DatabaseEntry implements BaseColumns {
        public static final String TABLE_NAME = "entries";
        public static final String COLUMN_NAME_IMAGE_NAME = "name";
        public static final String COLUMN_NAME_EMOTION = "emotion";
        public static final String COLUMN_NAME_IMAGE_PATH = "path";
    }

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DatabaseEntry.TABLE_NAME +
            " (" + DatabaseEntry._ID + " INTEGER PRIMARY KEY," + DatabaseEntry.COLUMN_NAME_IMAGE_NAME
            + " TEXT," + DatabaseEntry.COLUMN_NAME_EMOTION + " TEXT," + DatabaseEntry.COLUMN_NAME_IMAGE_PATH
            + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DatabaseEntry.TABLE_NAME;
}
