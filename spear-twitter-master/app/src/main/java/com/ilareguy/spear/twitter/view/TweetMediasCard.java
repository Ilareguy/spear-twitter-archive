package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.util.ViewHelper;

import java.util.List;

public final class TweetMediasCard extends SimpleDraweeView {

    private final OnClickListener onDraweeClickListener = new OnClickListener(){
        @Override
        public void onClick(View view){
            MainActivity.getInstance().viewMedias(medias, 0);
        }
    };

    private static final ViewGroup.LayoutParams LAYOUT_PARAMS = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            600
    );

    private static final ResizeOptions mediaResizeOptions = new ResizeOptions(400, 400);

    private final List<Tweet.Entities.MediaEntity> medias;

    public TweetMediasCard(Context context, final List<Tweet.Entities.MediaEntity> medias){
        super(context, null, R.style.SelectableItemForeground);
        this.medias = medias;
        init();
    }

    private void init(){
        // Show the first media only
        setOnClickListener(onDraweeClickListener);
        setLayoutParams(LAYOUT_PARAMS);

        setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.parse(medias.get(0).mediaUrl))
            .setResizeOptions(mediaResizeOptions)
            .build());

        // Apply shape
        ViewHelper.applyCardShapeTo(this);
    }
}
