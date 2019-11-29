package com.example.softwareengineeringapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class MainActivity extends AppCompatActivity {

    private static EV3 ev3;

    private static TachoMotor motorLeft;
    private static TachoMotor motorRight;
    private static TachoMotor motorGrab;

    private static Button forward;
    private static Button backward;
    private static Button right;
    private static Button left;
    private static Button grab;
    private static Button drop;



    private static final String TAG = Prelude.ReTAG("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("EV3IS").connect(); // replace with your own brick name

            // connect to EV3 via bluetooth
            ev3 = new EV3(conn);
            forward = findViewById(R.id.moveUp);
            backward = findViewById(R.id.moveDown);
            left = findViewById(R.id.moveLeft);
            right = findViewById(R.id.moveRight);
            grab = findViewById(R.id.pickup);
            drop = findViewById(R.id.drop);


            Prelude.trap(() -> ev3.run(this::initializeMotors));

            setupEditable(R.id.speedConfig, (x) -> applyMotor((m) -> {
                m.setSpeed(x);
                m.start();
            }, motorRight));

            setupEditable(R.id.speedConfig, (x) -> applyMotor((m) -> {
                m.setSpeed(x);
                m.start();
            }, motorLeft));

            setupEditable(R.id.grabConfig, (x) -> applyMotor((m) -> {
                m.setSpeed(x);
                m.start();
            }, motorGrab));

            forward.setOnClickListener(x -> {
                try {
                    motorLeft.setTimeSpeed(20, 0, 100, 0, true);
                    motorRight.setTimeSpeed(20, 0, 100, 0, true);
                }catch(Exception e){}
            });
        } catch (IOException e) {
            Log.e(TAG, "fatal error: cannot connect to EV3");
            e.printStackTrace();
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_level1, R.id.navigation_level2, R.id.navigation_level3, R.id.navigation_test)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    public void initializeMotors(EV3.Api api){
        motorRight = api.getTachoMotor(EV3.OutputPort.D);
        motorLeft = api.getTachoMotor(EV3.OutputPort.A);
        motorGrab = api.getTachoMotor(EV3.OutputPort.B);
        Prelude.trap(() -> motorGrab.setType(TachoMotor.Type.MEDIUM));
    }

    private void setupEditable(@IdRes int id, Consumer<Integer> f) {
        SeekBar e = findViewById(id);
        e.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                f.call(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void applyMotor(@NonNull ThrowingConsumer<TachoMotor, Throwable> f, TachoMotor motor) {
        if (motor != null)
            Prelude.trap(() -> f.call(motor));
    }
}


