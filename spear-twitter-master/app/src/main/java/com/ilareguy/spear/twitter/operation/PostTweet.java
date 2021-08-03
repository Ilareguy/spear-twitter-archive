package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ilareguy.spear.BackgroundOperationExecutor;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.data.MediaMetadata;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.TweetDraft;
import com.ilareguy.spear.util.StringHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class PostTweet extends BackgroundOperationExecutor.Operation
        implements BackgroundOperationExecutor.Operation.Step{

    private final TweetDraft tweetDraft;
    private final List<Long> mediaIds = new ArrayList<>();

    public PostTweet(final TweetDraft tweetDraft){
        this.tweetDraft = tweetDraft;

        // Register steps
        registerMediaUploadSteps();

        // Register the tweet upload itself
        registerStep(this);
    }

    private void registerMediaUploadSteps(){
        if(tweetDraft.getAttachedMedias().size() == 0) return;

        final List<MediaMetadata> medias_to_upload = tweetDraft.getAttachedMedias();
        int current_media_index = 0;

        for(MediaMetadata mediaMetadata : medias_to_upload){
            current_media_index++;
            registerStep(new UploadMedia(this,
                    mediaMetadata, current_media_index, medias_to_upload.size()));
        }
    }

    public final void registerNewMediaId(final long mediaId){
        mediaIds.add(mediaId);
    }

    @Override
    public @Nullable SpearError run(final StepHandler handler){
        // Build request
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                "https://api.twitter.com/1.1/statuses/update.json"
        );
        oauth_request.addParameter("status", tweetDraft.getRawText());

        // In reply to another tweet?
        //oauth_request.addParameter("in_reply_to_status_id", 00);

        // Quoting another tweet?
        // oauth_request.addParameter("attachment_url", "");

        if(mediaIds.size() > 0){
            oauth_request.addParameter("media_ids", buildMediaIdsString());
        }

        // Send & parse
        TaskResult<Response> response = null;
        try{
            // Send
            response = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return response.getError();
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                final SpearError error = SpearError.build(OK_HTTP_ERROR,
                        "Network error. Response received: "
                                + StringHelper.inputStreamToString(response.getObject().body().byteStream()));
                response.getObject().close();
                return error;
            }

            // Parse
            final Tweet posted_tweet = LoganSquare.parse(
                    response.getObject().body().byteStream(), Tweet.class);
            response.getObject().close();

            // Cache
            posted_tweet.cache();

            return null;
        }catch (IOException e) {
            response.getObject().close();
            return SpearError.build(e);
        }
    }

    private String buildMediaIdsString(){
        boolean first = true;
        final StringBuilder b = new StringBuilder();
        for(Long media_id : mediaIds){
            if(first)first = false;
            else b.append(",");
            b.append(media_id);
        }
        return b.toString();
    }

}
