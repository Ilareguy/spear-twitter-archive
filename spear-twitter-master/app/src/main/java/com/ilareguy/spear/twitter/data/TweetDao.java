package com.ilareguy.spear.twitter.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public abstract class TweetDao {

    @Query("SELECT * FROM tweet WHERE id=(:uid)")
    public abstract Tweet get(long uid);

    @Query("SELECT * FROM tweet WHERE id=(:uid) AND cached_timestamp>(:limit_cache_timestamp)")
    public abstract Tweet getCachedAfter(long uid, long limit_cache_timestamp);

    @Query("SELECT * FROM tweet WHERE in_reply_to_status_id=(:parent_id) ORDER BY id DESC LIMIT (:max_count)")
    public abstract List<Tweet> getReplies(long parent_id, int max_count);

    @Query("SELECT * FROM tweet WHERE id < (:id_after) AND author_id=(:author_id) ORDER BY id DESC LIMIT (:max_count)")
    public abstract List<Tweet> getUserTweetsAfter(long author_id, long id_after, int max_count);

    @Query("SELECT * FROM tweet WHERE id > (:id_after) AND author_id=(:author_id) ORDER BY id ASC LIMIT (:max_count)")
    public abstract List<Tweet> getUserTweetsBefore(long author_id, long id_after, int max_count);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(Tweet... tweets);

    @Delete
    public abstract void delete(Tweet... tweets);

    @Query("DELETE FROM tweet WHERE author_id=(:author_id)")
    public abstract void deleteAllFromAuthor(long author_id);

}
