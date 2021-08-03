package com.ilareguy.spear.twitter.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

/**
 * Represents a single Tweet that has not yet been sent over to Twitter.
 */
@Entity(indices = {@Index("author_id")})
public class TweetDraft{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int entryId;

    @ColumnInfo(name = "author_id")
    private long authorId;

    @ColumnInfo(name = "raw_text")
    private String rawText;

    @ColumnInfo(name = "in_reply_to_tweet_id")
    private long inReplyToTweetId;

    @ColumnInfo(name = "attached_medias", typeAffinity = ColumnInfo.BLOB)
    private List<MediaMetadata> attachedMedias;

    public TweetDraft(){
        this.attachedMedias = new ArrayList<>();
    }


    public int getEntryId(){
        return entryId;
    }

    public void setEntryId(int entryId){
        this.entryId = entryId;
    }

    public long getAuthorId(){
        return authorId;
    }

    public void setAuthorId(long authorId){
        this.authorId = authorId;
    }

    public String getRawText(){
        return rawText;
    }

    public void setRawText(String rawText){
        this.rawText = rawText;
    }

    public long getInReplyToTweetId(){
        return inReplyToTweetId;
    }

    public void setInReplyToTweetId(long inReplyToTweetId){
        this.inReplyToTweetId = inReplyToTweetId;
    }

    public List<MediaMetadata> getAttachedMedias(){
        return attachedMedias;
    }

    public void setAttachedMedias(List<MediaMetadata> attachedMedias){
        this.attachedMedias = attachedMedias;
    }




    public static abstract class Converters{
        @TypeConverter
        public static List<MediaMetadata> fromBytes(byte[] bytes){
            if(bytes == null)
                return null;

            try{
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                return (ArrayList<MediaMetadata>) ois.readObject();
            }catch(IOException | ClassNotFoundException e){
                return null;
            }
        }

        @TypeConverter
        public static byte[] toBytes(List<MediaMetadata> medias){
            if(medias == null || medias.size() == 0)
                return null;

            try{
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(medias);
                return bos.toByteArray();
            }catch(IOException e){
                return null;
            }
        }
    }
}
