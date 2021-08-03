package com.ilareguy.spear;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ilareguy.spear.util.RestorableState;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A MainPage is a type of page that is capable of taking and handling page transactions.
 * At any given time, one PageAbstract is active inside a MainPage. For that reason, the main
 * page can be considered to be the "root" (or top-level) page in an activity.
 *
 * It is capable of taking transactions between pages and handling them properly. It "remembers"
 * the past transactions and can rewind them in a sequential manner. It also supports layout changes
 * by automagically bundling and recovering the past transactions.
 *
 * During a transaction, affected pages (the currently visible one, and the one that is coming into
 * display) are loaded and saved by this MainPage.
 *
 * Because saving and loading/restoring pages can require asynchronous calls, this class implements
 * the isTransactionPending() method to see whether a transaction is pending. Only one transaction can
 * be executed at a time. For this reason, loadPage() and back() may return false, which indicates
 * that the transaction cannot be executed at this time because there is already another transaction
 * pending.
 */
public final class MainPage extends PageAbstract{
    public MainPage(final Context context){ super(context); }

    private static final int ELEVATION_EXIT = 10;
    private static final int ELEVATION_ENTER = 11;

    private final Deque<PreviousPageData> transactionStack = new ArrayDeque<>();

    private boolean transactionPending = false;
    private PageAbstract currentPage = null;
    private RelativeLayout rootView;
    private PageAbstract loadPage_enteringPage;
    private PageAbstract back_enteringPage;
    private int transitionAnimatorsDoneCount;

    /**
     * Requests the given page to be loaded and set as current.
     * @param nextPage The page to load.
     * @return True if the transaction has begun; false if there is already a transaction pending.
     */
    public boolean loadPage(PageAbstract nextPage){
        if(transactionPending) return false;
        transactionPending = true;
        nextPage.setMainPageInstance(this);
        getPageLoader().loadPage(nextPage, this::loadPage_onPostLoadPage);
        return true;
    }

    /**
     * Removes the current page and moves back to the previous one from the stack.
     * @return True if there is a transaction running (regardless of whether or not this call to back()
     * started it or not) or if the page currently active consumed the back event;
     * false if there is no transaction running AND no page to move back to.
     */
    public boolean back(){
        if(currentPage != null && currentPage.onBack()
                || transactionPending) return true;
        if(transactionStack.size() == 0) return false;
        transactionPending = true;

        try {
            // Build the next page from the stack
            back_enteringPage = PageBundler.buildPageFromBundle(transactionStack.pop().pageState, getContext());
        }catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                | NoSuchMethodException | InvocationTargetException e){
            // Couldn't load the previous page!
            e.printStackTrace();
            throw new RuntimeException("Couldn't load the previous page from the stack! Perhaps it lacks the required constructor?" +
                    " There needs to be a public constructor that takes a Context as its only parameter.");
        }

        // Load it
        back_enteringPage.setMainPageInstance(this);
        getPageLoader().loadPage(back_enteringPage, this::back_onPostLoadPage);

        return true;
    }

    /**
     * @return True if there is a transaction pending; false otherwise.
     */
    public final boolean isTransactionPending(){ return transactionPending; }

    /**
     * @return The page currently active. Null is returned if loadPage() has not yet been called,
     * or if loadPage() was called and the transaction is still pending.
     */
    public final PageAbstract getCurrentPage(){ return currentPage; }




    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                            ActivityAbstract.Orientation orientation){
        return new MainVariant(this);
    }

    public void setActivity(ActivityAbstract a){ this.rootView = a.getRootView(); }

    private static void setRootViewLayoutParams(View view){
        final ViewGroup.LayoutParams layout_params = view.getLayoutParams();
        layout_params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layout_params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layout_params);
    }

    @Override
    public Bundle saveState(){
        final Bundle bundle = super.saveState();

        if(currentPage == null) {
            // If there is no current page, then there is no past transactions either
            return bundle;
        }

        // Save the current page's state
        bundle.putBundle("CURRENT_PAGE", PageBundler.bundlePage(currentPage));

        // Save the transaction history
        bundle.putInt("TRANSACTIONS_COUNT", transactionStack.size());
        int i = 0;
        for (PreviousPageData previousPage : transactionStack) {
            bundle.putBundle(String.valueOf(i), previousPage.saveState());
            i++;
        }

        return bundle;
    }

    @Override
    public void restoreState(Bundle bundle){
        final Bundle current_page_bundle = bundle.getBundle("CURRENT_PAGE");
        if(current_page_bundle == null) return;

        // Restore current page
        try {
            this.currentPage = PageBundler.buildPageFromBundle(current_page_bundle, getContext());
        }catch(ClassNotFoundException | IllegalAccessException | InstantiationException
                | NoSuchMethodException | InvocationTargetException e){
            e.printStackTrace();
            throw new RuntimeException("Couldn't restore the current page! Perhaps it lacks the required constructor?" +
            " There needs to be a public constructor that takes a Context as its only parameter.");
        }

        // Restore page history
        final int transactions_count = bundle.getInt("TRANSACTIONS_COUNT");
        for(int i = 0; i < transactions_count; i++){
            transactionStack.add(new PreviousPageData(bundle.getBundle(String.valueOf(i))));
        }

        // Load the current page
        currentPage.setMainPageInstance(this);
        getPageLoader().loadPage(currentPage, (PageAbstract.Variant newVariant) -> {
            // Page is loaded
            rootView.addView(newVariant.getRootView());
            setRootViewLayoutParams(newVariant.getRootView());
        });
    }

    private PageLoader getPageLoader(){ return App.getInstance().getActivityInstance().getPageLoader(); }

    ///////////////////////////////////////////////////////////
    /////////////////////   back() flow   /////////////////////
    ///////////////////////////////////////////////////////////

    private void back_onPostLoadPage(final @NonNull PageAbstract.Variant newVariant){
        // The page is now ready; add it to the view hierarchy and build its animator on the next UI loop
        rootView.addView(newVariant.getRootView());
        setRootViewLayoutParams(newVariant.getRootView());
        newVariant.getRootView().setElevation(ELEVATION_ENTER);
        newVariant.getRootView().setVisibility(View.INVISIBLE); // Make it invisible for now
        currentPage.getRootView().setElevation(ELEVATION_EXIT);

        // Build the animator on the next loop, so the views are laid out
        newVariant.getRootView().post(this::back_onEnteringVariantReadyBuildAnimators);
    }

    private void back_onEnteringVariantReadyBuildAnimators(){
        // At this point, the entering variant is ready to build its animator
        back_enteringPage.getRootView().setVisibility(View.VISIBLE);
        transitionAnimatorsDoneCount = 0;

        final @Nullable Animator enter_animator = back_enteringPage.getVariant().buildPopEnterAnimator();
        final @Nullable Animator exit_animator = currentPage.getVariant().buildExitAnimator();

        if(enter_animator != null){
            enter_animator.addListener(back_animatorListener);
            enter_animator.start();
        }else
            transitionAnimatorsDoneCount++;

        if(exit_animator != null){
            exit_animator.addListener(back_animatorListener);
            exit_animator.start();
        }else
            transitionAnimatorsDoneCount++;

        back_checkAnimators();
    }

    private final BaseAnimatorListener back_animatorListener = new BaseAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            transitionAnimatorsDoneCount++;
            back_checkAnimators();
        }
    };

    private void back_checkAnimators(){
        if(transitionAnimatorsDoneCount >= 2){
            // Both animations are done
            getPageLoader().unloadPage(currentPage, this::back_onPostUnloadCurrentPage);
            currentPage = back_enteringPage;
            back_enteringPage = null;
        }
    }

    private void back_onPostUnloadCurrentPage(PageAbstract unloadedPage){
        // unloadedPage was successfully unloaded; remove it from the view hierarchy
        rootView.removeView(unloadedPage.getRootView());
        // This transaction is over
        transactionPending = false;
    }


    ///////////////////////////////////////////////////////////
    ///////////////////// loadPage() flow /////////////////////
    ///////////////////////////////////////////////////////////

    private void loadPage_onPostLoadPage(final @NonNull PageAbstract.Variant newVariant){
        // The page is now ready; add it to the view hierarchy and build its animator on the next UI loop
        this.loadPage_enteringPage = newVariant.getPageInstance();
        rootView.addView(newVariant.getRootView());
        setRootViewLayoutParams(newVariant.getRootView());
        newVariant.getRootView().setElevation(ELEVATION_ENTER);
        newVariant.getRootView().setVisibility(View.INVISIBLE); // Make it invisible for now
        if(currentPage != null)
            currentPage.getRootView().setElevation(ELEVATION_EXIT);

        // Build animator on the next loop, so the views are laid out
        newVariant.getRootView().post(this::loadPage_onEnteringVariantReadyBuildAnimators);
    }

    private void loadPage_onEnteringVariantReadyBuildAnimators(){
        // At this point, the entering variant is ready to build its animator
        loadPage_enteringPage.getRootView().setVisibility(View.VISIBLE);
        transitionAnimatorsDoneCount = 0;

        final @Nullable Animator enter_animator = loadPage_enteringPage.getVariant().buildEnterAnimator();
        final @Nullable Animator exit_animator =
                (currentPage == null) ? null : currentPage.getVariant().buildPopExitAnimator();

        if(enter_animator != null){
            enter_animator.addListener(loadPage_animatorListener);
            enter_animator.start();
        }else
            transitionAnimatorsDoneCount++;

        if(exit_animator != null){
            exit_animator.addListener(loadPage_animatorListener);
            exit_animator.start();
        }else
            transitionAnimatorsDoneCount++;

        loadPage_checkAnimators(); // If there are no animators, just move on now
    }

    private final BaseAnimatorListener loadPage_animatorListener = new BaseAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            transitionAnimatorsDoneCount++;
            loadPage_checkAnimators();
        }
    };

    private void loadPage_checkAnimators(){
        if(transitionAnimatorsDoneCount >= 2){
            // Both animations are done
            loadPage_unloadCurrentPage();
            currentPage = loadPage_enteringPage;
            loadPage_enteringPage = null;
        }
    }

    private void loadPage_unloadCurrentPage(){
        if(currentPage == null){
            transactionPending = false;
            return;
        }
        getPageLoader().unloadPage(currentPage, this::loadPage_onPostUnloadCurrentPage);
    }

    private void loadPage_onPostUnloadCurrentPage(PageAbstract unloadedPage){
        // unloadedPage was successfully unloaded; remove it from the view hierarchy
        rootView.removeView(unloadedPage.getRootView());
        // Add its data into the transaction stack
        transactionStack.add(new PreviousPageData(unloadedPage));
        // This transaction is over
        transactionPending = false;
    }

    /**
     * Details about a transaction.
     */
    private static final class PreviousPageData implements RestorableState {
        /**
         * The Bundle containing everything to restore state of a page. This is the Bundle returned
         * by PageBundler.
         */
        Bundle pageState;

        /**
         * The class name of the page contained in the pageState.
         */
        String pageClassName;

        PreviousPageData(PageAbstract page){
            this.pageState = PageBundler.bundlePage(page);
            this.pageClassName = page.getClass().getName();
        }

        PreviousPageData(Bundle bundle){ restoreState(bundle); }

        @Override
        public Bundle saveState(){
            final Bundle bundle = new Bundle();
            bundle.putBundle("STATE", pageState);
            bundle.putString("PAGE_CLASS_NAME", pageClassName);
            return bundle;
        }

        @Override
        public void restoreState(Bundle bundle){
            this.pageState = bundle.getBundle("STATE");
            this.pageClassName = bundle.getString("PAGE_CLASS_NAME");
        }
    }

    private final class MainVariant extends PageAbstract.Variant<RelativeLayout>{
        MainVariant(final PageAbstract pageInstance){ super(pageInstance); }

        @Override
        protected View inflate(LayoutInflater inflater){
            return MainPage.this.rootView;
        }

        @Override
        protected void initializeLayout(){}
    }

    private static abstract class BaseAnimatorListener implements Animator.AnimatorListener{
        @Override
        public void onAnimationStart(Animator animation){}
        @Override
        public void onAnimationCancel(Animator animation){}
        @Override
        public void onAnimationRepeat(Animator animation){}
    }

}
