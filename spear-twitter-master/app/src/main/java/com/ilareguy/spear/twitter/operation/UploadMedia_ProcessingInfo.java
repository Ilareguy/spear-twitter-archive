package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import androidx.annotation.Nullable;

/**
 * For some reason, this class needs to be in its own file for it to compile properly.
 * I LOVE Java!
 */
@JsonObject
public class UploadMedia_ProcessingInfo{
    @JsonField public String state;
    @JsonField(name = "check_after_secs") public int waitSeconds;
    @JsonField public @Nullable UploadMedia_Error error;
}
