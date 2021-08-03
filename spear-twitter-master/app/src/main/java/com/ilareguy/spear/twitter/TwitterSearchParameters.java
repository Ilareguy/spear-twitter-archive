package com.ilareguy.spear.twitter;

import java.util.ArrayList;
import java.util.List;

/**
 * https://developer.twitter.com/en/docs/tweets/search/guides/standard-operators
 */
public final class TwitterSearchParameters{

    public enum TweetType{
        RECENT,
        POPULAR,
        MIXED
    }

    public enum TweetAttitude{
        DEFAULT,
        POSITIVE,
        NEGATIVE
    }

    public enum TweetFilter{
        SAFE,
        MEDIA,
        RETWEETS,
        NATIVE_VIDEO,   // an uploaded video, Amplify video, Periscope, or Vine
        PERISCOPE,      // a Periscope video URL
        VINE,
        IMAGES,         // links identified as photos, including third parties such as Instagram
        TWIMG,          // a pic.twitter.com link representing one or more photos
        LINKS
    }

    public enum TweetFilterType{
        DEFAULT,
        EXCLUDE,
        INCLUDE
    }

    private static final TweetFilter[] FILTERS = TweetFilter.values();

    private StringBuilder baseQueryStringBuilder = new StringBuilder();
    private List<UrlFilter> urlFilters = new ArrayList<>();
    private TweetFilterType[] filterTypes;
    private TweetAttitude attitude = TweetAttitude.DEFAULT;
    private TweetType tweetType = TweetType.MIXED;
    private boolean searchUsers = true;

    public TwitterSearchParameters(final String string_query){
        init();
        baseQueryStringBuilder.append(string_query);
        baseQueryStringBuilder.append(" ");
    }

    public TwitterSearchParameters(){
        init();
    }

    private void init(){
        filterTypes = new TweetFilterType[FILTERS.length];
        for(int i = 0; i < filterTypes.length; i++)
            filterTypes[i] = TweetFilterType.DEFAULT;
    }

    /**
     * @return Returns the base query string — that is, without any filter applied to it.
     */
    public String getBaseQueryString(){
        return baseQueryStringBuilder.toString();
    }

    public String buildQueryString(){
        StringBuilder finalQueryStringBuilder = new StringBuilder();
        finalQueryStringBuilder.append(baseQueryStringBuilder.toString());

        // Filters
        int i = 0;
        for(TweetFilterType f : filterTypes){
            if(f == TweetFilterType.DEFAULT){
                i++;
                continue;
            }
            if(f == TweetFilterType.EXCLUDE) finalQueryStringBuilder.append("-");
            finalQueryStringBuilder.append("filter:");
            finalQueryStringBuilder.append(FILTERS[i].name().toLowerCase());
            finalQueryStringBuilder.append(" ");
            i++;
        }

        // URL filterTypes
        for(UrlFilter f : urlFilters){
            if(f.type == TweetFilterType.DEFAULT) continue;
            if(f.type == TweetFilterType.EXCLUDE) finalQueryStringBuilder.append("-");
            finalQueryStringBuilder.append("url:");
            finalQueryStringBuilder.append(f.url);
            finalQueryStringBuilder.append(" ");
        }

        // Attitude
        if(attitude == TweetAttitude.NEGATIVE) finalQueryStringBuilder.append(":(");
        else if(attitude == TweetAttitude.POSITIVE) finalQueryStringBuilder.append(":)");

        return finalQueryStringBuilder.toString();
    }

    /**
     * @param terms Can be #hashtags, $cashtags, @usernames, or words.
     */
    public void addTerms(String... terms){
        for(String str : terms){
            baseQueryStringBuilder.append(str);
            baseQueryStringBuilder.append(" ");
        }
    }

    public void addExactTerms(String... terms){
        for(String str : terms){
            baseQueryStringBuilder.append("\"");
            baseQueryStringBuilder.append(str);
            baseQueryStringBuilder.append("\" ");
        }
    }

    public void excludeTerms(String... terms){
        for(String str : terms){
            baseQueryStringBuilder.append("-");
            baseQueryStringBuilder.append(str);
            baseQueryStringBuilder.append(" ");
        }
    }

    public void excludeExactTerms(String... terms){
        for(String str : terms){
            baseQueryStringBuilder.append("-");
            baseQueryStringBuilder.append("\"");
            baseQueryStringBuilder.append(str);
            baseQueryStringBuilder.append("\" ");
        }
    }

    /**
     * @param username Do not include the '@'.
     */
    public void fromUsername(String username){
        baseQueryStringBuilder.append("from:");
        baseQueryStringBuilder.append(username);
        baseQueryStringBuilder.append(" ");
    }

    /**
     * @param username Do not include the '@'.
     */
    public void toUsername(String username){
        baseQueryStringBuilder.append("to:");
        baseQueryStringBuilder.append(username);
        baseQueryStringBuilder.append(" ");
    }

    public void addFilter(TweetFilter filter, TweetFilterType type){
        filterTypes[filter.ordinal()] = type;
    }

    public void addUrlFilter(String url, TweetFilterType type){
        urlFilters.add(new UrlFilter(url, type));
    }

    public void setAttitude(TweetAttitude a){
        this.attitude = a;
    }

    public void setSearchUsers(boolean s){ this.searchUsers = s; }

    public boolean searchUsers(){ return searchUsers; }

    public void setTweetType(TweetType t){ this.tweetType = t; }

    public TweetType getTweetType(){ return tweetType; }



    private static final class UrlFilter{
        final String url;
        final TweetFilterType type;
        UrlFilter(final String url, final TweetFilterType type){
            this.url = url;
            this.type = type;
        }
    }

}
