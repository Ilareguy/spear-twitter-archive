package com.ilareguy.spear.oauth;

public class AccessToken{

    private String token;
    private String secret;

    public AccessToken(){}
    public AccessToken(final String token, final String secret){
        this.token = token;
        this.secret = secret;
    }

    /**
     * Builds and returns a new OAuthRequest object with its access token
     * set to the TwitterAccount's access token, and the consumer key set
     * to the App's consumer key.
     */
    public BasicRequest buildOAuthRequest(RequestAbstract.Method method, String url) {
        return new BasicRequest(OAuth.getInstance().getConsumerKey(),
                OAuth.getInstance().getGlobalAccessToken(),
                method, url);
    }

    public MultipartRequest buildOAuthMultipartRequest(RequestAbstract.Method method, String url){
        return new MultipartRequest(OAuth.getInstance().getConsumerKey(),
                OAuth.getInstance().getGlobalAccessToken(),
                url);
    }

    public void setToken(String token){ this.token = token; }
    public void setSecret(String secret){ this.secret = secret; }
    public String getToken(){ return token; }
    public String getSecret(){ return secret; }

}
