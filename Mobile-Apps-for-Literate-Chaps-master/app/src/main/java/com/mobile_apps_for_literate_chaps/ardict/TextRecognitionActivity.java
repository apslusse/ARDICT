package com.mobile_apps_for_literate_chaps.ardict;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.ToggleButton;


import org.json.JSONObject;

import java.util.Deque;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TextRecognitionActivity extends AppCompatActivity {

    // ADVANCED var should either be set in settings (if we decide that functionality)
    // or removed altogether when advancedFilter() is done.
    static boolean ADVANCED = false;
    // BASIC_THRESHOLD var will be set in settings (likely a slider)
    static int BASIC_THRESHOLD = 5;
    // Caches definitions so we don't need to look up the same thing multiple frames in a row
    // TODO: Aaron make sure to check this before any API call and update it immediately after.
    static Hashtable<String, String> CACHED_DEFINITIONS = new Hashtable<String, String>();
    // Records the *50* most recently used words.
    // TODO:  Aaron keep this updated and remove words from CACHED_DEFINITIONS whenever they
    //        leave the LRU
    static ConcurrentLinkedQueue<String> LRU = new ConcurrentLinkedQueue<String>();

    private ArrayList<String> learnedWords;
    private String currentWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();

        Intent intent = getIntent();
        String picturePath = intent.getStringExtra("picturePath");

        setContentView(R.layout.activity_recognize);

        FirebaseVisionImage fimg = getImg(picturePath);

        recognizeText(fimg);
    }

    // Function to link more information button to new activity
    public void moreInfo(View view) {
        Intent intent = new Intent(TextRecognitionActivity.this, MoreInfoActivity.class);
        intent.putExtra("currentWord", currentWord);
        startActivity(intent);
    }

    private FirebaseVisionImage getImg(String pic_path) {
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.tale_typed);
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);
        Bitmap bmp = RotateBitmap(BitmapFactory.decodeFile(pic_path), 90);
        ImageView imageView = (ImageView) findViewById(R.id.recogImg);
        imageView.setImageBitmap(bmp);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);

        return image;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void recognizeText(FirebaseVisionImage image) {

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                processTextBlock(firebaseVisionText);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }

    private void processTextBlock(FirebaseVisionText result) {
        // [START mlkit_process_text_block]
        String resultText = result.getText();
        EditText definitionFieldSingle = (EditText) findViewById(R.id.recogText);
        //definitionFieldSingle.setText(resultText);
        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    // TODO: @Lucthai @allibail draw these rectangles to the screen
                    Rect elementFrame = element.getBoundingBox();

                    Log.d("classifier", elementText);
                    // Need to figure out the interaction between classes.
                    // Do we pass to the filter word here?
                    // Do we also do the API calls here?
                    // Or does this function just get every word and put it in a data structure
                    // and some Main function somewhere else handles all the sub-tasks?
                    if (filterWord(elementText)) {
                        // display the word
                        getWordDefinition(elementText, definitionFieldSingle);
                    } // else not needed
                }
            }
        }
        // [END mlkit_process_text_block]
    }

    public void getWordDefinition(String Word, EditText whereToPut) {
        currentWord = Word;
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

                        definition = Word + ": " + definition;
                        whereToPut.setText(deleteInBrackets(definition));
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
    // Basic Filter functionality
    // Filters based off of word length alone
    // Returns true if word passes filter, false otherwise
    private static boolean basicFilter(String word) {
        return word.length() >= BASIC_THRESHOLD;
    }

    // Advanced Filter functionality
    // Filter functionality still undecided.
    // Idea: Words > certain length AND not in top 500? most common English words
    // Returns true if word passes filter, false otherwise
    private static boolean advancedFilter(String word) {
        // Implement later
        return false;
    }

    public static boolean filterWord(String word) {
        // Dunno if this is correct Java ternary syntax
        return (ADVANCED) ? advancedFilter(word) : basicFilter(word);
    }

    // -----------Learned words functionality---------------
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(learnedWords);
        editor.putString("learned words", json);
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("learned words", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        learnedWords = gson.fromJson(json, type);
        if (learnedWords == null) {
            learnedWords = new ArrayList<String>();
        }
    }

    public void learnedButtonPressed(View view) {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        // User just marked word as learned
        if (currentWord == "") {
            return;
        }
        if (toggle.isChecked()) {
            learnedWords.add(currentWord);
            saveData();
        } else {
            learnedWords.remove(currentWord);
            saveData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(TextRecognitionActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.learnedWords:
                Intent learnedIntent = new Intent(TextRecognitionActivity.this, LearnedWordsActivity.class);
                learnedIntent.putExtra("key", learnedWords);
                startActivity(learnedIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

