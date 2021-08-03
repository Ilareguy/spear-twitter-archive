package com.ilareguy.spear.twitter.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ilareguy.spear.twitter.MediaType;
import com.ilareguy.spear.util.StringHelper;

public class MediaMetadata implements Parcelable{

    private long id;
    private String path;
    private String name;
    private String mime;
    private MediaType type;

    public MediaMetadata(){}

    private MediaMetadata(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.path = in.readString();
        this.type = MEDIA_TYPES[in.readInt()];
    }

    public MediaMetadata(long id, String path, String name){
        this.id = id;
        this.path = path;
        this.name = name;
        this.type = getMediaTypeFromFilename(path);
    }

    private MediaType getMediaTypeFromFilename(final String filename){
        this.mime = StringHelper.getFileMime(filename);

        if(mime.equalsIgnoreCase("image/gif"))
            return MediaType.ANIMATED_GIF;

        if(mime.startsWith("image/"))
            return MediaType.PHOTO;

        if(mime.startsWith("video/"))
            return MediaType.VIDEO;

        return MediaType.INVALID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeInt(type.ordinal());
    }

    public static final MediaType[] MEDIA_TYPES = MediaType.values();
    public static final Creator<MediaMetadata> CREATOR = new Creator<MediaMetadata>() {
        public MediaMetadata createFromParcel(Parcel source) {
            return new MediaMetadata(source);
        }
        public MediaMetadata[] newArray(int size) {
            return new MediaMetadata[size];
        }
    };

    public long getId(){ return id; }
    public String getPath(){ return path; }
    public String getName(){ return name; }
    public MediaType getType(){ return type; }
    public String getMime(){ return mime; }
}
