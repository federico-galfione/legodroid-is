package com.example.softwareengineeringapp.classes;

/*import android.icu.text.Edits;*/
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GreenFinder {
    private float view_ratio = 0.0f;
    private int min_area = 0;

    private int sat_lower = 96;
    private int sat_upper = 255;
    private int green_lower = 40;
    private int green_upper = 70;

    private int height;
    private int size;

    private boolean debug = false;
    private String orientation = "portrait";

    private Mat frame;

    private Mat hsv;
    private ArrayList<Mat> split_hsv;
    private Mat mask_sat;
    private Mat hue;
    private Mat mask_green;
    private Mat mask;


    public GreenFinder(Mat frame, int height, int size) {
        //this.frame = frame.clone();
        this.height = height;
        this.size = size;
    }

    public GreenFinder(Mat frame, boolean debug, int height, int size) {
        this(frame, height, size);
        if (debug) {
            this.frame = frame;
            this.debug = true;
        }

    }

    public void setFrame(Mat frame) {
        this.frame =frame;
        Rect area = new Rect(new Point(0,this.height - (this.size/2) ),
                new Point(frame.width(),this.height + (this.size/2)));
        this.frame = new Mat(this.frame,area);
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
        this.green_lower = lower;
        this.green_upper = upper;
    }


    public /*Mat*/ double findGreen(Mat frame1){

        Imgproc.rectangle(frame1,
                new Point(0,this.height - (this.size/2) ),
                new Point(frame.width(),this.height + (this.size/2)),
                new Scalar(0,255,0), 2);

        hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        //convert RGB/BGR to HSV (hue saturation value)
        //IMPORTANTE: cambiato da RGB2HSV a BRG2HSV
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        //Divides a multi-channel array into several single-channel arrays
        Core.split(hsv, split_hsv);

        mask_sat = new Mat();

        //Applies a fixed-level threshold to each array element.
        //Application example: Separate out regions of an image corresponding to objects which we want to analyze.
        //This separation is based on the variation of intensity between the object pixels and the background pixels.
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        //size	2D array size: Size(cols, rows).
        //In the Size() constructor, the number of rows and the number of columns go in the reverse order
        //Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        //Performs advanced morphological transformations.
        //https://docs.opencv.org/2.4/doc/tutorials/imgproc/opening_closing_hats/opening_closing_hats.html
        //Imgproc.morphologyEx(mask_sat, mask_sat, Imgproc.MORPH_OPEN, kernel);

        hue = split_hsv.get(0);
        mask_green = new Mat();


        //Checks if array elements lie between the elements of two other arrays.
        Core.inRange(hsv, new Scalar(green_lower, 0, 0), new Scalar(green_upper, 255, 255), mask_green);


        mask = new Mat();


        Core.bitwise_and(mask_sat, mask_green, mask);



        Size Tot=mask.size();

        final int Green = Core.countNonZero(mask);

        double percentage = (Green*100)/(Tot.height * Tot.width);
        Log.e("TEST_GREEN_FINDER","Percentage: "+percentage);
        //return mask;


        //pulizia
        hsv.release();
        mask_sat.release();
        hue.release();
        mask_green.release();
        mask.release();
        frame.release();

        return percentage;
    }

}