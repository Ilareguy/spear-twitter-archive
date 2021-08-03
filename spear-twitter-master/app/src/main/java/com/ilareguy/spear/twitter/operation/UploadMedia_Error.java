package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class UploadMedia_Error{
    @JsonField public int code;
    @JsonField public String name;
    @JsonField public String message;
}
