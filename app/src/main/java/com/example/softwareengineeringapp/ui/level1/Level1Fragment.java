package com.example.softwareengineeringapp.ui.level1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.softwareengineeringapp.MainActivity;
import com.example.softwareengineeringapp.R;

import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Level1Fragment extends Fragment implements SensorEventListener {

    private Level1ViewModel level1ViewModel;

    private SensorManager sensorManager;
    private Sensor rotation;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private int startOrientation = 0;
    private int degreeToFollow = 0;
    private Button startLevel1;
    private Button stopLevel1;
    private TextView gyro;
    private TextView gyroTest;
    private Thread moveForwardThread;
    private Thread rotateLeftThread;
    private Thread rotateRightThread;

    class Sharing {
        public volatile int currentDegree = 0;
        public volatile int startingDegree = 0;
        public volatile TachoMotor motorLeft;
        public volatile  TachoMotor motorRight;
        public volatile int arrivingDegree = 0;
        public volatile boolean rotating = false;

        public void setMotors(TachoMotor motorLeft, TachoMotor motorRight){
            Log.i("MYLOG MOTORS", motorLeft+"");
            this.motorLeft = motorLeft;
            this.motorRight = motorRight;
        }
    }

    final Sharing sharedElements = new Sharing();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);

        startLevel1 = root.findViewById(R.id.start_level1_button);
        stopLevel1 = root.findViewById(R.id.stop_level1_button);
        gyro = root.findViewById(R.id.gyro_level1);
        gyroTest = root.findViewById(R.id.gyro_level1_test);

        sharedElements.setMotors(MainActivity.motorLeft, MainActivity.motorRight);
        Log.i("MYLOG MAINACTIVITY", MainActivity.motorLeft+"");

        rotateLeftThread = new Thread(this::rotateLeft);

        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        startLevel1.setOnClickListener((view) -> {
            if(rotation != null){
                sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
            }
            rotateLeftThread.start();
        });

        // Blocca i motori
        stopLevel1.setOnClickListener((view) -> {
            try {
                sharedElements.motorLeft.stop();
                sharedElements.motorRight.stop();
                sharedElements.rotating = false;
            }catch (Exception e){}

        });


        return root;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            sharedElements.currentDegree = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 720 - startOrientation) % 360));

            if(startOrientation == 0) {
                startOrientation = sharedElements.currentDegree;
            }
            Log.i("MYLOG GYRO", String.valueOf(sharedElements.currentDegree));
            if(sharedElements.rotating && sharedElements.arrivingDegree >= sharedElements.currentDegree){
                try {
                    sharedElements.motorLeft.stop();
                    sharedElements.motorRight.stop();
                    sharedElements.rotating = false;
                }catch(Exception e){}
            }
            gyro.setText(String.valueOf(sharedElements.currentDegree));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void moveForward(){
        try {
            for(int i = 0; i < 10; i++) {
                int speedMotorLeft = 100;
                int speedMotorRight = 100;
                /*if (((360 - currentDegree) % 360) > (degreeToFollow + currentDegree))
                    speedMotorRight = 50;
                if (((360 - currentDegree) % 360) < (degreeToFollow + currentDegree))
                    speedMotorLeft = 50;*/
                sharedElements.motorLeft.setTimeSpeed(speedMotorLeft, 0, 200, 0, false);
                sharedElements.motorRight.setTimeSpeed(speedMotorRight, 0, 200, 0, false);
                sharedElements.motorLeft.waitCompletion();
                sharedElements.motorRight.waitCompletion();
                Log.i("MYLOG FORWARD", "move forward 2 seconds " + sharedElements.currentDegree);
            }
        }catch(Exception e){
            Log.e("MYLOG ERROR FORWARD", e.toString());
        }
    }

    private void rotateLeft() {
        try {
            Log.i("MYLOG SHARED", sharedElements.currentDegree + " " + sharedElements.motorLeft);
            sharedElements.motorLeft.setTimeSpeed(-30, 0, 10000, 0, false);
            sharedElements.motorRight.setTimeSpeed(30, 0, 10000, 0, false);
            sharedElements.rotating = true;
            sharedElements.arrivingDegree = ((sharedElements.currentDegree - 90) + 360) % 360;
            Log.i("MYLOG LEFT", "move left: " + sharedElements.currentDegree + " " + sharedElements.startingDegree);
        }catch (Exception e) {
            Log.e("MYLOG ERROR LEFT", e.toString());
        }
        // sharedElements.startingDegree = sharedElements.currentDegree;
    }

    private void rotateRight() {
        do {
            try {
                sharedElements.motorLeft.setTimeSpeed(10, 0, 100, 0, false);
                sharedElements.motorRight.setTimeSpeed(-10, 0, 100, 0, false);
                sharedElements.motorLeft.waitCompletion();
                sharedElements.motorRight.waitCompletion();
                Log.i("MYLOG RIGHT", "move right: " + sharedElements.currentDegree);
                Log.i("MYLOG RIGHT", "move right: " + sharedElements.currentDegree);
            } catch (Exception e) {
                Log.e("MYLOG ERROR RIGHT", e.toString());
            }
        } while(sharedElements.currentDegree%90 != 0);

    }

}