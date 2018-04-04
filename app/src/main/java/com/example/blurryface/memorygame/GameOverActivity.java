package com.example.blurryface.memorygame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.blurryface.memorygame.MainActivity.hiScore;

public class GameOverActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    int high;
    TextView highScoreText,playerScoreText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        //initialise UI
        highScoreText = (TextView) findViewById(R.id.yourHighScoretext);
        playerScoreText = (TextView) findViewById(R.id.yourScoreText);
        sharedPreferences = getSharedPreferences("score",MODE_PRIVATE);
        high = sharedPreferences.getInt("highScore",hiScore);
        highScoreText.setText(String.valueOf(high));
        String playerScore=getIntent().getStringExtra("scoress");
        playerScoreText.setText(playerScore);
    }
    //replay the game
    public void onReplay(View view){
        Intent intent = new Intent(GameOverActivity.this,GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    //quit the game and take user to the main page
    public void onQuit(View view){
        Intent intent = new Intent(GameOverActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
