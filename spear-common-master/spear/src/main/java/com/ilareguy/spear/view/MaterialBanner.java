package com.ilareguy.spear.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilareguy.spear.R;
import com.ilareguy.spear.util.Helper;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class MaterialBanner extends ConstraintLayout{

    public interface OnEventListener{
        void onPrimaryActionClick();
        void onSecondaryActionClick();
    }

    private ImageView iconView;
    private TextView textView;
    private Button primaryButtonView;
    private Button secondaryButtonView;
    private @Nullable OnEventListener onEventListener = null;

    public MaterialBanner(Context context){
        super(context);
        init(context, null);
    }

    public MaterialBanner(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public void setText(CharSequence text){
        textView.setText(text);
    }

    public void setIconResourceId(@DrawableRes int res){
        if(res <= 0) return;
        iconView.setImageDrawable(getResources().getDrawable(res, null));
    }

    public void setHighlightColor(@ColorInt int c){
        iconView.setColorFilter(c);
        primaryButtonView.setTextColor(c);
        secondaryButtonView.setTextColor(c);
    }

    public void setPrimaryActionText(@Nullable CharSequence t){
        primaryButtonView.setText((t == null) ? "" : t);
        primaryButtonView.setVisibility((t == null) ? View.GONE : View.VISIBLE);
    }

    public void setSecondaryActionText(@Nullable CharSequence t){
        secondaryButtonView.setText((t == null) ? "" : t);
        secondaryButtonView.setVisibility((t == null) ? View.GONE : View.VISIBLE);
    }

    public void hide(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0, -Helper.dpToPx(120, getResources()));
        animator.setDuration(300);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animator){}
            @Override
            public void onAnimationEnd(Animator animator){
                MaterialBanner.this.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animator){}
            @Override
            public void onAnimationRepeat(Animator animator){}
        });
        animator.start();
    }

    public void show(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, -Helper.dpToPx(120, getResources()), 0);
        animator.setDuration(300);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animator){
                MaterialBanner.this.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animator){}
            @Override
            public void onAnimationCancel(Animator animator){}
            @Override
            public void onAnimationRepeat(Animator animator){}
        });
        animator.start();
    }

    private void init(Context context, @Nullable AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.material_banner, this);

        iconView = findViewById(R.id.icon);
        textView = findViewById(R.id.text);
        primaryButtonView = findViewById(R.id.button_primary);
        secondaryButtonView = findViewById(R.id.button_secondary);

        if(attrs != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialBanner);

            try{
                setHighlightColor(a.getColor(R.styleable.MaterialBanner_highlightColor, 0));
                setText(a.getString(R.styleable.MaterialBanner_text));
                setIconResourceId(a.getResourceId(R.styleable.MaterialBanner_icon, 0));
                setPrimaryActionText(a.getText(R.styleable.MaterialBanner_primaryActionText));
                setSecondaryActionText(a.getText(R.styleable.MaterialBanner_secondaryActionText));
            }finally{
                a.recycle();
            }
        }

        setBackgroundColor(getResources().getColor(R.color.backgroundColorSecondary, null));
        primaryButtonView.setOnClickListener((View v) -> {
            if(onEventListener != null)
                onEventListener.onPrimaryActionClick();
        });

        secondaryButtonView.setOnClickListener((View v) -> {
            if(onEventListener != null)
                onEventListener.onSecondaryActionClick();
        });
    }

    public void setOnEventListener(@Nullable OnEventListener l){ onEventListener = l; }

}
