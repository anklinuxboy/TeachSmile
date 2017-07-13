package com.developer.ankit.teachsmile.app.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.developer.ankit.teachsmile.R;

import java.util.ArrayList;
import java.util.List;

public class TeachSmileWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private int appWidgetID;
        private List<WidgetItem> widgetItemList = new ArrayList<>();

        ListRemoteFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
            widgetItemList.clear();
        }

        @Override
        public int getCount() {
            return widgetItemList.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget_item_layout);
            rv.setTextViewText(R.id.emotion_setting, widgetItemList.get(i).getEmotion());
            rv.setTextViewText(R.id.emotion_pics_saved,
                    Integer.toString(widgetItemList.get(i).getPhotosSaved()));

            Bundle extras = new Bundle();
            extras.putInt(TeachSmileWidgetProvider.EXTRA_ITEM, i);
            Intent fillIntent = new Intent();
            fillIntent.putExtras(extras);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
