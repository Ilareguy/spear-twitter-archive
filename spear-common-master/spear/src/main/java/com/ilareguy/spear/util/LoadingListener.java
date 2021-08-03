package com.ilareguy.spear.util;

public class LoadingListener {

    public interface OnLoadingStateListener{

        /**
         * Called when the loading state has changed.
         * @param loading Is true when there is one or more component loading;
         *                false otherwise.
         */
        void onLoadingStateChanged(boolean loading);
    }

    private OnLoadingStateListener listener = null;
    private int currentlyLoadingCount = 0;

    public synchronized void setOnLoadingStateListener(OnLoadingStateListener l){
        listener = l;
    }

    /**
     * Your components must call this to notify that they starting a loading operation.
     * In theory, each loadingStart() invocation has a matching loadingEnd() call.
     */
    public synchronized void loadingStart(){
        currentlyLoadingCount++;
        if(currentlyLoadingCount == 1 && listener != null){
            listener.onLoadingStateChanged(true);
        }
    }

    /**
     * Your components must call this to notify that they are done with their loading operations.
     * Only call this once for every previous loadingStart() invocation.
     */
    public synchronized void loadingEnd(){
        currentlyLoadingCount--;
        if(currentlyLoadingCount == 0 && listener != null){
            listener.onLoadingStateChanged(false);
        }
    }

    public synchronized boolean isLoading(){
        return (currentlyLoadingCount > 0);
    }

}
