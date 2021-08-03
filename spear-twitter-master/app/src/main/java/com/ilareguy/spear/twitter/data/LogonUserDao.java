package com.ilareguy.spear.twitter.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class LogonUserDao {

    @Query("SELECT * FROM LogonUser WHERE uid=(:uid)")
    public abstract LogonUser get(long uid);

    @Query("SELECT * FROM LogonUser ORDER BY last_logon_timestamp DESC")
    public abstract List<LogonUser> getAll();

    @Query("SELECT * FROM LogonUser WHERE is_current=1")
    public abstract LogonUser getCurrent();

    @Query("SELECT COUNT(*) FROM LogonUser")
    public abstract int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(LogonUser user);

    @Update
    public abstract void update(LogonUser user);

    @Insert
    public abstract void insert(LogonUser user);

    @Delete
    public abstract void delete(LogonUser user);

}
