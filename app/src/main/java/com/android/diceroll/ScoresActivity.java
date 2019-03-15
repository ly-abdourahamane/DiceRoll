package com.android.diceroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        SharedPreferences sharedPref =
                getApplicationContext().getSharedPreferences("TheFileName",Context.MODE_PRIVATE);
        int musicState = sharedPref.getInt("score", 0);
        TextView t = (TextView) findViewById(R.id.scoreTextView);
        t.setText(""+musicState);

        /* SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("music on", false);
            editor.apply();*/

    }
}
