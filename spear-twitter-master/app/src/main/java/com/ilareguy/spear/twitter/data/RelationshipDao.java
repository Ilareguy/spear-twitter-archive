package com.ilareguy.spear.twitter.data;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public abstract class RelationshipDao{

    /*@Query("SELECT EXISTS(SELECT 1 FROM relationship WHERE source_id=(:source_id) AND target_id=(:target_id) AND following=true)")
    public abstract boolean follows(long source_id, long target_id);*/

    @Query("SELECT * FROM relationship WHERE source_id=(:source_id) AND target_id=(:target_id)")
    public abstract @Nullable Relationship get(long source_id, long target_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(Relationship relationship);

}
