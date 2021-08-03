package com.ilareguy.spear.twitter.page;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.twitter.view.LoginToTwitterView;
import com.ilareguy.spear.util.Helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class Login extends PageAbstract {
    public Login(Context context){ super(context); }

    public interface OnLoginSuccessfulListener{
        void onLoginSuccessful(LogonUser newToken, User newUser);
    }

    private @Nullable OnLoginSuccessfulListener onLoginSuccessfulListener = null;

    public void setOnLoginSuccessfulListener(@Nullable OnLoginSuccessfulListener onLoginSuccessfulListener)
    { this.onLoginSuccessfulListener = onLoginSuccessfulListener; }

    private final class SingleVariant extends Variant<ConstraintLayout>{
        SingleVariant(PageAbstract page){ super(page); }

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            final ConstraintLayout root_view = new ConstraintLayout(getContext());
            inflater.inflate(R.layout.page_login, root_view, true);
            return root_view;
        }

        @Override
        protected void initializeLayout(){
            // At this point, the contents is not yet visible so all the views' properties aren't
            // necessarily known. Start animations on the next drawing loop
            MainActivity.getInstance().runOnUiThread(this::animateContents);

            getRootView().findViewById(R.id.login_button).setOnClickListener((View v) -> startLogin());
        }

        private void animateContents(){
            final AnimatorSet animator = new AnimatorSet();
            animator.playTogether(buildLogoAnimator(), buildContentsAnimator());
            animator.start();
        }

        private Animator buildLogoAnimator(){
            final View logo = getRootView().findViewById(R.id.logo);
            final float end_y = logo.getY() - Helper.dpToPx(200, getContext().getResources());
            final ObjectAnimator animator = ObjectAnimator.ofFloat(logo,
                    "translationY", logo.getY(), end_y);
            animator.setDuration(300);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            return animator;
        }

        private Animator buildContentsAnimator(){
            final View contents_frame = getRootView().findViewById(R.id.contents_frame);
            final float end_y = contents_frame.getY();
            final AnimatorSet set = new AnimatorSet();

            // Translate
            final ObjectAnimator translate_animator = ObjectAnimator.ofFloat(contents_frame,
                    "translationY", (end_y + 100), end_y);
            translate_animator.setStartDelay(30);
            translate_animator.setDuration(300);
            translate_animator.setInterpolator(new FastOutSlowInInterpolator());

            // Fade
            final ObjectAnimator alpha_animator = ObjectAnimator.ofFloat(contents_frame,
                    "alpha", 0f, 1f);
            alpha_animator.setStartDelay(30);
            alpha_animator.setDuration(350);
            alpha_animator.setInterpolator(new LinearInterpolator());

            set.playTogether(alpha_animator, translate_animator);
            return set;
        }

        private void startLogin(){
            final LoginDialog dialog = new LoginDialog(MainActivity.getInstance());

            dialog.loginToTwitterView.setOnResultListener(new LoginToTwitterView.OnResultListener() {
                @Override
                public void onError(SpearError error) {
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(LogonUser newToken, User newUser) {
                    dialog.dismiss();
                    if(onLoginSuccessfulListener != null)
                        onLoginSuccessfulListener.onLoginSuccessful(newToken, newUser);
                }
            });

            dialog.show();
        }

    }

    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                            ActivityAbstract.Orientation orientation){
        return new SingleVariant(this);
    }

    private static final class LoginDialog extends Dialog{
        LoginDialog(Activity a){
            super(a);
            this.loginToTwitterView = new LoginToTwitterView(a);
        }

        private final LoginToTwitterView loginToTwitterView;

        @Override
        public void onCreate(Bundle savedInstance){
            super.onCreate(savedInstance);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            setContentView(loginToTwitterView);
        }

        @Override
        public void show(){
            super.show();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(lp);
        }
    }
}
