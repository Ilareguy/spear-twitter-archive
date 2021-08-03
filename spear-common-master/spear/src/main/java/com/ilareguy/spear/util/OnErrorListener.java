package com.ilareguy.spear.util;

import com.ilareguy.spear.SpearError;

import androidx.annotation.NonNull;

public interface OnErrorListener{
    void onError(@NonNull final SpearError e);
}
