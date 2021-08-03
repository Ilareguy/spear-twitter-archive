package com.ilareguy.spear.oauth;

import com.ilareguy.spear.util.Timestamp;

public class AuthenticationRequest extends RequestAbstract{

    public AuthenticationRequest(final ConsumerKey consumer_key, RequestAbstract.Method method, String url){
        super(consumer_key, method, url);
    }

    @Override
    public void sign(){
        addParameter("oauth_version", "1.0");
        addParameter("oauth_nonce", RequestAbstract.getNonce());
        addParameter("oauth_timestamp", String.valueOf(Timestamp.now()));
        addParameter("oauth_consumer_key", getConsumerKey().getKey());
        addParameter("oauth_signature_method", "HMAC-SHA1");

        // Then add the signature, *after* the other parameters are set
        addParameter("oauth_signature", (SignatureGenerator.getSignatureForRequest(this)));
    }

}
