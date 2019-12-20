package com.example.softwareengineeringapp.ui.test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.softwareengineeringapp.MainActivity;
import com.example.softwareengineeringapp.R;

public class TestFragment extends Fragment implements SensorEventListener{

    private TestViewModel testViewModel;

    private Button forward;
    private Button backward;
    private Button left;
    private Button right;
    private Button grab;
    private Button drop;
    private SeekBar speedConfig;
    private SeekBar grabConfig;
    private TextView gyro;
    private SensorManager sensorManager;

    private int startOrientation = 0;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        testViewModel =
                ViewModelProviders.of(this).get(TestViewModel.class);
        View root = inflater.inflate(R.layout.fragment_test, container, false);
        forward = root.findViewById(R.id.moveUp);
        backward = root.findViewById(R.id.moveDown);
        left = root.findViewById(R.id.moveLeft);
        right = root.findViewById(R.id.moveRight);
        grab = root.findViewById(R.id.pickup);
        drop = root.findViewById(R.id.drop);
        speedConfig = root.findViewById(R.id.speedConfig);
        grabConfig = root.findViewById(R.id.grabConfig);
        gyro = root.findViewById(R.id.gyro);


        forward.setOnTouchListener(new CustomHandler(() -> move(speedConfig.getProgress(),speedConfig.getProgress())));
        backward.setOnTouchListener(new CustomHandler(() -> move(-speedConfig.getProgress(),-speedConfig.getProgress())));
        left.setOnTouchListener(new CustomHandler(() -> move(-speedConfig.getProgress(),speedConfig.getProgress())));
        right.setOnTouchListener(new CustomHandler(() -> move(speedConfig.getProgress(),-speedConfig.getProgress())));

        grab.setOnTouchListener(new CustomHandler(() -> grabbing(grabConfig.getProgress())));
        drop.setOnTouchListener(new CustomHandler(() -> grabbing(-grabConfig.getProgress())));

        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);

        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(rotation != null){
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
        }
        return root;
    }


    // Muove il robot
    private void move(int speedMotorLeft, int speedMotorRight){
        try {
            MainActivity.motorLeft.setTimeSpeed(speedMotorLeft, 0, 100, 0, true);
            MainActivity.motorRight.setTimeSpeed(speedMotorRight, 0, 100, 0, true);
        }catch(Exception e){}
    }

    // Aziona il motore della chela
    private void grabbing(int speedMotor){
        try{
            MainActivity.motorGrab.setTimeSpeed(speedMotor, 0, 100, 0, true);
        }catch(Exception e){}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            int degrees = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 360 - startOrientation) % 360));
            if(startOrientation == 0)
                startOrientation = degrees;
            gyro.setText(String.valueOf(degrees));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*Classe per il funzionamento continuo dei motori mentre si tiene premuto un pulsante*/
    private class CustomHandler implements View.OnTouchListener{
        private Handler mHandler;
        private Runnable movement;

        CustomHandler(Runnable movement){
            this.movement = movement;
        }

        @Override public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction, 10);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction);
                    mHandler = null;
                    break;
            }
            return false;
        }

        Runnable mAction = new Runnable() {
            @Override public void run() {
                movement.run();
                mHandler.postDelayed(this, 10);
            }
        };
    }

    public void onDestroyView (){
        super.onDestroyView();
    }

}