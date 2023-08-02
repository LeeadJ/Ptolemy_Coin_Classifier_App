package com.example.ptolemycoinclassifier;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

public class MyApp extends Application {

    private Uri capturedImageUri;
    private Bitmap selectedImageBitmap;

    public Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    public void setCapturedImageUri(Uri uri) {
        capturedImageUri = uri;
    }

    public Bitmap getSelectedImageBitmap() {
        return selectedImageBitmap;
    }

    public void setSelectedImageBitmap(Bitmap bitmap) {
        selectedImageBitmap = bitmap;
    }
}

