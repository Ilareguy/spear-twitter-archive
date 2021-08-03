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
import com.ilareguy.spear.twitter.data.SearchResult;
import com.ilareguy.spear.twitter.data.Tweet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class GetTweetReplies extends DataFetcherAbstract<List<Tweet>>{

    public static final class Args{
        public Tweet baseTweet;
        public int maxCount;
        public long authenticatingUserId = 0;
    }

    private final Args args;

    public GetTweetReplies(final PageAbstract page, final Args args,
                           final Policy policy) {
        super(page, policy);
        this.args = args;
    }

    @Override
    protected @NonNull Result<List<Tweet>> onCache() {
        List<Tweet> cached_replies = TwitterApplication.getTwitterInstance().getCacheDatabase().tweetDao().getReplies(
                args.baseTweet.getId(), args.maxCount);
        args.baseTweet.setReplies(cached_replies);
        return new Result<>(cached_replies);
    }

    @Override
    protected @NonNull Result<List<Tweet>> onRemote() {
        /*
         * As of May 2018, Twitter does not yet support fetching replies for a specific tweet directly
         * for its public API (however, looking into the mobile site's traffic reveals that this should
         * be available in the v2, i.e. api.twitter.com/2/...).
         *
         * A workaround consists of using Twitter's Search API to get a list of tweets mentioning the
         * original author and looking for the "in_response_to_status_id" field matching the original
         * tweet id.
         *
         * See https://stackoverflow.com/questions/2693553/replies-to-a-particular-tweet-twitter-api
         */
        List<Tweet> replies = new ArrayList<>();

        // Build an OAuthRequest
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/search/tweets.json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("q", "to:" + args.baseTweet.getAuthor().getUsername());
        oauth_request.addParameter("since_id", String.valueOf(args.baseTweet.getId()));
        oauth_request.addParameter("count", "50");
        oauth_request.addParameter("include_entities", "true");
        oauth_request.addParameter("result_type", "mixed");
        oauth_request.addParameter("tweet_mode", "extended");

        SearchResult search_result;
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

            search_result = LoganSquare.parse(response.getObject().body().byteStream(), SearchResult.class);
            response.getObject().close();
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        // Try to find all the included replies to the given base tweet
        final long base_tweet_id = args.baseTweet.getId();
        for (Tweet tweet: search_result.getTweets()) {
            if(tweet.getInReplyToTweetId() == base_tweet_id){
                // Cache it
                tweet.cache(args.authenticatingUserId);

                // Cache its author
                tweet.getAuthor().cache();

                // Add it to the list
                replies.add(tweet);
            }
        }

        // Indicate success
        args.baseTweet.setReplies(replies);
        return new Result<>(replies);
    }
}
