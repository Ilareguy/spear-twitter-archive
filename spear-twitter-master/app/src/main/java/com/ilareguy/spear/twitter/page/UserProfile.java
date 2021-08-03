package com.ilareguy.spear.twitter.page;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.snackbar.Snackbar;
import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.data_fetcher.DataFetcherAbstract;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.async_task.FollowUser;
import com.ilareguy.spear.twitter.async_task.GetRelationship;
import com.ilareguy.spear.twitter.async_task.GetUser;
import com.ilareguy.spear.twitter.data.Relationship;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.twitter.data.UserDao;
import com.ilareguy.spear.twitter.view.TwitterTextView;
import com.ilareguy.spear.util.StringHelper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class UserProfile extends PageAbstract {
    public UserProfile(Context c){ super(c); }

    private @Nullable User.Identification targetUserId = null;
    private @Nullable User targetUser = null;

    private boolean followingTarget = false;
    private boolean followedByTarget = false;
    private boolean updatingRelationship = false;

    public void setTargetUserId(User.Identification targetUserId){ this.targetUserId = targetUserId; }

    protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                         ActivityAbstract.Orientation orientation){
        return new MobileVariant(this);
    }

    private final class MobileVariant extends Variant<NestedScrollView>
            implements Variant.AsyncLifecycle{
        MobileVariant(PageAbstract pageInstance){ super(pageInstance); }

        @Override
        protected NestedScrollView inflate(LayoutInflater inflater){
            final NestedScrollView root_view = new NestedScrollView(getContext(), null, R.style.AppTheme);
            root_view.setBackgroundColor(getContext().getResources().getColor(R.color.backgroundColorPrimary, null));
            inflater.inflate(R.layout.page_user_profile, root_view, true);
            return root_view;
        }

        @Override
        protected void initializeLayout(){
            findViewById(R.id.follow_button).setOnClickListener(this::updateRelationship);
            if(targetUser != null) onUserUpdated();
        }

        private Resources getResources(){ return getRootView().getResources(); }
        private View findViewById(@IdRes int id){ return getRootView().findViewById(id); }

        private void onUserUpdated(){
            findViewById(R.id.badge_translator).setVisibility(targetUser.getTranslator()
                    ? View.VISIBLE : View.GONE);
            findViewById(R.id.badge_verified).setVisibility(targetUser.getVerified()
                    ? View.VISIBLE : View.GONE);
            ((TextView) findViewById(R.id.username)).setText("@" + targetUser.getUsername());
            ((TextView) findViewById(R.id.display_name)).setText(targetUser.getDisplay_name());
            ((TwitterTextView) findViewById(R.id.description)).setText(targetUser.getDescription());
            ((SimpleDraweeView) findViewById(R.id.banner_picture)).setImageURI(targetUser.getBannerUrl());
            ((SimpleDraweeView) findViewById(R.id.profile_picture)).setImageURI(targetUser.getProfilePictureUrl());
            ((TextView) findViewById(R.id.location_text))
                    .setText(targetUser.getLocation().equalsIgnoreCase("")
                            ? getResources().getString(R.string.location_unknown)
                            : targetUser.getLocation());
            ((TextView) findViewById(R.id.like_count_text))
                    .setText(getResources().getString(R.string.user_like_count,
                            StringHelper.numberWithSuffix(targetUser.getFavoritesCount())));
            ((TextView) findViewById(R.id.tweet_count_text))
                    .setText(getResources().getString(R.string.user_tweet_count,
                            StringHelper.numberWithSuffix(targetUser.getTweetsCount())));
        }

        private void onRelationshipUpdated(){
            findViewById(R.id.follows_you).setVisibility(followedByTarget
                    ? View.VISIBLE : View.GONE);
            final Button follow_button = (Button) findViewById(R.id.follow_button);
            follow_button.setAlpha(1.0f);
            follow_button.setVisibility(targetUserId.userId != TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid()
                    ? View.VISIBLE : View.GONE);
            follow_button.setBackgroundColor(getResources().getColor(
                    followingTarget ? android.R.color.white : R.color.colorPrimary,
                     null
            ));
            follow_button.setTextColor(getResources().getColor(
                    followingTarget ? android.R.color.black : android.R.color.white,
                    null
            ));
            follow_button.setText(getResources().getString(
                    followingTarget ? R.string.unfollow : R.string.follow
            ));
        }

        @Override
        public void restoreState(Bundle bundle){
            followingTarget = bundle.getBoolean("followingTarget");
            followedByTarget = bundle.getBoolean("followedByTarget");
            onRelationshipUpdated();
        }

        @Override
        public void load(@Nullable Bundle savedState){
            final UserDao dao = TwitterApplication.getTwitterInstance().getCacheDatabase().userDao();

            // Check if we're building this page from a previous state
            if(savedState != null){
                // Retrieve the previous target user
                targetUserId = (savedState.getInt("ID_METHOD") == User.IdentificationMethod.BY_ID.ordinal())
                        ? new User.Identification(savedState.getLong("TARGET_USER_ID"))
                        : new User.Identification(savedState.getString("TARGET_USER_USERNAME"));
            }else if(targetUserId == null) {
                // There is no target user set; default back to the logon user
                targetUserId = new User.Identification(TwitterApplication.getTwitterInstance()
                        .getCurrentLogonUser().getUid());
            }

            // Load the actual user data
            targetUser = (targetUserId.idMethod == User.IdentificationMethod.BY_ID)
                    ? dao.get(targetUserId.userId)
                    : dao.get(targetUserId.username);
        }

        @Override
        public void save(){}

        @Override
        public @NonNull Bundle saveState(){
            final Bundle bundle = super.saveState();
            bundle.putInt("ID_METHOD", targetUserId.idMethod.ordinal());
            if(targetUserId.idMethod == User.IdentificationMethod.BY_ID)
                bundle.putLong("TARGET_USER_ID", targetUserId.userId);
            else
                bundle.putString("TARGET_USER_USERNAME", targetUserId.username);
            bundle.putBoolean("followingTarget", followingTarget);
            bundle.putBoolean("followedByTarget", followedByTarget);
            return bundle;
        }

        @Override
        public @Nullable Animator buildEnterAnimator(){
            final AnimatorSet animators = new AnimatorSet();

            final ObjectAnimator fade_animator = ObjectAnimator.ofFloat(getRootView(), "alpha",
                    0f, 1f);
            fade_animator.setDuration(200);
            fade_animator.setInterpolator(new LinearInterpolator());

            final ObjectAnimator slide_animator = ObjectAnimator.ofFloat(getRootView(), "y",
                    200, 0);
            slide_animator.setDuration(350);
            slide_animator.setInterpolator(new FastOutSlowInInterpolator());

            animators.playTogether(fade_animator, slide_animator);
            animators.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { remoteLoadUser(); }
                @Override
                public void onAnimationEnd(Animator animation) {}
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            return animators;
        }

        private void updateRelationship(View followButton){
            if(updatingRelationship) return;
            updatingRelationship = true;
            followButton.setAlpha(0.3f);

            new FollowUser(getPageInstance(), targetUserId, !followingTarget).asyncExecute(
                    (DataFetcherAbstract.Result<User> result) -> {
                        if(result.isSuccessful()){
                            updatingRelationship = false;
                            followingTarget = !followingTarget;
                            Snackbar snackbar = Snackbar.make(getRootView(),
                                    followingTarget
                                            ? getResources().getString(
                                                    R.string.following_username, targetUser.getUsername())
                                            : getResources().getString(
                                                    R.string.unfollowed_username, targetUser.getUsername()),
                                    2200);
                            snackbar.show();
                            onRelationshipUpdated();
                        }
                    }
            );
        }

        private void remoteLoadUser(){
            // Get the latest user data
            new GetUser(getPageInstance(), targetUserId, DataFetcherAbstract.Policy.FORCE_REMOTE)
                    .asyncExecute((DataFetcherAbstract.Result<User> result) -> {
                        if(result.isSuccessful()){
                            targetUser = result.getObject();
                            onUserUpdated();
                        }
                    });

            // Get the latest relationship data
            new GetRelationship(getPageInstance(),
                    new User.Identification(TwitterApplication.getTwitterInstance()
                            .getCurrentLogonUser().getUid()),
                    targetUserId, DataFetcherAbstract.Policy.OAUTH_OR_CACHE)
                    .asyncExecute((DataFetcherAbstract.Result<Relationship> result) -> {
                        final Relationship relationship = result.getObject();
                        if(relationship != null){
                            followingTarget = relationship.isFollowingTarget();
                            followedByTarget = relationship.isFollowedByTarget();
                            onRelationshipUpdated();
                        }
                    });
        }
    }
}
