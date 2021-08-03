package com.ilareguy.spear.twitter;

import android.content.Context;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.App;
import com.ilareguy.spear.EventOrigin;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.twitter.page.Splash;
import com.ilareguy.spear.twitter.page.UserProfile;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends ActivityAbstract implements TwitterNavigation {

    public static MainActivity getInstance(){
        return (MainActivity) App.getInstance().getActivityInstance();
    }

    @Override
    protected PageAbstract buildMainPage(){
        return new Splash(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////////// TwitterNavigation interface ////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    @Override
    public void viewUser(long userId, @Nullable final EventOrigin origin){
        final UserProfile next_page = new UserProfile(this);
        next_page.setTargetUserId(new User.Identification(userId));
        getRootPage().loadPage(next_page);
    }

    @Override
    public void viewUser(String username, @Nullable final EventOrigin origin){
        final UserProfile next_page = new UserProfile(this);
        next_page.setTargetUserId(new User.Identification(username));
        getRootPage().loadPage(next_page);
    }

    @Override
    public void viewTweet(long tweetId, @Nullable final EventOrigin origin){
        //
    }

    @Override
    public void viewHashtag(String hashtag, @Nullable final EventOrigin origin){
        //
    }

    @Override
    public void switchAccount(@NonNull final LogonUser newLogonUser, @Nullable final EventOrigin origin){
        //
    }

    @Override
    public void registerNewAccount(){
        //
    }

    @Override
    public void viewURL(String url, @Nullable final EventOrigin origin){
        //
    }

    @Override
    public void viewMedia(String url){
        //
    }

    @Override
    public void viewMedias(List<Tweet.Entities.MediaEntity> medias, int startPosition){
        //
    }

    @Override
    public void searchTwitter(TwitterSearchParameters queryBuilder){
        //
    }

    @Override
    public void composeTweet(String text, long inResponseToTweetId){
        //
    }
}
