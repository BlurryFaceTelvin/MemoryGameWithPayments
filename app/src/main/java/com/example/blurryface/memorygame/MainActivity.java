package com.example.blurryface.memorygame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    public static int hiScore;
    TextView highScoretextView;
    int defaultScore=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        highScoretextView = (TextView)findViewById(R.id.highScoreTxt);
        sharedPreferences = getSharedPreferences("score",MODE_PRIVATE);
        hiScore = sharedPreferences.getInt("highScore",defaultScore);
        highScoretextView.setText(String.valueOf(hiScore));
    }
    public void playGame(View view)
    {
        //begin playing the game
        Intent intent = new Intent(this,GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
