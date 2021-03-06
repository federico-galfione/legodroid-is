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
    private int red_lower2 = 0;
    private int red_upper2 = 10;

    private int blue_lower = 105;
    private int blue_upper = 130;

    private int yellow_lower = 16;
    private int yellow_upper = 31;

    /*
    private int yellow_lower = 20;
    private int yellow_upper = 80;
    */

    private boolean debug = false;
    private String orientation = "portrait";

    private Mat frame;

    private Mat hsv;
    private Mat mask_sat;
    private Mat kernel;
    private Mat hue;
    private Mat mask_red;
    private Mat mask_red2;
    private Mat mask_blue;
    private Mat mask_yellow;
    private Mat mask_hue;
    private Mat mask;
    private Mat hierarchy;
    private Mat circles;
    private Mat check;



    public BallFinder(Mat frame) {
        this.frame = frame;
    }

    public BallFinder(Mat frame, boolean debug) {
        this(frame);
        if (debug) {
            this.frame = frame;
            this.debug = true;
        }
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
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

        hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        //convert RGB/BGR to HSV (hue saturation value)
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);
        //Divides a multi-channel array into several single-channel arrays
        Core.split(hsv, split_hsv);

        mask_sat = new Mat();
        //Applies a fixed-level threshold to each array element.
        //Application example: Separate out regions of an image corresponding to objects which we want to analyze.
        //This separation is based on the variation of intensity between the object pixels and the background pixels.
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        //size	2D array size: Size(cols, rows).
        //In the Size() constructor, the number of rows and the number of columns go in the reverse order
        kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        //Performs advanced morphological transformations.
        //https://docs.opencv.org/2.4/doc/tutorials/imgproc/opening_closing_hats/opening_closing_hats.html
        Imgproc.morphologyEx(mask_sat, mask_sat, Imgproc.MORPH_OPEN, kernel);

        hue = split_hsv.get(0);
        mask_red = new Mat();
        mask_red2 = new Mat();
        mask_blue = new Mat();
        mask_yellow = new Mat();

        //Checks if array elements lie between the elements of two other arrays.
        Core.inRange(hsv, new Scalar(red_lower, 0, 0), new Scalar(red_upper, 255, 255), mask_red);
        Core.inRange(hsv, new Scalar(red_lower2, 0, 0), new Scalar(red_upper2, 255, 255), mask_red2);
        Core.inRange(hsv, new Scalar(blue_lower, 0, 0), new Scalar(blue_upper, 255, 255), mask_blue);
        Core.inRange(hsv, new Scalar(yellow_lower, 0, 0), new Scalar(yellow_upper, 255, 255), mask_yellow);

        mask_hue = new Mat();
        mask = new Mat();

        Core.bitwise_or(mask_red, mask_red2, mask_hue);
        Core.bitwise_or(mask_hue, mask_blue, mask_hue);
        Core.bitwise_or(mask_hue, mask_yellow, mask_hue);
        Core.bitwise_and(mask_sat, mask_hue, mask);
        /*Core.bitwise_or(mask_red, mask_blue, mask_hue);
        Core.bitwise_or(mask_hue, mask_yellow, mask_hue);
        Core.bitwise_and(mask_sat, mask_hue, mask);*/


        List<MatOfPoint> contours = new ArrayList<>();
        hierarchy = new Mat();
        circles=new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (debug) {
            Point p1 = new Point(frame.width() * view_ratio, 0);
            Point p2 = new Point(frame.width() * view_ratio, frame.height());

            if (orientation == "landscape") {
                p1 = new Point(0, frame.height() * view_ratio);
                p2 = new Point(frame.width(), frame.height() * view_ratio);
            }

            Imgproc.line(destinazione, p1, p2, new Scalar(0, 255, 255), 2);
        }

        //VISUALIZZAZIONE
        float[] radius = new float[1];
        Point center = new Point();
        String testString = "";
        for (MatOfPoint c : contours) {
            Imgproc.minEnclosingCircle(new MatOfPoint2f(c.toArray()), center, radius);

            boolean cond = center.x > frame.width() * view_ratio;

            if (orientation == "landscape")
                cond = center.y > frame.height() * view_ratio;

            if (cond && Imgproc.contourArea(c) > min_area) {
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

                if(!color.equals("unknown")){
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

                        Imgproc.circle(destinazione, center, (int) radius[0], color_rgb, 2);
                    }
                }
            }
        }


        hsv.release();
        mask_sat.release();
        kernel.release();
        hue.release();
        mask_red.release();
        mask_red2.release();
        mask_blue.release();
        mask_yellow.release();
        mask_hue.release();
        mask.release();
        hierarchy.release();
        circles.release();

        return balls;
    }

    private boolean checkBalls(ArrayList<Ball> list, Ball ball){

        Iterator<Ball> iter = list.iterator();
        Ball b;
        boolean add = true;
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
        return add;
    }

    private boolean check_existance(Point center, float radius, Mat mask){
        try {
            if(center.x - radius <0 || center.y - radius <0 || center.x + radius>mask.width() || center.y + radius>mask.height()){
                return false;
            }
            Rect area = new Rect(new Point(center.x - radius, center.y - radius),
                    new Point(center.x + radius, center.y + radius));
            check = new Mat(mask, area);
            Size Tot = check.size();
            final int area_test = Core.countNonZero(check);
            double percentage = (area_test * 100) / (Tot.height * Tot.width);
            check.release();

            check.release();

            if (percentage > 55) {// percentuale effettiva 79
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