package com.developer.ankit.teachsmile.app.images;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.developer.ankit.teachsmile.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import timber.log.Timber;

/**
 * Created by ankit on 7/16/17.
 */

public class ImageViewerAdapter extends RecyclerView.Adapter<ImageViewerAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;

    public ImageViewerAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public Cursor swapCursor(Cursor cursor) {
        if (this.cursor == cursor) {
            return null;
        }

        Cursor oldCursor = this.cursor;
        this.cursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }

        return oldCursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View imageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_item_list,
                parent, false);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String imagePath = cursor.getString(0);
        Timber.d("image path " + imagePath);

        Picasso.with(holder.imageEmotion.getContext())
                .load("file://" + imagePath)
                .fit()
                .centerCrop()
                .rotate(-90)
                .into(holder.imageEmotion);
    }

    @Override
    public int getItemCount() {
        return (cursor == null ? 0 : cursor.getCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageEmotion;

        public ViewHolder(View itemView) {
            super(itemView);

            imageEmotion = (ImageView) itemView.findViewById(R.id.image_emotion);
        }
    }
}
