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
    private Thread moveLongThread;
    private Thread moveShortThread;
    private Thread rotateLeftThread;
    private Thread rotateRightThread;
    private Thread turnAroundThread;
    private Thread adjustThread;
    private Thread routineThread;

    private boolean flag = false;

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
        rotateRightThread = new Thread(this::rotateRight);
        turnAroundThread = new Thread(this::turnAround);
        moveLongThread = new Thread(this::moveLong);
        routineThread = new Thread(this::routine);

        // Instanzio il sensorManager
        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);
        // Prendo il sensore di rotazione del cellulare
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Al click del pulsante avvia il primo livello
        startLevel1.setOnClickListener((view) -> {
            if(rotation != null){
                sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
            }
            routineThread.start();
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

            if(!flag) {
                int temp = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 720 - startOrientation) % 360));
                startOrientation = temp;
                flag = true;
            }
            sharedElements.currentDegree = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 720 - startOrientation) % 360));

            Log.i("MYLOG GYRO", "CURRENT: " + sharedElements.currentDegree + " ARRIVING: " + sharedElements.arrivingDegree + " STARTING: " + sharedElements.startingDegree + " ROTATING: " + sharedElements.rotating);
            if(sharedElements.rotating && sharedElements.arrivingDegree == sharedElements.currentDegree && sharedElements.arrivingDegree != sharedElements.startingDegree){
                try {
                    sharedElements.motorLeft.brake();
                    sharedElements.motorRight.brake();
                    sharedElements.motorLeft.stop();
                    sharedElements.motorRight.stop();
                    sharedElements.rotating = false;
                    adjustThread = new Thread(this::adjustOrientation);
                    adjustThread.start();
                    if(rotateLeftThread.getState() == Thread.State.TIMED_WAITING)
                        rotateLeftThread.interrupt();
                    if(rotateRightThread.getState() == Thread.State.TIMED_WAITING)
                        rotateRightThread.interrupt();
                    if(turnAroundThread.getState() == Thread.State.TIMED_WAITING)
                        turnAroundThread.interrupt();

                }catch(Exception e){}
            }
            gyro.setText(String.valueOf(sharedElements.currentDegree));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void routine(){
        try {
            /*moveShortThread = new Thread(this::moveShort);
            moveShortThread.start();
            moveShortThread.join();*/
            rotateLeftThread = new Thread(this::rotateLeft);
            rotateLeftThread.start();
            rotateLeftThread.join();
            adjustThread.join();
            Log.i("MYLOG ROUTINE", "first!" );
            /*moveLongThread = new Thread(this::moveLong);
            moveLongThread.start();
            moveLongThread.join();*/
            turnAroundThread = new Thread(this::turnAround);
            turnAroundThread.start();
            turnAroundThread.join();
            adjustThread.join();
            Log.i("MYLOG ROUTINE", "second!" );
            /*moveLongThread = new Thread(this::moveLong);
            moveLongThread.start();
            moveLongThread.join();*/
            rotateLeftThread = new Thread(this::rotateLeft);
            rotateLeftThread.start();
            rotateLeftThread.join();
            adjustThread.join();
            Log.i("MYLOG ROUTINE", "third!" );

        }catch(Exception e){}
    }

    private void moveLong(){
        moveForward(1000, 3000, 1000);
    }

    private void moveShort(){
        moveForward(500, 1000, 500);
    }

    private void moveForward(int step1, int step2, int step3){
        try {
            sharedElements.arrivingDegree = sharedElements.currentDegree;
            for(int i = 0; i < 2; i++) {
                int speedMotorLeft = 20;
                int speedMotorRight = 20;
                sharedElements.motorLeft.setTimeSpeed(speedMotorLeft, step1/2, step2/2, step3/2, true);
                sharedElements.motorRight.setTimeSpeed(speedMotorRight, step1/2, step2/2, step3/2, true);
                Thread.currentThread().sleep(step1 + step2 + step3);
                adjustThread = new Thread(this::adjustOrientation);
                adjustThread.start();
                try{
                    adjustThread.join();
                }catch(Exception e){}
                Log.i("MYLOG FORWARD", "move forward 2 seconds " + sharedElements.currentDegree);
            }
        }catch(Exception e){
            Log.e("MYLOG ERROR FORWARD", e.toString());
        }
    }

    private void rotateLeft() {
        rotate(-90, -5, 5);
    }

    private void turnAround(){
        rotate(180, -5, 5);
    }

    private void rotateRight(){
        rotate(90, 5, -5);
    }

    private void rotate(int rotationDegree, int speedLeft, int speedRight){
        try {
            Log.i("MYLOG SHARED", sharedElements.currentDegree + " " + sharedElements.motorLeft);
            sharedElements.arrivingDegree = ((sharedElements.currentDegree + rotationDegree) + 360) % 360;
            sharedElements.motorLeft.setTimeSpeed(speedLeft, 0, 10000, 0, true);
            sharedElements.motorRight.setTimeSpeed(speedRight, 0, 10000, 0, true);
            sharedElements.rotating = true;
            Thread.currentThread().sleep(10000);
            Log.i("MYLOG LEFT", "move left: " + sharedElements.currentDegree + " " + sharedElements.startingDegree);
        }catch (Exception e) {
            Log.e("MYLOG ERROR LEFT", e.toString());
        }
    }

    private void adjustOrientation() {
        try {
            Thread.currentThread().sleep(200);
            Log.i("ADJUST!", sharedElements.currentDegree + " " + sharedElements.arrivingDegree);
            while (sharedElements.currentDegree != sharedElements.arrivingDegree) {
                if (sharedElements.arrivingDegree == 0) {
                    if (360 - sharedElements.currentDegree > 1 && 360 - sharedElements.currentDegree <= 20) {
                        sharedElements.motorLeft.setStepSpeed(5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(-5, 0, 2, 0, true);
                    } else {
                        sharedElements.motorLeft.setStepSpeed(-5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(5, 0, 2, 0, true);
                    }
                    Thread.currentThread().sleep(200);
                }else{
                    if (sharedElements.currentDegree < sharedElements.arrivingDegree) {
                        sharedElements.motorLeft.setStepSpeed(5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(-5, 0, 2, 0, true);
                    } else {
                        sharedElements.motorLeft.setStepSpeed(-5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(5, 0, 2, 0, true);
                    }
                    Thread.currentThread().sleep(100);
                }
                Log.i("ADJUST WHILE!", sharedElements.currentDegree + " " + sharedElements.arrivingDegree);
            }
            sharedElements.startingDegree = sharedElements.currentDegree;
            sharedElements.motorRight.stop();
            sharedElements.motorLeft.stop();
        }catch(Exception e){}
    }

}