package com.ilareguy.spear.twitter.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public abstract class UserTweetInteractionDao{

    @Query("SELECT * FROM UserTweetInteraction WHERE authenticating_user_id=(:authenticating_user_id) AND tweet_id=(:tweet_id)")
    public abstract UserTweetInteraction get(long authenticating_user_id, long tweet_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(UserTweetInteraction... interactions);

}
