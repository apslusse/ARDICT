package com.mobile_apps_for_literate_chaps.ardict;

import android.content.Context;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;

public class VisionImage extends AppCompatActivity {
    public FirebaseVisionImage imageFromPath(Context context, Uri uri) {
        // [START image_from_path]
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
            image = null;
        }
        return image;
        // [END image_from_path]
    }
}