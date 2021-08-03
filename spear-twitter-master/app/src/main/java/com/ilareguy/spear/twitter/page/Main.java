package com.ilareguy.spear.twitter.page;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.view.PageTabLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Main extends com.ilareguy.spear.PageAbstract{

    private static final class MobileVariant extends PageAbstract.Variant<ConstraintLayout>{
        private MobileVariant(final PageAbstract pageInstance){
            super(pageInstance);
            pages[0] = new HomeFeed(getContext());
            pages[1] = new UserProfile(getContext());
            pages[2] = new TestPage(getContext());

            pages[1].setTitle("Profile");
            ((UserProfile) pages[1]).setTargetUserId(
                    new User.Identification(TwitterApplication.getTwitterInstance().getCurrentLogonUser().getUid()));
            pages[2].setTitle("Inbox");
        }

        private final PageAbstract[] pages = new PageAbstract[3];

        private int currentlySelectedTabIndex = 0;

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            // Create root view
            final ConstraintLayout root_view = new ConstraintLayout(inflater.getContext());

            // Inflate its contents
            inflater.inflate(R.layout.activity_main, root_view, true);

            return root_view;
        }

        @Override
        protected void initializeLayout(){
            final PageTabLayout tab_layout = getRootView().findViewById(R.id.tab_layout);
            tab_layout.setViewPager(getRootView().findViewById(R.id.pager));
            tab_layout.setPages(pages, currentlySelectedTabIndex);
        }

        @Override
        public @NonNull Bundle saveState(){
            final Bundle state = super.saveState();
            state.putInt("SELECTED_TAB_INDEX", ((PageTabLayout) getRootView().findViewById(R.id.tab_layout)).getSelectedTabPosition());
            for(int i = 0; i < pages.length; i++){
                state.putBundle("PAGE_" + String.valueOf(i), pages[i].saveState());
            }
            return state;
        }

        @Override
        public void restoreState(final @NonNull Bundle savedState){
            super.restoreState(savedState);
            this.currentlySelectedTabIndex = savedState.getInt("SELECTED_TAB_INDEX");
            for(int i = 0; i < pages.length; i++){
                pages[i].restoreState(savedState.getBundle("PAGE_" + String.valueOf(i)));
            }
        }
    }

    public Main(Context context){ super(context); }

    @Override
    protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                            ActivityAbstract.Orientation orientation){
        return new MobileVariant(this);
    }

    private static final class TestPage extends PageAbstract{
        public TestPage(Context context){ super(context); }
        @Override
        protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                                ActivityAbstract.Orientation orientation){
            return new TestPageVariant(this);
        }
    }

    private static final class TestPageVariant extends PageAbstract.Variant<FrameLayout>{
        TestPageVariant(PageAbstract page){ super(page); }

        @Override
        protected @NonNull View inflate(LayoutInflater inflater){
            final FrameLayout root_view = new FrameLayout(inflater.getContext());
            //
            return root_view;
        }

        @Override
        protected void initializeLayout(){
            //
        }
    }

}