package com.ilareguy.spear;

import androidx.annotation.StringRes;

public class SpearError{

    private static final SpearErrorCode CODES[] = SpearErrorCode.values();

    public static SpearError build(final SpearErrorCode code) {
        return new SpearError(code.ordinal(), code.name());
    }

    public static SpearError build(final SpearErrorCode code, final String message) {
        return new SpearError(code.ordinal(), code.name(), message);
    }

    public static SpearError build(final Exception e) {
        return new SpearError(SpearErrorCode.JAVA_EXCEPTION.ordinal(), e.getClass().getName(), e.getMessage());
    }

    private final String mName;
    private final String mMessage;
    private final int mCode;

    public SpearError(final int code, final String name){
        mCode = code;
        mName = name;
        mMessage = resolveErrorMessage();
    }

    public SpearError(final int code, final String name, final String message){
        mCode = code;
        mName = name;
        mMessage = message;
    }

    private String resolveErrorMessage(){
        return App.getInstance().getResources().getString(getStringId());
    }

    private @StringRes int getStringId(){
        switch(CODES[mCode]){
            case INTERNET_UNAVAILABLE_REVERT_TO_CACHE: return R.string.INTERNET_UNAVAILABLE_REVERT_TO_CACHE;
        }

        return R.string.ERROR_UNKNOWN;
    }

    public final int getCode(){ return mCode; }
    public final String getMessage(){ return mMessage; }
    public final String getName(){ return mName; }

}
