package com.ilareguy.spear.twitter.data_stream;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.User;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserFeedStream extends TweetStreamAbstract {

    private final User.Identification targetUser;
    //private final LogonUser contextTwitterAccount;

    public UserFeedStream(final PageAbstract page, final User.Identification targetUser,
                          final LogonUser contextTwitterAccount){
        super(page, contextTwitterAccount.getUid());
        //this.contextTwitterAccount = contextTwitterAccount;
        this.targetUser = targetUser;
    }

    @Override
    protected @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsForward(
            int maxCount, final @Nullable Tweet thresholdTweet){
        if(targetUser.idMethod == User.IdentificationMethod.BY_USERNAME){
            // Currently no way to find tweets in the cache if we don't have a proper user ID
            return buildResultObject(new LinkedList<>());
        }

        return buildResultObject(new LinkedList<>(TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao().getUserTweetsAfter(
                targetUser.userId,
                (thresholdTweet == null) ? Long.MAX_VALUE : thresholdTweet.getId(),
                maxCount
        )));
    }

    @Override
    protected @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsBackward(
            int maxCount, final @Nullable Tweet thresholdTweet){
        if(targetUser.idMethod == User.IdentificationMethod.BY_USERNAME){
            // Currently no way to find tweets in the cache if we don't have a proper user ID
            return buildResultObject(new LinkedList<>());
        }

        return buildResultObject(new LinkedList<>(TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao().getUserTweetsBefore(
                targetUser.userId,
                thresholdTweet.getId(),
                maxCount
        )));
    }

    @Override
    protected @NonNull
    BasicRequest buildOAuthRequest(int maxCount,
                                   final @Nullable Tweet thresholdTweet){
        BasicRequest oauth_request = buildBaseOAuthRequest();
        if(thresholdTweet != null)
            oauth_request.addParameter("max_id", String.valueOf(thresholdTweet.getId() - 1));
        return oauth_request;
    }

    private BasicRequest buildBaseOAuthRequest(){
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/statuses/user_timeline.json");
        oauth_request.addParameter("count", "200");
        oauth_request.addParameter("exclude_replies", "1");
        oauth_request.addParameter("include_entities", "1");
        oauth_request.addParameter("tweet_mode", "extended");
        if(targetUser.idMethod == User.IdentificationMethod.BY_ID)
            oauth_request.addParameter("user_id", String.valueOf(targetUser.userId));
        else
            oauth_request.addParameter("screen_name", targetUser.username);
        return oauth_request;
    }

    @Override
    protected void cacheData(final LinkedList<Tweet> tweets){
        final long logon_user_id = TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid();
        TwitterApplication.getTwitterInstance().getCacheDatabase().runInTransaction(() -> {
            for (Tweet tweet : tweets) {
                tweet.cache(logon_user_id);
                tweet.getAuthor().cache();
            }
        });
    }

    @Override
    protected void clearCachedData(){
        if(targetUser.idMethod == User.IdentificationMethod.BY_ID){
            TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao()
                    .deleteAllFromAuthor(targetUser.userId);
        }
    }

}
