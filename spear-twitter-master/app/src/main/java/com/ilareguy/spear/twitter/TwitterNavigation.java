package com.ilareguy.spear.twitter;

import com.ilareguy.spear.EventOrigin;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.Tweet;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Declares various methods that activities must implement to allow the user to
 * navigate around the app. These methods take their regular parameters, as well as
 * a map containing views that could be shared when transitioning between two
 * fragments.
 */
public interface TwitterNavigation {

    /**
     * Called when the user wants to view a specific Twitter user profile.
     */
    void viewUser(long userId, @Nullable final EventOrigin origin);
    void viewUser(String username, @Nullable final EventOrigin origin);

    /**
     * Called when the user wants to view a specific tweet.
     */
    void viewTweet(long tweetId, @Nullable final EventOrigin origin);

    /**
     * Called when the user wants to view data bout a specific hashtag or cashtag.
     */
    void viewHashtag(String hashtag, @Nullable final EventOrigin origin);

    /**
     * Called when the user wants to switch Twitter user.
     */
    void switchAccount(@NonNull final LogonUser newLogonUser, @Nullable final EventOrigin origin);

    /**
     * Called when the user wants to register a new Twitter user in the app.
     */
    void registerNewAccount();

    /**
     * Called when the user has clicked a URL.
     */
    void viewURL(String url, @Nullable final EventOrigin origin);

    /**
     * Called when the user wants to see a specific media.
     */
    void viewMedia(String url);

    /**
     * Called when the user wants to see a list of media entities.
     */
    void viewMedias(List<Tweet.Entities.MediaEntity> medias, int startPosition);

    /**
     * Search Twitter.
     */
    void searchTwitter(TwitterSearchParameters queryBuilder);

    /**
     * Called when the user wants to compose a new tweet.
     */
    void composeTweet(String text, long inResponseToTweetId);

}
