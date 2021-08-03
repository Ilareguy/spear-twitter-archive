package com.ilareguy.spear.stream;

import android.os.Bundle;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.util.OnErrorListener;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.TaskTyped;
import com.ilareguy.spear.util.LoadingListener;
import com.ilareguy.spear.util.RestorableState;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An abstract class that performs reading operations on a parallel SpearTask. This class does not
 * handle any network- or cache-specific operations in itself.
 *
 * @param <DataType> The type of data this stream reads.
 * @param <ReadResultType> The custom class, extending SpearTaskResult, that this stream returns.
 */
public abstract class StreamAbstract<
        DataType,
        ReadResultType extends StreamAbstract.ReadResult<DataType>
        > extends LoadingListener
        implements OnErrorListener, RestorableState{

    public static final class ReadResult<DataType> extends TaskResult<LinkedList<DataType>>{
        public ReadResult(final @NonNull LinkedList<DataType> o){ super(o); }
        public ReadResult(final @NonNull SpearError e){ super(e); }
        public ReadResult(final @NonNull LinkedList<DataType> o, final @NonNull SpearError e){ super(o, e); }

        public final boolean hasResult(){ return (getObject() != null && getObject().size() > 0); }
    }

    public interface OnReadComplete<DataType, ReadResultType extends StreamAbstract.ReadResult<DataType>>{
        void onReadForwardComplete(final ReadResultType result);
        void onReadBackwardComplete(final ReadResultType result);
    }

    private final PageAbstract page;

    private boolean finalizedForward = false;
    private boolean finalizedBackward = false;

    private @Nullable OnReadComplete<DataType, ReadResultType> readCompleteListener = null;
    private @Nullable OnErrorListener errorListener = null;

    public StreamAbstract(final PageAbstract page){
        super();
        this.page = page;
    }

    public final boolean readForward(int max_count, @Nullable DataType threshold_object){
        if(isLoading()) return false;

        loadingStart();
        new ReadTask<>(this,
                page,
                ReadTask.ReadDirection.FORWARD,
                max_count,
                threshold_object).execute();
        return true;
    }

    public final boolean readBackward(int max_count, @Nullable DataType threshold_object){
        if(isLoading()) return false;

        loadingStart();
        new ReadTask<>(this,
                page,
                ReadTask.ReadDirection.BACKWARD,
                max_count,
                threshold_object).execute();
        return true;
    }

    public void reset(){ finalizedForward = false; }
    public void setOnErrorListener(@Nullable OnErrorListener l){ this.errorListener = l; }

    @Override
    public void onError(final @NonNull SpearError error){
        //

        if(errorListener != null)
            errorListener.onError(error);
    }

    @Override
    public Bundle saveState(){
        return new Bundle();
    }

    @Override
    public void restoreState(Bundle bundle){}

    /**
     * When returning a result from your read operations, you should either call the methods, or
     * manually create a new object of type ReadResultType; in the latter case, never pass null
     * as the read data as this will produce a NullPointerException.
     */
    protected final ReadResultType buildResultObject(final @NonNull SpearError e){ return buildResultObject(new LinkedList<DataType>(), e); }
    protected abstract ReadResultType buildResultObject(final @NonNull LinkedList<DataType> o);
    protected abstract ReadResultType buildResultObject(final @NonNull LinkedList<DataType> o, final @NonNull SpearError e);

    /**
     * Your stream must implement this method. It will be called from a worker thread, so you're free
     * to perform any cache, remote, or otherwise lasting reading operations in them.
     *
     * The data you return must be sorted in a sequential order. The first element in the array you
     * return must be the element coming directly after the passed thresholdObject object. In the event
     * that thresholdObject is null, it means that this is the first read operation and there is
     * currently no threshold.
     *
     * You can return up to maxCount objects in the array, but it doesn't have to be that much. You're
     * also free to read up more objects than that and cache the exceeding items, but the array you
     * return shouldn't contain more than maxCount objects.
     *
     * Your implementation is responsible for calling finalizeForward() if it reaches the end of the
     * stream.
     *
     * @param maxCount The maximum amount of objects of type DataType to return.
     * @param thresholdObject The threshold object to use for reading. The data you return should come
     *                        directly after this object.
     * @return An object of type ReadResultType, containing an array of DataType and/or an SpearError.
     */
    protected abstract @NonNull ReadResultType doReadForward(int maxCount, @Nullable DataType thresholdObject);

    /**
     * Your stream must implement this method. It will be called from a worker thread, so you're free
     * to perform any cache, remote, or otherwise lasting reading operations in them.
     *
     * The data you return must be sorted in a sequential order. The last element in the array you
     * return must be the element coming directly before the passed thresholdObject object.
     *
     * You can return up to maxCount objects in the array but it doesn't have to be that much. You're
     * also free to read up more objects than that and cache the exceeding items, but the array you
     * return shouldn't contain more the maxCount objects.
     *
     * Your implementation is responsible for calling finalizeBackward() if it reaches the beginning
     * of the stream.
     *
     * @param maxCount The maximum amount of objects of type DataType to return.
     * @param thresholdObject The threshold object to use for reading. The data you return should come
     *                        directly before this object.
     * @return An object of type ReadResultType, containing an array of DataType and/or an SpearError.
     */
    protected abstract @NonNull ReadResultType doReadBackward(int maxCount, @NonNull DataType thresholdObject);

    protected void onReadForwardComplete(final @NonNull ReadResultType result){
        if(readCompleteListener != null) readCompleteListener.onReadForwardComplete(result);
    }

    protected void onReadBackwardComplete(final @NonNull ReadResultType result){
        if(readCompleteListener != null) readCompleteListener.onReadBackwardComplete(result);
    }

    public final void setReadCompleteListener(final @NonNull OnReadComplete<DataType, ReadResultType> l)
        { this.readCompleteListener = l; }
    public final boolean isFinalizedForward(){ return finalizedForward; }
    public final boolean isFinalizedBackward(){ return finalizedBackward; }
    public final void finalizeForward(){ finalizeForward(true); }
    public final void finalizeForward(boolean f){ this.finalizedForward = f; }
    public final void finalizeBackward(){ finalizeBackward(true); }
    public final void finalizeBackward(boolean f){ this.finalizedBackward = f; }
    public final PageAbstract getPage(){ return page; }

    private static final class ReadTask<
            DataType,
            ReadResultType extends StreamAbstract.ReadResult<DataType>
            >
            extends TaskTyped<Void, Void, LinkedList<DataType>, ReadResultType>{

        enum ReadDirection{
            FORWARD,
            BACKWARD
        }

        private final StreamAbstract<DataType, ReadResultType> streamObject;
        final ReadDirection readDirection;
        final int maxCount;
        final @Nullable DataType threshold_object;

        ReadTask(final StreamAbstract<DataType, ReadResultType> streamObject,
                 final @Nullable PageAbstract page,
                 final ReadDirection readDirection,
                 int maxCount,
                 final @Nullable DataType threshold_object){
            super(page);
            this.readDirection = readDirection;
            this.streamObject = streamObject;
            this.maxCount = maxCount;
            this.threshold_object = threshold_object;
        }

        @Override
        protected @NonNull ReadResultType doInBackground(Void... v){
            return (readDirection == ReadDirection.FORWARD)
                    ? streamObject.doReadForward(maxCount, threshold_object)
                    : streamObject.doReadBackward(maxCount, threshold_object);
        }

        @Override
        protected void onPostExecute(@NonNull ReadResultType result){
            if(readDirection == ReadDirection.FORWARD)
                streamObject.onReadForwardComplete(result);
            else
                streamObject.onReadBackwardComplete(result);

            if(result.getError() != null)
                streamObject.onError(result.getError());

            streamObject.loadingEnd();
        }
    }
}
