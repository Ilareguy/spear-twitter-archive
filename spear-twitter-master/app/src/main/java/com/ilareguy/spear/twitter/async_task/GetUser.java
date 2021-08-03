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
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.twitter.data.UserDao;
import com.ilareguy.spear.util.Timestamp;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OBJECT_NOT_IN_CACHE;
import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public final class GetUser extends DataFetcherAbstract<User>{

    private final User.Identification userIdentification;

    public GetUser(final PageAbstract page, User.Identification userIdentification,
                   final Policy policy){
        super(page, policy);
        this.userIdentification = userIdentification;
    }

    public GetUser(final PageAbstract page, final long user_id, final Policy policy) {
        super(page, policy);
        userIdentification = new User.Identification(user_id);
    }

    public GetUser(final PageAbstract page, final String username, final Policy policy) {
        super(page, policy);
        userIdentification = new User.Identification(username);
    }

    @Override
    protected @NonNull Result<User> onCache() {
        // Read a User from the cache
        UserDao user_dao = TwitterApplication.getTwitterInstance().getCacheDatabase().userDao();
        User cached_user;
        if (getAgeThreshold() > 0) {
            if (userIdentification.idMethod == User.IdentificationMethod.BY_ID)
                cached_user = user_dao.get_cached_after(userIdentification.userId, (Timestamp.now() - getAgeThreshold()));
            else
                cached_user = user_dao.get_cached_after(userIdentification.username, (Timestamp.now() - getAgeThreshold()));
        } else {
            if (userIdentification.idMethod == User.IdentificationMethod.BY_ID)
                cached_user = user_dao.get(userIdentification.userId);
            else
                cached_user = user_dao.get(userIdentification.username);
        }

        if (cached_user == null) {
            // Didn't exist
            return new Result<>(SpearError.build(OBJECT_NOT_IN_CACHE));
        }

        // Indicate cache read successful
        return new Result<>(cached_user);
    }

    @Override
    protected @NonNull Result<User> onRemote() {
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.GET,
                "https://api.twitter.com/1.1/users/show.json"
        );
        oauth_request.setCallingPage(getPage());
        oauth_request.addParameter("include_entities", "false");

        if (userIdentification.idMethod == User.IdentificationMethod.BY_USERNAME) {
            oauth_request.addParameter("screen_name", userIdentification.username);
        } else {
            oauth_request.addParameter("user_id", String.valueOf(userIdentification.userId));
        }

        User new_user;
        TaskResult<Response> response = null;
        try {
            response = ((getPage() == null)
                ? PageAbstract.sendOAuthRequest_s(oauth_request)
                : getPage().sendOAuthRequest(oauth_request));

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

        // save it in the cache
        new_user.cache();

        // Indicate success
        return new Result<>(new_user);
    }
}
