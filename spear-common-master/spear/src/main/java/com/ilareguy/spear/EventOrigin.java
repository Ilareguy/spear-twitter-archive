package com.ilareguy.spear;

public class EventOrigin{
    public final int cx, cy;

    public EventOrigin(int cx, int cy){
        this.cx = cx;
        this.cy = cy;
    }

    public EventOrigin(int[] ints){
        this.cx = ints[0];
        this.cy = ints[1];
    }

    public EventOrigin(float[] floats){
        this.cx = (int) floats[0];
        this.cy = (int) floats[1];
    }
}
