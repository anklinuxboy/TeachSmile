package com.developer.ankit.teachsmile.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TeachSmileContentProvider extends ContentProvider {

    private static final int ITEM_LIST = 1;
    private static final int ITEM_ID = 2;
    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(DatabaseContract.AUTHORITY, "images", ITEM_LIST);
        URI_MATCHER.addURI(DatabaseContract.AUTHORITY, "images/#", ITEM_ID);
    }

    private TeachSmileDBHelper helper = null;

    @Override
    public boolean onCreate() {
        helper = new TeachSmileDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ITEM_LIST:
                return DatabaseContract.DatabaseEntry.CONTENT_TYPE;
            case ITEM_ID:
                return DatabaseContract.DatabaseEntry.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if (URI_MATCHER.match(uri) != ITEM_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        if (URI_MATCHER.match(uri) == ITEM_LIST) {
            long id = db.insert();
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
