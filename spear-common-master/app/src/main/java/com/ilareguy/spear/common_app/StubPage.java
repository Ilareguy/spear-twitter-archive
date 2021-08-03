package com.ilareguy.spear.common_app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.MainPage;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.view.BackdropLayout;
import com.ilareguy.spear.view.PageStub;

import java.util.Random;

import androidx.annotation.NonNull;

public class StubPage extends PageAbstract {
    public StubPage(Context context){ super(context); }

    private final class StubVariant extends Variant<FrameLayout>{
        StubVariant(PageAbstract page){ super(page); }

        private final SingleTestPage[] pages = new SingleTestPage[3];

        private int currentPageIndex = pages.length;
        private PageStub stub;

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            final FrameLayout root_view = new FrameLayout(getContext());
            inflater.inflate(R.layout.stub_page, root_view, true);
            stub = root_view.findViewById(R.id.stub);

            for(int i = 0; i < pages.length; i++){
                pages[i] = new SingleTestPage(getContext());
            }

            return root_view;
        }

        @Override
        public void restoreState(final Bundle state){
            super.restoreState(state);
            for(int i = 0; i < pages.length; i++){
                pages[i].restoreState(state.getBundle(("PAGE_" + i)));
            }
            currentPageIndex = state.getInt("CURRENT_INDEX");
        }

        @Override
        protected void initializeLayout(){
            showNextPage();
        }

        private void showNextPage(){
            currentPageIndex++;
            if(currentPageIndex >= pages.length)
                currentPageIndex = 0;
            else if(currentPageIndex < 0)
                currentPageIndex = 0;

            stub.setPage(pages[currentPageIndex], (PageStub stub, PageAbstract newPage) -> {
                final BackdropLayout backdrop_layout = (BackdropLayout) newPage.getRootView();
                final ViewGroup back_layer = backdrop_layout.findViewById(R.id.back_layer);
                final Random random = new Random();

                ((TextView) newPage.getRootView().findViewById(R.id.label)).setText(getStringForPageIndex());

                back_layer.setOnClickListener((View v) -> showNextPage());

                backdrop_layout.findViewById(R.id.front_layer).setOnClickListener((View v) ->
                {
                    back_layer.setLayoutParams(new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            random.nextInt(600)
                    ));
                    backdrop_layout.notifyBackLayerChanged();
                });
            });
        }

        private String getStringForPageIndex(){
            final StringBuilder s = new StringBuilder();
            s.append("Page # ");
            s.append(currentPageIndex + 1);
            return s.toString();
        }

        @Override
        public Bundle saveState(){
            final Bundle bundle = super.saveState();

            for(int i = 0; i < pages.length; i++){
                bundle.putBundle(("PAGE_" + i), pages[i].saveState());
            }

            bundle.putInt("CURRENT_INDEX", currentPageIndex - 1);

            return bundle;
        }

    }

    private static class SingleTestPage extends PageAbstract {
        public SingleTestPage(Context context){ super(context); }

        private final class SingleTestVariant extends Variant<BackdropLayout>{
            SingleTestVariant(PageAbstract page){ super(page); }

            @Override
            protected @NonNull View inflate(final LayoutInflater inflater){
                final BackdropLayout root_view = new BackdropLayout(getContext());
                inflater.inflate(R.layout.stub_single_test_page, root_view, true);
                root_view.onFinishInflate();
                return root_view;
            }

            @Override
            public void restoreState(final Bundle state){
                super.restoreState(state);
                getRootView().onRestoreInstanceState(state.getParcelable("LAYOUT"));
            }

            @Override
            protected void initializeLayout(){
            }

            @Override
            public Bundle saveState(){
                final Bundle bundle = super.saveState();
                bundle.putParcelable("LAYOUT", getRootView().onSaveInstanceState());
                return bundle;
            }

        }

        @Override
        protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                                             ActivityAbstract.Orientation orientation){
            return new SingleTestVariant(this);
        }
    }

    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                            ActivityAbstract.Orientation orientation){
        return new StubVariant(this);
    }
}
