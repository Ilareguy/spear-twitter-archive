package com.ilareguy.spear;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * An ActivityAbstract is meant to be the one and only activity running into a Spear app.
 * It takes care of calling the appropriate callbacks , that your implementation can or should
 * override, at the appropriate time. This takes away some of the complexities related to Android's
 * activities.
 */
public abstract class ActivityAbstract extends Activity{

    public enum VariantType{
        MOBILE,
        TABLET
    }

    public enum Orientation{
        PORTRAIT,
        LANDSCAPE
    }

    private boolean restoring;
    private PageLoader pageLoader;
    private VariantType variantType;
    private Orientation orientation;
    private MainPage rootPage;
    private RelativeLayout rootView;

    public ActivityAbstract(){
        super();
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Initialize
        this.restoring = (savedInstanceState != null);
        App.getInstance().setActivityInstance(this);
        this.orientation = (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE
                ? Orientation.LANDSCAPE
                : Orientation.PORTRAIT);
        this.variantType = (getResources().getBoolean(R.bool.isTablet)
                ? VariantType.TABLET
                : VariantType.MOBILE);

        // Inflate & create the pageLoader
        rootView = new RelativeLayout(this);
        setContentView(rootView);
        this.pageLoader = new PageLoader(this);

        // Create the root page
        rootPage = new MainPage(this);
        rootPage.setActivity(this);

        if(restoring){
            // The current page will be loaded in restoreInstance()
            restoreInstance(savedInstanceState);
        }else{
            // Load the main page
            rootPage.loadPage(buildMainPage());
        }
    }

    @Override
    public void onBackPressed(){
        if(!rootPage.back())
            super.onBackPressed();
    }

    @Override
    protected final void onResume(){ super.onResume(); }

    @Override
    protected final void onStart(){ super.onStart(); }

    @Override
    public final void onRestoreInstanceState(Bundle savedInstanceState)
    { /* Don't use this method; restore state in restoreInstance instead. */ }

    @Override
    protected final void onRestart(){ super.onRestart(); }
    @Override
    protected final void onPause(){ super.onPause(); }
    @Override
    protected final void onStop(){ super.onStop(); }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBundle("ROOT_PAGE_STATE", rootPage.saveState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onDestroy(){ super.onDestroy(); }

    private void restoreInstance(final @NonNull Bundle savedInstance){
        rootPage.restoreState(savedInstance.getBundle("ROOT_PAGE_STATE"));
    }

    /**
     * Your activity must return a new instance of your main app's main page. The page must not
     * be inflated or added to any view hierarchy yet.
     */
    protected abstract PageAbstract buildMainPage();

    /**
     * Returns true when the activity is being restored from a previous instance, which usually
     * happens when there was a layout change; or false when the activity is first created and
     * displayed, in which case some initial loading is done in the background and the application's
     * logo/icon is displayed and possibly animated.
     *
     * Note: this will return a valid value only after the activity's onCreate has been called. Do
     * not rely on this inside your class' constructor.
     */
    protected final boolean isRestoring(){ return restoring; }

    public final VariantType getVariantType(){ return variantType; }
    public final Orientation getOrientation(){ return orientation; }
    public final PageLoader getPageLoader(){ return pageLoader; }
    public final RelativeLayout getRootView(){ return rootView; }
    public final MainPage getRootPage(){ return rootPage; }
}
