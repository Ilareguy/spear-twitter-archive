package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;

import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.data_fetcher.DataFetcherAbstract;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.async_task.LikeTweet;
import com.ilareguy.spear.twitter.async_task.RetweetTweet;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.util.LoadingListener;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public abstract class TweetViewAbstract extends ConstraintLayout implements TweetToolbarView.OnToolbarEventListener{

    private Tweet tweet = null;
    private @Nullable LoadingListener loadingListener;
    private PageAbstract page;

    public TweetViewAbstract(Context context) {
        super(context);
    }

    public TweetViewAbstract(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TweetViewAbstract(Context context, AttributeSet attrs, int d) {
        super(context, attrs, d);
    }

    public final Tweet getTweet() { return tweet; }
    public void setTweet(Tweet tweet) { this.tweet = tweet; }
    public void setLoadingListener(LoadingListener l){ loadingListener = l; }
    public @Nullable LoadingListener getLoadingListener(){ return loadingListener; }

    @Override
    public boolean onLikeChange(final Tweet tweet, boolean new_state){
        if(page == null){
            // Missing fragment object to properly send the request!
            App._e("TweetViewAbstract.java:onRetweet(): Missing SpearFragmentAbstract object! Cannot send request.");
            return false;
        }

        // [un]like the tweet
        if(getLoadingListener() != null)
            getLoadingListener().loadingStart();
        new LikeTweet(page, tweet.getId(), new_state).asyncExecute(onPostExecuteListener);

        return true; // Good to go
    }

    @Override
    public boolean onRetweet(final Tweet tweet, final boolean new_state){
        if(page == null){
            // Missing fragment object to properly send the request!
            App._e("TweetViewAbstract.java:onRetweet(): Missing SpearFragmentAbstract object! Cannot send request.");
            return false;
        }

        // [un]retweet the tweet
        getLoadingListener().loadingStart();
        new RetweetTweet(page, tweet.getId(), new_state).asyncExecute(onPostExecuteListener);

        return true; // Good to go
    }

    @Override
    public void onReply(final Tweet tweet){
        if(tweet != null)
            MainActivity.getInstance().composeTweet("@" + tweet.getAuthor().getUsername(), tweet.getId());
    }

    @Override
    public void onQuote(final Tweet tweet){
        // @TODO
    }

    @Override
    public void onShare(final Tweet tweet){
        // @TODO
    }

    private DataFetcherAbstract.OnPostExecuteListener<Tweet> onPostExecuteListener =
            (DataFetcherAbstract.Result<Tweet> result) -> {
        // @TODO: Show success/error toast or something

        if(result.isSuccessful()){
            setTweet(result.getObject());
        }

        if(getLoadingListener() != null)
            getLoadingListener().loadingEnd();
    };

    public void setPage(final PageAbstract page){ this.page = page; }
    public final PageAbstract getPage(){ return page; }
}
