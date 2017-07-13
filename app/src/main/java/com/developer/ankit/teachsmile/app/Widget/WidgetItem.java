package com.developer.ankit.teachsmile.app.Widget;

/**
 *
 */

public class WidgetItem {

    private String emotion;
    private int photosSaved;

    public WidgetItem(String emotion, int emotionPhotosSaved) {
        this.emotion = emotion;
        this.photosSaved = emotionPhotosSaved;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public int getPhotosSaved() {
        return photosSaved;
    }

    public void setPhotosSaved(int photosSaved) {
        this.photosSaved = photosSaved;
    }
}
