package com.ilareguy.spear.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ilareguy.spear.App;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.util.DiffCallback;
import com.ilareguy.spear.util.LoadingListener;
import com.ilareguy.spear.util.OnErrorListener;
import com.ilareguy.spear.util.RestorableState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AdapterAbstract<
        ViewType extends View,
        DataType extends DiffCallback<DataType>
        > extends RecyclerView.Adapter<StaticAdapter.ViewHolder<ViewType>>
        implements StreamAbstract.OnReadComplete<DataType, StreamAbstract.ReadResult<DataType>>,
        LoadingListener.OnLoadingStateListener, RestorableState{

    private StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>> stream = null;

    private @Nullable LoadingListener loadingListener = null;
    private @Nullable Bundle streamState = null;

    private final int maxItemsCount;
    private final int itemsLoadCount;

    private final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView;

    protected AdapterAbstract(final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView,
                              int maxItemsCount){
        this.recyclerView = recyclerView;
        this.maxItemsCount = maxItemsCount;
        this.itemsLoadCount = (maxItemsCount / 3);
    }

    protected abstract @Nullable DataType getDataAtIndex(int index);

    public void setStream(final @NonNull StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>> stream,
                          boolean startLoading){
        if(this.stream != null){
            this.stream.setReadCompleteListener(null);
        }

        this.stream = stream;

        if(streamState != null){
            this.stream.restoreState(streamState);
            streamState = null;
        }

        // Start the initial loading process
        stream.setReadCompleteListener(this);
        stream.setOnLoadingStateListener(this);

        if(startLoading)
            stream.readForward(maxItemsCount, null);
    }

    public void refresh(){
        // Reset stream
        stream.reset();
        // Load more
        stream.readForward(maxItemsCount, null);
    }

    @Override
    public void onLoadingStateChanged(boolean loading){
        App._d(loading
                ? "Stream loading started."
                : "Stream loading ended.");
        if(loadingListener != null){
            if(loading) loadingListener.loadingStart();
            else        loadingListener.loadingEnd();
        }
    }

    @Override
    public Bundle saveState(){
        return (stream == null) ? new Bundle() : stream.saveState();
    }

    @Override
    public void restoreState(Bundle bundle){
        if(stream != null) stream.restoreState(bundle);
        else streamState = bundle;
    }

    @Override
    public final @NonNull StaticAdapter.ViewHolder<ViewType> onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new StaticAdapter.ViewHolder<>(getRecyclerView().buildView(parent.getContext()));
    }

    @Override
    public final void onBindViewHolder(@NonNull StaticAdapter.ViewHolder<ViewType> holder, int absolute_position){
        getRecyclerView().onBindViewHolder(holder.getView(), getDataAtIndex(absolute_position));
    }

    public void setLoadingListener(@Nullable LoadingListener l){ this.loadingListener = l; }
    public final StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>> getStream(){ return stream; }
    public final @NonNull RecyclerViewAbstract<ViewType, DataType> getRecyclerView(){ return recyclerView; }

    /**
     * This method only forwards the listener object to its Stream object. Therefore, only call
     * this once you've called setStream() with a valid Stream.
     */
    public void setOnErrorListener(@Nullable OnErrorListener l){ stream.setOnErrorListener(l); }

    protected final int getMaxItemsCount(){ return maxItemsCount; }
    protected final int getItemsLoadCount(){ return itemsLoadCount; }

}
