package com.ilareguy.spear.view;

import android.content.Context;
import android.view.View;

import com.ilareguy.spear.R;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ImageViewerOverlay extends ConstraintLayout{

    //

    public ImageViewerOverlay(Context context){
        super(context);
        init();
    }

    private void init(){
        View view = View.inflate(getContext(), R.layout.image_viewer_overlay, this);
    }

}
