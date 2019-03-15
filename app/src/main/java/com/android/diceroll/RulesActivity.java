package com.android.diceroll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
    }
    public void goToRules(View view){
        Intent homeGame = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(homeGame);
    }
}
