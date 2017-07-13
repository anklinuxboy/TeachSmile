package com.developer.ankit.teachsmile.app.CameraScreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.support.annotation.NonNull;

import com.affectiva.android.affdex.sdk.Frame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

public class ImageSaver {

    private ImageSaver() {}

    public static Bitmap getBitmapFromFrame(@NonNull final Frame frame) {
        Bitmap bitmap = null;

        if (frame instanceof Frame.BitmapFrame) {
            bitmap = ((Frame.BitmapFrame) frame).getBitmap();
            Timber.d("get bitmap from frame");
        } else {
            switch (frame.getColorFormat()) {
                case RGBA:
                    bitmap = getBitmapFromRGBAFrame(frame);
                    break;
                case YUV_NV21:
                    bitmap = getBitmapFromYuvFrame(frame);
            }
        }

        return bitmap;
    }

    private static Bitmap getBitmapFromYuvFrame(Frame frame) {
        byte[] pixels = ((Frame.ByteArrayFrame) frame).getByteArray();
        YuvImage yuvImage = new YuvImage(pixels, ImageFormat.NV21, frame.getWidth(), frame.getHeight(), null);
        return convertYuvImageToBitmap(yuvImage);
    }

    public static Bitmap convertYuvImageToBitmap(@NonNull final YuvImage yuvImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            Timber.e("Exception while closing output stream", e);
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static Bitmap getBitmapFromRGBAFrame(Frame frame) {
        byte[] pixels = ((Frame.ByteArrayFrame) frame).getByteArray();
        Bitmap bitmap = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));
        return bitmap;
    }

    public static void saveBitmapToFile(Bitmap finalScreenshot, File cameraFile) throws IOException {
        try {
            FileOutputStream outputStream = new FileOutputStream(cameraFile);
            finalScreenshot.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            finalScreenshot.recycle();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new FileNotFoundException("Unable to save bitmap");
        }
    }


}
