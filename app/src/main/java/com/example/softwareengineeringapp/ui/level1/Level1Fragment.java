package com.example.softwareengineeringapp.ui.level1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);


        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);

        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(rotation != null){
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
        }

        return root;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            int degrees = (Math.round((int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]) + 360 - startOrientation) % 360));
            if(startOrientation == 0)
                startOrientation = degrees;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void moveForward(){

    }

    private void rotateLeft() {

    }

    private void rotateRight() {

    }

}