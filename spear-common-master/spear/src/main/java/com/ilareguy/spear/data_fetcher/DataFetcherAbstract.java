package com.ilareguy.spear.data_fetcher;

import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.TaskTyped;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.ilareguy.spear.SpearErrorCode.INTERNET_UNAVAILABLE;

/**
 * Reads data from the cache and/or an OAuth request/response.
 *
 * @param <T> The type of data that will be returned.
 */
public abstract class DataFetcherAbstract<T> {

    public enum Policy {

        /**
         * Forces the reader to call a remote OAuth server to retrieve
         * the data, ignoring the cache.
         */
        FORCE_REMOTE,

        /**
         * Forces the reader to read the data from the local cache,
         * ignoring the remote OAuth server. Data returned is the least
         * likely to be up-to-date.
         */
        FORCE_CACHE,

        /**
         * The reader will read data contained in the cache:
         * if the data didn't exist, then an OAuth request
         * will be performed (calling OnOauth()).
         *
         * This is useful if you only want to perform an OAuth request
         * if the cached data does not exist.
         */
        CACHE_OR_OAUTH,

        /**
         * The reader will attempt to perform an OAuth request:
         * if there was an error, such as if there is no active connection,
         * then the reader will perform a cache read.
         */
        OAUTH_OR_CACHE
    }

    public enum ResultType {

        /**
         * Indicates that the returned data was cached.
         */
        CACHED,

        /**
         * Indicates that the returned data is from a remote OAuth server.
         */
        OAUTH,

        /**
         * Indicates that the returned data is a combination of both cached data
         * and data received from the OAuth server.
         */
        MIXED
    }

    public static final class Result<T> extends TaskResult<T>{

        private ResultType mResultType = ResultType.CACHED;

        public Result(@NonNull T object){ super(object); }
        public Result(@NonNull SpearError error){ super(error); }
        public Result(@NonNull T object, @NonNull SpearError error){ super(object, error); }

        public ResultType getResultType() {
            return mResultType;
        }
        public void setResultType(ResultType type) {
            mResultType = type;
        }

    }

    public interface OnPostExecuteListener<T> {
        void onPostExecute(Result<T> result);
    }

    private static class _AsyncTask<T> extends TaskTyped<DataFetcherAbstract<T>, Void, T, Result<T>>{

        private DataFetcherAbstract<T> object;

        _AsyncTask(final @Nullable PageAbstract callingPage){
            super(callingPage);
        }

        @Override
        public DataFetcherAbstract.Result<T> doInBackground(DataFetcherAbstract<T>[] args) {
            object = args[0];
            return object.execute();
        }

        @Override
        public void onPostExecute(DataFetcherAbstract.Result<T> result) {
            object.getOnPostExecuteListener().onPostExecute(result);
        }

    }

    private final Policy policy;
    private final long ageThreshold;
    private final PageAbstract page;
    private OnPostExecuteListener<T> onPostExecuteListener = null;

    public DataFetcherAbstract(final PageAbstract page, final Policy policy) {
        this.policy = policy;
        this.ageThreshold = 0;
        this.page = page;
    }

    public DataFetcherAbstract(final PageAbstract page, final Policy policy,
                               final long cache_age_threshold_sec) {
        this.policy = policy;
        this.ageThreshold = cache_age_threshold_sec;
        this.page = page;
    }

    /**
     * Called when it is time to perform a cache read.
     * Your implementation must either call
     * result_handle.success(T data_read) to notify a successful
     * read; or
     * result_handle.TwitterAppError(TwitterAppError error) to notify an error.
     *
     * If getAgeThreshold() is non-zero, your implementation should only return
     * data whose age is younger than the returned age threshold.
     *
     * If neither is called, the reader will assume that an error occurred.
     */
    protected abstract @NonNull Result<T> onCache();

    /**
     * Called when it is time to perform an OAuth request.
     * Your implementation must return the received object of
     * type T, or null if an error occurred.
     */
    protected abstract @NonNull Result<T> onRemote();

    public @NonNull Result<T> execute() {
        Result<T> result;
        if (policy == Policy.FORCE_REMOTE) {
            if (!App.getInstance().isNetworkAvailable()) {
                return new Result<>(SpearError.build(INTERNET_UNAVAILABLE));
            }

            // Only perform an OAuth request
            result = onRemote();
            result.setResultType(ResultType.OAUTH);
        } else if (policy == Policy.FORCE_CACHE) {
            // Only perform a cache read
            result = onCache();
            result.setResultType(ResultType.CACHED);
        } else if (policy == Policy.CACHE_OR_OAUTH) {
            // Perform a cache read, then possibly an OAuth as well
            result = onCache();

            // Check if there was an error
            if (!result.isSuccessful()) {
                // Couldn't read the object from the cache; perform
                // an OAuth request

                if (!App.getInstance().isNetworkAvailable()) {
                    return new Result<>(SpearError.build(INTERNET_UNAVAILABLE));
                }

                result = onRemote();
                result.setResultType(ResultType.OAUTH);
            } else
                result.setResultType(ResultType.CACHED);
        }else/* if(policy == Policy.OAUTH_OR_CACHE)*/{
            // Start by performing an OAuth request
            result = onRemote();
            result.setResultType(ResultType.OAUTH);

            if(!result.isSuccessful()){
                // An error occurred; perform a cache read
                result = onCache();
                result.setResultType(ResultType.CACHED);
            }
        }

        return result;
    }

    public void asyncExecute(OnPostExecuteListener<T> onPostExecuteListener) {
        this.onPostExecuteListener = onPostExecuteListener;
        new _AsyncTask<T>(getPage()).execute(this);
    }

    public final Policy getPolicy() { return policy; }
    private OnPostExecuteListener<T> getOnPostExecuteListener() {
        return onPostExecuteListener;
    }
    protected final long getAgeThreshold() {
        return ageThreshold;
    }
    public final PageAbstract getPage(){ return page; }
}
