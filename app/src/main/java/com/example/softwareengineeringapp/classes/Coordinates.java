package com.example.softwareengineeringapp.classes;

import android.view.MotionEvent;
import android.widget.Switch;

public class Coordinates {
    public int x;
    public int y;
    private Switch selector;

    public Coordinates(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Coordinates(int x, int y, Switch s){
        this.x = x;
        this.y = y;
        this.selector = s;
    }

    public boolean getHasBall(){
        return (this.selector != null) ? this.selector.isChecked() : false;
    }
}
