package com.ilareguy.spear.twitter.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {
        User.class,
        Tweet.class,
        LogonUser.class,
        Relationship.class,
        UserHomeFeed.class,
        UserTweetInteraction.class
}, version = 1)
@TypeConverters({Tweet.Converters.class, TweetDraft.Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract TweetDao tweetDao();
    public abstract LogonUserDao logonUserDao();
    public abstract RelationshipDao relationshipDao();
    public abstract UserHomeFeedDao userHomeFeedDao();
    public abstract UserTweetInteractionDao tweetInteractionDao();
}
