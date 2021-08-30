package com.mobile_apps_for_literate_chaps.ardict;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import android.content.Context;
import android.graphics.Canvas;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ARDictAPIService extends AppCompatActivity {

    public static final ARDictAPIService instance = new ARDictAPIService();

    public void getWordDefinition(String Word, EditText whereToPut) {
        String url = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" + Word + "?key=f07d71e6-7423-4070-a84c-b9e6408f2de4";
        RequestQueue queue = Volley.newRequestQueue(this);
        // EditText definitionFieldSingle = (EditText) findViewById(R.id.recogText);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("****************RESPONSE ***************");
                        System.out.println(response);

                        // Converting to JSON
                        JsonArray jsonObject = new JsonParser().parse(response).getAsJsonArray();
                        String definition = jsonObject.get(0).getAsJsonObject()
                                .get("def").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("sseq").getAsJsonArray()
                                .get(0).getAsJsonArray()
                                .get(0).getAsJsonArray()
                                .get(1).getAsJsonObject()
                                .get("dt").getAsJsonArray()
                                .get(0).getAsJsonArray()
                                .get(1).getAsString();

                        definition = "Def: " + definition;

                        whereToPut.setText(deleteInBrackets(definition));
//                        if (filterWord(elementText)) {
//                            // Word passes through filter
//                            // TODO: Change to post the definition
//                            //
//                            definitionFieldSingle.setText(elementText);
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public static String deleteInBrackets(String str) {
        return str.replaceAll("\\{.*?\\}","");
    }

    public void getMedicalDefinition(String Word) {

        String url = "https://www.dictionaryapi.com/api/v3/references/medical/json/" + Word + "?key=f98fd8ba-7058-4aa7-b2fd-7bd87d8cd02f";
        JsonObjectRequest getRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                    }             }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("FAIL");
            }
        });

    }

}

