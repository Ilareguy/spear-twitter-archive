package com.ilareguy.spear.util;

import android.content.Context;

import com.ilareguy.spear.view.ImageViewerOverlay;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

public final class UrlMediaViewer{
    public static final class UnsupportedUrlException extends Exception{
        //
    }

    private final String url;
    private final Context context;

    public UrlMediaViewer(final Context context, final String url){
        this.url = url;
        this.context = context;
    }

    public final void show() throws UnsupportedUrlException{
        show(0);
    }

    public final void show(int startPosition) throws UnsupportedUrlException{
        // Retrieve a list of medias
        /* @TODO: This could be a link to some sort of online album. Retrieve a list of pictures in
         * an AsyncTask.
         */
        List<String> medias = new ArrayList<>();
        medias.add(url);

        // Build & show
        new ImageViewer.Builder<>(context, medias)
                .hideStatusBar(false)
                .allowZooming(true)
                .setStartPosition(startPosition)
                .setOverlayView(new ImageViewerOverlay(context))
                .show();
    }

}
