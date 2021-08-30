package com.mobile_apps_for_literate_chaps.ardict;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LearnedWordsActivity extends AppCompatActivity {

    private ArrayList<String> learnedWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learned_words);

        // Retrieve learnedWords array from TextRecognitionActivity
        learnedWords = getIntent().getStringArrayListExtra("key");

        // Testing purposes
//        for (int i = 0; i < 50; ++i) {
//            learnedWords.add(Integer.toString(i));
//        }

//        final ListView lv = findViewById(R.id.learnedList);
//        final List<String> categories_list = learnedWords;
//        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_list_item_1, categories_list);
//        lv.setAdapter(arrayAdapter);

        MyCustomAdapter adapter = new MyCustomAdapter(learnedWords, this);
        ListView lView = findViewById(R.id.learnedList);
        lView.setAdapter(adapter);




    }
}
