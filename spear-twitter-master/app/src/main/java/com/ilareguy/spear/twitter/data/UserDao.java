package com.ilareguy.spear.twitter.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public abstract class UserDao {

    @Query("SELECT * FROM user WHERE uid=(:uid)")
    public abstract User get(long uid);

    @Query("SELECT uid, username, display_name, verified, translator, profile_image_default_url FROM user WHERE uid=(:uid)")
    public abstract User getMinimum(long uid);

    @Query("SELECT * FROM user WHERE username=(:username)")
    public abstract User get(String username);

    @Query("SELECT * FROM user WHERE uid=(:uid) AND cached_timestamp>(:limit_cache_timestamp)")
    public abstract User get_cached_after(long uid, long limit_cache_timestamp);

    @Query("SELECT * FROM user WHERE username=(:username) AND cached_timestamp>(:limit_cache_timestamp)")
    public abstract User get_cached_after(String username, long limit_cache_timestamp);

    @Query("SELECT * FROM user WHERE username=(:username)")
    public abstract User getByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(User... users);

    @Delete
    public abstract void delete(User... users);
}
