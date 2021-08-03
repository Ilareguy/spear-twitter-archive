package com.ilareguy.spear.twitter.async_task;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.data_fetcher.RemoteDataFetcherAbstract;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.Tweet;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class RetweetTweet extends RemoteDataFetcherAbstract<Tweet>{
    private final long tweetId;
    private final boolean retweet; // True if we're trying to retweet a tweet; false if we're trying to UNretweet a tweet

    public RetweetTweet(final PageAbstract page, final long tweetId,
                        final boolean retweet){
        super(page);
        this.tweetId = tweetId;
        this.retweet = retweet;
    }

    @Override
    protected @NonNull Result<Tweet> onRemote(){
        return retweet ? doRetweet() : doUnRetweet();
    }

    private @NonNull Result<Tweet> doRetweet(){
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                "https://api.twitter.com/1.1/statuses/retweet/" + String.valueOf(tweetId) + ".json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("include_entities", "true");
        oauth_request.addParameter("tweet_mode", "extended");

        _JsonRetweetResponse json_response;
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

            json_response = LoganSquare.parse(response.getObject().body().byteStream(), _JsonRetweetResponse.class);
            response.getObject().close();
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        /*
         * If the request was successful (Twitter returned code 200), then the operation worked. However
         * the data returned by Twitter may not reflect the new [un]retweeted just yet, so set it manually
         * before caching.
         */
        json_response.tweet.setRetweeted(true);

        json_response.tweet.cache(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());
        return new Result<>(json_response.tweet);
    }

    private @NonNull Result<Tweet> doUnRetweet(){
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                "https://api.twitter.com/1.1/statuses/unretweet/" + String.valueOf(tweetId) + ".json"
        );
        oauth_request.addParameter("include_entities", "true");
        oauth_request.addParameter("tweet_mode", "extended");

        Tweet tweet;
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

            tweet = LoganSquare.parse(response.getObject().body().byteStream(), Tweet.class);
            response.getObject().close();
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        /*
         * If the request was successful (Twitter returned code 200), then the operation worked. However
         * the data returned by Twitter may not reflect the new [un]retweeted just yet, so set it manually
         * before caching.
         */
        tweet.setRetweeted(false);

        tweet.cache(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());
        return new Result<>(tweet);
    }

    @JsonObject
    protected static class _JsonRetweetResponse{
        @JsonField(name = "retweeted_status")
        public Tweet tweet;
    }
}
