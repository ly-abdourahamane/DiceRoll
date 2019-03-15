package com.android.diceroll;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sm = null;
    TextView viewAccel1;
    TextView viewAccel2;
    TextView viewAccel3;
    TextView viewAccel4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        viewAccel1 = (TextView) findViewById(R.id.textViewAccel1);
        viewAccel2 = (TextView) findViewById(R.id.textViewAccel2);
        viewAccel3 = (TextView) findViewById(R.id.textViewAccel3);
        viewAccel4 = (TextView) findViewById(R.id.textViewAccel4);
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
        Sensor mMagneticField = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
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
