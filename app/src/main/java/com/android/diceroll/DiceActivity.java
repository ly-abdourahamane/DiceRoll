package com.android.diceroll;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class DiceActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {
    // components
    private SensorManager sm = null;
    private MediaRecorder mRecorder;
    private Button buttonStart;
    private Handler mHandler;
    private ImageView image;
    private TextView scoreText;

    private Point size = new Point();

    // constants
    private static final int TIMEOUT = 2000;
    private static final int TICKS_PER_SECOND = 25;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    private static final int MAX_FRAMESKIP = 5;

    private float MULTIPLIER = 11;

    private static final String SOUND_URL = "raw/shake_dice.mp3";

    // booleans
    private boolean gameStarted = false;
    private boolean objectiveDone = false;

    // states
    private State state = State.STOP;
    private State diceReturnState =State.DOWN;

    // score
    private int score = 0;
    private MediaPlayer mSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dice);
        image = findViewById(R.id.imgDe);
        mHandler = new Handler();
        buttonStart = (Button) findViewById(R.id.startButton);
        scoreText = findViewById(R.id.scoreTextView);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);


        View view = findViewById(R.id.diceMainLayout);
        view.setOnTouchListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    DiceActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        } else {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSleepTask.run();

        }

        getWindowManager().getDefaultDisplay().getSize(size);
    }
    private Runnable mSleepTask = new Runnable() {
        public void run() {

            mRecorder.start();
            mHandler.postDelayed(eventSound, 250);
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        objectiveDone = true;
        return false;
    }

    private Runnable eventSound = new Runnable() {
        public void run() {
            Double amp = 190 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
            if(amp < 0){
                amp = 0.0;
            } else if(amp > 100 && state == State.SOUND){
                objectiveDone = true;
            }

            mHandler.postDelayed(eventSound, 250);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mRecorder.setOutputFile("/dev/null");
                    try {
                        mRecorder.prepare();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mSleepTask.run();
                } else {
                    //User denied Permission.
                }
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor = event.sensor.getType();
        float [] values = event.values;

        synchronized (this) {
            if (sensor == Sensor.TYPE_ACCELEROMETER) {
                float x = values[0];
                float y = values[1];
                float z = values[2];

                if (state == State.DICEROLL) {
                    // lancer de dés
                    int res = (int) ((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 6) + 1;

                    gotoLocation(-x, y);
                    changeDiceWithValue(res);
                } else {
                    // tourner vers une direction
                    switch (state) {
                        case UP:
                            Log.d("DEBUG MVT", y+"");
                            objectiveDone = (y <= -5);
                            break;
                        case DOWN:
                            Log.d("DEBUG MVT", y+"");
                            objectiveDone = (y >= 5);
                            break;
                        case RIGHT:
                            Log.d("DEBUG MVT", x+"");
                            objectiveDone = (x >= 5);
                            break;
                        case LEFT:
                            Log.d("DEBUG MVT", x+"");
                            objectiveDone = (x <= -5);
                            break;
                        default: //rien
                            break;
                    }
                }
            }
        }
    }



    private Handler shakeDiceHandler;
    private Handler gameWaitHandler;
    Timer t;

    /**
     * Partie
     */
    private void launchGame() {
        shakeDiceHandler = new Handler();
        gameWaitHandler = new Handler();

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            Log.d("ROULER LES DES", state.name());
            state = State.DICEROLL;
            // lance le dé pour 2 secondes
            playGameSound(null);
            shakeDiceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                state = diceReturnState;
                Log.d("MINIJEU", state.name());
                // temps imparti avant échec du jeu
                gameWaitHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TIME UP", state.name());
                        if (!objectiveDone) {
                            Log.d("PERDU, FIN JEU", state.name());
                            Toast.makeText(DiceActivity.this, "Vous avez perdu, jouez encore" +
                                    "", Toast.LENGTH_LONG).show();
                            stopGame();
                            t.cancel();
                        }
                    }
                }, 3*TIMEOUT/2);
                if (objectiveDone) {
                    Log.d("TROUVE", state.name());
                    Toast.makeText(DiceActivity.this, "Bravo vous avez trouvé", Toast.LENGTH_LONG).show();
                    gameWaitHandler.removeCallbacksAndMessages(null);
                    score++;
                    updateScoreDisplay();
                    objectiveDone = false;
                }
                }
            }, TIMEOUT);

            Log.d("FIN TOUR", state.name());
            updateScoreDisplay();
            if (!gameStarted) {
                t.cancel();
            }
            }
        }, 0, TIMEOUT * 3);
    }


    /**
     * Attend
     */
    private void waitThis() {
        double next_tick = System.currentTimeMillis();
        int loops = 0;
        while (System.currentTimeMillis() > next_tick*2
                && loops < MAX_FRAMESKIP) {
            next_tick += SKIP_TICKS;
            loops++;
        }
    }

    /**
     * Va à la position x,y (sans dépasser de l'écran)
     *
     * @param x
     * @param y
     */
    private void gotoLocation(float x, float y) {
        waitThis();
        changeImageLocation(x, y);
    }

    /**
     * change la position de l'image
     *
     * @param x
     * @param y
     */
    private void changeImageLocation(float x, float y) {
        float vx = x * MULTIPLIER + image.getX();
        float vy = y * MULTIPLIER + image.getY();
        if (vx < 0) {
            vx = 0;
        } else if (vx >= size.x - image.getWidth()) {
            vx = size.x - image.getWidth();
        }

        if (vy < 0) {
            vy = 0;
        } else if (vy >= size.y - image.getHeight()) {
            vy = size.y - image.getHeight();
        }

        image.setX(vx);
        image.setY(vy);
    }

    /**
     * Change l'image du dé (avec valeurs entre 1 et 6)
     *
     * @param value
     */
    private void changeDiceWithValue(int value) {
        switch(value) {
            case 1:
                image.setImageResource(R.drawable.onesound);
                diceReturnState = State.SOUND;
                break;
            case 2:
                image.setImageResource(R.drawable.twotouch);
                diceReturnState = State.TOUCH;
                break;
            case 3:
                image.setImageResource(R.drawable.upthree);
                diceReturnState = State.UP;
                break;
            case 4:
                image.setImageResource(R.drawable.rightfour);
                diceReturnState = State.RIGHT;
                break;
            case 5:
                image.setImageResource(R.drawable.downfive);
                diceReturnState = State.DOWN;
                break;
            default:
                image.setImageResource(R.drawable.leftsix);
                diceReturnState = State.LEFT;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * démarre/arrête le jeu
     *
     * @param view
     */
    public void startOrStopGame(View view) {
        if(!gameStarted) {
            gameStarted = true;
            buttonStart.setText(TextConstants.STOP);
            launchGame();
        } else {
            stopGame();
            t.cancel();
        }


    }

    /**
     * Arrête le jeu
     */
    private void stopGame() {
        gameStarted = false;
        buttonStart.setText(TextConstants.START);
        state = State.STOP;
        SharedPreferences sharedPref =
                getApplicationContext().getSharedPreferences("TheFileName", Context.MODE_PRIVATE);

        int musicState = sharedPref.getInt("score", 0);
        if(musicState > score){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("score", musicState);
            editor.apply();
        }else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("score", score);
            editor.apply();
        }



    }

    /**
     * Màj affichage du score
     */
    private void updateScoreDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreText.setText(TextConstants.SCORE + score);
            }
        });

    }

    public void playGameSound(View view) {
        mSound = MediaPlayer.create(getApplicationContext(), R.raw.shake_dice);
        mSound.start();
        mSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            };
        });
    }
}
