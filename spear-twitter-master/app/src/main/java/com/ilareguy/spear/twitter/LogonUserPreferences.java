package com.ilareguy.spear.twitter;

import android.app.Application;

import com.ilareguy.spear.util.PreferencesManager;

public class LogonUserPreferences extends PreferencesManager {

    public EntryBool FIRST_USE = new EntryBool("first_use", true);
    public EntryString USERNAME = new EntryString("username");
    public EntryString ACCESS_TOKEN = new EntryString("access_token");
    public EntryString ACCESS_TOKEN_SECRET = new EntryString("access_token_secret");

    public LogonUserPreferences(Application application, final String filename) {
        super(application, filename);

        // Register preference entries
        registerEntry(FIRST_USE);
        registerEntry(USERNAME);
        registerEntry(ACCESS_TOKEN);
        registerEntry(ACCESS_TOKEN_SECRET);
    }

}
