package com.ilareguy.spear.oauth;

public class ConsumerKey{

    private final String mKey;
    private final String mSecret;


    public ConsumerKey(final String key, final String secret){
        mKey = key;
        mSecret = secret;
    }

    public final String getKey(){ return mKey; }
    public final String getSecret(){ return mSecret; }
}
