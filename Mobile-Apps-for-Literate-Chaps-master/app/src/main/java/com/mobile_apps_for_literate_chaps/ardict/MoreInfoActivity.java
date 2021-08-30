package com.mobile_apps_for_literate_chaps.ardict;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

class WordInformation {
    public String definition = "";
    public String type = "";
}

public class MoreInfoActivity extends AppCompatActivity {

    private ArrayList<WordInformation> moreInformation = new ArrayList<>();
    private String currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        currentWord = getIntent().getStringExtra("currentWord");
        final TextView wv = findViewById(R.id.wordView);
        wv.setText(currentWord);
        getWordDefinitions(currentWord);
    }

    public void updateList() {
        final ListView lv = findViewById(R.id.wordList);
        ArrayList<String> categories_list = new ArrayList<String>();
        for (int i = 0; i < moreInformation.size(); ++i) {
            String temp = (i + 1) + ". (" + moreInformation.get(i).type + ") ";
            temp += moreInformation.get(i).definition + ".";
            categories_list.add(temp);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, categories_list);
        lv.setAdapter(arrayAdapter);
    }

    public void getWordDefinitions(String word) {
        String url = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" + word + "?key=f07d71e6-7423-4070-a84c-b9e6408f2de4";
        if (moreInformation.size() > 0) {
            moreInformation.clear();
        }
        RequestQueue requestQueue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Converting to JSON
                        JsonArray jsonObject = new JsonParser().parse(response).getAsJsonArray();
                        for (int i = 0; i < jsonObject.size(); ++i) {
                            WordInformation wordObj = new WordInformation();
                            JsonArray tempArray = jsonObject.get(i).getAsJsonObject().get("shortdef").getAsJsonArray();
                            if (tempArray.size() > 0) {
                                wordObj.definition = tempArray.get(0).getAsString();
                            } else {
                                continue;
                            }
                            if (jsonObject.get(i).getAsJsonObject().has("fl")) {
                                wordObj.type = jsonObject.get(i).getAsJsonObject().get("fl").getAsString();
                            }
                            else {
                                wordObj.type = "Unknown";
                            }
                            moreInformation.add(wordObj);
                        }
                        updateList();
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                requestQueue.stop();
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }
}
