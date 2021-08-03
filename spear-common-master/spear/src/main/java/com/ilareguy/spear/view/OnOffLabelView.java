package com.ilareguy.spear.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ilareguy.spear.R;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public final class OnOffLabelView extends ConstraintLayout implements
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener{
    public interface OnStateChangeListener{
        void onStateChange(boolean on);
    }

    public OnOffLabelView(Context context){ super(context); init(null); }
    public OnOffLabelView(Context c, AttributeSet a){ super(c, a); init(a); }
    public OnOffLabelView(Context c, AttributeSet a, int d){ super(c, a, d); init(a); }

    private @Nullable OnStateChangeListener onStateChangeListener = null;
    private @NonNull Children children;

    private void init(@Nullable AttributeSet attributes){
        // Inflate
        LayoutInflater.from(getContext()).inflate(R.layout.on_off_label, this, true);

        // Find children
        this.children = new Children(this);

        // Hook listeners
        children._switch.setOnCheckedChangeListener(this);
        setOnClickListener(this);

        // Style
        if(attributes != null){
            applyStyle(attributes);
        }
    }

    private void applyStyle(final @NonNull AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OnOffLabelView,
                0, 0);

        try {
            children.label.setText(a.getString(R.styleable.OnOffLabelView_text));
            children.label.setTextColor(a.getColor(R.styleable.OnOffLabelView_textColor, 0));
            children._switch.setChecked(a.getBoolean(R.styleable.OnOffLabelView_selected, false));

            final @IdRes int icon_res_id = a.getResourceId(R.styleable.OnOffLabelView_icon, 0);
            if(icon_res_id != 0){
                children.icon.setImageResource(a.getResourceId(R.styleable.OnOffLabelView_icon, 0));
                children.icon.setVisibility(View.VISIBLE);
                children.icon.setColorFilter(a.getColor(R.styleable.OnOffLabelView_iconTint, 0));
            }else
                children.icon.setVisibility(View.GONE);

        } finally {
            a.recycle();
        }
    }

    public void setOnStateChangeListener(@Nullable OnStateChangeListener onStateChangeListener)
    {this.onStateChangeListener = onStateChangeListener;}

    public boolean isOn(){ return children._switch.isChecked(); }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b){
        if(onStateChangeListener != null) onStateChangeListener.onStateChange(b);
    }

    @Override
    public void onClick(View view){
        children._switch.toggle();
    }

    private static final class Children{
        public Children(final ViewGroup parent){
            label = parent.findViewById(R.id.label);
            _switch = parent.findViewById(R.id._switch);
            icon = parent.findViewById(R.id.icon);
        }

        final TextView label;
        final Switch _switch;
        final ImageView icon;
    }
}
