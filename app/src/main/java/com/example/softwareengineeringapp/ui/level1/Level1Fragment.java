package com.example.softwareengineeringapp.ui.level1;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.softwareengineeringapp.MainActivity;
import com.example.softwareengineeringapp.R;
import com.example.softwareengineeringapp.classes.Ball;
import com.example.softwareengineeringapp.classes.BallFinder;
import com.example.softwareengineeringapp.classes.Coordinates;
import com.example.softwareengineeringapp.classes.GreenFinder;
import com.example.softwareengineeringapp.classes.LineFinder;
import com.example.softwareengineeringapp.classes.Position;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.photo.Photo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Level1Fragment extends Fragment{

    public static final String giallo = "#FFEB3B";
    public static final String blu = "#1F92FF";
    public static final String rosso = "#FF3131";

    private Position posizione;
    public static final int tempo_1_casella = 2400;
    public static final int tempo_90_gradi = 1200;
    public static final int tempo_grab_release = 1100;
    public static final int velocita_default_avanti_indietro = 20;
    public static final int velocita_default_rotazione = 20;
    public static final int velocita_default_grab_release = 40;
    public float gradi_destinazione;
    private boolean check_camera = false;
    private boolean trova_linee = false;
    private boolean trova_mine = false;
    private boolean trova_limite = false;
    private double green_perc=0;
    ArrayList<Ball> elenco_mine;
    private boolean stop=false;
    private BallFinder ballFinder;
    private GreenFinder greenFinder;
    private Mat frame;
    private Mat frame_draw;
    private Mat frame_cut;
    private Mat frame_out;
    private Level1ViewModel level1ViewModel;
    private Button startLevel1;
    private Button stopLevel1;
    private Thread movementTestThread;
    final Sharing sharedElements = new Sharing();
    private int max_frame_width = 640;
    private int max_frame_height = 480;
    private String TAG = "Level1";
    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView greenPrc;

    private TableLayout fieldContainer;
    private ArrayList<Coordinates> field = new ArrayList<>();


    class Sharing {
        public volatile TachoMotor motorLeft;
        public volatile  TachoMotor motorRight;
        public volatile TachoMotor motorGrab;
        public volatile GyroSensor giroscopio;

        public void setMotors(TachoMotor motorLeft, TachoMotor motorRight, TachoMotor motorGrab, GyroSensor giroscopio){
            Log.i("MYLOG MOTORS", motorLeft+"");
            this.motorLeft = motorLeft;
            this.motorRight = motorRight;
            this.motorGrab = motorGrab;
            this.giroscopio = giroscopio;
        }
    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);

        startLevel1 = root.findViewById(R.id.start_level1_button);
        stopLevel1 = root.findViewById(R.id.stop_level1_button);
        greenPrc = root.findViewById(R.id.textView);
        fieldContainer = root.findViewById(R.id.output_table);

        sharedElements.setMotors(MainActivity.motorLeft, MainActivity.motorRight, MainActivity.motorGrab, MainActivity.giroscopio);

        //CREAZIONE CAMPO
        posizione = new Position(9,9);

        movementTestThread = new Thread(this::startLevel1);

        startLevel1.setText("AVVIA PROVA");
        startLevel1.setOnClickListener((view) -> {
            switch (startLevel1.getText().toString()){
                case "AVVIA PROVA":
                    gradi_destinazione=gradi();
                    manageOpenCV(root);
                    movementTestThread.start();
                    startLevel1.setText("MOSTRA OUTPUT");
                    break;
                case "MOSTRA OUTPUT":
                    createGrid(posizione.getNumero_righe(),posizione.getNumero_colonne());
                    startLevel1.setText("AGGIORNA OUTPUT");
                    break;
                case "AGGIORNA OUTPUT":
                    createGrid(posizione.getNumero_righe(),posizione.getNumero_colonne());
                    break;
            }
        });

        stopLevel1.setText("PAUSA");
        stopLevel1.setOnClickListener((view) -> {
            try {
                switch (stopLevel1.getText().toString()){
                    case "PAUSA":
                        stop=true;
                        stopLevel1.setText("RIPRENDI");
                        break;
                    case "RIPRENDI":
                        stop=false;
                        stopLevel1.setText("PAUSA");
                        break;
                }
            }catch (Exception e){
                Log.e("ERRORE",e.toString());
            }
        });

        return root;
    }

    private void startLevel1(){

        Timer gc = new Timer();
        gc.schedule(new TimerTask() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
            }
        },0,5000);

        this.greenPrc.setVisibility(View.VISIBLE);
        this.ballFinder = new BallFinder(null, true);
        this.greenFinder = new GreenFinder(null, true, 440, 80);

        manage_opencv(false);

        ArrayList<String> mosse = new ArrayList<>();

        boolean controlla_davanti = true;
        Ball target=null;
        Ball m = null;
        int ultima_riga = 0;
        boolean search = true;
        boolean mina_raggiunta = false;
        boolean trasporta_mina = false;
        boolean ok=true;
        boolean prova_in_corso = true;

        while (prova_in_corso){
            if (controlla_davanti) {
                target = cerca_mina_distante();
                target = cerca_mina_distante();
                if (target != null) {
                    while (!mina_raggiunta) {
                        m = cerca_mina_vicino(target.color);
                        m = cerca_mina_vicino(target.color);
                        if (m == null) {
                            move(Position.muovi_avanti);
                            mosse.add(0,Position.muovi_avanti);
                        } else {
                            raccogli_mina(m);
                            trasporta_mina = true;
                            mina_raggiunta = true;
                            search = false;
                            controlla_davanti = false;
                        }
                    }
                } else {
                    controlla_davanti = false;
                }
            } else if (trasporta_mina) {
                //ritorna a 0 0, rilascia la mina e ritona in posizione di partenza
                move(Position.muovi_ruota_destra);
                move(Position.muovi_ruota_destra);
                String ei;
                Iterator<String> it = mosse.iterator();
                while(it.hasNext()){
                    ei=it.next();
                    move(ei);
                    it.remove();
                }
                move(Position.muovi_avanti); //entro in zona sicura
                release();
                move(Position.muovi_indietro); //esco dalla zona sicura
                if(posizione.getOrientazione().equals(Position.orientazione_sinistra)){
                   move(Position.muovi_ruota_destra);
                }else{
                    if(ultima_riga==0){
                        move(Position.muovi_ruota_sinistra);
                    }else{
                        move(Position.muovi_ruota_destra);
                        move(Position.muovi_ruota_destra);
                    }
                }
                trasporta_mina = false;
                mina_raggiunta = false;
                search = true;
            } else {
                while (search) {
                    while (posizione.getRiga() < ultima_riga && ok) {
                        ok = move(Position.muovi_avanti);
                        if (ok) {
                            mosse.add(0,Position.muovi_avanti);
                        }
                    }
                    if (ok) {
                        if(!posizione.getOrientazione().equals(Position.orientazione_destra)){
                            move(Position.muovi_ruota_destra);
                        }
                        target = cerca_mina_distante();
                        target = cerca_mina_distante();
                        if (target != null) {
                            mosse.add(0,Position.muovi_ruota_sinistra);
                            while (!mina_raggiunta) {
                                m = cerca_mina_vicino(target.color);
                                m = cerca_mina_vicino(target.color);
                                if (m == null) {
                                    move(Position.muovi_avanti);
                                    mosse.add(0,Position.muovi_avanti);
                                } else {
                                    if(posizione.getColonna()==0){
                                        mosse.remove(0);
                                    }
                                    raccogli_mina(m);
                                    trasporta_mina = true;
                                    mina_raggiunta = true;
                                    search = false;
                                }
                            }
                        } else {
                            move(Position.muovi_ruota_sinistra);
                            ultima_riga++;
                        }
                    }else{
                        //PROVA TERMINATA, LIMITE SUPERIORE CAMPO RAGGIUNTO
                        move(Position.muovi_ruota_destra);
                        move(Position.muovi_ruota_destra);
                        while (posizione.getRiga() > 0){
                            move(Position.muovi_avanti);
                        }
                        search = false;
                        prova_in_corso = false;
                    }
                }
            }
        }


    }

    private void raccogli_mina(Ball x){
        float gradi_inizio = gradi_destinazione;

        //Punta la mina, in modo che il centro del cerchio sia al centro dello schermo
        punta_mina(x);
        gradi_destinazione = gradi();

        //Avanzo verso la mina
        move(Position.muovi_avanti);
        field.add(new Coordinates(posizione.getRiga(),posizione.getColonna(),x.color));

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
        while(bx > 260 || bx < 220) {
            move_correggi_angolo(d, 10, 150);

            Ball t = cerca_mina_vicino(bc);
            if(t!=null){
                bx=t.center.x;
            }
        }
    }

    private Ball cerca_mina_vicino(){
        ArrayList<Ball> bl = new ArrayList<>();
        Ball result=null;
        Ball b=null;
        int red=0;
        int blue=0;
        int yellow=0;
        for (int i=0; i<10;i++){
            try{
                Iterator<Ball> iter = elenco_mine.iterator();
                while(iter.hasNext()){
                    b=iter.next();
                    if(result==null || b.radius>result.radius){
                        result = b;
                    }
                }
                if(result!=null){
                    bl.add(result);
                    switch (result.color){
                        case "red":
                            red++;
                            break;
                        case "blue":
                            blue++;
                            break;
                        case "yellow":
                            yellow++;
                            break;
                    }
                    result = null;
                }
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("CERCA_DEBUG",e.toString());
            }
        }

        String n_mag = "";
        if(red >= blue && red >= yellow){
            n_mag="red";
        }else if(blue >= red && blue >= yellow){
            n_mag="blue";
        }else{
            n_mag="yellow";
        }

        Iterator<Ball> iter = bl.iterator();
        double temp=-1;
        double bd;
        while(iter.hasNext()){
            b=iter.next();
            bd=b.radius;
            if(b.color.equals(n_mag)){
                if(bd>temp || temp<0){
                    temp=bd;
                    result=b;
                }
            }
        }

        if(result==null){
            Log.i("CERCA_DEBUG", "OUTPUT -> null");
        }else {
            Log.i("CERCA_DEBUG", "OUTPUT -> r:" + result.center.x + " c:" + result.color);
        }

        return result;
    }

    //Cerca la mina più grande del colore specificato nei parametri
    private Ball cerca_mina_vicino(String c){

        manage_opencv(true,false,true,false);

        ArrayList<Ball> bl = new ArrayList<>();
        Ball result=null;
        Ball b;
        for (int i=0; i<5;i++){
            try{
                if(elenco_mine!=null) {
                    Iterator<Ball> iter = elenco_mine.iterator();
                    while (iter.hasNext()) {
                        b = iter.next();
                        if (c.equals(b.color) && (result == null || b.radius > result.radius)) {
                            result = b;
                        }
                    }
                    bl.add(result);
                    result = null;
                }
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("CERCA_DEBUG",e.toString());
            }
        }

        Iterator<Ball> iter = bl.iterator();
        double temp=-1;
        double bd;
        while(iter.hasNext()){
            b=iter.next();
            if(b!=null) {
                bd = b.radius;
                if ((bd > temp || temp < 0) && bd>=40) {
                    temp = bd;
                    result = b;
                }
            }
        }

        if(result==null){
            Log.i("CERCA_DEBUG", "VICINO OUTPUT -> null");
        }else {
            Log.i("CERCA_DEBUG", "VICINO OUTPUT -> c:" + result.center.x + " r:" + result.radius +" c:" + result.color);
        }

        manage_opencv(false);
        return result;
    }

    private Ball cerca_mina_distante(){

        manage_opencv(true,false,true,false);

        ArrayList<Ball> bl = new ArrayList<>();
        Ball result=null;
        Ball b;
        int red=0;
        int blue=0;
        int yellow=0;
        for (int c=0; c<5;c++){
            try{
                if(elenco_mine!=null){
                    Iterator<Ball> iter = elenco_mine.iterator();
                    while(iter.hasNext()){
                        b=iter.next();
                        b.toString();
                        if(b.center.x > 160 && b.center.x < 320 && b.center.y<390 && b.radius>5){
                            result = b;
                        }
                    }
                    if(result!=null){
                        bl.add(result);
                        switch (result.color){
                            case "red":
                                red++;
                                break;
                            case "blue":
                                blue++;
                                break;
                            case "yellow":
                                yellow++;
                                break;
                        }
                        result = null;
                    }
                }
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("CERCA_DEBUG",e.toString());
            }
        }

        String n_mag = "";
        if(red >= blue && red >= yellow){
            n_mag="red";
        }else if(blue >= red && blue >= yellow){
            n_mag="blue";
        }else{
            n_mag="yellow";
        }

        Iterator<Ball> iter = bl.iterator();
        double temp=-1;
        double bd;
        while(iter.hasNext()){
            b=iter.next();
            bd=Math.abs(max_frame_height/2 - b.center.x);
            if(b.color.equals(n_mag)){
                if(bd<temp || temp<0){
                    temp=bd;
                    result=b;
                }
            }
        }

        if(result==null){
            Log.i("CERCA_DEBUG", "DISTANTE OUTPUT -> null");
        }else {
            Log.i("CERCA_DEBUG", "DISTANTE OUTPUT -> x:" + result.center.x + " r:"+result.radius+" c:" + result.color);
        }

        manage_opencv(false);

        return result;
    }

    private void manage_opencv(Boolean opencv_attivo,boolean linee,boolean mine,boolean limite){
        this.check_camera=opencv_attivo;
        this.trova_linee=linee;
        this.trova_mine=mine;
        this.trova_limite=limite;
    }
    private void manage_opencv(Boolean opencv_attivo){
        this.check_camera=opencv_attivo;
    }

    private void print_pc(){
        Log.i("POSIZIONE_DEBUG",posizione.getOrientazione()+" r:"+posizione.getRiga()+" c:"+posizione.getColonna());
        Log.i("CAMPO_DEBUG","r:"+posizione.getNumero_righe()+" c:"+posizione.getNumero_colonne());
    }

    private boolean limite_campo(){

        manage_opencv(true,false,false,true);

        boolean result=false;
        double prc = this.green_perc;
        for (int c=0; c<5;c++){
            if(this.green_perc > prc){
                prc=this.green_perc;
            }
            try{
                Thread.currentThread().sleep(500);
            }catch(Exception e){
                Log.e("LIMITE_CAMPO",e.toString());
            }
        }
        if(prc >= 40){
            result=true;
            if(posizione.getOrientazione().equals(Position.orientazione_destra) &&
                    posizione.getNumero_colonne() > posizione.getColonna()+1){
                posizione.aggiorna_campo(posizione.getNumero_righe(),posizione.getColonna()+1);
            }else if(posizione.getOrientazione().equals(Position.orientazione_alto) &&
                    posizione.getNumero_righe() > posizione.getRiga()+1){
                posizione.aggiorna_campo(posizione.getRiga()+1,posizione.getNumero_colonne());
            }
        }

        manage_opencv(false);

        Log.i("CERCA_LIMITE",""+result);

        return result;
    }

    private void correggi_angolo(){
        boolean first=true;
        //distanza tra angolo attuale e angolo di destinazione
        float c = Math.abs( ((gradi_destinazione - gradi())+180)%360 - 180 );
        float c_prev=c;
        float starting_c=c;
        String d = Position.muovi_ruota_destra;
        while(c > 1) {
            Log.i("CORREGGI_ANGOLO_DEBUG","c_prev: "+c_prev+"  c:"+c);
            if(first) {
                move_correggi_angolo(d, 10, 400);
                first=false;
            }else{
                move_correggi_angolo(d, 10, 200);
            }
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

        while(stop){}

        boolean limite_raggiunto = false;
        if(action.equals(Position.muovi_avanti)){
            limite_raggiunto = limite_campo();
        }
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
                sharedElements.motorLeft.waitUntilReady();
                sharedElements.motorRight.waitUntilReady();
                sharedElements.motorLeft.setTimeSpeed(speed_sx, 0, time, 0, true);
                sharedElements.motorRight.setTimeSpeed(speed_dx, 0, time, 0, true);
                sharedElements.motorLeft.waitCompletion();
                sharedElements.motorRight.waitCompletion();
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

    private void manageOpenCV(View root){
        mOpenCvCameraView =root.findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(max_frame_width, max_frame_height);

        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "Camera Stopped");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                if(frame_out!=null){
                    frame_out.release();
                }

                frame = inputFrame.rgba();

                int x = (frame.width()-frame.height())/2;
                Rect area = new Rect(new Point(x,0 ),
                        new Point(x+frame.height(),frame.height()));
                frame_draw = new Mat(frame,area);
                Core.rotate(frame_draw,frame_draw,Core.ROTATE_90_CLOCKWISE);
                frame_cut = frame_draw.clone();

                if(check_camera) {
                    if(trova_linee) {
                        LineFinder lineFinder = new LineFinder(frame_cut, true);
                        lineFinder.setThreshold(300, 20);
                        lineFinder.setOrientation("landscape");
                        ArrayList<Double> li = lineFinder.findLine(frame_draw);
                        Iterator<Double> iter = li.iterator();
                        String ang = "";
                        while (iter.hasNext()) {
                            ang = ang + "  " + iter.next();
                        }
                        Log.e("line", ang);
                    }

                    if(trova_mine) {
                        ballFinder.setFrame(frame_cut);
                        ballFinder.setViewRatio(0.0f);
                        ballFinder.setOrientation("landscape");
                        ArrayList<Ball> f = ballFinder.findBalls(frame_draw);
                        elenco_mine = f;
                    }

                    if(trova_limite) {
                        greenFinder.setFrame(frame_cut);
                        greenFinder.setViewRatio(0.0f);
                        greenFinder.setOrientation("landscape");
                        double prc = greenFinder.findGreen(frame_draw);
                        green_perc = prc;
                        greenPrc.setText("Percentage: " + prc);
                    }
                }


                frame_out = Mat.zeros(frame.size(),frame.type());
                frame_draw.copyTo(frame_out.submat(area));

                frame.release();
                frame_draw.release();
                frame_cut.release();

                return frame_out;
            }
        });
        mOpenCvCameraView.enableView();
    }


    //output
    private void createGrid(int numRow, int numCol){
        Coordinates temp;
        this.fieldContainer.removeAllViewsInLayout();
        // Questo ciclo for genera la grglia con il numero delle celle e lo switch
        for(int i = numRow - 1; i >= 0; i--){
            TableRow row = (TableRow) LayoutInflater.from(this.fieldContainer.getContext()).inflate(R.layout.grid_row, null);
            for(int j = 0; j < numCol; j++){
                TableLayout cell = (TableLayout) LayoutInflater.from(this.fieldContainer.getContext()).inflate(R.layout.grid_cell, null);
                cell.setTag("cell_"+i+"_"+j);

                temp=minaInPosizione(i,j,field);
                if(temp!=null){
                    switch (temp.color){
                        case "yellow":
                            cell.setBackgroundColor(Color.parseColor(giallo));
                            break;
                        case "blue":
                            cell.setBackgroundColor(Color.parseColor(blu));
                            break;
                        case "red":
                            cell.setBackgroundColor(Color.parseColor(rosso));
                            break;
                    }
                }

                //aggiunge testo
                ((TextView) cell.findViewById(R.id.cell_position)).setText(i+", "+j);

                //this.field.add(new Coordinates(i, j, cell.findViewById(R.id.select_cell)));
                row.addView(cell, this.fieldContainer.getLayoutParams());
                row.invalidate();
            }
            this.fieldContainer.addView(row, this.fieldContainer.getLayoutParams());
            this.fieldContainer.invalidate();
        }
    }

    public Coordinates minaInPosizione(int x, int y, ArrayList<Coordinates> al){
        Coordinates result=null;
        Coordinates it;
        Iterator<Coordinates> iter = al.iterator();
        while(iter.hasNext() && result==null){
            it = iter.next();
            if (it.x==x && it.y==y && !it.color.equals("")){
                result = it;
            }
        }
        return result;
    }


}