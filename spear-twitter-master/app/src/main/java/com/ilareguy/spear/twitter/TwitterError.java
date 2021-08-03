package com.ilareguy.spear.twitter;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.SpearErrorCode;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class TwitterError extends SpearError{

    private static final TwitterErrorCode CODES[] = TwitterErrorCode.values();

    @JsonObject
    public static final class TwitterErrorJsonResponse{
        @JsonObject
        public static final class Error{
            @JsonField
            public String message;
            @JsonField
            public int code;
        }

        @JsonField
        public List<Error> errors;
    }

    public static TwitterError build(final TwitterErrorCode code) {
        return new TwitterError(code.ordinal(), code.name(), resolveErrorMessage(code));
    }

    public static TwitterError build(final TwitterErrorCode code, final String message) {
        return new TwitterError(code.ordinal(), code.name(), message);
    }

    public static TwitterError build(final Exception e) {
        return new TwitterError(SpearErrorCode.JAVA_EXCEPTION.ordinal(), e.getClass().getName(),
                e.getMessage());
    }

    private final @Nullable TwitterErrorJsonResponse.Error nativeTwitterError;

    @SuppressWarnings("WeakerAccess")
    public TwitterError(final int code, final String name, final String message){
        super(code, name, message);
        this.nativeTwitterError = null;
    }

    private TwitterError(TwitterErrorJsonResponse.Error e){
        super(TwitterErrorCode.NATIVE_TWITTER_ERROR.ordinal(), "NATIVE_TWITTER_ERROR",
                "Twitter error " + e.code + ": " + e.message);
        this.nativeTwitterError = e;
    }

    private static String resolveErrorMessage(final TwitterErrorCode code){
        return TwitterApplication.getTwitterInstance().getResources().getString(getStringId(code));
    }

    private static @StringRes int getStringId(final TwitterErrorCode code){
        switch(CODES[code.ordinal()]){
            //
        }

        return R.string.ERROR_UNKNOWN;
    }

}
