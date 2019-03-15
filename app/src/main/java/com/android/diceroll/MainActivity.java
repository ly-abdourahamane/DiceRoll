package com.android.diceroll;

import android.Manifest;
import android.content.pm.PackageManager;
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

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private MediaRecorder mRecorder;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mHandler = new Handler();

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
            mHandler.postDelayed(eventSound, 1000);
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
            Double amp = 190 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
            TextView b = findViewById(R.id.textView10);
            b.setText("Sound "+ amp  );


            mHandler.postDelayed(eventSound, 1000);
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
}
