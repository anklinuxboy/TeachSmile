package com.developer.ankit.teachsmile.app;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ankit on 6/17/17.
 */

public class Utils {

    public static String EXTERNAL_DIR_NAME = "TeachSmile";
    public static String PATH_TO_DIR = Environment.getExternalStorageDirectory() + File.separator + EXTERNAL_DIR_NAME;

    private static String getFileName() {
        String EXTENSION = ".png";
        String HEADER = "IMG";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd_HH:mm:ss", Locale.US);
        String date = df.format(c.getTime());
        return (HEADER+date+EXTENSION);
    }

    public static File getFile() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), EXTERNAL_DIR_NAME);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = getFileName();
        File screenShotFile = new File(folder, fileName);

        return screenShotFile;
    }
}
