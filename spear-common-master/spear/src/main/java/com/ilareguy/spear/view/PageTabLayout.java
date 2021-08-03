package com.ilareguy.spear.view;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * Much like a regular TabLayout, but it displays instances of PageAbstract instead of Android
 * fragments.
 */
public class PageTabLayout extends TabLayout{
    public PageTabLayout(Context c){ super(c); init(c, null); }
    public PageTabLayout(Context c, AttributeSet a){ super(c, a); init(c, a); }
    public PageTabLayout(Context c, AttributeSet a, int d){ super(c, a, d); init(c, a); }

    private boolean addingPages = false;
    private int readyCount;

    private @Nullable PagerAdapter adapter = null;
    private @Nullable ViewPager pager = null;

    private void init(final Context context, final @Nullable AttributeSet attrs){
    }

    public void setViewPager(final ViewPager pager){
        this.pager = pager;
        this.adapter = new PagerAdapter();
        this.pager.setAdapter(adapter);
        setupWithViewPager(this.pager);
    }

    /**
     * Sets the list of pages that need to be displayed by this PageTabLayout. This will replace
     * the old pages that were set. This method will take care of loading all the pages using
     * PageLoader. Since some page may require async loading, they will only become visible when
     * they're ready.
     * @return Returns true if the pages are currently being added; false otherwise, such as if there
     * are already pages being added from a previous call or if no pager was set.
     */
    /*public boolean setPages(final List<PageAbstract> pages, int selectedIndex){
        // TODO: 2018-08-23 This crashes
        return setPages((PageAbstract[]) pages.toArray(), selectedIndex);
    }*/

    public boolean setPages(final PageAbstract[] pages, final int selectedIndex){
        if(addingPages              // Can't add pages; already adding previous pages
                || adapter == null) // Adapter/ViewPager not set
            return false;

        // Remove the old pages & tabs
        removeAllTabs();
        adapter.pages.clear();
        readyCount = 0;

        // Add the new ones one-by-one
        final int total_pages = pages.length;
        final PageAbstract.Variant<?>[] loaded_variants = new PageAbstract.Variant<?>[pages.length];
        int current_index = 0;
        for(PageAbstract page : pages){
            final int this_variant_index = current_index;
            current_index++;

            App.getInstance().getActivityInstance().getPageLoader().loadPage(page,
                    (final PageAbstract.Variant newVariant) -> {
                        readyCount++;
                        loaded_variants[this_variant_index] = newVariant;
                        if(readyCount == total_pages){
                            // This was the last page to be added
                            onTabsReady(loaded_variants, selectedIndex);
                        }
                    });
        }

        return true;
    }

    private void onTabsReady(final PageAbstract.Variant<?>[] loadedVariants, final int selectedIndex){
        for(PageAbstract.Variant<?> v : loadedVariants) adapter.pages.add(v);
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(selectedIndex, false);
        this.addingPages = false;
    }

    @Override
    public Parcelable onSaveInstanceState(){
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state){
        super.onRestoreInstanceState(state);
    }

    private static final class PagerAdapter extends androidx.viewpager.widget.PagerAdapter{
        private final List<PageAbstract.Variant<?>> pages = new ArrayList<>();

        PagerAdapter(){
            super();
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            final View page_root_view = pages.get(position).getRootView();
            collection.addView(page_root_view);
            return page_root_view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object pageRootView) {
            collection.removeView((View) pageRootView);
        }

        @Override
        public int getCount(){ return pages.size(); }

        @Override
        public boolean isViewFromObject(View view, Object pageRootView){
            return view == pageRootView;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return pages.get(position).getPageInstance().getTitle();
        }
    }
}
