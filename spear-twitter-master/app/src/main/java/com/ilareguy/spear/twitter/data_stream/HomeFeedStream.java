package com.ilareguy.spear.twitter.data_stream;

import android.os.Bundle;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.UserHomeFeed;
import com.ilareguy.spear.util.Timestamp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeFeedStream extends TweetStreamAbstract {

    private final LogonUser twitterAccount;

    public HomeFeedStream(final PageAbstract page, LogonUser twitterAccount){
        super(page, twitterAccount.getUid());
        this.twitterAccount = twitterAccount;
    }

    public List<Tweet> loadTweets(long firstTweetId, int count){
        final List<Tweet> tweets = TwitterApplication.getTwitterInstance()
                .getCacheDatabase().userHomeFeedDao().getAfter(
                        twitterAccount.getUid(),
                        firstTweetId + 1,
                        count
                );
        for(Tweet t : tweets) t.onPostReadFromCache(twitterAccount.getUid());
        return tweets;
    }

    @Override
    protected @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsForward(
            int maxCount, final @Nullable Tweet thresholdTweet){
        return buildResultObject(new LinkedList<>(TwitterApplication.getTwitterInstance()
                .getCacheDatabase().userHomeFeedDao().getAfter(
                    twitterAccount.getUid(),
                    (thresholdTweet == null) ? Long.MAX_VALUE : thresholdTweet.getId(),
                    maxCount
        )));
    }

    @Override
    protected @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsBackward(
            int maxCount, final @Nullable Tweet thresholdTweet){

        // Get a list of tweets from the cache. This list will contain the tweets that are directly
        // before threshold_tweet, but in the wrong order. If you order tweet ids DESC in the DAO, then
        // what you get is tweets in the right order, but you potentially jumped over a ton of tweets
        final List<Tweet> raw_result = TwitterApplication.getTwitterInstance()
                .getCacheDatabase().userHomeFeedDao().getBefore(
                        twitterAccount.getUid(),
                        thresholdTweet.getId(),
                        maxCount
                );

        // Result needs to be reversed
        final LinkedList<Tweet> final_list = new LinkedList<>();
        for(Tweet t : raw_result)
            final_list.addFirst(t);

        return buildResultObject(final_list);
    }

    @Override
    protected @NonNull BasicRequest buildOAuthRequest(int maxCount,
                                   final @Nullable Tweet thresholdTweet){
        BasicRequest oauth_request = buildBaseOAuthRequest();
        if(thresholdTweet != null)
            oauth_request.addParameter("max_id", String.valueOf(thresholdTweet.getId() - 1));
        return oauth_request;
    }

    private BasicRequest buildBaseOAuthRequest(){
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/statuses/home_timeline.json");
        oauth_request.addParameter("count", "200");
        oauth_request.addParameter("exclude_replies", "1");
        oauth_request.addParameter("include_entities", "1");
        oauth_request.addParameter("tweet_mode", "extended");
        return oauth_request;
    }

    @Override
    protected void cacheData(final LinkedList<Tweet> tweets){
        final UserHomeFeed user_home_feed_object = new UserHomeFeed();
        final long logon_user_id = twitterAccount.getUid();
        user_home_feed_object.setLogonUserId(logon_user_id);
        twitterAccount.setLastHomeFeedRefreshTimestamp(Timestamp.now());
        TwitterApplication.getTwitterInstance().getCacheDatabase().logonUserDao().save(twitterAccount);

        TwitterApplication.getTwitterInstance().getCacheDatabase().runInTransaction(() -> {
            for (Tweet tweet : tweets) {
                tweet.cache(logon_user_id);
                tweet.getAuthor().cache();
                user_home_feed_object.setTweet(tweet);
                user_home_feed_object.cache();
            }
        });
    }

    @Override
    protected void clearCachedData(){
        TwitterApplication.getTwitterInstance().getCacheDatabase().userHomeFeedDao()
                .deleteAllForUser(twitterAccount.getUid());
    }
}
