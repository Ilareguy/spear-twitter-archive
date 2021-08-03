package com.ilareguy.spear.stream;

import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.SpearErrorCode;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Abstract stream class that is capable of performing reading operations either from the cache
 * or from a remote endpoint (using OAuth).
 *
 * @param <DataType> The type of data this stream reads.
 */
public abstract class CacheOAuthStreamAbstract<
        DataType
        > extends StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>>{

    /**
     * Describes the various reading strategies available for this type of stream.
     */
    public enum ReadStrategy{

        /**
         * The stream will attempt to read the latest data available from the cache upon the first
         * read call, and perform a remote request anytime it runs out of cached data to display.
         */
        NORMAL,

        /**
         * The stream will behave the same as for NORMAL, but the first read operation will be done
         * remotely, and, upon success, the previously cached data will be cleared.
         */
        NORMAL_FRESH_START,

        /**
         * Only cached data will be displayed. No remote call will ever be performed for as long as
         * the strategy set for the stream is CACHE_ONLY.
         */
        CACHE_ONLY,

        /**
         * Only remote data will be displayed. No cache read will ever be performed for as long as
         * the strategy set for the stream is REMOTE_ONLY.
         * Caution should be exercised when using this since this will obviously result in an
         * increased network load and reading time.
         */
        REMOTE_ONLY
    }

    private ReadStrategy readStrategy = ReadStrategy.NORMAL;
    private boolean firstRead = true;

    public CacheOAuthStreamAbstract(final PageAbstract page){
        super(page);
    }

    protected final @NonNull StreamAbstract.ReadResult<DataType> doReadForward(int maxCount, @Nullable DataType thresholdObject){
        if(firstRead && readStrategy == ReadStrategy.NORMAL_FRESH_START){
            // Read latest data available remotely
            final StreamAbstract.ReadResult<DataType> remote_read_result = _doReadForwardRemote(maxCount, null);

            if(!remote_read_result.isSuccessful()){
                // Error occurred; return cached data instead
                firstRead = false;
                final StreamAbstract.ReadResult<DataType> cache_read_result =
                        doReadForwardCache(maxCount, thresholdObject);

                final SpearError remote_error = remote_read_result.getError();

                return buildResultObject(
                        cache_read_result.getObject(),
                        (remote_error.getCode() == SpearErrorCode.INTERNET_UNAVAILABLE.ordinal()

                                // Internet was unavailable; indicate that we've reverted to cached data
                        ? SpearError.build(SpearErrorCode.INTERNET_UNAVAILABLE_REVERT_TO_CACHE)

                                // It was another error; keep it as-is
                        : remote_error)
                );
            }

            firstRead = false;
            return remote_read_result;
        }

        firstRead = false;
        switch(readStrategy){
            case CACHE_ONLY:
                return doReadForwardCache(maxCount, thresholdObject);

            case REMOTE_ONLY:
                return _doReadForwardRemote(maxCount, thresholdObject);

            default:
                return doReadForwardNormal(maxCount, thresholdObject);
        }
    }

    @Override
    public void reset(){
        super.reset();
        firstRead = true;
    }

    protected final @NonNull StreamAbstract.ReadResult<DataType> doReadBackward(int maxCount, @NonNull DataType thresholdObject){
        return doReadBackwardCache(maxCount, thresholdObject);
    }

    private @NonNull StreamAbstract.ReadResult<DataType> doReadForwardNormal(int max_count, @Nullable DataType threshold_object){
        // Start off by reading the cache
        final StreamAbstract.ReadResult<DataType> cache_read_result = doReadForwardCache(max_count, threshold_object);
        final LinkedList<DataType> cached_data = cache_read_result.getObject();

        if(cached_data.size() >= max_count){
            // Everything was read from the cache
            return cache_read_result;
        }

        // Not everything was read from the cache; finish off with a remote read
        final StreamAbstract.ReadResult<DataType> remote_read_result = doReadForwardRemote(
                max_count - cached_data.size(),
                cached_data.size() == 0 ? null : cached_data.getLast()
        );

        if(!remote_read_result.hasResult()){
            // Couldn't read remote data!
            return buildResultObject(
                    cached_data, // Return the cached object previously read
                    remote_read_result.getError() // Pass in the error the occured from the remote read
            );
        }

        // At this point, some data was read from the remote endpoint.
        // Cache the newly read data
        final LinkedList<DataType> remote_data = remote_read_result.getObject();
        cacheData(remote_data);
        // Concatenate the cache and remote data containers
        for(DataType o : remote_data)
            cached_data.addLast(o);
        // Return the final container
        return buildResultObject(cached_data);
    }

    /**
     * Tells the stream that some objects were read from a remote endpoint. Call this when you're done
     * reading your data so that the stream can cache the items appropriately.
     * @param read_objects The objects read from a remote endpoint.
     */
    protected final void reportRemoteSuccess(final LinkedList<DataType> read_objects){
        if(firstRead)
            clearCachedData();
        cacheData(read_objects);
    }

    /**
     * Your implementation must override this. It will be invoked when it's time to clear the cache
     * of the old data.
     */
    protected abstract void clearCachedData();

    /**
     * Your implementation must override this. It will be invoked when it's time to cache the given
     * data to faster retrieve it later on, if required.
     */
    protected abstract void cacheData(final LinkedList<DataType> data);

    /**
     * The various read operations that your implementation must override.
     * No caching shall be done in those methods! Instead, override the cacheData() method, which will
     * be invoked automagically at appropriate times.
     */
    protected abstract @NonNull StreamAbstract.ReadResult<DataType>
        doReadForwardCache(int max_count, @Nullable DataType threshold_object);
    protected abstract @NonNull StreamAbstract.ReadResult<DataType>
        doReadForwardRemote(int max_count, @Nullable DataType threshold_object);
    protected abstract @NonNull StreamAbstract.ReadResult<DataType>
        doReadBackwardCache(int max_count, @Nullable DataType threshold_object);

    /**
     * Sets the strategy for this stream. This should be called *before* any read operation
     * is done.
     */
    public final void setReadStrategy(ReadStrategy s){ this.readStrategy = s; }

    private @NonNull StreamAbstract.ReadResult<DataType>
        _doReadForwardRemote(int max_count, @Nullable DataType threshold_object){
        // If Internet is unavailable, save some time now and return an error
        if(!App.getInstance().isNetworkAvailable()){
            return buildResultObject(SpearError.build(SpearErrorCode.INTERNET_UNAVAILABLE));
        }

        return doReadForwardRemote(max_count, threshold_object);
    }

    @Override
    protected StreamAbstract.ReadResult<DataType> buildResultObject(final @NonNull LinkedList<DataType> o){
        return new StreamAbstract.ReadResult<>(o);
    }

    @Override
    protected StreamAbstract.ReadResult<DataType> buildResultObject(final @NonNull LinkedList<DataType> o, final @NonNull SpearError e){
        return new StreamAbstract.ReadResult<>(o, e);
    }

}
