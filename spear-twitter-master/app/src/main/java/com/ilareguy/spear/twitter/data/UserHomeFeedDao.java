package com.ilareguy.spear.twitter.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class UserHomeFeedDao {

    @Query("SELECT * FROM Tweet INNER JOIN UserHomeFeed ON UserHomeFeed.tweet_id=Tweet.id WHERE " +
            "UserHomeFeed.logon_user_id=(:logon_user_id) AND UserHomeFeed.tweet_id < (:after_id) " +
            "ORDER BY UserHomeFeed.tweet_id DESC LIMIT :max_count")
    public abstract List<Tweet> getAfter(long logon_user_id, long after_id, int max_count);

    @Query("SELECT * FROM Tweet INNER JOIN UserHomeFeed ON UserHomeFeed.tweet_id=Tweet.id WHERE " +
            "UserHomeFeed.logon_user_id=(:logon_user_id) AND UserHomeFeed.tweet_id > (:before_id) " +
            "ORDER BY UserHomeFeed.tweet_id LIMIT :max_count")
    public abstract List<Tweet> getBefore(long logon_user_id, long before_id, int max_count);

    @Insert(onConflict = REPLACE)
    public abstract void save(UserHomeFeed... entries);

    @Query("DELETE FROM UserHomeFeed WHERE logon_user_id=(:user_id)")
    public abstract void deleteAllForUser(long user_id);

}
