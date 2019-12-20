package com.example.softwareengineeringapp;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity {

    private static EV3 ev3;

    public static TachoMotor motorLeft;
    public static TachoMotor motorRight;
    public static TachoMotor motorGrab;

    public static MainActivity mainActivity;


    private static final String TAG = Prelude.ReTAG("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        try {
            BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("EV3IS").connect(); // replace with your own brick name

            // connect to EV3 via bluetooth
            ev3 = new EV3(conn);

            Prelude.trap(() -> ev3.run(this::initializeMotors));


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
}


