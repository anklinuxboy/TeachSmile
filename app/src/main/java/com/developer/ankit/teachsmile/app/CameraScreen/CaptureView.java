package com.developer.ankit.teachsmile.app.CameraScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Process;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ankit on 7/10/17.
 */

public class CaptureView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private CaptureThread captureThread;
    private CaptureThreadListener listener;

    public CaptureView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(this);

        captureThread = new CaptureThread(surfaceHolder, listener);
    }

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (captureThread.isStopped()) {
            captureThread = new CaptureThread(holder, listener);
        }

        captureThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        captureThread.stopThread();
        while (retry) {
            try {
                captureThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestBitmap() {
        if (listener == null)
            return;
        if (captureThread == null || captureThread.isStopped())
            return;
        captureThread.requestCapture = true;
    }

    public void setEventListener(CaptureThreadListener listener) {
        this.listener = listener;

        if (listener != null) {
            captureThread.setEventListener(listener);
        }
    }

    interface CaptureThreadListener {
        void onBitmapGenerated(Bitmap bitmap);
    }

    class CaptureThread extends Thread {
        private final SurfaceHolder holder;
        private CaptureThreadListener listener;
        private volatile boolean stopFlag = false;
        private volatile boolean requestCapture = false;

        public CaptureThread(SurfaceHolder holder, CaptureThreadListener listener) {
            this.holder = holder;
            this.listener = listener;
        }

        public void stopThread() {
            stopFlag = true;
        }

        public boolean isStopped() {
            return stopFlag;
        }

        public void setEventListener(CaptureThreadListener listener) {
            this.listener = listener;


        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            while (!stopFlag) {
                Canvas c = null;
                Bitmap screenshotBitmap = null;
                try {
                    c = holder.lockCanvas();

                    if (requestCapture) {
                        Rect surfaceBound = holder.getSurfaceFrame();
                        screenshotBitmap = Bitmap.createBitmap(surfaceBound.width(),
                                surfaceBound.height(), Bitmap.Config.ARGB_8888);
                        requestCapture = false;
                    }

                } finally {
                    if (screenshotBitmap != null && listener != null) {
                        listener.onBitmapGenerated(Bitmap.createBitmap(screenshotBitmap));
                        screenshotBitmap.recycle();
                    }
                }
            }
        }
    }
}
