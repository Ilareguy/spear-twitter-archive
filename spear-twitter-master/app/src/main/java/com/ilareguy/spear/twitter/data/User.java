/*
Example response for a user:
{
    "id":1636590253,
    "id_str":"1636590253",
    "name":"Tim Cook",
    "screen_name":"tim_cook",
    "location":"Cupertino",
    "description":"CEO Apple, Fan of Auburn football and Duke basketball",
    "url":null,
    "entities":{"description":{"urls":[]}},
    "protected":false,
    "followers_count":10973340,
    "friends_count":57,
    "listed_count":103,
    "created_at":"Wed Jul 31 22:41:25 +0000 2013",
    "favourites_count":1164,
    "utc_offset":null,
    "time_zone":null,
    "geo_enabled":true,
    "verified":true,
    "statuses_count":558,
    "lang":"en",
    "contributors_enabled":false,
    "is_translator":false,
    "is_translation_enabled":false,
    "profile_background_color":"C0DEED",
    "profile_background_image_url":"http:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png",
    "profile_background_image_url_https":"https:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png",
    "profile_background_tile":false,
    "profile_image_url":"http:\/\/pbs.twimg.com\/profile_images\/378800000483764274\/ebce94fb34c055f3dc238627a576d251_normal.jpeg",
    "profile_image_url_https":"https:\/\/pbs.twimg.com\/profile_images\/378800000483764274\/ebce94fb34c055f3dc238627a576d251_normal.jpeg",
    "profile_link_color":"0084B4",
    "profile_sidebar_border_color":"FFFFFF",
    "profile_sidebar_fill_color":"DDEEF6",
    "profile_text_color":"333333",
    "profile_use_background_image":false,
    "has_extended_profile":false,
    "default_profile":false,
    "default_profile_image":false,
    "following":true,
    "follow_request_sent":false,
    "notifications":false,
    "translator_type":"none"
}
*/

package com.ilareguy.spear.twitter.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.UserPicturesURLParser;
import com.ilareguy.spear.util.Timestamp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import static com.ilareguy.spear.twitter.UserPicturesURLParser.VARIANT_BANNER_ORIGINAL;
import static com.ilareguy.spear.twitter.UserPicturesURLParser.VARIANT_PROFILE_ORIGINAL;

@Entity(primaryKeys = {"uid", "username"})
@JsonObject
public class User {

    public enum IdentificationMethod {
        BY_ID,
        BY_USERNAME
    }

    /**
     * Used to identify a specific user.
     */
    public static final class Identification{
        public User.IdentificationMethod idMethod;
        public long userId;
        public String username;

        public Identification(long userId){
            this.userId = userId;
            this.idMethod = User.IdentificationMethod.BY_ID;
        }

        public Identification(String username){
            this.username = username;
            this.idMethod = User.IdentificationMethod.BY_USERNAME;
        }

        public static boolean isSameUser(final User.Identification identification, final User user){
            if(identification.idMethod == User.IdentificationMethod.BY_ID)
                return user.getUid() == identification.userId;
            return user.getUsername().equalsIgnoreCase(identification.username);
        }
    }

    /*
     * Some more data could be saved.
     * See https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-show
     */
    @NonNull
    @JsonField(name = "id")
    private long uid;

    @NonNull
    @JsonField(name = "screen_name")
    private String username = ""; // EXCLUDING the "@"

    @ColumnInfo(name = "display_name")
    @JsonField(name = "name")
    private String display_name;

    @Nullable
    @JsonField
    private String description;

    @Nullable
    @JsonField
    private String location;

    @Nullable
    @JsonField
    private String url;

    @NonNull
    @JsonField(name = "verified")
    private Boolean verified;

    @NonNull
    @JsonField(name = "is_translator")
    private Boolean translator;

    @Nullable
    @ColumnInfo(name = "is_protected")
    @JsonField(name = "protected")
    private Boolean profileProtected;

    @Nullable
    @ColumnInfo(name = "followers_count")
    @JsonField(name = "followers_count")
    private Long followersCount;

    @Nullable
    @JsonField(name = "friends_count")
    @ColumnInfo(name = "following_count")
    private Long followingCount;

    @Nullable
    @JsonField(name = "favourites_count")
    @ColumnInfo(name = "favourites_count")
    private Integer favoritesCount;

    @Nullable
    @JsonField(name = "statuses_count")
    @ColumnInfo(name = "tweets_count")
    private Integer tweetsCount;

    @NonNull
    @JsonField(name = "profile_image_url_https")
    @ColumnInfo(name = "profile_image_default_url")
    private String profileImageDefaultUrl;

    @Nullable
    @JsonField(name = "profile_banner_url")
    @ColumnInfo(name = "profile_banner_default_url")
    private String profileBannerDefaultUrl;

    @Nullable
    @JsonField
    @ColumnInfo(name = "profile_link_color")
    private String profileLinkColor;

    @Nullable
    @JsonField
    private Boolean following;

    @Nullable
    @JsonField
    private Boolean followRequestSent;

    @ColumnInfo(name = "cached_timestamp")
    private Long cachedTimestamp;

    @Ignore
    private String thumbnailUrl = null;

    @Ignore
    private String bannerUrl = null;

    @Ignore
    private String profilePictureUrl = null;

    public User() {
    }

    /*@OnJsonParseComplete
    void onParseComplete() {
        //
    }*/

    public void cache() {
        cachedTimestamp = Timestamp.now();
        TwitterApplication.getTwitterInstance().getCacheDatabase().userDao().save(this);
    }

    public String getThumbnailUrl() {
        if(thumbnailUrl == null && profileImageDefaultUrl != null)
            thumbnailUrl = UserPicturesURLParser
                    .getProfilePictureVariantURL(this, VARIANT_PROFILE_ORIGINAL);
        return thumbnailUrl;
    }

    public String getBannerUrl() {
        if(bannerUrl == null && profileBannerDefaultUrl != null)
            bannerUrl = UserPicturesURLParser
                    .getProfileBannerVariantURL(this, VARIANT_BANNER_ORIGINAL);
        return bannerUrl;
    }

    public String getProfilePictureUrl() {
        if(profilePictureUrl == null && profileImageDefaultUrl != null)
            profilePictureUrl = UserPicturesURLParser
                    .getProfilePictureVariantURL(this, VARIANT_PROFILE_ORIGINAL);
        return profilePictureUrl;
    }

    ///////////////////


    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Boolean getTranslator() {
        return translator;
    }

    public void setTranslator(Boolean translator) {
        this.translator = translator;
    }

    public Boolean getProfileProtected() {
        return profileProtected;
    }

    public void setProfileProtected(Boolean profileProtected) {
        this.profileProtected = profileProtected;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Integer getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(Integer favoritesCount) {
        this.favoritesCount = favoritesCount;
    }

    public Integer getTweetsCount() {
        return tweetsCount;
    }

    public void setTweetsCount(Integer tweetsCount) {
        this.tweetsCount = tweetsCount;
    }

    public String getProfileImageDefaultUrl() {
        return profileImageDefaultUrl;
    }

    public void setProfileImageDefaultUrl(String profileImageDefaultUrl) {
        this.profileImageDefaultUrl = profileImageDefaultUrl;
    }

    public String getProfileBannerDefaultUrl() {
        return profileBannerDefaultUrl;
    }

    public void setProfileBannerDefaultUrl(String profileBannerDefaultUrl) {
        this.profileBannerDefaultUrl = profileBannerDefaultUrl;
    }

    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    public void setProfileLinkColor(String profileLinkColor) {
        this.profileLinkColor = profileLinkColor;
    }

    public Boolean getFollowing() {
        return (following == null) ? false : following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Long getCachedTimestamp() {
        return cachedTimestamp;
    }

    public void setCachedTimestamp(Long cachedTimestamp) {
        this.cachedTimestamp = cachedTimestamp;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean getFollowRequestSent() {
        return followRequestSent;
    }

    public void setFollowRequestSent(Boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
    }
}
