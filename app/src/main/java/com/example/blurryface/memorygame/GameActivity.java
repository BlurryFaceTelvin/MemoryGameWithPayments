package com.example.blurryface.memorygame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.models.payment.checkout.CheckoutResponse;
import com.africastalking.models.payment.checkout.MobileCheckoutRequest;
import com.africastalking.services.PaymentService;
import com.africastalking.utils.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class GameActivity extends AppCompatActivity {
    //specify the volume of our sounds and the rate at which they play
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

    //AfricasTalking Payments
    PaymentService paymentService;
    //progress dialog
    SpotsDialog gamePayDialog;
    OkHttpClient client;
    Request request;
    int status;
    boolean onFirstResume;
    SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //initialisation
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
                            button1.setBackgroundColor(Color.rgb(237,240,25));
                            button1.startAnimation(wobble);
                            soundPool.play(sample1,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 2:
                            button2.setBackgroundColor(Color.rgb(192,25,240));
                            button2.startAnimation(wobble);
                            soundPool.play(sample2,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 3:
                            button3.setBackgroundColor(Color.rgb(203,33,61));
                            button3.startAnimation(wobble);
                            soundPool.play(sample3,leftVolume,rightVolume,priority,loop,rate);
                            break;
                        case 4:
                            button4.setBackgroundColor(Color.rgb(25,240,217));
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
        //initialise progress dialog
        gamePayDialog = new SpotsDialog(this,"LOADING");
        dialog = new SpotsDialog(this,"Processing");
        //AfricasTalking
        try {
            AfricasTalking.initialize("192.168.137.80",35897,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        //set our status to 0 to mean first resume
        onFirstResume = true;
        status = 0;

    }
    //play a sequence based on our level of difficulty
    private void playASequence()
    {
        //random generator
        Random rand = new Random();
        int random;
        for (int i=0;i<difficultLevel;i++)
        {
            //range of 1 to 4
            random = 1+rand.nextInt(4);
            sequenceToCopy[i] = random;
        }
        //reset state
        //have a delay of 1 second before resetting
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isResponding = false;
                elementToPlay = 0;
                playerResponses = 0;
                textWatchGo.setText(R.string.watch);
                playSequence = true;
                button1.setBackgroundColor(Color.WHITE);
                button2.setBackgroundColor(Color.WHITE);
                button3.setBackgroundColor(Color.WHITE);
                button4.setBackgroundColor(Color.WHITE);
            }
        }, 1000);

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
                textScore.setText(String.valueOf(playerScore));
                if(playerResponses==difficultLevel)
                {
                    isResponding=false;
                    difficultLevel++;
                    textDifficulty.setText(String.valueOf(difficultLevel));
                    textScore.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playASequence();
                        }
                    },500);

                }
            }
            else
            {
                textWatchGo.setText(R.string.failed);
                //have a pop up for payments and replay a sequence
                gamePayDialog.show();
                final AlertDialog.Builder paymentDialog = new AlertDialog.Builder(GameActivity.this);
                paymentDialog.setMessage("You failed, Would you like to pay for another trial");
                paymentDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gamePayDialog.dismiss();
                        isResponding=false;
                        difficultLevel=1;
                        if(playerScore>highScore)
                        {
                            highScore =playerScore;
                            prefs.edit().putInt("highScore",highScore).apply();

                        }
                        difficultLevel =1;
                        //send to game over Screen
                        Intent intent = new Intent(GameActivity.this,GameOverActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("scoress",String.valueOf(playerScore));
                        startActivity(intent);
                    }
                });
                paymentDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //when user says yes we have the checkout
                        new Paying().execute();
                        status=5;

                    }
                });
                AlertDialog alertDialog = paymentDialog.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

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
                    button1.setBackgroundColor(Color.rgb(237,240,25));
                    soundPool.play(sample1,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(1);
                    break;
                case R.id.button2:
                    button2.setBackgroundColor(Color.rgb(192,25,240));
                    soundPool.play(sample2,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(2);
                    break;
                case R.id.button3:
                    button3.setBackgroundColor(Color.rgb(203,33,61));
                    soundPool.play(sample3,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(3);
                    break;
                case R.id.button4:
                    button4.setBackgroundColor(Color.rgb(25,240,217));
                    soundPool.play(sample4,leftVolume,rightVolume,priority,loop,rate);
                    checkElement(4);
                    break;
                case R.id.replayBtn:
                    if(!playSequence)
                    {
                        playerScore = 0;
                        textScore.setText(String.valueOf(playerScore));
                        playASequence();
                    }
                    break;
            }

        }
    }
    public void sequenceFinished()
    {
        Handler handler =  new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playSequence = false;
                textWatchGo.setText(R.string.go);
                isResponding = true;
                button1.setBackgroundColor(Color.WHITE);
                button2.setBackgroundColor(Color.WHITE);
                button3.setBackgroundColor(Color.WHITE);
                button4.setBackgroundColor(Color.WHITE);
            }
        }, 1000);


    }

    public class Paying extends AsyncTask<Void,String,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                paymentService = AfricasTalking.getPaymentService();
                MobileCheckoutRequest checkoutRequest = new MobileCheckoutRequest("MusicApp","KES 10","0703280748");
                paymentService.checkout(checkoutRequest, new Callback<CheckoutResponse>() {
                    @Override
                    public void onSuccess(CheckoutResponse data) {
                        gamePayDialog.dismiss();
                        Toast.makeText(GameActivity.this,data.status,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        gamePayDialog.dismiss();
                        Log.e("err",throwable.getMessage());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //when user first gets to the activity
        if(onFirstResume){
            onFirstResume = false;
            Log.e("resume",String.valueOf(status));
        }else if(!onFirstResume&&status==5) {
            //after mpesa pop up
            status = 3;
            Log.e("resume",String.valueOf(status));

            dialog.show();
            //wait for ten seconds to confirm
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmPayment();
                }
            }, 10000);
        }else{
            Log.e("resume","normal");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(status==5){
            //pause by the checkout
            status = 5;
            Log.e("pause",String.valueOf(status));
        }
        else {
            //normal pause
            status=3;
            Log.e("pause",String.valueOf(status));
        }
    }
    public void confirmPayment(){
        client = new OkHttpClient();
        request = new Request.Builder().url("http://192.168.137.80:30001/transaction/status").build();
        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dialog.dismiss();
                Log.e("failure",e.getMessage());
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                dialog.dismiss();
                String status = response.body().string();
                //if user either cancels or has insufficient funds we go to game over
                if(status.equals("Failed")){
                    //if it fails to pay sends you to game over page
                    showMessage("failed");
                    Intent intent = new Intent(GameActivity.this,GameOverActivity.class);
                    intent.putExtra("scoress",String.valueOf(playerScore));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }else if(status.equals("Success")){
                    //if successful add the time and player gets another chance to continue
                    showMessage("successful");
                    //if statement and if the payment is successful payment is done


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //if statement and if the payment is successful payment is done
                            playASequence();
                        }
                    });

                }

            }
        });
    }
    public void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GameActivity.this,"Your payment has "+message,Toast.LENGTH_LONG).show();
            }
        });
    }

}
