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
import com.ilareguy.spear.twitter.data.Relationship;
import com.ilareguy.spear.twitter.data.RelationshipDao;
import com.ilareguy.spear.twitter.data.User;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OBJECT_NOT_IN_CACHE;
import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class GetRelationship extends DataFetcherAbstract<Relationship>{

    private final User.Identification follower, target;

    public GetRelationship(final PageAbstract page, final User.Identification follower,
                           final User.Identification target, final Policy policy){
        super(page, policy);
        this.follower = follower;
        this.target = target;
    }

    @Override
    protected @NonNull Result<Relationship> onCache() {
        if(follower.idMethod != User.IdentificationMethod.BY_ID ||
                target.idMethod != User.IdentificationMethod.BY_ID){
            // This DAO requires two IDs to work
            return new Result<>(SpearError.build(OBJECT_NOT_IN_CACHE));
        }

        RelationshipDao dao = TwitterApplication.getTwitterInstance().getCacheDatabase().relationshipDao();
        return new Result<>(dao.get(follower.userId, target.userId));
    }

    @Override
    protected @NonNull Result<Relationship> onRemote() {
        // build an OAuthRequest
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/friendships/show.json"
        );
        oauth_request.setCallingPage(getPage());

        if (follower.idMethod == User.IdentificationMethod.BY_USERNAME) {
            oauth_request.addParameter("source_screen_name", follower.username);
        } else {
            oauth_request.addParameter("source_id", String.valueOf(follower.userId));
        }

        if (target.idMethod == User.IdentificationMethod.BY_USERNAME) {
            oauth_request.addParameter("target_screen_name", target.username);
        } else {
            oauth_request.addParameter("target_id", String.valueOf(target.userId));
        }

        Relationship new_relationship;
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

            new_relationship = LoganSquare.parse(response.getObject().body().byteStream(), Relationship.class);
            response.getObject().close();
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        // Save it in the cache
        new_relationship.cache();

        // Indicate success
        return new Result<>(new_relationship);
    }

}
