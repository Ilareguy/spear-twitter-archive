package com.ilareguy.spear.oauth;

import com.ilareguy.spear.util.Timestamp;

public class BasicRequest extends RequestAbstract{

    private final AccessToken mAccessToken;

    public BasicRequest(final ConsumerKey consumer_key, final AccessToken access_token,
                        RequestAbstract.Method method, String url){
        super(consumer_key, method, url);
        mAccessToken = access_token;
    }

    public final AccessToken getAccessToken(){ return mAccessToken; }

    @Override
    public void sign(){
        if(!requiresSigning()) return;

        addParameter("oauth_version", "1.0");
        addParameter("oauth_nonce", RequestAbstract.getNonce());
        addParameter("oauth_timestamp", String.valueOf(Timestamp.now()));
        addParameter("oauth_consumer_key", getConsumerKey().getKey());
        addParameter("oauth_token", getAccessToken().getToken());
        addParameter("oauth_signature_method", "HMAC-SHA1");

        // Then add the signature, *after* the other parameters are set
        addParameter("oauth_signature", (SignatureGenerator.getSignatureForRequest(this)));
    }
}
