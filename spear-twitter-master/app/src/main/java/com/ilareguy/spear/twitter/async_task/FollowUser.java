package com.ilareguy.spear.twitter.async_task;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.data_fetcher.RemoteDataFetcherAbstract;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.Relationship;
import com.ilareguy.spear.twitter.data.User;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class FollowUser extends RemoteDataFetcherAbstract<User>{

    private final boolean follow;
    private final User.Identification userIdentification;

    public FollowUser(final PageAbstract page, User.Identification userIdentification,
                      boolean follow){
        super(page);
        this.userIdentification = userIdentification;
        this.follow = follow;
    }

    public FollowUser(final PageAbstract page, final long user_id, boolean follow) {
        super(page);
        userIdentification = new User.Identification(user_id);
        this.follow = follow;
    }

    public FollowUser(final PageAbstract page, final String username, boolean follow) {
        super(page);
        userIdentification = new User.Identification(username);
        this.follow = follow;
    }

    @Override
    protected @NonNull Result<User> onRemote() {
        // Build an OAuthRequest
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                follow ?
                        "https://api.twitter.com/1.1/friendships/create.json" :
                        "https://api.twitter.com/1.1/friendships/destroy.json"
        );
        oauth_request.setCallingPage(getPage());

        if (userIdentification.idMethod == User.IdentificationMethod.BY_USERNAME) {
            oauth_request.addParameter("screen_name", userIdentification.username);
        } else {
            oauth_request.addParameter("user_id", String.valueOf(userIdentification.userId));
        }

        User new_user;
        TaskResult<Response> response = null;
        try {
            response = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return new Result<>(response.getError());
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                response.getObject().close();
                return new Result<>(SpearError.build(OK_HTTP_ERROR));
            }

            new_user = LoganSquare.parse(response.getObject().body().byteStream(), User.class);
            response.getObject().close();
        } catch (IOException e) {
            response.getObject().close();
            return new Result<>(SpearError.build(e));
        }

        // Save it in the cache
        new_user.cache();

        // Save the new relationship
        final Relationship new_relationship = new Relationship();
        new_relationship.setSourceId(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid());
        new_relationship.setTargetId(new_user.getUid());
        new_relationship.setFollowingTarget(follow);

        // These two could be wrong, but getting the actual values would require another call to
        // Twitter's servers, so just assume these values for now.
        new_relationship.setCanMessageTarget(true);
        new_relationship.setFollowedByTarget(false);

        // Save the new relationship data
        new_relationship.cache();

        // Indicate success
        return new Result<>(new_user);
    }
}
