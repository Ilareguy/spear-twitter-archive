package com.ilareguy.spear.twitter;

import android.content.Context;

import com.ilareguy.spear.MediaViewerAbstract;
import com.ilareguy.spear.twitter.data.Tweet;

import java.util.List;

public class TwitterMediaViewer extends MediaViewerAbstract<Tweet.Entities.MediaEntity>{

    public TwitterMediaViewer(final Context context, final List<Tweet.Entities.MediaEntity> medias){
        super(context, medias);
    }

    @Override
    protected String getMediaUrl(final Tweet.Entities.MediaEntity media){
        return media.mediaUrl;
    }

    @Override
    protected String getMediaUrlHD(final Tweet.Entities.MediaEntity media){
        return media.mediaUrl;
    }
}
