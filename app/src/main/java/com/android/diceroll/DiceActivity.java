package com.android.diceroll;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.Random;

public class DiceActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {
    private SensorManager sm = null;
    private MediaRecorder mRecorder;
    private Handler mHandler;
    private ImageView image;
    private Button buttonStart;
    private boolean gameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dice);
        image = findViewById(R.id.imgDe);
        mHandler = new Handler();
        buttonStart = (Button) findViewById(R.id.startButton);

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
            boolean talked = false;
            Double amp = 190 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
            if(amp<0){
                amp = 0.0;
            }
            if(amp >100){
                talked = true;
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

                int res = (int) ((Math.abs(x) +Math.abs(y) + Math.abs(z)) % 6) + 1;
                changeDiceWithValue(res);
            }
        }
    }

    private void changeDiceWithValue(int value) {
        switch(value) {
            case 1:
                image.setImageResource(R.drawable.one);
                break;
            case 2:
                image.setImageResource(R.drawable.two);
                break;
            case 3:
                image.setImageResource(R.drawable.three);
                break;
            case 4:
                image.setImageResource(R.drawable.four);
                break;
            case 5:
                image.setImageResource(R.drawable.five);
                break;
            default:
                image.setImageResource(R.drawable.six);
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

    public int faceRandom() {
        int max = 6;
        int min = 1;
        Random rand = new Random();
        int nombreAleatoire = rand.nextInt(max - min + 1) + min;

        return nombreAleatoire;
    }

    public void startOrStopGame(View view) {
        gameStarted = !gameStarted;

        if(gameStarted) {
            buttonStart.setText("Sotp");
        } else {
            buttonStart.setText("Start");
        }

    }
}
