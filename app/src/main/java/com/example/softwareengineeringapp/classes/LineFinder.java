package com.example.softwareengineeringapp.classes;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

//CLASSE NON UTILIZZATA NELLE PROVE

public class LineFinder {
    private int thresh_lower;
    private int thresh_upper;

    private double angoloRette=55; //quadrante 1, da 90 (alto) a 0(destra)
    private double offset=3;
    private double altezzaP1 = 200;
    private double altezzaP2 = 260;

    private boolean debug = false;
    private String orientation = "portrait";

    private Mat frame;

    public LineFinder(Mat frame) {
        this.frame = frame;
    }

    public LineFinder(Mat frame, boolean debug) {
        this(frame);

        if (debug) {
            this.frame = frame;
            this.debug = true;
        }
    }

    public void setOrientation(String orientation) {
        if (orientation == "landscape" || orientation == "portrait")
            this.orientation = orientation;
        else
            throw new IllegalArgumentException("Invalid orientation, only \"portrait\" or \"landscape\" are allowed");
    }

    public void setThreshold(int center, int size) {
        this.thresh_lower = center - size / 2;
        this.thresh_upper = center + size / 2;
    }

    private ArrayList<Double> getLine(Mat frame, Mat lines) {

        ArrayList<Double> angoli = new ArrayList<>();

        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            Point p1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point p2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

            /*boolean cond = thresh_lower <= p1.x && p1.x <= thresh_upper &&
                           thresh_lower <= p2.x && p2.x <= thresh_upper;

            if (orientation == "landscape")
                cond = thresh_lower <= p1.y && p1.y <= thresh_upper &&
                       thresh_lower <= p2.y && p2.y <= thresh_upper;*/

            boolean checkL = false;
            boolean checkR = false;
            if(p1.x==p2.x && p1.y!=p2.y){
                //Eq. retta:  x = p1.x
            }else if(p1.x!=p2.x && p1.y==p2.y){
                //Eq. retta:  y = p1.y
            }else{
                //Eq. retta:  (x - p1.x)/(p2.x - p1.x) = (y - p1.y)/(p2.y - p1.y)
                double altezzaP3P4 = Math.tan(Math.toRadians(angoloRette))*frame.width();
                double altezzaP3 = altezzaP3P4 - altezzaP1;
                double altezzaP4 = altezzaP3P4 - altezzaP2;
                double r = ((0-p1.y)/(p2.y-p1.y)) + p1.x/(p2.x-p1.x);
                double k = p2.x - p1.x;
                double x_ = r*k;
                double m = Math.tan(Math.toRadians(angoloRette));
                checkL = x_ >= altezzaP1/m && x_ <= altezzaP2/m;
                checkR = x_ >= altezzaP4/m && x_ <= altezzaP3/m;
            }

            //if (cond) {
                if (debug){
                    double na=90-angoloRette;
                    double l1=na-offset;
                    double h1=na+offset;
                    double d=90+(90-na);
                    double l2=d-offset;
                    double h2=d+offset;

                    if( ( checkL && Math.toDegrees(theta) >= l1 && Math.toDegrees(theta) <= h1 ) ||
                            ( checkR && Math.toDegrees(theta) >= l2 && Math.toDegrees(theta) <= h2 ) ){
                        Imgproc.line(frame, p1, p2, new Scalar(255, 255, 0), 2);
                    }
                }

                angoli.add(Math.toDegrees(theta));

            //}
        }

        return angoli;
    }

    public ArrayList<Double> findLine(Mat destinazione) {
        Mat frame_gray = new Mat();
        Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(frame_gray, frame_gray, 3);

        Mat edges = new Mat();
        Mat lines = new Mat();

        Imgproc.Canny(frame_gray, edges, 20, 50);
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);

        if (debug) {
            double m=Math.tan(Math.toRadians(angoloRette));
            //y = m * x + altezza
            //x = ( y + altezza ) / m
            double altezzaP3P4 = Math.tan(Math.toRadians(angoloRette))*frame.width();
            double altezzaP3 = altezzaP3P4 - altezzaP1;
            double altezzaP4 = altezzaP3P4 - altezzaP2;
            Imgproc.line(destinazione,
                    new Point(0, altezzaP1), new Point(altezzaP1/m, 0), //R1
                    new Scalar(0, 255, 0), 2);
            Imgproc.line(destinazione,
                    new Point(0, altezzaP2), new Point(altezzaP2/m, 0), //R2
                    new Scalar(0, 255, 0), 2);
            Imgproc.line(destinazione,
                    new Point(frame.width(), -((-m)*frame.width() + altezzaP3)), new Point(altezzaP3/m, 0), //R3
                    new Scalar(0, 255, 0), 2);
            Imgproc.line(destinazione,
                    new Point(frame.width(), -((-m)*frame.width() + altezzaP4)), new Point(altezzaP4/m, 0), //R4
                    new Scalar(0, 255, 0), 2);
        }

        return getLine(destinazione, lines);
    }
}