package com.ilareguy.spear.view;

import android.os.Bundle;
import android.view.View;

import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.util.DiffCallback;

import java.util.Collection;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The simplest form of adapter available for the RecyclerViewAbstract. The entire dataset is held in
 * memory and can be accessed anytime by the owning RecyclerViewAbstract. The adapter will automagically
 * detect when new data needs to be loaded and do so in a background task.
 *
 * @param <ViewType> The type of view contained in the owning RecyclerViewAbstract.
 * @param <DataType> The type of data contained in the dataset.
 */
public class StaticAdapter<
        ViewType extends View,
        DataType extends DiffCallback<DataType>
        > extends AdapterAbstract<ViewType, DataType>{

    protected final LinkedList<DataType> dataset = new LinkedList<>();
    protected final int remainingItemsCountTrigger;

    protected int previousReadIndex = -1;

    public StaticAdapter(final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView){
        this(recyclerView, 80);
    }

    public StaticAdapter(final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView,
                         int maxItemsCount){
        super(recyclerView, maxItemsCount);
        this.remainingItemsCountTrigger = (getItemsLoadCount() / 2);
    }

    @Override
    protected @Nullable DataType getDataAtIndex(int index){
        loadMoreMaybe(index);
        this.previousReadIndex = index;
        return dataset.get(index);
    }

    protected void loadMoreMaybe(final int index){
        if(getStream().isLoading()) return;

        if((index > previousReadIndex) // Reading forward
                && !getStream().isFinalizedForward() && index >= (dataset.size() - remainingItemsCountTrigger))
            getStream().readForward(getItemsLoadCount(), dataset.getLast());
    }

    @Override
    public void onReadForwardComplete(final StreamAbstract.ReadResult<DataType> result){
        // Append the new data at the end
        for(DataType d : result.getObject())
            dataset.addLast(d);

        // Notify
        notifyDataSetChanged();
    }

    @Override
    public void onReadBackwardComplete(final StreamAbstract.ReadResult<DataType> result){}

    @Override
    public int getItemCount(){ return dataset.size(); }

    @Override
    public void refresh(){
        // Clear dataset
        dataset.clear();
        notifyDataSetChanged();
        super.refresh();
    }

    public void setDataset(Collection<DataType> data){
        dataset.clear();
        dataset.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public Bundle saveState(){
        final Bundle bundle = super.saveState();
        bundle.putInt("previousReadIndex", previousReadIndex);
        return bundle;
    }

    @Override
    public void restoreState(Bundle bundle){
        super.restoreState(bundle);
        this.previousReadIndex = bundle.getInt("previousReadIndex");
    }

    public final LinkedList<DataType> getRawDataset(){ return dataset; }

    public static final class ViewHolder<ViewType extends View>
            extends RecyclerView.ViewHolder{

        ViewHolder(ViewType v){
            super(v);
        }

        public final ViewType getView(){ return (ViewType) itemView; }
    }

}
