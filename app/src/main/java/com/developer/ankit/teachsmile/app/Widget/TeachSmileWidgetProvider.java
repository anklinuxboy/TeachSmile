package com.developer.ankit.teachsmile.app.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.developer.ankit.teachsmile.R;

public class TeachSmileWidgetProvider extends AppWidgetProvider {

    public static final String TOAST_ACTION = "toast_action";
    public static final String EXTRA_ITEM = "extra_item";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, TeachSmileWidgetProvider.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.appwidget_layout);

            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.widget_list, intent);


        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
