package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.util.StringHelper;
import com.ilareguy.spear.view.CheckableImageButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TweetToolbarView extends ConstraintLayout{
    public interface OnToolbarEventListener{
        /**
         * Called when the user pressed the "Like" button.
         * @param new_state The new state. True if the user wants to like the tweet; false if
         *                  they want to unlike the button.
         * @return Return false to consume and cancel the event; true otherwise.
         */
        boolean onLikeChange(final Tweet tweet, boolean new_state);

        /**
         * Called when the user pressed to "Retweet" button.
         * @return Return false to consume and cancel the event; true otherwise.
         */
        boolean onRetweet(final Tweet tweet, final boolean new_state);

        /**
         * Called when the user pressed the "Reply" button.
         */
        void onReply(final Tweet tweet);

        /**
         * Called when the user pressed the "Share" button.
         */
        void onShare(final Tweet tweet);

        /**
         * Called when the user pressed the "Quote" button.
         */
        void onQuote(final Tweet tweet);
    }

    public TweetToolbarView(Context c){ super(c); init(null); }
    public TweetToolbarView(Context c, AttributeSet a){ super(c, a); init(a); }
    public TweetToolbarView(Context c, AttributeSet a, int d){ super(c, a, d); init(a); }

    private @Nullable Tweet tweet = null;
    private TextView likesCount, retweetsCount;
    private CheckableImageButton likeButton, retweetButton;
    private @Nullable
    OnToolbarEventListener onToolbarEventListener = null;

    private void init(final AttributeSet attrs){
        LayoutInflater.from(getContext()).inflate(R.layout.tweet_toolbar, this, true);
        likesCount = findViewById(R.id.likes_count);
        retweetsCount = findViewById(R.id.retweets_count);
        likeButton = findViewById(R.id.like_button);
        retweetButton = findViewById(R.id.retweet_button);

        // Hook listeners
        likeButton.setOnCheckedStateChangeListener(
                (CheckableImageButton v, boolean checked) ->
                        (onToolbarEventListener != null && tweet != null)
                                && onToolbarEventListener.onLikeChange(tweet, checked));

        ((CheckableImageButton) findViewById(R.id.reply_button)).setOnCheckedStateChangeListener(
                (CheckableImageButton view, boolean checked) -> {
                    if(onToolbarEventListener != null && tweet != null)
                        onToolbarEventListener.onReply(tweet);

                    return false; // Always return false; the "Reply" button can't be checked
                });

        ((CheckableImageButton) findViewById(R.id.quote_button)).setOnCheckedStateChangeListener(
                (CheckableImageButton view, boolean checked) -> {
                    if(onToolbarEventListener != null && tweet != null)
                        onToolbarEventListener.onQuote(tweet);

                    return false; // Always return false; the "Quote" button can't be checked
                });

        retweetButton.setOnCheckedStateChangeListener(
                (CheckableImageButton view, boolean checked) ->
                        (onToolbarEventListener != null && tweet != null)
                                && onToolbarEventListener.onRetweet(tweet, checked));
    }

    public void setTweet(final Tweet tweet){
        this.tweet = tweet;

        retweetsCount.setText(StringHelper.numberWithSuffix(tweet.getRetweetCount()));
        likesCount.setText(StringHelper.numberWithSuffix(tweet.getFavoriteCount()));
        likeButton.setCheckedSilent(tweet.isLiked());
        retweetButton.setCheckedSilent(tweet.isRetweeted());
    }

    public void setOnToolbarEventListener(@Nullable OnToolbarEventListener l){
        this.onToolbarEventListener = l;
    }
}
