package com.ilareguy.spear.twitter.page;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.PageBundler;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.view.PageStub;

import java.lang.reflect.InvocationTargetException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class Splash extends PageAbstract {
    public Splash(Context context){ super(context); }

    private PageStub stub;
    private boolean twitterInitialized = false;
    private boolean loggingIn = false;
    private boolean usingApp = false;

    public final @Nullable Main getAppPage(){ return usingApp ? (Main) stub.getPage() : null; }

    private final class SingleVariant extends PageAbstract.Variant<FrameLayout> {
        SingleVariant(PageAbstract page){ super(page); }

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            // Create root view
            final FrameLayout root_view = new FrameLayout(getContext());

            // Inflate
            inflater.inflate(R.layout.page_splash, root_view, true);
            stub = root_view.findViewById(R.id.stub);

            root_view.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary, null));
            return root_view;
        }

        @Override
        public void restoreState(Bundle state){
            twitterInitialized = state.getBoolean("INIT");
            loggingIn = state.getBoolean("LOGGING_IN");
            usingApp = state.getBoolean("USING_APP");

            if(loggingIn || usingApp) {
                try {
                    stub.setPage(
                            PageBundler.buildPageFromBundle(state.getBundle("STUB_PAGE"), getContext()));

                    if(loggingIn){
                        ((Login) stub.getPage()).setOnLoginSuccessfulListener(Splash.this::onLoginSuccessful);
                    }

                }catch(ClassNotFoundException | IllegalAccessException | InstantiationException
                        | NoSuchMethodException | InvocationTargetException e){
                    e.printStackTrace();
                    // Couldn't retrieve the current page!
                }
            }
        }

        @Override
        protected void initializeLayout(){
            if(!twitterInitialized){
                // Start initialization process
                doInBackground(Splash.this::initializeTwitter, (Void r) -> {});

                // Show logo page
                showLogoPage();

                doInBackground(
                        // Wait a moment on the logo page
                        Splash.this::waitOnLogo,

                        // Move on to the next page
                        (Void r) -> {
                            final LogonUser current_logon_user = TwitterApplication.getTwitterInstance().getCurrentLogonUser();
                            if(current_logon_user == null)
                                showLoginPage();
                            else
                                showAppPage();
                        });

            }
        }

        @Override
        public Bundle saveState(){
            final Bundle bundle = super.saveState();
            bundle.putBoolean("INIT", twitterInitialized);
            bundle.putBoolean("LOGGING_IN", loggingIn);
            bundle.putBoolean("USING_APP", usingApp);
            bundle.putBundle("STUB_PAGE", PageBundler.bundlePage(stub.getPage()));
            return bundle;
        }

    }

    @WorkerThread
    private Void waitOnLogo(){
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Displays the logo screen.
     */
    private void showLogoPage(){
        stub.setPage(new Logo(getContext()));
    }

    /**
     * Displays the actual home page for the app.
     */
    private void showAppPage(){
        usingApp = true;
        loggingIn = false;

        stub.setPage(new Main(getContext()));
    }

    /**
     * Displays the login page for when there is no Twitter user registered yet.
     */
    private void showLoginPage(){
        loggingIn = true;
        usingApp = false;

        final Login login_screen = new Login(getContext());
        login_screen.setOnLoginSuccessfulListener(this::onLoginSuccessful);
        stub.setPage(login_screen);
    }

    private void onLoginSuccessful(LogonUser newToken, User newUser){
        showAppPage();
    }

    @WorkerThread
    private Void initializeTwitter(){
        TwitterApplication.getTwitterInstance().setCurrentUser(
                TwitterApplication.getTwitterInstance().getCacheDatabase().logonUserDao().getCurrent());

        twitterInitialized = true;
        return null;
    }

    @Override
    protected final Variant onCreateVariant(ActivityAbstract.VariantType variantType,
                                                         ActivityAbstract.Orientation orientation){
        return new SingleVariant(this);
    }
}
