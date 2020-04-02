package com.example.softwareengineeringapp.classes;

import android.view.MotionEvent;
import android.widget.Switch;

public class Coordinates {
    public int x;
    public int y;
    public String color;

    public Coordinates(int x, int y){
        this.x = x;
        this.y = y;
        this.color = "";
    }

    public Coordinates(int x, int y, String s){
        this.x = x;
        this.y = y;
        this.color = s;
    }

}
