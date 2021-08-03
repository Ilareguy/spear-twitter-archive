package com.ilareguy.spear.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.ilareguy.spear.R;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ClearableTextLayout extends ConstraintLayout implements View.OnClickListener{
    public ClearableTextLayout(Context c){ super(c); init(null); }
    public ClearableTextLayout(Context c, AttributeSet a){ super(c, a); init(a); }
    public ClearableTextLayout(Context c, AttributeSet a, int d){ super(c, a, d); init(a); }

    private TextInputEditText editTextView;

    private void init(final AttributeSet attrs){
        // Inflate
        LayoutInflater.from(getContext()).inflate(R.layout.clearable_text_layout, this, true);

        editTextView = findViewById(R.id.text);
        findViewById(R.id.clear_button).setOnClickListener(this);
    }

    public final TextInputEditText getEditTextView(){ return editTextView; }

    @Override
    public void onClick(View v){
        editTextView.setText("");
    }
}
