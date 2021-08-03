package com.ilareguy.spear.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilareguy.spear.R;
import com.ilareguy.spear.util.Helper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MaterialHeader extends ConstraintLayout{
    public MaterialHeader(Context c){ super(c); init(null); }
    public MaterialHeader(Context c, AttributeSet a){ super(c, a); init(a); }
    public MaterialHeader(Context c, AttributeSet a, int d){ super(c, a, d); init(a); }

    private Children children = null;

    private void init(@Nullable AttributeSet attrs){
        // Inflate
        LayoutInflater.from(getContext()).inflate(R.layout.material_header, this, true);
        final int padding = (int) Helper.dpToPx(12, getResources());
        setPadding(0, padding, 0, 0);

        // Find children
        this.children = new Children(this);

        // Style
        if(attrs != null){
            applyStyle(attrs);
        }
    }

    public void setText(final String text){
        children.label.setText(text);
    }

    public void setText(final CharSequence text){
        children.label.setText(text);
    }

    public void setIcon(final @IdRes int resource_id){
        if(resource_id != 0){
            children.icon.setImageResource(resource_id);
            children.icon.setVisibility(View.VISIBLE);
        }else
            children.icon.setVisibility(View.GONE);
    }

    private void applyStyle(final @NonNull AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MaterialHeader,
                0, 0);

        try {
            children.label.setTextColor(a.getColor(R.styleable.MaterialHeader_textColor, 0));
            children.icon.setColorFilter(a.getColor(R.styleable.MaterialHeader_iconTint, 0));
            children.separator.setVisibility(a.getBoolean(R.styleable.MaterialHeader_showSeparator, true)
                ? View.VISIBLE : View.GONE);
            setIcon(a.getResourceId(R.styleable.MaterialHeader_icon, 0));
            setText(a.getString(R.styleable.MaterialHeader_text));

        } finally {
            a.recycle();
        }

    }

    private static final class Children{
        Children(MaterialHeader parent){
            icon = parent.findViewById(R.id.icon);
            label = parent.findViewById(R.id.label);
            separator = parent.findViewById(R.id.separator);
        }

        final ImageView icon;
        final TextView label;
        final View separator;
    }
}
