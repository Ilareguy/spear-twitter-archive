package com.ilareguy.spear.twitter;

import android.content.Context;
import android.net.Uri;

import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.Communicator;
import com.ilareguy.spear.oauth.ConsumerKey;
import com.ilareguy.spear.twitter.data.AppDatabase;
import com.ilareguy.spear.twitter.data.LogonUser;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.Serializable;

import androidx.annotation.Nullable;
import androidx.room.Room;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

import static com.ilareguy.spear.twitter.Config.CONSUMER_KEY;
import static com.ilareguy.spear.twitter.Config.CONSUMER_SECRET;
import static com.ilareguy.spear.twitter.Config.VERSION_MAJOR;
import static com.ilareguy.spear.twitter.Config.VERSION_MINOR;
import static com.ilareguy.spear.twitter.Config.VERSION_REVISION;

public class TwitterApplication extends com.ilareguy.spear.App{

    private static final String CACHE_DATABASE_FILENAME = "CACHE";

    private GlobalPreferences globalPreferences;
    private AppDatabase cacheDatabase;

    public TwitterApplication() {
        super();
    }

    public static TwitterApplication getTwitterInstance() {
        return (TwitterApplication) getInstance();
    }

    @Override
    public void onCreate(){
        initGlobalPreferences(); // load global settings
        new OAuth(new ConsumerKey(CONSUMER_KEY, CONSUMER_SECRET));
        OAuth.getInstance().setGlobalCommunicator(new Communicator());
        initCacheDatabase(this);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Karla-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        super.onCreate();
        JodaTimeAndroid.init(this);
    }

    /*@Override
    @WorkerThread
    protected void asyncInitialize(){
        super.asyncInitialize();
    }*/

    private void initGlobalPreferences() {
        globalPreferences = new GlobalPreferences(this);
        globalPreferences.load();
    }

    private void initCacheDatabase(Context context) {
        cacheDatabase = Room.databaseBuilder(context, AppDatabase.class, CACHE_DATABASE_FILENAME).build();
    }

    /**
     * Sets the current TwitterAccount currently using the App.
     */
    public void setCurrentUser(@Nullable LogonUser newCurrentAccount) {
        if(newCurrentAccount == null) return;

        // Set the new current user in memory
        OAuth.getInstance().setGlobalAccessToken(newCurrentAccount);

        // Set the new current in global preferences
        globalPreferences.CURRENT_LOGON_USER.setValue(newCurrentAccount.getUid());
        globalPreferences.commit();
    }

    // Getters & Setters
    public GlobalPreferences getGlobalPreferences() {
        return globalPreferences;
    }
    public String getVersionString() {
        return (String.valueOf(VERSION_MAJOR) + "." + String.valueOf(VERSION_MINOR) + VERSION_REVISION);
    }
    public LogonUser getCurrentLogonUser() {
        return (LogonUser) OAuth.getInstance().getGlobalAccessToken();
    }

    public AppDatabase getCacheDatabase() {
        return cacheDatabase;
    }

    public static final class _ImageLoader implements ImageLoader, Serializable{
        @Override
        public void loadImage(String s, SimpleDraweeView imageView, ImageType imageType){
            imageView.setImageRequest(ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse("file://" + s))
                    .setResizeOptions(imageLoaderResizeOptions)
                    .build());
        }
    }

    private static final ResizeOptions imageLoaderResizeOptions = new ResizeOptions(200, 200);
    public static final _ImageLoader imageLoader = new _ImageLoader();

}
