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

public class Level1Fragment extends Fragment implements SensorEventListener {

    private Level1ViewModel level1ViewModel;

    private SensorManager sensorManager;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private int startOrientation = 0;
    private int degreeToFollow = 0;
    private static int currentDegree = 0;
    private Button startLevel1;
    private Button stopLevel1;
    private TextView gyro;
    private Thread moveForwardThread;
    private Thread rotateLeftThread;
    private Thread rotateRightThread;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);

        startLevel1 = root.findViewById(R.id.start_level1_button);
        stopLevel1 = root.findViewById(R.id.stop_level1_button);
        gyro = root.findViewById(R.id.gyro_level1);

        moveForwardThread = new Thread(this::moveForward);
        rotateLeftThread = new Thread(this::rotateLeft);
        rotateRightThread = new Thread(this::rotateRight);


        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);
        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        startLevel1.setOnClickListener((view) -> {
            if(rotation != null){
                sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
            }
            moveForwardThread.run();
        });

        stopLevel1.setOnClickListener((view) -> {
            try {
                if(moveForwardThread.getState() != null)
                    moveForwardThread.interrupt();
                if(rotateLeftThread != null)
                    rotateLeftThread.interrupt();
                if(rotateRightThread != null)
                    rotateRightThread.interrupt();

            }catch (Exception e){}

        });


        return root;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            Level1Fragment.currentDegree = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 720 - startOrientation) % 360));
            if(startOrientation == 0) {
                startOrientation = Level1Fragment.currentDegree;
            }
            //gyro.setText(gyro.getText() + "\n" + String.valueOf(currentDegree));
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
                MainActivity.motorLeft.setTimeSpeed(speedMotorLeft, 0, 200, 0, false);
                MainActivity.motorRight.setTimeSpeed(speedMotorRight, 0, 200, 0, false);
                MainActivity.motorLeft.waitCompletion();
                MainActivity.motorRight.waitCompletion();
                Log.i("MYLOG FORWARD", "move forward 2 seconds " + Level1Fragment.currentDegree);
            }
            this.rotateLeftThread.run();
        }catch(Exception e){
            Log.e("MYLOG ERROR FORWARD", e.toString());
            this.moveForwardThread.interrupt();
        }
    }

    private void rotateLeft() {
        do{
            try {
                MainActivity.motorLeft.setTimeSpeed(-10, 0, 100, 0, false);
                MainActivity.motorRight.setTimeSpeed(10, 0, 100, 0, false);
                MainActivity.motorLeft.waitCompletion();
                MainActivity.motorRight.waitCompletion();
                Log.i("MYLOG LEFT", "move left: " + Level1Fragment.currentDegree);
            }catch (Exception e) {
                Log.e("MYLOG ERROR LEFT", e.toString());
                this.rotateLeftThread.interrupt();
            }
        }while(currentDegree%90 != 0);
        this.rotateRightThread.run();
    }

    private void rotateRight() {
        do {
            try {
                MainActivity.motorLeft.setTimeSpeed(10, 0, 100, 0, false);
                MainActivity.motorRight.setTimeSpeed(-10, 0, 100, 0, false);
                MainActivity.motorLeft.waitCompletion();
                MainActivity.motorRight.waitCompletion();
                Log.i("MYLOG RIGHT", "move right: " + Level1Fragment.currentDegree);
            } catch (Exception e) {
                Log.e("MYLOG ERROR RIGHT", e.toString());
                this.rotateRightThread.interrupt();
            }
        } while(currentDegree%90 != 0);
        this.moveForwardThread.run();

    }

}