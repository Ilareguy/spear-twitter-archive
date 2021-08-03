package com.ilareguy.spear.twitter.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.ilareguy.spear.twitter.TwitterApplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = {"source_id", "target_id"})
@JsonObject
public class Relationship{
    // https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-show

    @JsonObject
    public static class JSONRelationship{
        @JsonField(name = "target")
        public JSONTarget jsonTarget;

        @JsonField(name = "source")
        public JSONSource jsonSource;
    }

    @JsonObject()
    public static class JSONTarget{
        @JsonField(name = "id")
        public long id;

        @JsonField(name = "following")
        public boolean following;

        @JsonField(name = "followed_by")
        public boolean followed_by;
    }

    @JsonObject
    public static class JSONSource{
        @JsonField(name = "id")
        public long id;

        @JsonField(name = "can_dm")
        public boolean canDirectMessage;
    }

    @JsonField(name = "relationship")
    @Ignore
    private JSONRelationship jsonRelationship;

    @ColumnInfo(name = "source_id")
    private long sourceId;

    @ColumnInfo(name = "target_id")
    private long targetId;

    @ColumnInfo(name = "following_target")
    private boolean followingTarget;

    @ColumnInfo(name = "followed_by_target")
    private boolean followedByTarget;

    @ColumnInfo(name = "can_dm")
    private boolean canMessageTarget;

    public Relationship(){}

    public void cache(){
        TwitterApplication.getTwitterInstance().getCacheDatabase().relationshipDao().save(this);
    }

    @OnJsonParseComplete
    void onParseComplete() {
        this.followedByTarget = jsonRelationship.jsonTarget.following;
        this.followingTarget = jsonRelationship.jsonTarget.followed_by;
        this.targetId = jsonRelationship.jsonTarget.id;
        this.sourceId = jsonRelationship.jsonSource.id;
        this.canMessageTarget = jsonRelationship.jsonSource.canDirectMessage;
    }

    public JSONRelationship getJsonRelationship(){
        return jsonRelationship;
    }

    public void setJsonRelationship(JSONRelationship jsonRelationship){
        this.jsonRelationship = jsonRelationship;
    }

    public long getSourceId(){
        return sourceId;
    }

    public void setSourceId(long sourceId){
        this.sourceId = sourceId;
    }

    public long getTargetId(){
        return targetId;
    }

    public void setTargetId(long targetId){
        this.targetId = targetId;
    }

    public boolean isFollowingTarget() {
        return followingTarget;
    }

    public void setFollowingTarget(boolean followingTarget) {
        this.followingTarget = followingTarget;
    }

    public boolean isFollowedByTarget() {
        return followedByTarget;
    }

    public void setFollowedByTarget(boolean followedByTarget) {
        this.followedByTarget = followedByTarget;
    }

    public boolean isCanMessageTarget() {
        return canMessageTarget;
    }

    public void setCanMessageTarget(boolean canMessageTarget) {
        this.canMessageTarget = canMessageTarget;
    }
}
