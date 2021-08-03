package com.ilareguy.spear.twitter;

public enum TwitterErrorCode{
    /**
     * Twitter returned one of their own error codes and message. Such an error could be, for example,
     * code 88: "Rate limit exceeded".
     * See https://developer.twitter.com/en/docs/basics/response-codes for error codes used by Twitter.
     */
    NATIVE_TWITTER_ERROR,

    /**
     * There was an error fetching a user's timeline. This happens when both the cache read
     * failed (typically when the cache is empty) AND there was an error performing an OAuth
     * request.
     *
     * This is most likely result of fetching a timeline for the first time since it was cleared
     * and having no Internet access.
     */
    CANNOT_FETCH_TIMELINE,

    /**
     * The cache home timeline for a specific user is empty.
     */
    USER_TIMELINE_CACHE_EMPTY,

    /**
     * The user has canceled the OAuth authentication process (likely), or something wrong happened
     * whilst parsing Twitter's response (unlikely).
     */
    OAUTH_AUTHENTICATION_FAILED,

    /**
     * Somehow, the we tried setting the current TwitterAccount with an account that wasn't previously
     * registered with the app. This shouldn't happen unless the app user messed around with the app
     * data.
     */
    USER_NOT_LOGGED_IN,

    /**
     * Somehow the app couldn't handle or decode a response sent by Twitter.
     */
    TWITTER_INVALID_RESPONSE,

}
