package com.developer.ankit.teachsmile.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case ITEM_LIST:
                builder.setTables(DatabaseContract.DatabaseEntry.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = DatabaseContract.DatabaseEntry.SORT_ORDER;
                }
                break;
            case ITEM_ID:
                builder.setTables(DatabaseContract.DatabaseEntry.TABLE_NAME);
                builder.appendWhere(DatabaseContract.DatabaseEntry._ID + " = " +
                uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
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
            long id = db.insert(DatabaseContract.DatabaseEntry.TABLE_NAME, null, contentValues);
            return getUriForId(id, uri);
        }
        return null;
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }

        throw new SQLException("Problem while inserting into uri " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int delCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case ITEM_LIST:
                delCount = db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case ITEM_ID:
                String id = uri.getLastPathSegment();
                String where = DatabaseContract.DatabaseEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME,
                        where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int updateRows = 0;
        switch (URI_MATCHER.match(uri)) {
            case ITEM_LIST:
                updateRows = db.update(DatabaseContract.DatabaseEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            case ITEM_ID:
                String id = uri.getLastPathSegment();
                String where = DatabaseContract.DatabaseEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }

                updateRows = db.update(DatabaseContract.DatabaseEntry.TABLE_NAME,
                        contentValues,
                        where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateRows;
    }
}
