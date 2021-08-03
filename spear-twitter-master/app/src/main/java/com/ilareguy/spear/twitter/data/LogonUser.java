package com.ilareguy.spear.twitter.data;

import android.app.Application;

import com.ilareguy.spear.oauth.AccessToken;
import com.ilareguy.spear.twitter.LogonUserPreferences;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index(value = "username", unique = true))
public class LogonUser extends AccessToken{

    @Ignore
    private LogonUserPreferences preferences = null;

    @PrimaryKey
    private long uid;

    private String username;
    private String profile_picture_uri;
    private String banner_uri;

    @ColumnInfo(name = "is_current")
    private boolean isCurrent = false;

    @ColumnInfo(name = "last_logon_timestamp")
    private long lastLogonTimestamp;

    @ColumnInfo(name = "last_home_feed_refresh_timestamp")
    private long lastHomeFeedRefreshTimestamp;

    public LogonUser() {
        super();
        isCurrent = false;
    }

    public LogonUser(final String access_token, final String token_secret) {
        super(access_token, token_secret);
        isCurrent = false;
    }

    public LogonUser(final String username, final long user_id, final String access_token, final String token_secret) {
        super(access_token, token_secret);
        isCurrent = false;
        this.username = username;
        this.uid = user_id;
    }

    private static String getPreferencesFilenameForUserID(final String user_id) {
        return ("com.ilareguy.spear.twitter.account." + user_id);
    }

    /**
     * Loads the account's preferences. If the preferences file did not
     * already exist, it'll be generated here.
     * It does nothing if the preferences were already loaded.
     */
    public void loadPreferences(Application application) {
        if (preferences != null && preferences.isLoaded()) return; // Preferences already loaded

        if (preferences == null)
            preferences = new LogonUserPreferences(application, getPreferencesFilename());

        preferences.load();

        // Check if it's the first use
        if (preferences.FIRST_USE.getValue()) {
            // First use; create preferences file
            preferences.ACCESS_TOKEN.setValue(getToken());
            preferences.ACCESS_TOKEN_SECRET.setValue(getSecret());
            preferences.FIRST_USE.setValue(false);
            preferences.USERNAME.setValue(username);

            preferences.commit();
        }
    }

    private String getPreferencesFilename() {
        return getPreferencesFilenameForUserID(String.valueOf(getUid()));
    }


    // Getters & Setters

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

    public String getProfile_picture_uri(){
        return profile_picture_uri;
    }

    public void setProfile_picture_uri(String profile_picture_uri){
        this.profile_picture_uri = profile_picture_uri;
    }

    public String getBanner_uri(){
        return banner_uri;
    }

    public void setBanner_uri(String banner_uri){
        this.banner_uri = banner_uri;
    }


    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        this.isCurrent = current;
    }

    public long getLastLogonTimestamp() {
        return lastLogonTimestamp;
    }

    public void setLastLogonTimestamp(long lastLogonTimestamp) {
        this.lastLogonTimestamp = lastLogonTimestamp;
    }

    public long getLastHomeFeedRefreshTimestamp(){
        return lastHomeFeedRefreshTimestamp;
    }

    public void setLastHomeFeedRefreshTimestamp(long lastHomeFeedRefreshTimestamp){
        this.lastHomeFeedRefreshTimestamp = lastHomeFeedRefreshTimestamp;
    }
}
