package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.util.Helper;

public class TweetViewSmall extends TweetViewAbstract {

    private TextView textView;
    private TextView authorUsername;
    private SimpleDraweeView authorThumbnail;
    private ImageView badgeVerified;

    public TweetViewSmall(Context context) {
        super(context);
        init();
    }

    public TweetViewSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TweetViewSmall(Context context, AttributeSet attrs, int d) {
        super(context, attrs, d);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.tweet_view_small, this, true);
        final int padding = (int) Helper.dpToPx(8, getResources());
        setPadding(0, 0, 0, padding);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        //final TweetViewAbstract this_view = this;

        textView = findViewById(R.id.contents);
        authorUsername = findViewById(R.id.author_username);
        authorThumbnail = findViewById(R.id.author_thumbnail);
        badgeVerified = findViewById(R.id.badge_verified);
    }

    @Override
    public void setTweet(Tweet tweet) {
        this.setTweet(tweet, false);
    }

    public void setTweet(Tweet tweet, boolean update_thumbnail) {
        super.setTweet(tweet);
        textView.setText(tweet.getText());

        if (tweet.getAuthor() != null) {
            authorUsername.setText("@" + tweet.getAuthor().getUsername());

            // Hide or show verified badge
            badgeVerified.setVisibility(tweet.getAuthor().getVerified() ? VISIBLE : GONE);

            // load thumbnail
            if (update_thumbnail)
                authorThumbnail.setImageURI(tweet.getAuthor().getThumbnailUrl());
        }
    }

}
