package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterNavigation;
import com.ilareguy.spear.twitter.data.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class UserCard extends ConstraintLayout implements View.OnClickListener{
    public UserCard(Context c){ super(c); init(); }
    public UserCard(Context c, AttributeSet a){ super(c, a); init(); }
    public UserCard(Context c, AttributeSet a, int d){ super(c, a, d); init(); }

    private @Nullable TwitterNavigation twitterNavigation;
    private User user;

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.user_card, this, true);
        setOnClickListener(this);
    }

    public void setTwitterNavigation(@Nullable final TwitterNavigation twitterNavigation){
        this.twitterNavigation = twitterNavigation;
    }

    public void setUser(@NonNull final User user){
        this.user = user;
        ((TextView) findViewById(R.id.display_name)).setText(user.getDisplay_name());
        ((TextView) findViewById(R.id.username)).setText("@" + user.getUsername());
        ((TextView) findViewById(R.id.description)).setText(user.getDescription());
        ((SimpleDraweeView) findViewById(R.id.profile_picture)).setImageURI(user.getProfilePictureUrl());
        ((SimpleDraweeView) findViewById(R.id.banner_picture)).setImageURI(user.getBannerUrl());
        findViewById(R.id.badge_verified).setVisibility(user.getVerified() ? View.VISIBLE : View.GONE);
        findViewById(R.id.badge_translator).setVisibility(user.getTranslator() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View var1){
        if(twitterNavigation != null && user != null)
            twitterNavigation.viewUser(user.getUid(), null);
    }
}
