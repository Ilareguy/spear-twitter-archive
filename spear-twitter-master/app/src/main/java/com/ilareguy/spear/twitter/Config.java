package com.ilareguy.spear.twitter;

public abstract class Config {

    /*
     * These settings MUST match the ones set on Twitter's API page.
     * See https://apps.twitter.com/app/15034642/show
     */
    public static final String CONSUMER_KEY = "GVixQIo5jVB5XplUDaUac9Zhr";
    public static final String CONSUMER_SECRET = "LFQkuXEPPZELr8uQE9koULx6RObS96hKk6uIhksLYSjYMUGzAL";
    public static final String OAUTH_AUTHENTICATION_CALLBACK = "https://ilareguy.com/twitter-callback";

    // Constants
    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;
    public static final String VERSION_REVISION = "-APLHA";

    /**
     * The timestamp format used by Twitter.
     */
    public static final String TWITTER_TIMESTAMP_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

}
