package com.developer.ankit.teachsmile.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ankit on 6/17/17.
 */

public class Utils {
    public static String getFileName() {
        String EXTENSION = ".jpg";
        String HEADER = "IMG";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd_HH:mm:ss", Locale.US);
        String date = df.format(c.getTime());
        return (HEADER+date+EXTENSION);
    }
}
