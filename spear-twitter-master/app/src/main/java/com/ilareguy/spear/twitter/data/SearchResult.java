package com.ilareguy.spear.twitter.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Represents a single search result returned by Twitter.
 */
@JsonObject
public class SearchResult {

    @JsonObject
    public static final class Metadata{
        @JsonField(name = "completed_in")
        private float completedIn;

        @JsonField(name = "count")
        private int count;

        @JsonField(name = "max_id")
        private long maxId;

        @JsonField(name = "since_id")
        private long sinceId;




        public float getCompletedIn() {
            return completedIn;
        }

        public void setCompletedIn(float completedIn) {
            this.completedIn = completedIn;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getMaxId() {
            return maxId;
        }

        public void setMaxId(long maxId) {
            this.maxId = maxId;
        }

        public long getSinceId() {
            return sinceId;
        }

        public void setSinceId(long sinceId) {
            this.sinceId = sinceId;
        }
    }

    @JsonField(name = "search_metadata")
    private Metadata metadata;

    @JsonField(name = "statuses")
    private List<Tweet> tweets;





    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
}
