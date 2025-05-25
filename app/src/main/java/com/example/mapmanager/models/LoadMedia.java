package com.example.mapmanager.models;

import android.net.Uri;

public class LoadMedia {
    Uri uri;
    double progress;
    public LoadMedia() {}

    public LoadMedia(Uri uri, double progress) {
        this.uri = uri;
        this.progress = progress;
    }

    public int getProgress() {
        return (int) progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
