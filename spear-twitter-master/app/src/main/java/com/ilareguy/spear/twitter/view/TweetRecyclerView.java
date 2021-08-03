package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.view.RecyclerViewAbstract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TweetRecyclerView extends RecyclerViewAbstract<TweetViewBig, Tweet> {
    public TweetRecyclerView(Context c){ this(c, null); }
    public TweetRecyclerView(Context context, AttributeSet attrs){ this(context, attrs, 0); }
    public TweetRecyclerView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    public @NonNull TweetViewBig buildView(final Context context){
        TweetViewBig new_view = new TweetViewBig(context);
        new_view.onFinishInflate();
        new_view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return new_view;
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewBig view, @Nullable Tweet tweet){
        if(tweet != null){
            view.setTweet(tweet);
        }
    }

}
