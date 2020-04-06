package com.example.softwareengineeringapp;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.softwareengineeringapp.classes.ConnectionsActivity;
import com.example.softwareengineeringapp.classes.Coordinates;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends ConnectionsActivity {

    public static EV3 ev3;

    public static TachoMotor motorLeft;
    public static TachoMotor motorRight;
    public static TachoMotor motorGrab;
    public static GyroSensor giroscopio;
    public ArrayList<Coordinates> posizioneMine= new ArrayList<>();

    public static MainActivity mainActivity;
    private static final String TAG = Prelude.ReTAG("MainActivity");

    private static final boolean DEBUG = true;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final long ADVERTISING_DURATION = 30000;
    private static final String SERVICE_ID =
            "it.unive.dais.nearby.apps.SERVICE_ID";
    private State mState = State.UNKNOWN;
    private String mName;
    private TextView mPreviousStateView;
    private TextView mCurrentStateView;
    private String KEY = "abcdefgh";
    PopupWindow popupWindow;
    private boolean[] mStop;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final Runnable mDiscoverRunnable =
            new Runnable() {
                @Override
                public void run() {
                    setState(State.DISCOVERING);
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        Log.i("CONNESSIONE_EV3", "Tentativo di connessione ad EV3");
        try {
            BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("EV3IS").connect(); // replace with your own brick name
            ev3 = new EV3(conn);
            Prelude.trap(() -> ev3.run(this::initializeMotors));
        } catch (IOException e) {
            Log.e("CONNESSIONE_EV3", "fatal error: cannot connect to EV3");
            e.printStackTrace();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_level1, R.id.navigation_level2, R.id.navigation_level3, R.id.navigation_test)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Unable to load OpenCV");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }



    public void initializeMotors(EV3.Api api){
        motorRight = api.getTachoMotor(EV3.OutputPort.D);
        motorLeft = api.getTachoMotor(EV3.OutputPort.A);
        motorGrab = api.getTachoMotor(EV3.OutputPort.B);
        giroscopio = api.getGyroSensor(EV3.InputPort._2);
        Prelude.trap(() -> motorGrab.setType(TachoMotor.Type.MEDIUM));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Swap the two functions below if you want to start on Discovering rather than Advertising.
        mName = "INCOGNITO_SURFERS";
        setState(State.DISCOVERING);
        //setState(State.ADVERTISING);
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private void setState(State state) {
        if (mState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }

    public enum State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }
    private void onStateChanged(State oldState, State newState) {

        // Update Nearby Connections to the new state.
        switch (newState) {
            case DISCOVERING:
                if (isAdvertising()) {
                    stopAdvertising();
                }
                disconnectFromAllEndpoints();
                startDiscovering();
                break;
            case ADVERTISING:
                if (isDiscovering()) {
                    stopDiscovering();
                }
                disconnectFromAllEndpoints();
                startAdvertising();
                break;
            case CONNECTED:
                if (isDiscovering()) {
                    stopDiscovering();
                } else if (isAdvertising()) {
                    // Continue to advertise, so others can still connect,
                    // but clear the discover runnable.

                    //removeCallbacks(mDiscoverRunnable);
                }
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            byte[] bytes = payload.asBytes();

            String str_bytes = new String(bytes);

            // those are needed if you are a robot!
            Integer aux = Character.getNumericValue(str_bytes.charAt(0));
            if((aux >= 0 && aux <=6) && ((str_bytes.charAt(1)=='S'))){
                if(aux == 0 || aux == 1) {
                    logD(
                            String.format(
                                    "STOP/RESUME message intercepted %s",
                                    str_bytes));
                    // il messaggio è per noi!
                    return;
                }
                else {
                    logD(
                            String.format(
                                    "STOP/RESUME message ignored %s",
                                    str_bytes));
                    // altrimenti lo ignoriamo
                    return;
                }
            }

            if (str_bytes.toLowerCase().contains("obiettivo")) {
                logD(
                        String.format(
                                "Recovery message: %s",
                                str_bytes));
                // messaggio del protocollo passivo
                String coordinate = str_bytes.split(":")[1];
                posizioneMine.add(new Coordinates(Integer.parseInt(coordinate.split(";")[0]),
                        Integer.parseInt(coordinate.split(";")[1]),""));

                return;
            }

            if (str_bytes.toLowerCase().contains("recupero")) {
                logD(
                        String.format(
                                "Recovery message: %s",
                                str_bytes));
                // messaggio del protocollo passivo (broadcast) terza prova
                return;
            }

            if (str_bytes.toLowerCase().contains("benvenuto")) {
                logD(
                        String.format(
                                "Welcome message: %s",
                                str_bytes));
                // messaggio di benvenuto
                return;
            }

            try {
                SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
                Cipher c = Cipher.getInstance("DES/ECB/ISO10126Padding");
                c.init(c.DECRYPT_MODE, key);

                byte[] plaintext = c.doFinal(bytes);
                String s = new String(plaintext);

                logD(
                        String.format(
                                "BYTE received %s from endpoint %s",
                                s, endpoint.getName()));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (InvalidKeyException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (NoSuchPaddingException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (BadPaddingException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (BadPaddingException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (IllegalBlockSizeException)",
                                endpoint.getName()));
                e.printStackTrace();
            }
        }
    }

    public void send_benvenuto() {
        String x = "Benvenuto sono INCOGNITO_SURFERS";
        byte[] bytes = x.getBytes();
        send(Payload.fromBytes(bytes));
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        connectToEndpoint(endpoint);
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.
        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        send_benvenuto();
    }

}


