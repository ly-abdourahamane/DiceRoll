package com.android.diceroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        SharedPreferences sharedPref =
                getApplicationContext().getSharedPreferences("TheFileName", Context.MODE_PRIVATE);
        int musicState = sharedPref.getInt("score", 0);
        if (musicState > 0){
            CheckBox c1 = findViewById(R.id.checkBox1);
            c1.setChecked(true);
        }
        if (musicState >= 5){
            CheckBox c1 = findViewById(R.id.checkBox2);
            c1.setChecked(true);

        }
        if (musicState >= 10){
            CheckBox c1 = findViewById(R.id.checkBox3);
            c1.setChecked(true);

        }
        if (musicState >= 15){
            CheckBox c1 = findViewById(R.id.checkBox4);
            c1.setChecked(true);

        }
        if (musicState >= 20){
            CheckBox c1 = findViewById(R.id.checkBox5);
            c1.setChecked(true);

        }
    }
}
