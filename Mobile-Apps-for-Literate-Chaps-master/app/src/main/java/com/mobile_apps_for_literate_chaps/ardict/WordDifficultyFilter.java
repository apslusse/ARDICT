package com.mobile_apps_for_literate_chaps.ardict;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class WordDifficultyFilter extends AppCompatActivity {
    // ADVANCED var should either be set in settings (if we decide that functionality)
    // or removed altogether when advancedFilter() is done.
    static boolean ADVANCED = false;
    // BASIC_THRESHOLD var will be set in settings (likely a slider)
    static int BASIC_THRESHOLD = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Basic Filter functionality
    // Filters based off of word length alone
    // Returns true if word passes filter, false otherwise
    private static boolean basicFilter(String word) {
        return word.length() > BASIC_THRESHOLD;
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

}
