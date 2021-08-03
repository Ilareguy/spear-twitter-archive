package com.ilareguy.spear.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.util.OnErrorListener;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.util.DiffCallback;
import com.ilareguy.spear.util.LoadingListener;
import com.ilareguy.spear.util.RestorableState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerViewAbstract<
        ViewType extends View,
        DataType extends DiffCallback<DataType>
        > extends RecyclerView
        implements OnErrorListener, RestorableState{

    private static final float FLING_SPEED_FACTOR = 1.22f;

    private @Nullable LoadingListener loadingListener = null;
    private @Nullable OnErrorListener onErrorListener;
    private @Nullable PageAbstract page;
    private @Nullable Bundle adapterState = null;

    public RecyclerViewAbstract(Context c) { super(c); init(); }
    public RecyclerViewAbstract(Context c, AttributeSet a) { super(c, a); init(); }
    public RecyclerViewAbstract(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    public abstract @NonNull ViewType buildView(final Context context);
    public abstract void onBindViewHolder(@NonNull ViewType view, @Nullable DataType data);

    private void init(){
        setLayoutManager(new LinearLayoutManager(getContext()));
        addItemDecoration(new RecyclerViewDivider(getContext()));
        setVerticalScrollBarEnabled(true);
    }

    @Override
    public final void onError(final @NonNull SpearError error){
        // @TODO: Stop loading remote contents until cleared to do so

        // Notify listener
        if(onErrorListener != null)
            onErrorListener.onError(error);
    }

    @Override
    public boolean fling(int velocityX, int velocityY){
        // if FLING_SPEED_FACTOR between [0, 1[ => slowdown
        velocityY *= FLING_SPEED_FACTOR;
        return super.fling(velocityX, velocityY);
    }

    @Override
    public Bundle saveState(){
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", onSaveInstanceState());
        if(_getAdapter() != null) bundle.putBundle("ADAPTER", _getAdapter().saveState());
        return bundle;
    }

    @Override
    public void restoreState(Bundle bundle){
        if(_getAdapter() != null)
            _getAdapter().restoreState(bundle.getBundle("ADAPTER"));
        else
            adapterState = bundle.getBundle("ADAPTER");
        onRestoreInstanceState(bundle.getParcelable("SUPER"));
    }

    public void setStream(@Nullable StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>> stream,
                          boolean startLoading){
        _getAdapter().setStream(stream, startLoading);
        _getAdapter().setOnErrorListener(this);
        if(adapterState != null) {
            _getAdapter().restoreState(adapterState);
            adapterState = null;
        }
    }

    public void refresh(){ _getAdapter().refresh(); }
    public void setOnErrorListener(@NonNull OnErrorListener l){ onErrorListener = l; }
    public void setLoadingListener(@Nullable LoadingListener l){ loadingListener = l; _getAdapter().setLoadingListener(l); }
    public final @Nullable LoadingListener getLoadingListener(){ return loadingListener; }
    public void setPage(PageAbstract page){ this.page = page; }
    public final @Nullable PageAbstract getPage(){ return page; }

    private AdapterAbstract<ViewType, DataType> _getAdapter(){ return (AdapterAbstract<ViewType, DataType>) getAdapter(); }
}
