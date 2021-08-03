package com.ilareguy.spear;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.ilareguy.spear.util.Helper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Takes care of loading pages. See {@link PageAbstract} class.
 */
public class PageLoader{

    private final ActivityAbstract activityInstance;
    private final BlockingQueue<Runnable> asyncLifecycleQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor asyncLifecycleExecutor = new ThreadPoolExecutor(
            0, 2,
            1, TimeUnit.SECONDS,
            asyncLifecycleQueue
    );

    protected PageLoader(ActivityAbstract activityInstance){
        this.activityInstance = activityInstance;
    }

    /**
     * Loads the given page and calls the given callback when it is ready to be added to a view
     * hierarchy.
     * @param newPage The page to load and initialize.
     * @param onPostPrepareVariant Callback for when the newly loaded page is ready.
     */
    public void loadPage(PageAbstract newPage,
                         @NonNull OnPostPrepareVariant onPostPrepareVariant){
        new PrepareVariant(newPage, onPostPrepareVariant);
    }

    /**
     * Unloads the given page and calls the given callback when it has successfully been unloaded.
     * @param page The page to unload.
     * @param onPostUnloadPage Callback for when the page has successfully been unloaded.
     */
    public void unloadPage(PageAbstract page,
                           @Nullable OnPostUnloadPage onPostUnloadPage){
        new UnloadPage(page, onPostUnloadPage);
    }

    public final ActivityAbstract getActivityInstance(){ return activityInstance; }

    /**
     * Prepares the given page for entering the view hierarchy. This includes loading its data,
     * potentially asynchronously, inflating its layout and preparing its views.
     */
    private final class PrepareVariant{

        private PageAbstract.Variant variantToPrepare;

        private final OnPostPrepareVariant onVariantReady;

        private PrepareVariant(final PageAbstract pageToPrepare,
                               final OnPostPrepareVariant onVariantReady){
            this.onVariantReady = onVariantReady;
            preparePage(pageToPrepare);
        }

        private void preparePage(final PageAbstract pageToPrepare){
            // Create the page variant
            variantToPrepare = pageToPrepare.createVariant(activityInstance.getVariantType(),
                    activityInstance.getOrientation());

            if(variantToPrepare instanceof PageAbstract.Variant.AsyncLifecycle){
                // Load the variant's data asynchronously
                asyncLoadPageVariant();
                return;
            }

            // Load the variant's data synchronously
            // Should there be an syncLoad() method in PageAbstract.Variant that would be called when
            // it is not of type AsyncLifecycle?
            onPostPageVariantLoad(variantToPrepare);
        }

        private void asyncLoadPageVariant(){
            Helper.doInBackground(asyncLifecycleExecutor,
                    () -> {
                        ((PageAbstract.Variant.AsyncLifecycle) variantToPrepare).load(variantToPrepare.getPageInstance().getSavedVariantState());
                        return variantToPrepare;
                    }, this::onPostPageVariantLoad);
        }

        private void onPostPageVariantLoad(final PageAbstract.Variant newVariant){
            // The given variant has finished loading its data
            inflateVariant(newVariant);

            // Check if the variant needs to be restored from a previous state
            final Bundle saved_variant_state = newVariant.getPageInstance().getSavedVariantState();
            if(saved_variant_state != null)
                newVariant.restoreState(saved_variant_state);

            // Initialize its views
            newVariant.initializeLayout();

            // The variant is now ready to be displayed!
            onVariantReady.onPostPrepareVariant(newVariant);
        }

        private void inflateVariant(final PageAbstract.Variant newVariant){
            newVariant.setRootView(newVariant.inflate(LayoutInflater.from(activityInstance)));

            // Check and make sure the view has properly inflated its root view
            /*if(newVariant.getRootView() == null){
            }*/
        }
    }

    private final class UnloadPage{
        private final @Nullable OnPostUnloadPage onPostUnloadPage;

        UnloadPage(PageAbstract pageToUnload,
                   @Nullable OnPostUnloadPage onPostUnloadPage){
            this.onPostUnloadPage = onPostUnloadPage;
            unloadPage(pageToUnload);
        }

        private void unloadPage(PageAbstract pageToUnload){
            if(pageToUnload.getVariant() instanceof PageAbstract.Variant.AsyncLifecycle){
                Helper.doInBackground(asyncLifecycleExecutor,
                        () -> {
                            ((PageAbstract.Variant.AsyncLifecycle) pageToUnload.getVariant()).save();
                            return pageToUnload;
                        }, this::onPostUnloadPage);
            }else{
                onPostUnloadPage(pageToUnload);
            }
        }

        private void onPostUnloadPage(PageAbstract unloadedPage){
            if(onPostUnloadPage != null){
                onPostUnloadPage.onPostUnloadPage(unloadedPage);
            }
        }
    }

    /**
     * Called when a variant/page has successfully been initialized. At this point in time, the
     * variant is not yet visible.
     */
    public interface OnPostPrepareVariant{ void onPostPrepareVariant(@NonNull PageAbstract.Variant newVariant); }

    /**
     * Called when a page has been successfully unloaded.
     */
    public interface OnPostUnloadPage{ void onPostUnloadPage(@NonNull PageAbstract unloadedPage); }

}
