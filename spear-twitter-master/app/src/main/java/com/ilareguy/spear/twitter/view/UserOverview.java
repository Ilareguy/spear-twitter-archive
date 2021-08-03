package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.Task;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.data_fetcher.DataFetcherAbstract;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.async_task.GetUser;
import com.ilareguy.spear.twitter.data.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public final class UserOverview extends ConstraintLayout{
    public UserOverview(Context c){ super(c); init(); }
    public UserOverview(Context c, AttributeSet a){ super(c, a); init(); }
    public UserOverview(Context c, AttributeSet a, int d){ super(c, a, d); init(); }

    private PageAbstract page;
    private @Nullable LoadUserTask loadUserTask = null;
    private long userId = 0;
    private TextView authorDisplayName, authorUsername;
    private SimpleDraweeView authorThumbnail;
    private ImageView badgeVerified, badgeTranslator;

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.user_overview, this, true);
    }

    @Override
    public void onFinishInflate(){
        // Find views
        authorDisplayName = findViewById(R.id.author_display_name);
        authorUsername = findViewById(R.id.author_username);
        authorThumbnail = findViewById(R.id.author_thumbnail);
        badgeVerified = findViewById(R.id.badge_verified);
        badgeTranslator = findViewById(R.id.badge_translator);

        super.onFinishInflate();
    }

    public void setPage(PageAbstract page){ this.page = page; }

    public void setUser(final @Nullable User user){
        if(user == null){
            userId = 0;
            return;
        }

        userId = user.getUid();

        authorUsername.setText("@" + user.getUsername());
        authorDisplayName.setText(user.getDisplay_name());

        // Hide or show badges
        badgeVerified.setVisibility(user.getVerified() ? VISIBLE : GONE);
        badgeTranslator.setVisibility(user.getTranslator() ? VISIBLE : GONE);
        authorThumbnail.setImageURI(user.getThumbnailUrl());
    }

    /**
     * Will asynchronously load the user's overview for the user with the given userId.
     */
    public void loadFromId(long user_id){
        // Cancel the currently-running task if necessary
        if(loadUserTask != null)
            loadUserTask.cancel(true);

        // Start a new background task
        loadUserTask = new LoadUserTask(page, this, user_id);
        loadUserTask.execute();
    }

    private static final class LoadUserTask extends Task<Void, Void, User>{
        final long user_id;
        final @NonNull UserOverview overviewObject;

        LoadUserTask(final PageAbstract page,
                     final @NonNull UserOverview overviewObject, long user_id){
            super(page);
            this.user_id = user_id;
            this.overviewObject = overviewObject;
        }

        @Override
        protected TaskResult<User> doInBackground(Void... v){
            return new GetUser(getCallingPage(), user_id, DataFetcherAbstract.Policy.CACHE_OR_OAUTH)
                    .execute();
        }

        @Override
        protected void onPostExecute(@NonNull TaskResult<User> result){
            overviewObject.setUser(result.getObject());
        }
    }

    public long getUserId(){ return userId; }
}
