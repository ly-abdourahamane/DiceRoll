package com.android.diceroll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void goToRules(View view){
        Intent rulesGame = new Intent(getApplicationContext(), RulesActivity.class);
        startActivity(rulesGame);
    }
    public void goToUnlocks(View view){
        Intent unlockGame = new Intent(getApplicationContext(), AchievementsActivity.class);
        startActivity(unlockGame);
    }
    public void goToPlay(View view){
        Intent diceGame = new Intent(getApplicationContext(), DiceActivity.class);
        startActivity(diceGame);
    }
    public void goToRScores(View view){
        Intent scoresGame = new Intent(getApplicationContext(), ScoresActivity.class);
        startActivity(scoresGame);
    }
}
