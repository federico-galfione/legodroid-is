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
import android.view.SurfaceView;
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
import com.example.softwareengineeringapp.classes.Ball;
import com.example.softwareengineeringapp.classes.BallFinder;
import com.example.softwareengineeringapp.classes.GreenFinder;
import com.example.softwareengineeringapp.classes.LineFinder;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;

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
    private Thread movementTestThread;

    private boolean flag = false;

    class Sharing {
        public volatile int currentDegree = 0;
        public volatile int startingDegree = 0;
        public volatile TachoMotor motorLeft;
        public volatile  TachoMotor motorRight;
        public volatile TachoMotor motorGrab;
        public volatile int arrivingDegree = 0;
        public volatile boolean rotating = false;
        public volatile boolean grabbed = false;

        public void setMotors(TachoMotor motorLeft, TachoMotor motorRight, TachoMotor motorGrab){
            Log.i("MYLOG MOTORS", motorLeft+"");
            this.motorLeft = motorLeft;
            this.motorRight = motorRight;
            this.motorGrab = motorGrab;
        }
    }

    final Sharing sharedElements = new Sharing();

    //OPEN//////////////////////////////////////////////////////////////////////////////////////////
    private int max_frame_width = 500;
    private int max_frame_height = 500;

    private String TAG = "AndroidIngSwOpenCV";

    private CameraBridgeViewBase mOpenCvCameraView;
    //CLOSE/////////////////////////////////////////////////////////////////////////////////////////

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);

        startLevel1 = root.findViewById(R.id.start_level1_button);
        stopLevel1 = root.findViewById(R.id.stop_level1_button);
        gyro = root.findViewById(R.id.gyro_level1);

        sharedElements.setMotors(MainActivity.motorLeft, MainActivity.motorRight, MainActivity.motorGrab);
        Log.i("MYLOG MAINACTIVITY", MainActivity.motorLeft+"");

        movementTestThread = new Thread(this::movementTest);

        // Instanzio il sensorManager
        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);
        // Prendo il sensore di rotazione del cellulare
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Al click del pulsante avvia il primo livello
        startLevel1.setOnClickListener((view) -> {
            manageOpenCV(root);
            if(rotation != null){
                sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
            }
            movementTestThread.start();
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
            gyro.setText(String.valueOf(sharedElements.currentDegree));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void movementTest(){
        moveForward(1000);
    }


    private void moveForward(int time){
        try {
            sharedElements.arrivingDegree = sharedElements.currentDegree;
            int speedMotorLeft = 30;
            int speedMotorRight = 30;
            sharedElements.motorLeft.setTimeSpeed(speedMotorLeft, 0, time, 0, true);
            sharedElements.motorRight.setTimeSpeed(speedMotorRight, 0, time, 0, true);
            Thread.currentThread().sleep(time);
        }catch(Exception e){
            Log.e("MYLOG ERROR FORWARD", e.toString());
        }
    }

    private void rotateLeft() {
        rotate(-90, -5, 5);
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

    /*
    private void adjustOrientation() {
        try {
            Thread.currentThread().sleep(200);
            Log.i("ADJUST!", sharedElements.currentDegree + " " + sharedElements.arrivingDegree);
            while (sharedElements.currentDegree != sharedElements.arrivingDegree) {
                if (sharedElements.arrivingDegree == 0) {
                    if (360 - sharedElements.currentDegree >= 1 && 360 - sharedElements.currentDegree <= 20) {
                        sharedElements.motorLeft.setStepSpeed(5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(-5, 0, 2, 0, true);
                    } else {
                        sharedElements.motorLeft.setStepSpeed(-5, 0, 2, 0, true);
                        sharedElements.motorRight.setStepSpeed(5, 0, 2, 0, true);
                    }
                    Thread.currentThread().sleep(100);
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
    }*/

    /*
    private void manageBall(boolean operation){
        try {
            if (operation) {
                sharedElements.motorGrab.setTimeSpeed(100, 0, 1000, 0, true);
                sharedElements.grabbed = true;
                Log.i("DISTANCE","Grabbato");

            } else {
                sharedElements.motorGrab.setTimeSpeed(-100, 0, 1000, 0, true);
                sharedElements.grabbed = false;
            }
            Thread.currentThread().sleep(1500);
        }catch(Exception e){}
    }*/

    private void manageOpenCV(View root){
        //OPEN//////////////////////////////////////////////////////////////////////////////////////
        // Configura l'elemento della camera

        mOpenCvCameraView = root.findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(max_frame_width, max_frame_height);
        //Log.e("ROT",""+mOpenCvCameraView.getRotation());
        //mOpenCvCameraView.setRotation(90);
        // Log.e("ROT",""+mOpenCvCameraView.getRotation());

        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "Camera Stopped");
            }

            // Viene eseguito ad ogni frame, con inputFrame l'immagine corrente
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                // Salva il frame corrente su un oggetto Mat, ossia una matrice bitmap

                //PER EMULATORE
                //Mat frame = inputFrame.rgba();

                //PER SMARTPHONE
                Mat frame2 = inputFrame.rgba();

                /////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////
                Mat frame=frame2.t();

                Core.rotate(frame2.t(),frame,2);
                Imgproc.resize(frame.t(),frame,frame2.size());

                Mat frame3= new Mat();
                frame.copyTo(frame3);


                //COMMENTARE SU SMARTPHONE, NON COMMENTARE SU EMULATORE
                //Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGB2BGR);

                LineFinder lineFinder = new LineFinder(frame, true);
                lineFinder.setThreshold(300, 20);
                lineFinder.setOrientation("landscape");

                ArrayList<Double> x = lineFinder.findLine(frame3);
                Iterator<Double> iter = x.iterator();
                String ang = "";
                while(iter.hasNext()){
                    ang = ang + "  " + iter.next();
                }
                Log.e("line", ang
                        //String.valueOf(lineFinder.findLine())
                );

                BallFinder ballFinder = new BallFinder(frame, true);
                ballFinder.setViewRatio(0.0f);
                ballFinder.setOrientation("landscape");
                ArrayList<Ball> f = ballFinder.findBalls(frame3);
                //Mat ret = ballFinder.findBalls(frame3);

                GreenFinder gFinder = new GreenFinder(frame, true, frame.height()/2,100);
                gFinder.setViewRatio(0.0f);
                gFinder.setOrientation("landscape");
                //Mat ret = gFinder.findGreen();
                double prc = gFinder.findGreen(frame3);
                TextView t = root.findViewById(R.id.textView);
                t.setText("Percentage: " + prc);

                /*for (Ball b : f) {
                    Log.e("ball", String.valueOf(b.center.x));
                    Log.e("ball", String.valueOf(b.center.y));
                    Log.e("ball", String.valueOf(b.radius));
                    Log.e("ball", b.color);
                }*/

                return frame3;
                //return ret;
            }
        });

        // Abilita la visualizzazione dell'immagine sullo schermo
        mOpenCvCameraView.enableView();
        //CLOSE/////////////////////////////////////////////////////////////////////////////////////
    }
}