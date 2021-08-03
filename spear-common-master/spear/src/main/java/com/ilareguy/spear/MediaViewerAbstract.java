package com.ilareguy.spear;

import android.content.Context;

import com.ilareguy.spear.view.ImageViewerOverlay;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.List;

public abstract class MediaViewerAbstract<T> implements ImageViewer.Formatter<T> {
    private final List<T> medias;
    private final Context context;

    public MediaViewerAbstract(final Context context, final List<T> medias){
        this.medias = medias;
        this.context = context;
    }

    public final void show(){
        show(0);
    }

    public final void show(int startPosition){
        // Build & show
        new ImageViewer.Builder<>(context, medias)
                .hideStatusBar(false)
                .allowZooming(true)
                .setStartPosition(startPosition)
                //.setImageChangeListener(onImageViewerImageChangeListener)
                .setOverlayView(new ImageViewerOverlay(context))
                .setFormatter(this)
                .show();
    }

    @Override
    public String format(T t){
        return getMediaUrl(t);
    }

    /**
     * Must return the media's default URL.
     * In the case of a video, GIF or otherwise animated media, this must return a URL to the
     * media's thumbnail.
     */
    protected abstract String getMediaUrl(final T media);

    /**
     * Must return the media's high definition URL.
     * In the case of a video, GIF or otherwise animated media, this must return a URL to the
     * media's thumbnail.
     */
    protected abstract String getMediaUrlHD(final T media);

}
