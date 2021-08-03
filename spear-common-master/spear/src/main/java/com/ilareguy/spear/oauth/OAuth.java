package com.ilareguy.spear.oauth;

public class OAuth{

    private static OAuth instance;

    private Communicator globalCommunicator = null;
    private final ConsumerKey consumerKey;
    private AccessToken globalAccessToken = null;

    public OAuth(ConsumerKey consumerKey){
        instance = this;
        this.consumerKey = consumerKey;
    }

    public static OAuth getInstance(){ return instance; }

    public void setGlobalAccessToken(AccessToken a){ globalAccessToken = a; }
    public final AccessToken getGlobalAccessToken(){ return globalAccessToken; }
    public final ConsumerKey getConsumerKey(){ return consumerKey; }
    public final Communicator getGlobalCommunicator(){ return globalCommunicator; }
    public void setGlobalCommunicator(Communicator c){ globalCommunicator = c; }
}
