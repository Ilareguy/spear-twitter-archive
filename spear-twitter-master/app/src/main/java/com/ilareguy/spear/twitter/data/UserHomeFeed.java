package com.ilareguy.spear.twitter.data;

import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.util.Timestamp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Defines a specific user's home feed.
 */
@Entity(primaryKeys = {"tweet_id","logon_user_id"},
        indices = {
                @Index(value = "author_id"),
                @Index(value = "tweet_id"),
                @Index(value = "logon_user_id")
        })
public class UserHomeFeed {

    @ColumnInfo(name = "tweet_id")
    private long tweetId;

    @ColumnInfo(name = "logon_user_id")
    private long logonUserId;

    @ColumnInfo(name = "author_id")
    private long authorId;

    @ColumnInfo(name = "cached_timestamp")
    private long cachedTimestamp;

    public UserHomeFeed() {
    }

    public UserHomeFeed(long logon_user_id, Tweet tweet) {
        this.logonUserId = logon_user_id;
        setTweet(tweet);
    }

    public void cache() {
        this.cachedTimestamp = Timestamp.now();
        TwitterApplication.getTwitterInstance().getCacheDatabase().userHomeFeedDao().save(this);
    }

    public void setTweet(final Tweet t) {
        this.tweetId = t.getId();
        this.authorId = t.getAuthorId();
    }

    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getLogonUserId() {
        return logonUserId;
    }

    public void setLogonUserId(long logonUserId) {
        this.logonUserId = logonUserId;
    }

    public long getCachedTimestamp() {
        return cachedTimestamp;
    }

    public void setCachedTimestamp(long cachedTimestamp) {
        this.cachedTimestamp = cachedTimestamp;
    }

    @Override
    public String toString() {
        return "UserHomeFeed{" +
                ", tweetId=" + tweetId +
                ", logonUserId=" + logonUserId +
                ", authorId=" + authorId +
                ", cachedTimestamp=" + cachedTimestamp +
                '}';
    }
}
