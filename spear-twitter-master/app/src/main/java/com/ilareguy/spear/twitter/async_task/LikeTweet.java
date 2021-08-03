package com.ilareguy.spear.twitter.async_task;

import com.bluelinelabs.logansquare.LoganSquare;
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

public final class LikeTweet extends RemoteDataFetcherAbstract<Tweet>{
    private final long tweetId;
    private final boolean like; // True if we're trying to like a tweet; false if we're trying to UNlike a tweet

    public LikeTweet(final PageAbstract page, final long tweetId, final boolean like){
        super(page);
        this.tweetId = tweetId;
        this.like = like;
    }

    @Override
    protected @NonNull Result<Tweet> onRemote(){
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                like
                        ? "https://api.twitter.com/1.1/favorites/create.json"
                        : "https://api.twitter.com/1.1/favorites/destroy.json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("id", String.valueOf(tweetId));
        oauth_request.addParameter("include_entities", "true");
        oauth_request.addParameter("tweet_mode", "extended");

        Tweet new_tweet;
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
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        /*
         * If the request was successful (Twitter returned code 200), then the operation worked. However
         * the data returned by Twitter may not reflect the new [un]like just yet, so set it manually
         * before caching.
         */
        new_tweet.setLiked(like);

        new_tweet.cache(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());
        return new Result<>(new_tweet);
    }
}
