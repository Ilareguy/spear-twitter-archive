package com.ilareguy.spear.twitter.data;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.twitter.twittertext.Extractor;

import java.io.Serializable;

@JsonObject
public abstract class TweetEntityAbstract implements Serializable{
    public enum Type{
        CASHTAG,
        HASHTAG,
        MENTION,
        MEDIA,
        URL
    }

    public static final Extractor entitiesExtractor = new Extractor();

    public static Tweet.Entities getEntities(String text){
        Tweet.Entities extracted_entities = new Tweet.Entities();
        extracted_entities.addUrlEntities(entitiesExtractor.extractURLsWithIndices(text));
        extracted_entities.addExtractedEntities(entitiesExtractor.extractCashtagsWithIndices(text));
        extracted_entities.addExtractedEntities(entitiesExtractor.extractHashtagsWithIndices(text));
        extracted_entities.addExtractedEntities(entitiesExtractor.extractMentionedScreennamesWithIndices(text));
        return extracted_entities;
    }

    /*
     * Don't parse those as the tweet's contents is likely to change. We'll compute the indices
     * later.
     */
    /*
    @JsonField(name = "indices")
    private int[] parsedIndices = new int[2];*/

    public int indexStart;
    public int indexEnd;

    public abstract Type getType();

    public TweetEntityAbstract(){}
    public TweetEntityAbstract(Extractor.Entity extracted_entity){
        indexStart = extracted_entity.getStart();
        indexEnd = extracted_entity.getEnd();
    }
}
