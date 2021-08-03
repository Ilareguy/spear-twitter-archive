package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import androidx.annotation.Nullable;

/**
 * For some reason, this class needs to be in its own file for it to compile properly.
 * I LOVE Java!
 */
@JsonObject
class UploadMedia_CommandFinalizeResult{
    @JsonField
    public @Nullable UploadMedia_ProcessingInfo processingInfo;
}
