package com.ilareguy.spear;

public enum SpearErrorCode{

    ERROR_UNKNOWN,

    /**
     * This error is intended to tell the developer that they are doing something wrong. A user should
     * never see an error of this type!
     */
    SPEAR_API_ERROR,

    /**
     * A native Java exception.
     */
    JAVA_EXCEPTION,

    /**
     * When the App tried to retrieve an object from the cache, but it couldn't
     * be found.
     */
    OBJECT_NOT_IN_CACHE,

    /**
     * There was an error parsing the JSON response received from the OAuth
     * server.
     */
    OAUTH_RESPONSE_INVALID_JSON,

    /**
     * The app couldn't establish a connection to the network; Internet unavailable.
     */
    INTERNET_UNAVAILABLE,

    /**
     * Like INTERNET_UNAVAILABLE, but indicates that the app reverted to reading data from the
     * cache instead of the remote endpoint.
     */
    INTERNET_UNAVAILABLE_REVERT_TO_CACHE,

    STREAM_FINALIZED,
    TASK_CANCELLED,
    OK_HTTP_ERROR,
    REQUEST_CANCELED,
    FILE_NOT_FOUND,

}
