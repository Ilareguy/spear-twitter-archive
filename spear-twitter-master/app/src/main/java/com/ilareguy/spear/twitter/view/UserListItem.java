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

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class UserListItem extends ConstraintLayout implements View.OnClickListener{
    public UserListItem(Context c){ super(c); init(null); }
    public UserListItem(Context c, AttributeSet a){ super(c, a); init(a); }
    public UserListItem(Context c, AttributeSet a, int d){ super(c, a, d); init(a); }

    private User user = null;
    private @Nullable TwitterNavigation nav = null;

    private void init(final AttributeSet attr){
        LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, this, true);
        setOnClickListener(this);
    }

    public void setUser(final @Nullable User user){
        this.user = user;

        ((SimpleDraweeView) findViewById(R.id.thumbnail)).setImageURI(user == null ? null : user.getProfilePictureUrl());
        ((TextView) findViewById(R.id.display_name)).setText(user == null ? "" : user.getDisplay_name());
        ((TextView) findViewById(R.id.username)).setText("@" + (user == null ? "" : user.getUsername()));
    }

    @Override
    public void onClick(View view){
        if(user != null && nav != null)
            nav.viewUser(user.getUid(), null);
    }

    public void setTwitterNavigation(@Nullable TwitterNavigation nav){ this.nav = nav; }
}
