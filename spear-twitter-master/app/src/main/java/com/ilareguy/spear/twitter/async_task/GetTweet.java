package com.ilareguy.spear.twitter.async_task;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.data_fetcher.DataFetcherAbstract;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.util.Timestamp;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OBJECT_NOT_IN_CACHE;
import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class GetTweet extends DataFetcherAbstract<Tweet>{

    private final long tweetId;
    private final long authenticatingUserId;
    private final boolean loadParentTweet;

    public GetTweet(final PageAbstract page, final long tweet_id, final Policy policy) {
        super(page, policy);
        this.tweetId = tweet_id;
        this.authenticatingUserId = TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid();
        this.loadParentTweet = false;
    }

    public GetTweet(final PageAbstract page, final long tweet_id, final Policy policy,
                    boolean loadParentTweet) {
        super(page, policy);
        this.tweetId = tweet_id;
        this.authenticatingUserId = TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid();
        this.loadParentTweet = loadParentTweet;
    }

    @Override
    protected @NonNull Result<Tweet> onCache() {
        Tweet cached_tweet;
        if (getAgeThreshold() > 0) {
            cached_tweet = TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao().getCachedAfter(tweetId, (Timestamp.now() - getAgeThreshold()));
        } else {
            cached_tweet = TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao().get(tweetId);
        }

        if (cached_tweet == null) {
            // Didn't exist
            return new Result<>(SpearError.build(OBJECT_NOT_IN_CACHE));
        }

        cached_tweet.onPostReadFromCache(authenticatingUserId, loadParentTweet);
        return new Result<>(cached_tweet);
    }

    @Override
    protected @NonNull Result<Tweet> onRemote() {
        // Request a Tweet from Twitter's servers.

        // Build an OAuthRequest
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/statuses/show.json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("id", String.valueOf(tweetId));
        oauth_request.addParameter("include_entities", "true");

        // Twitter truncates tweets by default.
        // See https://developer.twitter.com/en/docs/tweets/tweet-updates
        // and https://stackoverflow.com/questions/38717816/twitter-api-text-field-value-is-truncated
        // These parameters are not described in the API description.
        oauth_request.addParameter("tweet_mode", "extended");

        Tweet new_tweet = null;
        TaskResult<Response> response = null;
        try {
            response = getPage().sendOAuthRequest(oauth_request);

            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return new Result<>(response.getError());
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                response.getObject().close();
                return new Result<>(SpearError.build(OK_HTTP_ERROR));
            }

            new_tweet = LoganSquare.parse(response.getObject().body().byteStream(), Tweet.class);
            response.getObject().close();

            // Load parent tweet?
            if(loadParentTweet && new_tweet.getInReplyToTweetId() > 0)
                new_tweet.setParentTweet(getRemoteParentTweet(new_tweet.getInReplyToTweetId()));
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(new_tweet, SpearError.build(e));
        }

        // If the user was embedded in the tweet, then save its data into
        // the cache, as it is the most up-to-date information we now have
        // about them.
        if(new_tweet.getAuthor() != null){
            new_tweet.getAuthor().cache();
        }

        // Save it in the cache
        new_tweet.cache(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());

        // Indicate success
        return new Result<>(new_tweet);
    }

    private Tweet getRemoteParentTweet(long parent_tweet_id) throws IOException{
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/statuses/show.json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("id", String.valueOf(parent_tweet_id));
        oauth_request.addParameter("include_entities", "true");
        oauth_request.addParameter("tweet_mode", "extended");

        TaskResult<Response> response = getPage().sendOAuthRequest(oauth_request);
        if(!response.isSuccessful()){
            // Couldn't send the request for some reason
            return null;
        }else if(!response.getObject().isSuccessful()){
            // Problem with OkHttp
            response.getObject().close();
            return null;
        }

        final Tweet new_tweet = LoganSquare.parse(response.getObject().body().byteStream(), Tweet.class);
        response.getObject().close();

        if(new_tweet == null) return null;

        if(new_tweet.getAuthor() != null){
            new_tweet.getAuthor().cache();
        }
        new_tweet.cache(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());
        return new_tweet;
    }

}
