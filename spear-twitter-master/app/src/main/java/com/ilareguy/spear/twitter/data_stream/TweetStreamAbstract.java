package com.ilareguy.spear.twitter.data_stream;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.stream.CacheOAuthStreamAbstract;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.twitter.TwitterError;
import com.ilareguy.spear.twitter.data.Tweet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public abstract class TweetStreamAbstract extends CacheOAuthStreamAbstract<Tweet>{

    private final long authenticatingUserId;

    public TweetStreamAbstract(final PageAbstract page,
                               final long authenticatingUserId){
        super(page);
        this.authenticatingUserId = authenticatingUserId;
    }

    protected abstract @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsForward(
            int maxCount, final @Nullable Tweet thresholdTweet);
    protected abstract @NonNull StreamAbstract.ReadResult<Tweet> readCachedTweetsBackward(
            int maxCount, final @Nullable Tweet thresholdTweet);
    protected abstract @NonNull BasicRequest buildOAuthRequest(
            int maxCount, final @Nullable Tweet thresholdTweet);

    @Override
    protected final @NonNull StreamAbstract.ReadResult<Tweet> doReadForwardCache(int max_count,
                                                                                 @Nullable Tweet threshold_object){
        final StreamAbstract.ReadResult<Tweet> r = readCachedTweetsForward(max_count, threshold_object);

        if(r.isSuccessful() && r.getObject().peekFirst() != null){
            if(r.getObject().size() == 0){
                // No more tweets!
                finalizeForward();
            }else{
                finalizeBackward(false);
                for(Tweet t : r.getObject())
                    t.onPostReadFromCache(authenticatingUserId);
            }
        }

        return r;
    }

    @Override
    protected final @NonNull StreamAbstract.ReadResult<Tweet> doReadBackwardCache(int max_count,
                                                                                  @NonNull Tweet threshold_object){
        final StreamAbstract.ReadResult<Tweet> r = readCachedTweetsBackward(max_count, threshold_object);

        if(r.isSuccessful() && r.getObject().peekLast() != null){
            if(r.getObject().size() == 0){
                // No more tweets!
                finalizeBackward();
            }else{
                finalizeForward(false);
                for(Tweet t : r.getObject()) t.onPostReadFromCache(authenticatingUserId);
            }
        }

        return r;
    }

    @Override
    protected @NonNull StreamAbstract.ReadResult<Tweet> doReadForwardRemote(int max_count,
                                                                            @Nullable Tweet threshold_object){
        return processRemoteRequest(max_count, buildOAuthRequest(max_count, threshold_object));
    }

    private StreamAbstract.ReadResult<Tweet> processRemoteRequest(final int max_count, BasicRequest oauth_request){
        oauth_request.setCallingPage(getPage());

        try{
            final TaskResult<Response> response = getPage().sendOAuthRequest(oauth_request);

            // Error?
            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return buildResultObject(response.getError());
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                response.getObject().close();
                return buildResultObject(SpearError.build(OK_HTTP_ERROR));
            }

            final LinkedList<Tweet> read_tweets = new LinkedList<>(
                    LoganSquare.parseList(response.getObject().body().byteStream(),
                    Tweet.class)
            );

            // TODO: 2018-08-27 Check for and remove duplicate tweet sent by Twitter here

            // Let the stream know that we've successfully read objects from the remote endpoint
            reportRemoteSuccess(read_tweets);

            return buildResultObject(
                    (read_tweets.size() > max_count)
                    ? new LinkedList<>(read_tweets.subList(0, max_count))
                    : read_tweets
            );

        }catch(IOException e){
            return buildResultObject(TwitterError.build(e));
        }
    }
}
