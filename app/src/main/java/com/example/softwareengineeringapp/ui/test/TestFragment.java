package com.example.softwareengineeringapp.ui.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.softwareengineeringapp.MainActivity;
import com.example.softwareengineeringapp.R;

import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class TestFragment extends Fragment {

    private TestViewModel testViewModel;

    private Button forward;
    private Button backward;
    private Button left;
    private Button right;
    private Button grab;
    private Button drop;
    private SeekBar speedConfig;
    private SeekBar grabConfig;


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


        forward.setOnTouchListener(new CustomHandler(() -> move(speedConfig.getProgress(),speedConfig.getProgress())));
        backward.setOnTouchListener(new CustomHandler(() -> move(-speedConfig.getProgress(),-speedConfig.getProgress())));
        left.setOnTouchListener(new CustomHandler(() -> move(-speedConfig.getProgress(),speedConfig.getProgress())));
        right.setOnTouchListener(new CustomHandler(() -> move(speedConfig.getProgress(),-speedConfig.getProgress())));

        grab.setOnTouchListener(new CustomHandler(() -> grabbing(grabConfig.getProgress())));
        drop.setOnTouchListener(new CustomHandler(() -> grabbing(-grabConfig.getProgress())));

        return root;
    }



    private void move(int speedMotorLeft, int speedMotorRight){
        try {
            MainActivity.motorLeft.setTimeSpeed(speedMotorLeft, 0, 100, 0, true);
            MainActivity.motorRight.setTimeSpeed(speedMotorRight, 0, 100, 0, true);
        }catch(Exception e){}
    }

    private void grabbing(int speedMotor){
        try{
            MainActivity.motorGrab.setTimeSpeed(speedMotor, 0, 100, 0, true);
        }catch(Exception e){}
    }

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


}