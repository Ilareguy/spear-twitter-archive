package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * For some reason, this class needs to be in its own file for it to compile properly.
 * I LOVE Java!
 */
@JsonObject
class UploadMedia_CommandInitResult{
    @JsonField(name = "media_id") public long mediaId;
}
