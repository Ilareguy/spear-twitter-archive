package com.ilareguy.spear.util;

import android.graphics.Outline;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.ilareguy.spear.util.Helper;

public abstract class ViewHelper {

    private static final TypedValue outValue = new TypedValue();

    public static void applyCardShapeTo(View view){
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        Helper.dpToPx(4, view.getResources()));
            }
        });
        view.setClipToOutline(true);
    }

    public static void applySelectableItemBackgroundTo(View view){
        view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);
    }

    /*public static void setMargins(View v, int l, int t, int r, int b){
        if(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }*/

}
