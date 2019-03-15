package com.android.diceroll;

import android.Manifest;
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
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {
    private SensorManager sm = null;
    TextView viewAccel1;
    TextView viewAccel2;
    TextView viewAccel3;
    TextView viewAccel4;
    private MediaRecorder mRecorder;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        mHandler = new Handler();


        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        viewAccel1 = (TextView) findViewById(R.id.textViewAccel1);
        viewAccel2 = (TextView) findViewById(R.id.textViewAccel2);
        viewAccel3 = (TextView) findViewById(R.id.textViewAccel3);
        viewAccel4 = (TextView) findViewById(R.id.textViewAccel4);


        View view = findViewById(R.id.mainLinearLayout);
        view.setOnTouchListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
            //location = androidLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            //location = androidLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


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
        TextView b = findViewById(R.id.textView11);
        b.setText("Touched "+ posx + " " + posy);
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
            TextView b = findViewById(R.id.textView10);
            if(talked){
                b.setText("PARLÉ"+ amp);

            }else{
                b.setText("pas parlé "+ amp);

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

                viewAccel1.setText("x " + x);
                viewAccel2.setText("y " + y);
                viewAccel3.setText("z " + z);
                viewAccel4.setText("y + y + z " + (x+y+z));
            }
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
}
