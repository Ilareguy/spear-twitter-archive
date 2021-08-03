package com.ilareguy.spear;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import androidx.annotation.NonNull;

public abstract class PageBundler{

    /**
     * Builds a Bundle object containing the given page's current state and a way to retrieve the
     * original class corresponding the the given page at a later time.
     *
     * For this to work properly, the passed page's class MUST implement a public constructor with
     * only a Context for a parameter, as this is the one that buildPageFromBundle() will call
     * upon re-creation of the object later on.
     *
     * See buildPageFromBundle() to convert the returned Bundle back into a valid PageAbstract
     * object.
     */
    public static @NonNull Bundle bundlePage(@NonNull PageAbstract page){
        final Bundle page_bundle = new Bundle();
        page_bundle.putString("CLASS", page.getClass().getName());
        page_bundle.putBundle("STATE", page.saveState());
        return page_bundle;
    }

    /**
     * Builds and returns a PageAbstract object from the given Bundle.
     * If this method returns and no exception were thrown, then the page was successfully created.
     * In order for this to work properly, the original page's class MUST define a PUBLIC constructor
     * that takes a single Context object.
     *
     * The returned page still has to go through the PageLoader's loadPage() process to be ready for
     * use.
     *
     * @param bundle A Bundle object returned by bundlePage().
     * @param context A Context, required to build back the original page.
     * @return The page object.
     */
    public static @NonNull PageAbstract buildPageFromBundle(@NonNull Bundle bundle,
                                                            @NonNull Context context)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
            NoSuchMethodException, InvocationTargetException{

        // Find the constructor to build the page
        final Class<?> page_class = Class.forName(bundle.getString("CLASS"));
        final Constructor<?> page_constructor = page_class.getConstructor(Context.class);

        // Create a new instance of the page
        final PageAbstract page = (PageAbstract) page_constructor.newInstance(context);

        // Restore its state
        page.restoreState(bundle.getBundle("STATE"));

        return page;
    }

}
