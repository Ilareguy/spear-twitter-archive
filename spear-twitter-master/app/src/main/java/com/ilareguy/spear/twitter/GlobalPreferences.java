package com.ilareguy.spear.twitter;

import android.app.Application;

import com.ilareguy.spear.util.PreferencesManager;

public class GlobalPreferences extends PreferencesManager {

    public PreferencesManager.EntryBool FIRST_LAUNCH = new EntryBool("first_use", true);
    public EntryLong CURRENT_LOGON_USER = new EntryLong("current_user", 0);

    public GlobalPreferences(Application application) {
        super(application, "com.ilareguy.spear.twitter.GLOBAL_PREFERENCES");

        // Register settings to be loaded/saved automagically
        registerEntry(FIRST_LAUNCH);
        registerEntry(CURRENT_LOGON_USER); // UID of user currently logged on
    }

}
