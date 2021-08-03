package com.ilareguy.spear.twitter.page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.twitter.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Logo extends PageAbstract {
    public Logo(Context context){ super(context); }

    private final class SingleVariant extends Variant<ConstraintLayout>{
        SingleVariant(PageAbstract page){ super(page); }

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            final ConstraintLayout root_view = new ConstraintLayout(getContext());
            inflater.inflate(R.layout.page_logo, root_view, true);
            root_view.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary, null));
            return root_view;
        }

        @Override
        protected void initializeLayout(){
            //
        }

    }

    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                                         ActivityAbstract.Orientation orientation){
        return new SingleVariant(this);
    }
}
