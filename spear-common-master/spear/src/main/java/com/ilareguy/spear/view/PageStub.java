package com.ilareguy.spear.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.PageLoader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A PageStub can be added in a view hierarchy for pages that aren't visible right away. Their layout
 * parameters will be transferred to the page's root view when a page is set. Same applies for the
 * stub's id.
 *
 * When calling setPage(), this stub will effectively be removed from the view hierarchy and be
 * replaced by the page's root view. For this reason, after calling setPage(), you cannot rely on
 * your view hierarchy's findViewById() returning a PageStub, but rather the page itself, as the
 * contents is effectively replaced. However, a PageStub is aware of this, and subsequent calls to
 * setPage() will properly replace the previous page for the new given one. To achieve this, you
 * must keep a reference to the PageStub in your code, and call setPage() on it whenever you need
 * to, even if the stub itself isn't part of the view hierarchy anymore.
 */
public class PageStub extends View{
    public PageStub(Context c){ super(c); }
    public PageStub(Context c, AttributeSet a){ super(c, a); }
    public PageStub(Context c, AttributeSet a, int d){ super(c, a, d); }

    public interface OnPageAddedListener{
        /**
         * Called when the given stub has just added the given page in the view hierarchy.
         * @param stub The stub that was replaced by the given page.
         * @param newPage The page that was fully initialized and added.
         */
        void onPageAdded(final @NonNull PageStub stub, final @NonNull PageAbstract newPage);
    }

    private @Nullable PageAbstract page = null;

    private boolean pageLoading = false;

    /**
     * Sets the given page as this frame's contents. If the page wasn't loaded already, it will
     * be loaded in here automagically.
     * @param page The page to display.
     * @return Returns true if the page was set (and is currently potentially loading in the
     * background); or false if it couldn't be added, such as if there is already a page loading
     * in the background awaiting to be set.
     */
    public boolean setPage(final @NonNull PageAbstract page,
                           final @Nullable OnPageAddedListener onPageAddedListener){
        if(pageLoading) return false;

        if(page.isLoaded()){
            // The page is already good to be added; so add it now
            doSetPage(page);
            return true;
        }

        // The page needs to be loaded
        getPageLoader().loadPage(page, (PageAbstract.Variant newVariant) -> {
            doSetPage(page);
            if(onPageAddedListener != null)
                onPageAddedListener.onPageAdded(this, page);
        });
        return true;
    }
    public boolean setPage(final @NonNull PageAbstract page){ return setPage(page, null); }

    private void doSetPage(final @NonNull PageAbstract page){
        //final PageAbstract previous_page = this.page;
        final ViewGroup parent_container = getParentContainer();
        final ViewGroup.LayoutParams layout_params = getLayoutParams();

        // Replace the contents
        parent_container.removeView(getView());
        page.getRootView().setId(getId());
        page.getRootView().setLayoutParams(layout_params);
        parent_container.addView(page.getRootView());

        this.page = page;
        this.pageLoading = false;
    }

    private ViewGroup getParentContainer(){
        return (page == null)
                ? (ViewGroup) getParent()
                : (ViewGroup) page.getRootView().getParent();
    }

    private View getView(){
        return (page == null)
                ? this
                : page.getRootView();
    }

    private PageLoader getPageLoader(){ return App.getInstance().getActivityInstance().getPageLoader(); }

    public final @Nullable PageAbstract getPage(){ return page; }
}
