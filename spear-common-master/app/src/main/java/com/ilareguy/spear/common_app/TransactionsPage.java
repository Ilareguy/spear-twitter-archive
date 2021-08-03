package com.ilareguy.spear.common_app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class TransactionsPage extends PageAbstract{
    public TransactionsPage(Context context){ super(context); }

    public @ColorInt int backgroundColor;

    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                            ActivityAbstract.Orientation orientation){
        return new SingleVariant(this);
    }

    private final class SingleVariant extends PageAbstract.Variant<FrameLayout>
            implements Variant.AsyncLifecycle {
        SingleVariant(PageAbstract pageInstance) {
            super(pageInstance);
        }

        @Override
        protected View inflate(LayoutInflater inflater) {
            return new FrameLayout(getContext());
        }

        @Override
        protected void initializeLayout() {
            getRootView().setBackgroundColor(backgroundColor);
            getRootView().setOnClickListener((View v) -> {
                // Load another page
                getMainPage().loadPage(new TabsPage(getContext()));
            });
        }

        @Override
        @WorkerThread
        public void load(@Nullable Bundle savedState){
            //
        }

        @Override
        @WorkerThread
        public void save(){
            //
        }

        @Override
        public Bundle saveState(){
            final Bundle bundle = super.saveState();
            bundle.putString("TEST", "Hello, World!");
            return bundle;
        }
    }
}
