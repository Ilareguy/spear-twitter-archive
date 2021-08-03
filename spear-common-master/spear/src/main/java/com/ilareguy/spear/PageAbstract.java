package com.ilareguy.spear;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.util.Helper;
import com.ilareguy.spear.util.RestorableState;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import okhttp3.Response;

public abstract class PageAbstract implements RestorableState {

    /**
     * A Variant is responsible for inflating and handling its own UI logic. It can have as many
     * components as it needs. None of the inflating shall be done in the class' constructor; instead,
     * override the appropriate methods that will be invoked automagically when required.
     */
    public static abstract class Variant<RootViewType extends View> implements RestorableState{

        /**
         * Your custom variant can implement this interface if it needs to perform asynchronous
         * loading and/or saving operations on a worker thread. Such operation could include reading
         * and writing to the application's cache database. Both methods in this interface will be
         * invoked from a worker thread where such operation is permissible.
         *
         * Care must be taken in your implementation though as the page won't actually become visible
         * (or won't be destroyed) for as long as these methods don't return. For this reason, only
         * quick operations shall be executed in your implementation.
         */
        @WorkerThread
        public interface AsyncLifecycle{
            /**
             * Called before the variant becomes visible. If the variant is being created from a
             * previous state, then the state is passed as an argument.
             */
            void load(@Nullable Bundle savedState);

            /**
             * Called before the variant is destroyed and effectively removed from the view hierarchy.
             */
            void save();
        }

        private final PageAbstract pageInstance;

        private RootViewType rootView = null;

        protected Variant(final PageAbstract pageInstance){
            this.pageInstance = pageInstance;
        }

        public final RootViewType getRootView(){ return rootView; }
        public final PageAbstract getPageInstance(){ return pageInstance; }
        public final Context getContext(){ return pageInstance.getContext(); }
        public final MainPage getMainPage(){ return pageInstance.getMainPage(); }

        protected void setRootView(RootViewType rootView){ this.rootView = rootView; }
        protected final boolean hasAsyncOperations(){ return (this instanceof AsyncLifecycle); }

        /**
         * Your implementation must override this method. It will be invoked when it is time to
         * inflate your variant's layout.
         *
         * Your implementation is responsible for creating its root view, and if necessary, inflating
         * that root view's layout.
         *
         * @param inflater The inflater you can use to inflate your layout.
         * @return Returns the newly-inflated root view for this Variant.
         */
        protected abstract @NonNull View inflate(final LayoutInflater inflater);

        /**
         * Called after inflate(), when it is time to find and initialize your variant's layout
         * components and views, if there are any. After returning from this method, your variant
         * should be ready to be displayed.
         *
         * You should also hook any required listeners in there as this will be called when restoring
         * the page's instance.
         */
        protected abstract void initializeLayout();

        /**
         * Called when it's time to save this variant's state. This usually happens when a layout
         * or configuration change happened and the owning activity was re-created. You can use this
         * to save a variant state. Later on, restoreState() will be called and the Bundle you create
         * here will be passed as an argument.
         *
         * If your variant implements AsyncLifecycle, then this method will be called *after* the
         * async call to save().
         *
         * @return Return a Bundle containing your saved state. Don't forget to call super.saveState().
         */
        @Override
        public @NonNull Bundle saveState(){
            return new Bundle();
        }

        /**
         * Called when it is time to restore the state of a previous instance. The Bundle provided
         * here is the same as the one you returned in saveState().
         *
         * If your variant implements AsyncLifecycle, then this method will be called *after* the
         * async call to load(). This will also be called *before* initializeLayout() and *after*
         * inflate().
         *
         * Don't forget to call super.restoreState(savedState).
         *
         * @param savedState The Bundle generated in saveState().
         */
        @Override
        public void restoreState(final @NonNull Bundle savedState){
        }

        /**
         * There are four types of animations that can be defined by each Variant:
         *  1. Enter:       the variant is about to enter the screen for the first time;
         *  2. PopEnter:    the variant is about the enter the screen after it was previously removed;
         *  3. Exit:        the variant is about to exit the screen and be destroyed; and
         *  4. PopExit:     the variant is about to exit the screen to make place for another variant,
         *                  but is likely to be brought back eventually with a PopEnter animation.
         *
         * Each one of these methods will be called only after the variant has been fully loaded and
         * inflated. For this reason, it is safe to build animations that interact directly with
         * your variant's view hierarchy.
         */
        public @Nullable Animator buildEnterAnimator(){ return null; }
        public @Nullable Animator buildPopEnterAnimator(){ return null; }
        public @Nullable Animator buildExitAnimator(){ return null; }
        public @Nullable Animator buildPopExitAnimator(){ return null; }
    }

    private int uniqueTag;
    private String title = "";
    private MainPage mainPageInstance = null;

    private final Context context;
    private final ArrayList<TaskTyped> registeredTasks = new ArrayList<>();
    private final BlockingQueue<Runnable> asyncTaskQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor asyncTaskExecutor = new ThreadPoolExecutor(
            0, 1,
            1, TimeUnit.SECONDS,
            asyncTaskQueue
    );

    private @Nullable Variant<?> variant = null;
    private @Nullable Bundle savedVariantState = null;

    public PageAbstract(final Context context){
        this.context = context;
        uniqueTag = ("PAGE:" + String.valueOf(System.identityHashCode(this))).hashCode();
    }

    /**
     * Your implementation must return a new object of type Variant that matches the given type and
     * orientation.
     * @param type The type that needs to be supported by the returned Variant.
     * @param orientation The orientation that needs to be supported by the returned Variant.
     * @return A new instance of your own type that extends PageAbstract.Variant.
     */
    protected abstract @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                                     ActivityAbstract.Orientation orientation);

    public final @NonNull PageAbstract.Variant createVariant(ActivityAbstract.VariantType type,
                                                             ActivityAbstract.Orientation orientation){
        this.variant = onCreateVariant(type, orientation);
        return variant;
    }

    public MainPage getMainPage(){ return mainPageInstance; }

    public void setMainPageInstance(MainPage mainPageInstance){ this.mainPageInstance = mainPageInstance; }

    /**
     * Called when the user wants to go back, such as when they press the devices's back button.
     * @return True if you consumed the event; super.onBack() otherwise.
     */
    public boolean onBack(){
        return false;
    }

    /**
     * Convenient method to execute a task in the background on the page's background task executor.
     */
    public <ResultType> void doInBackground(final Helper.doInBackgroundFunc<ResultType> run,
                                            final Helper.doInBackgroundPostWork<ResultType> postRun){
        Helper.doInBackground(asyncTaskExecutor, run, postRun);
    }

    public @NonNull TaskResult<Response> sendOAuthRequest(final @NonNull RequestAbstract request){
        request.setTag(uniqueTag);
        return OAuth.getInstance().getGlobalCommunicator().sendRequest(request);
    }

    public static @NonNull TaskResult<Response> sendOAuthRequest_s(final @NonNull RequestAbstract request){
        return OAuth.getInstance().getGlobalCommunicator().sendRequest(request);
    }

    public final ThreadPoolExecutor getAsyncTaskExecutor(){ return asyncTaskExecutor; }
    public final @Nullable Bundle getSavedVariantState(){ return savedVariantState; }
    public synchronized final void registerTask(final @NonNull TaskTyped task){ registeredTasks.add(task); }
    public synchronized final void unregisterTask(final @NonNull TaskTyped task){ registeredTasks.remove(task); }
    public final String getTitle(){ return title; }
    public void setTitle(final String title){ this.title = title; }
    public View getRootView(){ return variant.getRootView(); }
    public final Context getContext(){ return context; }
    public final boolean isLoaded(){ return (variant != null); }

    /**
     * Returns the variant that you previously built in onCreateVariant().
     */
    protected Variant<?> getVariant(){ return variant; }

    @Override
    public @NonNull Bundle saveState(){
        final Bundle state = new Bundle();
        state.putInt("uniqueTag", uniqueTag);
        state.putBundle("variantState", (variant == null) ? null : variant.saveState()); // Save the current variant's state
        return state;
    }

    @Override
    public void restoreState(final @NonNull Bundle savedState){
        this.uniqueTag = savedState.getInt("uniqueTag");
        this.savedVariantState = savedState.getBundle("variantState");
    }

}
