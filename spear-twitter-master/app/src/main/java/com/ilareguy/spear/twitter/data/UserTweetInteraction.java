package com.ilareguy.spear.twitter.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = {"authenticating_user_id", "tweet_id"})
public class UserTweetInteraction{

    public UserTweetInteraction(){}

    @Ignore
    public UserTweetInteraction(long authenticatingUserId, long tweetId,
                                boolean liked, boolean retweeted,
                                boolean replied, boolean quoted){
        this.authenticatingUserId = authenticatingUserId;
        this.tweetId = tweetId;
        this.liked = liked;
        this.retweeted = retweeted;
        this.replied = replied;
        this.quoted = quoted;
    }

    @ColumnInfo(name = "authenticating_user_id")
    private long authenticatingUserId;

    @ColumnInfo(name = "tweet_id")
    private long tweetId;

    @ColumnInfo(name = "liked")
    private boolean liked;

    @ColumnInfo(name = "retweeted")
    private boolean retweeted;

    @ColumnInfo(name = "replied")
    private boolean replied;

    @ColumnInfo(name = "quoted")
    private boolean quoted;

    public long getAuthenticatingUserId(){
        return authenticatingUserId;
    }

    public void setAuthenticatingUserId(long authenticatingUserId){
        this.authenticatingUserId = authenticatingUserId;
    }

    public long getTweetId(){
        return tweetId;
    }

    public void setTweetId(long tweetId){
        this.tweetId = tweetId;
    }

    public boolean isLiked(){
        return liked;
    }

    public void setLiked(boolean liked){
        this.liked = liked;
    }

    public boolean isRetweeted(){
        return retweeted;
    }

    public void setRetweeted(boolean retweeted){
        this.retweeted = retweeted;
    }

    public boolean isReplied(){
        return replied;
    }

    public void setReplied(boolean replied){
        this.replied = replied;
    }

    public boolean isQuoted(){
        return quoted;
    }

    public void setQuoted(boolean quoted){
        this.quoted = quoted;
    }
}
