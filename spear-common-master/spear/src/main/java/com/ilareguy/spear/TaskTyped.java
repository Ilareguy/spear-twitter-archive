package com.ilareguy.spear;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An AsyncTask that returns a result of type SpearTaskResult, and that executes on the right
 * pool executor.
 *
 * Extend this class if your task returns a custom type extending SpearTaskResult. If it returns
 * a SpearTaskResult, the it is easier to extend the SpearTask instead.
 *
 * @param <Args> The arguments type this task is expecting.
 * @param <Progress> The progress type this task is returning.
 * @param <RawResultType> The raw type returned upon completion of this task.
 * @param <Result> The actual type returned by this task. This type must extend SpearTaskResult to
 *                track whether or not tasks executed successfully.
 */
public abstract class TaskTyped<Args, Progress, RawResultType, Result extends TaskResult<RawResultType>>{

    private final _AsyncTask<Args, Progress, RawResultType, Result> asyncTaskObject;
    private final @Nullable
    PageAbstract callingPage;

    protected TaskTyped(final @Nullable PageAbstract callingFragment){
        this.callingPage = callingFragment;
        this.asyncTaskObject = new _AsyncTask<>(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public final void execute(Args... args){
        if(callingPage != null){
            // Execute on the page's executor
            callingPage.registerTask(this);
            asyncTaskObject.executeOnExecutor(callingPage.getAsyncTaskExecutor(), args);
        }else{
            // Execute on a shared executor
            asyncTaskObject.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
        }
    }

    public final AsyncTask.Status getStatus() { return asyncTaskObject.getStatus(); }
    public final boolean isCancelled(){ return asyncTaskObject.isCancelled(); }
    public final void cancel(boolean mayInterruptIfRunning){ asyncTaskObject.cancel(mayInterruptIfRunning); }
    public final PageAbstract getCallingPage(){ return callingPage; }

    /**
     * To be implemented by children classes.
     */
    protected abstract @NonNull Result doInBackground(@NonNull Args... args);
    protected abstract void onPostExecute(final @NonNull Result result);

    /**
     * Can be overridden by children classes.
     */
    protected void onPreExecute(){}

    private static final class _AsyncTask<Args, Progress, RawType, Result extends TaskResult<RawType>>
            extends AsyncTask<Args, Progress, Result>{

        final TaskTyped<Args, Progress, RawType, Result> spearTaskObject;

        _AsyncTask(final TaskTyped<Args, Progress, RawType, Result> spearTaskObject){ this.spearTaskObject = spearTaskObject; }

        @Override
        public void onPreExecute(){
            spearTaskObject.onPreExecute();
        }

        @Override
        public Result doInBackground(Args... args){
            return spearTaskObject.doInBackground(args);
        }

        @Override
        public void onPostExecute(Result result){
            super.onPostExecute(result);
            unregisterTaskFromPage();
            spearTaskObject.onPostExecute(result);
        }

        @Override
        protected void onCancelled(Result result) {
            super.onCancelled(result);
            unregisterTaskFromPage();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            unregisterTaskFromPage();
        }

        private void unregisterTaskFromPage(){
            if(spearTaskObject.callingPage != null){
                spearTaskObject.callingPage.unregisterTask(spearTaskObject);
            }
        }
    }

}
