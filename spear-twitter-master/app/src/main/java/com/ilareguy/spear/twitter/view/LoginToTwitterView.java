package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.SpearErrorCode;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.data_fetcher.DataFetcherAbstract;
import com.ilareguy.spear.data_fetcher.RemoteDataFetcherAbstract;
import com.ilareguy.spear.oauth.AuthenticationRequest;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.TwitterError;
import com.ilareguy.spear.twitter.TwitterErrorCode;
import com.ilareguy.spear.twitter.async_task.GetUser;
import com.ilareguy.spear.twitter.async_task.SetCurrentLogonUser;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.util.URIParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Response;

import static com.ilareguy.spear.twitter.Config.OAUTH_AUTHENTICATION_CALLBACK;

public class LoginToTwitterView extends RelativeLayout {
    public LoginToTwitterView(Context c){ super(c); init(c, null); }
    public LoginToTwitterView(Context c, AttributeSet a){ super(c, a); init(c, a); }
    public LoginToTwitterView(Context c, AttributeSet a, int d){ super(c, a, d); init(c, a); }

    public interface OnResultListener{
        void onError(final SpearError error);
        void onSuccess(final LogonUser newToken, final @Nullable User newUser);
    }

    private View loadingView;
    private WebView webView;
    private OnResultListener onResultListener = null;

    /**
     * Sets the listener for when the authentication process has ended. You should call this and
     * provide an OnResultListener object if you ever want to know when something happens. It'd be
     * pointless not to, really. No I mean it, this whole thing would be completely useless otherwise.
     *
     * And just because I REALLY want you to call this, I'm not even going to null-check the listener
     * later on, so if you don't call this with a valid object (or don't call it at all), then it's
     * going to crash in your face in no time.
     */
    public void setOnResultListener(OnResultListener onResultListener){ this.onResultListener = onResultListener; }

    private void init(Context c, @Nullable AttributeSet attrs){
        // Inflate
        LayoutInflater.from(c).inflate(R.layout.login_to_twitter, this, true);

        // Find views
        loadingView = findViewById(R.id.loading);
        webView = findViewById(R.id.web_view);

        getAuthorizationToken();
    }

    private void getAuthorizationToken(){
        showLoading();

        AuthenticationRequest token_request = new AuthenticationRequest(OAuth.getInstance().getConsumerKey(),
                RequestAbstract.Method.POST,
                "https://api.twitter.com/oauth/request_token");

        // Execute request
        OAuth.getInstance().getGlobalCommunicator().sendRequest(token_request,
                (final @NonNull TaskResult<Response> response) -> {
                    if(!response.isSuccessful()){
                        onResultListener.onError(response.getError());
                        return;
                    }else if(!response.getObject().isSuccessful()){
                        onResultListener.onError(SpearError.build(SpearErrorCode.OK_HTTP_ERROR));
                        return;
                    }

                    String returned_string;
                    try{
                        returned_string = response.getObject().body().string();
                        response.getObject().close();
                    }catch(IOException e){
                        onResultListener.onError(SpearError.build(SpearErrorCode.OAUTH_RESPONSE_INVALID_JSON));
                        response.getObject().close();
                        return;
                    }

                    // Extract the parameters from the returned String
                    Map<String, String> result;
                    try {
                        result = URIParser.splitQuery(returned_string);
                    } catch (UnsupportedEncodingException e) {
                        onResultListener.onError(TwitterError.build(TwitterErrorCode.TWITTER_INVALID_RESPONSE));
                        return;
                    }

                    // Find the oauth_token value
                    String oauth_token = result.get("oauth_token");

                    // Set the web view's target URL
                    startUserAuthentication(oauth_token);
                }
        );
    }

    private void startUserAuthentication(String token){
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                final String url = req.getUrl().toString();

                if (url.startsWith(OAUTH_AUTHENTICATION_CALLBACK)) {
                    try {
                        MainActivity.getInstance().runOnUiThread(() -> {
                            try {
                                webView.stopLoading();
                                finishUserAuthentication(URIParser.splitURLQuery(new URL(url)));
                            }catch (UnsupportedEncodingException | MalformedURLException e){
                                onResultListener.onError(SpearError.build(e));
                            }
                        });
                    } catch (Exception e) {
                        onResultListener.onError(SpearError.build(e));
                    }
                }

                return super.shouldInterceptRequest(view, req);
            }
        });

        webView.loadUrl("https://api.twitter.com/oauth/authorize?force_login=true&oauth_token=" + token);
        showWebView();
    }

    private void finishUserAuthentication(Map<String, String> params) {
        showLoading();

        // Check if the user cancelled
        String denied = params.get("denied");
        if(denied != null && !denied.equals("")){
            onResultListener.onError(TwitterError.build(TwitterErrorCode.OAUTH_AUTHENTICATION_FAILED));
            return;
        }

        // Extract data from URL
        String oauth_token = params.get("oauth_token");
        String oauth_verifier = params.get("oauth_verifier");

        /*
         * At this point, the user has allowed the App to use their
         * Twitter account.
         * Twitter has redirected the ic_drawer_user to OAUTH_AUTHENTICATION_CALLBACK
         * and has appended to it, an oauth_token and an oauth_verifier.
         * This brings us to Section 6.3 of the OAuth 1.0 authentication flow.
         * See https://oauth.net/core/1.0/#auth_step3
         * and Step 3 in https://dev.twitter.com/web/sign-in/implementing
         *
         * We now need to request an Access Token from Twitter using the provided
         * Request Token.
         */

        // build the request to get an Access Token
        AuthenticationRequest oauth_request = new AuthenticationRequest(
                OAuth.getInstance().getConsumerKey(),
                RequestAbstract.Method.POST,
                "https://api.twitter.com/oauth/access_token"
        );
        oauth_request.addParameter("oauth_verifier", oauth_verifier);
        oauth_request.addParameter("oauth_token", oauth_token);

        // Send the request
        OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request,
                (final @NonNull TaskResult<Response> response) -> {
                    if(!response.isSuccessful()){
                        onResultListener.onError(SpearError.build(SpearErrorCode.JAVA_EXCEPTION));
                        return;
                    }else if(!response.getObject().isSuccessful()){
                        onResultListener.onError(SpearError.build(SpearErrorCode.OK_HTTP_ERROR));
                        return;
                    }

                    String returned_string;
                    final Map<String, String> new_user_params;

                    try{
                        returned_string = response.getObject().body().string();
                        new_user_params = URIParser.splitQuery(returned_string);
                        response.getObject().close();
                    }catch(IOException e){
                        onResultListener.onError(SpearError.build(e));
                        response.getObject().close();
                        return;
                    }

                    onUserAdd(new LogonUser(
                            new_user_params.get("screen_name"),
                            Long.valueOf(new_user_params.get("user_id")),
                            new_user_params.get("oauth_token"),
                            new_user_params.get("oauth_token_secret")
                    ));
                });
    }

    private void onUserAdd(LogonUser new_logon_user) {
        // Execute the task in the background
        new SetCurrentLogonUser(null).execute(
                new SetCurrentLogonUser.Args(new_logon_user, getContext(),
                        new SetCurrentLogonUser.OnCompletionListener() {
                            @Override
                            public void onLogonUserSwitched(LogonUser new_account) {
                                initialLoadUserData(new_account);
                            }

                            @Override
                            public void onError(SpearError error) {
                                onResultListener.onError(error);
                            }
                        }));
    }

    private void initialLoadUserData(final LogonUser newLogonUser){
        new RemoteDataFetcherAbstract<User>(null){

            @Override
            protected @NonNull Result<User> onRemote(){
                Result<User> u = new GetUser(null, newLogonUser.getUid(), Policy.FORCE_REMOTE).execute();
                newLogonUser.setBanner_uri(u.getObject().getBannerUrl());
                newLogonUser.setProfile_picture_uri(u.getObject().getProfilePictureUrl());
                TwitterApplication.getTwitterInstance().getCacheDatabase().logonUserDao().save(newLogonUser);
                return u;
            }

        }.asyncExecute((DataFetcherAbstract.Result<User> result) -> {
            // Good to go!
            onResultListener.onSuccess(newLogonUser, result.getObject());
        });
    }

    private void showLoading(){
        loadingView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
    }

    private void showWebView(){
        loadingView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }
}
