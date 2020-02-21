package com.example.softwareengineeringapp.ui.level2;

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

import com.example.softwareengineeringapp.classes.Ball;
import com.example.softwareengineeringapp.classes.BallFinder;
import com.example.softwareengineeringapp.classes.GreenFinder;
import com.example.softwareengineeringapp.classes.LineFinder;
import com.example.softwareengineeringapp.R;
//OPEN//////////////////////////////////////////////////////////////////////////////////////////////
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
//CLOSE/////////////////////////////////////////////////////////////////////////////////////////////

public class Level2Fragment extends Fragment {

    private Level2ViewModel level2ViewModel;

    //OPEN//////////////////////////////////////////////////////////////////////////////////////////
    private int max_frame_width = 500;
    private int max_frame_height = 500;

    private String TAG = "AndroidIngSwOpenCV";

    private CameraBridgeViewBase mOpenCvCameraView;
    //CLOSE/////////////////////////////////////////////////////////////////////////////////////////


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level2ViewModel =
                ViewModelProviders.of(this).get(Level2ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level2, container, false);
        final TextView textView = root.findViewById(R.id.text_level2);
        level2ViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

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
                //ArrayList<Ball> f = ballFinder.findBalls(frame3);
                ArrayList<Ball> ret = ballFinder.findBalls(frame3);

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

        return root;
    }
}