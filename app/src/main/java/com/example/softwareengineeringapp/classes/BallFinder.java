package com.example.softwareengineeringapp.classes;

/*import android.icu.text.Edits;*/
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BallFinder {
    private float view_ratio = 0.0f;
    private int min_area = 0;

    private int sat_lower = 96;
    private int sat_upper = 255;
    private int red_lower = 160;
    private int red_upper = 180;
    private int blue_lower = 105;
    private int blue_upper = 130;
    /*
    private int yellow_lower = 16;
    private int yellow_upper = 31;
     */

    private int yellow_lower = 30;
    private int yellow_upper = 80;

    private boolean debug = false;
    private String orientation = "portrait";

    private Mat frame;

    public BallFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public BallFinder(Mat frame, boolean debug) {
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

    public void setViewRatio(float view_ratio) {
        this.view_ratio = view_ratio;
    }

    public void setMinArea(int min_area) {
        this.min_area = min_area;
    }

    public void setSaturationThreshold(int lower, int upper) {
        this.sat_lower = lower;
        this.sat_upper = upper;
    }

    public void setRedThreshold(int lower, int upper) {
        this.red_lower = lower;
        this.red_upper = upper;
    }

    public void setBlueThreshold(int lower, int upper) {
        this.blue_lower = lower;
        this.blue_upper = upper;
    }

    public void setYellowThreshold(int lower, int upper) {
        this.yellow_lower = lower;
        this.yellow_upper= upper;
    }

    public ArrayList<Ball> /*Mat*/ findBalls(Mat destinazione) {
        ArrayList<Ball> balls = new ArrayList<>();

        Mat hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        //convert RGB/BGR to HSV (hue saturation value)
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);
        //Divides a multi-channel array into several single-channel arrays
        Core.split(hsv, split_hsv);

        Mat mask_sat = new Mat();
        //Applies a fixed-level threshold to each array element.
        //Application example: Separate out regions of an image corresponding to objects which we want to analyze.
        //This separation is based on the variation of intensity between the object pixels and the background pixels.
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        //size	2D array size: Size(cols, rows).
        //In the Size() constructor, the number of rows and the number of columns go in the reverse order
        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        //Performs advanced morphological transformations.
        //https://docs.opencv.org/2.4/doc/tutorials/imgproc/opening_closing_hats/opening_closing_hats.html
        Imgproc.morphologyEx(mask_sat, mask_sat, Imgproc.MORPH_OPEN, kernel);

        Mat hue = split_hsv.get(0);
        Mat mask_red = new Mat();
        Mat mask_blue = new Mat();
        Mat mask_yellow = new Mat();

        //Checks if array elements lie between the elements of two other arrays.
        Core.inRange(hsv, new Scalar(red_lower, 0, 0), new Scalar(red_upper, 255, 255), mask_red);
        Core.inRange(hsv, new Scalar(blue_lower, 0, 0), new Scalar(blue_upper, 255, 255), mask_blue);
        Core.inRange(hsv, new Scalar(yellow_lower, 0, 0), new Scalar(yellow_upper, 255, 255), mask_yellow);

        Mat mask_hue = new Mat();
        Mat mask = new Mat();

        Core.bitwise_or(mask_red, mask_blue, mask_hue);
        Core.bitwise_or(mask_hue, mask_yellow, mask_hue);
        Core.bitwise_and(mask_sat, mask_hue, mask);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat circles=new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        /*Imgproc.HoughCircles(mask,circles,Imgproc.HOUGH_GRADIENT, 2, 100, 100, 100, 0, 500);

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(frame, center, radius, new Scalar(255,0,255), 3, 8, 0 );
        }*/
        if (debug) {
            Point p1 = new Point(frame.width() * view_ratio, 0);
            Point p2 = new Point(frame.width() * view_ratio, frame.height());

            if (orientation == "landscape") {
                p1 = new Point(0, frame.height() * view_ratio);
                p2 = new Point(frame.width(), frame.height() * view_ratio);
            }

            Imgproc.line(destinazione, p1, p2, new Scalar(0, 255, 255), 2);

            //COMMENTATA DAL CASA, SCOMMENTARE IN CASO DI EMERGENZA
            //for (int i = 0; i < contours.size(); i++)
            //Imgproc.drawContours(frame, contours, i, new Scalar(255, 0, 0), 2);
        }

        //VISUALIZZAZIONE PALLE
        float[] radius = new float[1];
        Point center = new Point();
        String testString = "";
        for (MatOfPoint c : contours) {
            Imgproc.minEnclosingCircle(new MatOfPoint2f(c.toArray()), center, radius);

            boolean cond = center.x > frame.width() * view_ratio;

            if (orientation == "landscape")
                cond = center.y > frame.height() * view_ratio;

            if (cond && Imgproc.contourArea(c) > min_area) {
                // TODO: add color mean for area_hue
                int area_hue = (int) hue.get((int) center.y, (int) center.x)[0];
                String color;

                if (area_hue >= red_lower && area_hue <= red_upper)
                    color = "red";
                else if (area_hue >= blue_lower && area_hue <= blue_upper)
                    color = "blue";
                else if (area_hue >= yellow_lower && area_hue <= yellow_upper)
                    color = "yellow";
                else
                    color = "unknown";

                Ball myBall = new Ball(new Point(center.x,center.y), radius[0], color);
                boolean chk = checkBalls(balls, myBall);
                boolean chk2 = true;
                if(chk)
                    chk2 = check_existance(center,radius[0],mask);

                if (debug && chk2) {
                    balls.add(myBall);
                    Scalar color_rgb;

                    if (color == "red")
                        color_rgb = new Scalar(255, 0, 0);
                    else if (color == "blue")
                        color_rgb = new Scalar(0, 0, 255);
                    else if (color == "yellow")
                        color_rgb = new Scalar(255, 255, 0);
                    else
                        color_rgb = new Scalar(0, 0, 0);

                    if (color == "red" || color == "blue" || color == "yellow") {
                        Imgproc.circle(destinazione, center, (int) radius[0], color_rgb, 2);
                    }
                }
            }
        }
        Log.e("TEST_CENTER", testString);

        Log.e("BALLS", Arrays.toString(balls.stream().map(x -> x.center).toArray()));
        return balls;
    }

    //Controllo se ball.center è contenuto all'interno di altri cherchi.
    //Se così fosse, tengo il maggiore tra i due ed elimino l'altro.
    private boolean checkBalls(ArrayList<Ball> list, Ball ball){

            Iterator<Ball> iter = list.iterator();
            Ball b;
            boolean add = true;
        try {
            while (iter.hasNext()) {
                b = iter.next();
                if (Math.pow(ball.center.x - b.center.x, 2) + Math.pow(ball.center.y - b.center.y, 2) <= Math.pow(b.radius, 2)) {
                    if (ball.radius > b.radius) {
                        iter.remove();
                    } else {
                        add = false;
                    }
                }
            }
            if (add) {
                //list.add(ball);
            }

        }catch (Exception e){
            Log.e("EXCEPTION ITER", e.toString());
        }
        return add;
    }
    private boolean check_existance(Point center, float radius, Mat mask){
        try {
            if(center.x - radius <0 || center.y - radius <0 || center.x + radius>mask.width() || center.y + radius>mask.height()){
                return false;
            }
            Rect area = new Rect(new Point(center.x - radius, center.y - radius),
                    new Point(center.x + radius, center.y + radius));
            Mat check = new Mat(mask, area);
            Log.e("check1", "mat creation");
            Size Tot = check.size();
            final int area_test = Core.countNonZero(check);
            double percentage = (area_test * 100) / (Tot.height * Tot.width);
            check.release();
            Log.e("check1", "mat destroy");
            if (percentage > 45) {// percentuale effettiva 79
                return true;
            } else {
                return false;
            }
        }catch(Exception e){
            Log.e("checkstrano", e.toString());
        }
        return true;
    }

}