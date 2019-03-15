package com.android.diceroll;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

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

    // booleans
    private boolean gameStarted = false;
    private boolean objectiveDone = false;

    // states
    private State state = State.STOP;
    private State diceReturnState =State.DOWN;

    // score
    private int score = 0;

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

        float posx = event.getX();
        float posy = event.getY();
        int res = (int) ((Math.abs(posx) +Math.abs(posy)) % 6) + 1;
        changeDiceWithValue(res);
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
                            objectiveDone = (y >= 1);
                            break;
                        case DOWN:
                            objectiveDone = (y <= -1);
                            break;
                        case RIGHT:
                            objectiveDone = (x >= 1);
                            break;
                        case LEFT:
                            objectiveDone = (x <= -1);
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

    private Thread diceLaunchThread;

    /**
     * Partie
     */
    private void launchGame() {
        while (gameStarted) {
            state = State.DICEROLL;
            shakeDiceHandler = new Handler();
            // lance le dé pour 2 secondes
            shakeDiceHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    state = diceReturnState;
                    gameWaitHandler = new Handler();
                    // temps imparti avant échec du jeu
                    gameWaitHandler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            stopGame();
                        }
                    }, System.currentTimeMillis() + TIMEOUT);
                    if (objectiveDone) {
                        // si objectif réussi alors annuler le timeout
                        gameWaitHandler.removeCallbacksAndMessages(null);
                        objectiveDone = false;
                    }
                }
            }, System.currentTimeMillis() + TIMEOUT);

            updateScoreDisplay();

            // attente
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            buttonStart.setText(TextConstants.START);
            launchGame();
        } else {
            gameStarted = false;
            stopGame();
        }


    }

    /**
     * Arrête le jeu
     */
    private void stopGame() {
        buttonStart.setText(TextConstants.STOP);
        state = State.STOP;
    }

    /**
     * Màj affichage du score
     */
    private void updateScoreDisplay() {
        scoreText.setText(TextConstants.SCORE + score);
    }
}
