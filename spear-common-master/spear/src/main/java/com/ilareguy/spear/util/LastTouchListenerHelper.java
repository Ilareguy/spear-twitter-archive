package com.ilareguy.spear.util;

import android.view.MotionEvent;
import android.view.View;

public abstract class LastTouchListenerHelper {
    private static float[] lastTouchXY = new float[2];

    public static void listen(final View v){
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchXY[0] = event.getRawX();
                    lastTouchXY[1] = event.getRawY();
                }

                return false;
            }
        });
    }

    public static float[] getLastTouchXY(){ return lastTouchXY; }
}
