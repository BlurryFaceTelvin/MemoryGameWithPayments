package com.example.blurryface.memorygame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    float leftVolume = 1.0f;
    float rightVolume = 1.0f;
    int priority = 0;
    int loop=0;
    float rate = 1.0f;
    //Sound
    private SoundPool soundPool;
    private int sample1,sample2,sample3,sample4;
    //UI
    Animation wobble;
    TextView textScore,textDifficulty,textWatchGo;
    Button button1,button2,button3,button4;
    //Game Logic
    int difficultLevel = 1;
    int[] sequenceToCopy = new int[100];
    boolean playSequence = false,isResponding;
    int elementToPlay = 0,playerResponses,playerScore,highScore;
    Handler myHandler;

    //Storage
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //storage
        prefs = getSharedPreferences("score", Context.MODE_PRIVATE);
        highScore =prefs.getInt("highScore",0);

        //sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sample1 = soundPool.load(this,R.raw.sample1,0);
        sample2 = soundPool.load(this,R.raw.sample2,0);
        sample3 = soundPool.load(this,R.raw.sample3,0);
        sample4 = soundPool.load(this,R.raw.sample4,0);
        //ui
        textScore = (TextView)findViewById(R.id.textScore);
        textDifficulty = (TextView)findViewById(R.id.levelText);
        textWatchGo = (TextView)findViewById(R.id.textWatchGo);
        wobble = AnimationUtils.loadAnimation(this,R.anim.wobble);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);
        myHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(playSequence)
                {
                    switch(sequenceToCopy[elementToPlay])
                    {
                        case 1:
                            button1.startAnimation(wobble);
                            soundPool.play(sample1,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 2:
                            button2.startAnimation(wobble);
                            soundPool.play(sample2,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 3:
                            button3.startAnimation(wobble);
                            soundPool.play(sample3,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 4:
                            button4.startAnimation(wobble);
                            soundPool.play(sample4,leftVolume,rightVolume,priority,loop,rate);
                            break;
                    }
                    elementToPlay++;

                    if(elementToPlay==difficultLevel)
                    {
                        sequenceFinished();
                    }
                }
                myHandler.sendEmptyMessageDelayed(0,900);
            }
        };
        myHandler.sendEmptyMessage(0);
    }
    private void playASequence()
    {
        Random rand = new Random();
        int random;
        for (int i=0;i<difficultLevel;i++)
        {
            random = 1+rand.nextInt(4);
            sequenceToCopy[i] = random;
        }
        //reset state
        isResponding = false;
        elementToPlay = 0;
        playerResponses = 0;
        textWatchGo.setText("Watch");
        playSequence = true;
    }
    public void checkElement(int element)
    {
        if(isResponding)
        {
            playerResponses++;
            if(sequenceToCopy[playerResponses-1]==element)
            {
                //correct
                playerScore += ((element+1)*2);
                textScore.setText("Score"+playerScore);
                if(playerResponses==difficultLevel)
                {
                    isResponding=false;
                    difficultLevel++;
                    textDifficulty.setText("Level"+difficultLevel);
                    textScore.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playASequence();
                        }
                    },1000);

                }
            }
            else
            {
                textWatchGo.setText("Failed");
                isResponding=false;
                if(playerScore>highScore)
                {
                    highScore =playerScore;
                    prefs.edit().putInt("highScore",highScore).apply();
                    Toast.makeText(getApplicationContext(),"New Hi- Score",Toast.LENGTH_LONG).show();

                }
                difficultLevel =1;
            }
        }
    }
    public void buttonClick(View view)
    {
        if(!playSequence)
        {
            view.startAnimation(wobble);
            switch (view.getId())
            {
                case R.id.button1:
                    soundPool.play(sample1,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(1);
                    break;
                case R.id.button2:
                    soundPool.play(sample2,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(2);
                    break;
                case R.id.button3:
                    soundPool.play(sample3,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(3);
                    break;
                case R.id.button4:
                    soundPool.play(sample4,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(4);
                    break;
                case R.id.replayBtn:
                    if(!playSequence)
                    {
                        playerScore = 0;
                        textScore.setText("Score: "+playerScore);
                        playASequence();
                    }
                    break;
            }

        }
    }
    public void sequenceFinished()
    {
        playSequence = false;
        textWatchGo.setText("Go!");
        isResponding = true;
    }




}
