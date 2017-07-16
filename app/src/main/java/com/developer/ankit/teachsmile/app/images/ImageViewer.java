package com.developer.ankit.teachsmile.app.images;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.developer.ankit.teachsmile.R;
import com.developer.ankit.teachsmile.app.data.DatabaseContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankit on 7/15/17.
 */

public class ImageViewer extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.image_list)
    RecyclerView imageList;

    private int LOADER_ID = 0;
    private ImageViewerAdapter adapter;

    public static Intent startImageViewerActivity(Context context) {
        return new Intent(context, ImageViewer.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);
        imageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageViewerAdapter(this, null);
        imageList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseContract.CONTENT_URI, new String[]{DatabaseContract.DatabaseEntry.COLUMN_NAME_IMAGE_PATH},
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
