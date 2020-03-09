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
import android.widget.Toast;

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
import com.example.softwareengineeringapp.classes.Position;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

// CASA
import java.util.Timer;
import java.util.TimerTask;
//

public class Level1Fragment extends Fragment implements SensorEventListener {

    // CASA
    //private boolean lock = false;
    Position posizione;
    public static final int tempo_1_casella = 1600;
    public static final int tempo_90_gradi = 1600;
    public static final int tempo_grab_release = 1100;
    public static final int velocita_default_avanti_indietro = 30;
    public static final int velocita_default_rotazione = 10;
    public static final int velocita_default_grab_release = 40;
    public float gradi_destinazione;
    private boolean check_camera = true;
    private double green_perc=0;
    ArrayList<Ball> elenco_mine;
    //

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

        // CASA
        public volatile GyroSensor giroscopio;
        //

        public void setMotors(TachoMotor motorLeft, TachoMotor motorRight, TachoMotor motorGrab, GyroSensor giroscopio){
            Log.i("MYLOG MOTORS", motorLeft+"");
            this.motorLeft = motorLeft;
            this.motorRight = motorRight;
            this.motorGrab = motorGrab;

            this.giroscopio = giroscopio;
        }
    }

    final Sharing sharedElements = new Sharing();

    //OPEN//////////////////////////////////////////////////////////////////////////////////////////
    private int max_frame_width = 640;
    private int max_frame_height = 480;

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

        sharedElements.setMotors(MainActivity.motorLeft, MainActivity.motorRight, MainActivity.motorGrab, MainActivity.giroscopio);
        Log.i("MYLOG MAINACTIVITY", MainActivity.motorLeft+"");


        //CREAZIONE CAMPO
        posizione = new Position(6,6);


        movementTestThread = new Thread(this::movementTest);

        // Instanzio il sensorManager
        sensorManager = (SensorManager) MainActivity.mainActivity.getSystemService(MainActivity.SENSOR_SERVICE);
        // Prendo il sensore di rotazione del cellulare
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Al click del pulsante avvia il primo livello
        startLevel1.setOnClickListener((view) -> {
            gradi_destinazione=gradi();
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
       /* Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i("GIROSCOPIO_DEBUG",gradi()+"");
            }
        }, 1000, 1000);*/

       /*print_pc();

        move(Position.muovi_avanti);
        limite_campo();
        move(Position.muovi_avanti);
       */

        /*move(Position.muovi_ruota_destra);
        move(Position.muovi_avanti);
        move(Position.muovi_ruota_sinistra);
        move(Position.muovi_avanti);
        move(Position.muovi_indietro);*/

        //grab();
        //release();
        Log.e("CERCA_DEBUG","porcoddio");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int i = 0;
                Ball bb;
                Iterator<Ball> iti = elenco_mine.iterator();
                while(iti.hasNext() && i==0){
                    bb=iti.next();
                    Log.i("CERCA_DEBUG",bb.color);
                    i++;
                }
                Log.e("CERCA_DEBUG","porcoddio");
            }
        }, 1000, 1000);

        cerca_mina_distante();

    }


    // CASA
    private void raccogli_mina(Ball x){
        float gradi_inizio = gradi_destinazione;

        //Punta la mina, in modo che il centro del cerchio sia al centro dello schermo
        punta_mina(x);

        //Avanzo verso la mina
        move(Position.muovi_avanti);

        //Raccolgo la mina
        grab();

        //Ritorno alla posizione iniziale
        gradi_destinazione = gradi_inizio;
        move(Position.muovi_indietro);

    }

    private void punta_mina(Ball b){
        double bx = b.center.x;
        String bc = b.color;
        String d=null;
        if(bx > 243){
            d=Position.muovi_ruota_destra;
        }else if(bx < 237){
            d=Position.muovi_ruota_sinistra;
        }
        while(bx > 243 || bx < 237) {
            move_correggi_angolo(d, 10, 200);
            Ball t = cerca_mina_vicino(bc);
            if(t!=null){
                bx=t.center.x;
            }
        }
    }

    //Cerca la mina più grande del colore specificato nei parametri
    private Ball cerca_mina_vicino(String c){
        Ball result=null;
        Ball b=null;
        for (int i=0; i<5;i++){
            try{
                Iterator<Ball> iter = elenco_mine.iterator();
                while(iter.hasNext()){
                    b=iter.next();
                    if(c.equals(b.color) && (b==null || b.radius>result.radius)){
                        result = b;
                    }
                }
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("LIMITE_CAMPO",e.toString());
            }
        }
        return result;
    }

    private Ball cerca_mina_distante(){
        Ball result=null;
        Ball b=null;
        for (int c=0; c<5;c++){
            try{
                Iterator<Ball> iter = elenco_mine.iterator();
                while(iter.hasNext()){
                    b=iter.next();
                    Log.i("CERCA_DEBUG","x:"+result.center.x+" y:"+result.center.y+" r:"+result.radius+" c:"+result.color);
                    if(b.center.x > 192 && b.center.x < 288){
                        result = b;
                    }
                }
                Thread.currentThread().sleep(1000);
            }catch(Exception e){
                Log.e("LIMITE_CAMPO",e.toString());
            }
        }
        return result;
    }

    private void use_opencv(Boolean b){
        this.check_camera=b;
    }

    private void print_pc(){
        Log.i("POSIZIONE_DEBUG",posizione.getOrientazione()+" r:"+posizione.getRiga()+" c:"+posizione.getColonna());
        Log.i("CAMPO_DEBUG","r:"+posizione.getNumero_righe()+" c:"+posizione.getNumero_colonne());
    }

    private boolean limite_campo(){
        boolean result=false;
        double prc = this.green_perc;
        for (int c=0; c<5;c++){
            Log.i("POSIZIONE_CORRENTE_VERDE","c:"+c+" "+prc+"%");
            if(this.green_perc > prc){
                prc=this.green_perc;
            }
            try{
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("LIMITE_CAMPO",e.toString());
            }
        }
        Log.i("POSIZIONE_CORRENTE_VERDE",prc+"%");
        if(prc > 50){
            result=true;
            if(posizione.getOrientazione().equals(Position.orientazione_destra) &&
                    posizione.getNumero_colonne() > posizione.getColonna()+1){
                posizione.aggiorna_campo(posizione.getNumero_righe(),posizione.getColonna()+1);
            }else if(posizione.getOrientazione().equals(Position.orientazione_alto) &&
                    posizione.getNumero_righe() > posizione.getRiga()+1){
                posizione.aggiorna_campo(posizione.getRiga()+1,posizione.getNumero_colonne());
            }
        }
        Log.i("LIMITE_CAMPO_DEBUG","Percentuale verde: "+prc);
        return result;
    }

    private void correggi_angolo(){
        //distanza tra angolo attuale e angolo di destinazione
        float c = Math.abs( ((gradi_destinazione - gradi())+180)%360 - 180 );
        float c_prev=c;
        String d = Position.muovi_ruota_destra;
        while(c > 1) {
            Log.i("CORREGGI_ANGOLO_DEBUG","c_prev: "+c_prev+"  c:"+c);
            move_correggi_angolo(d, 10, 200);
            c_prev=c;
            c = Math.abs( ((gradi_destinazione - gradi())+180)%360 - 180 );
            if(c > c_prev){
                if(d.equals(Position.muovi_ruota_destra)){
                    d=Position.muovi_ruota_sinistra;
                }else{
                    d=Position.muovi_ruota_destra;
                }
            }
        }
    }

    private void move_correggi_angolo(String action, int speed, int time){
        int speed_dx = 0;
        int speed_sx = 0;
        if(action.equals(Position.muovi_ruota_destra)){
            speed_dx = -speed;
            speed_sx = speed;
        }else if(action.equals(Position.muovi_ruota_sinistra)){
            speed_dx = speed;
            speed_sx = -speed;
        }else{
            Log.e("MOVE_DEBUG","Errore: azione '" + action + "' non riconosciuta.");
        }
        try{
            sharedElements.motorLeft.setTimeSpeed(speed_sx, 0, time, 0, true);
            sharedElements.motorRight.setTimeSpeed(speed_dx, 0, time, 0, true);
            Thread.currentThread().sleep(time+500);
        }catch (Exception e){
            Log.e("MOVE_DEBUG",e.toString());
        }
    }

    private float gradi(){
        float d = -1;
        try {
            d = sharedElements.giroscopio.getAngle().get()%360;
            if (d<0){
                d = 360 + d;
            }
        }catch (Exception e){
            Log.e("GIROSCOPIO_DEBUG",e.toString());
        }
        return d;
    }

    private void grab(){
        try {
            sharedElements.motorGrab.setTimeSpeed(velocita_default_grab_release,0,tempo_grab_release,0,true);
            Thread.currentThread().sleep(tempo_grab_release+1000);
        }catch (Exception e){
            Log.e("PINZA_DEBUG",e.toString());
        }
    }

    private void release(){
        try {
            sharedElements.motorGrab.setTimeSpeed(-velocita_default_grab_release,0,tempo_grab_release,0,true);
            Thread.currentThread().sleep(tempo_grab_release+1000);
        }catch (Exception e){
            Log.e("PINZA_DEBUG",e.toString());
        }
    }

    private boolean move(String action){
        boolean result=false;
        if(action.equals(Position.muovi_avanti) || action.equals(Position.muovi_indietro)){
            result = move(action,velocita_default_avanti_indietro,tempo_1_casella);
        }else if(action.equals(Position.muovi_ruota_sinistra) || action.equals(Position.muovi_ruota_destra)){
            result = move(action,velocita_default_rotazione,tempo_90_gradi);
        }else{
            Log.e("MOVE_DEBUG","Errore: azione '" + action + "' non riconosciuta.");
        }
        return result;
    }

    private boolean move(String action, int speed, int time){
        boolean result=posizione.muovi(action);
        int speed_dx = 0;
        int speed_sx = 0;
        if(result) {
            Log.i("GIROSCOPIO_MOVE_DEBUG", "Prima effettivi: " + gradi() + " calcolati: " + gradi_destinazione);
            if (action.equals(Position.muovi_avanti)) {
                speed_dx = speed;
                speed_sx = speed;
            } else if (action.equals(Position.muovi_indietro)) {
                speed_dx = -speed;
                speed_sx = -speed;
            } else if (action.equals(Position.muovi_ruota_destra)) {

                gradi_destinazione = (gradi_destinazione + 90) % 360;

                speed_dx = -speed;
                speed_sx = speed;
            } else if (action.equals(Position.muovi_ruota_sinistra)) {

                gradi_destinazione = (gradi_destinazione - 90) % 360;
                if (gradi_destinazione < 0) {
                    gradi_destinazione = 360 + gradi_destinazione;
                }

                speed_dx = speed;
                speed_sx = -speed;
            } else {
                Log.e("MOVE_DEBUG", "Errore: azione '" + action + "' non riconosciuta.");
            }
            try {
                sharedElements.motorLeft.setTimeSpeed(speed_sx, 0, time, 0, true);
                sharedElements.motorRight.setTimeSpeed(speed_dx, 0, time, 0, true);
            } catch (Exception e) {
                Log.e("MOVE_DEBUG", e.toString());
            }
            try {
                Thread.currentThread().sleep(time + 1000);
            } catch (Exception e) {
                Log.e("MOVE_DEBUG", e.toString());
            }
            correggi_angolo();
            Log.i("GIROSCOPIO_MOVE_DEBUG", "Dopo: " + gradi() + " calcolati: " + gradi_destinazione);
        }
        print_pc();
        return result;
    }
    //

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

        mOpenCvCameraView =root.findViewById(R.id.HelloOpenCvView);
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
                /*Mat frame2 = inputFrame.rgba();

                /////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////
                Mat frame=frame2.t();

                Core.rotate(frame2.t(),frame,Core.ROTATE_90_COUNTERCLOCKWISE);
                Imgproc.resize(frame.t(),frame,frame2.size());

                Mat frame3= new Mat();
                frame.copyTo(frame3);*/

                // CASA
                Mat frame = inputFrame.rgba();
                //Photo.fastNlMeansDenoisingColored(frame,frame); rimozione rumore, not worth, lagga abbestia

                int x = (frame.width()-frame.height())/2;
                Rect area = new Rect(new Point(x,0 ),
                        new Point(x+frame.height(),frame.height()));
                Mat frame3 = new Mat(frame,area);
                Core.rotate(frame3,frame3,Core.ROTATE_90_CLOCKWISE);
                Mat frame_cut = frame3.clone();
                //

                //COMMENTARE SU SMARTPHONE, NON COMMENTARE SU EMULATORE
                //Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGB2BGR);

                if(check_camera) {
                    LineFinder lineFinder = new LineFinder(frame_cut, true);
                    lineFinder.setThreshold(300, 20);
                    lineFinder.setOrientation("landscape");

                    ArrayList<Double> li = lineFinder.findLine(frame3);
                    Iterator<Double> iter = li.iterator();
                    String ang = "";
                    while (iter.hasNext()) {
                        ang = ang + "  " + iter.next();
                    }
                    Log.e("line", ang
                            //String.valueOf(lineFinder.findLine())
                    );

                    BallFinder ballFinder = new BallFinder(frame_cut, true);
                    ballFinder.setViewRatio(0.0f);
                    ballFinder.setOrientation("landscape");
                    ArrayList<Ball> f = ballFinder.findBalls(frame3);
                    elenco_mine = new ArrayList<Ball>(f);
                    /*Ball bb;
                    Iterator<Ball> iti = elenco_mine.iterator();
                    while(iti.hasNext()){
                        bb=iti.next();
                        Log.i("CERCA_DEBUG",bb.color);
                    }*/


                    //Collections.copy(elenco_mine,f);
                    //Mat ret = ballFinder.findBalls(frame3);

                    GreenFinder gFinder = new GreenFinder(frame_cut, true, 440, 80);
                    gFinder.setViewRatio(0.0f);
                    gFinder.setOrientation("landscape");
                    //Mat ret = gFinder.findGreen();
                    double prc = gFinder.findGreen(frame3);
                    TextView t = root.findViewById(R.id.textView);
                    green_perc=prc;
                    t.setText("Percentage: " + prc);
                }


                // CASA
                Mat black = Mat.zeros(frame.size(),frame.type());
                frame3.copyTo(black.submat(area));
                return black;
                //

                //return frame3;

                //return ret;
            }
        });

        // Abilita la visualizzazione dell'immagine sullo schermo
        mOpenCvCameraView.enableView();
        //CLOSE/////////////////////////////////////////////////////////////////////////////////////
    }
}