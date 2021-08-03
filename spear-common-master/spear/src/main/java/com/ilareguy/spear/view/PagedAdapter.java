package com.ilareguy.spear.view;

import android.view.View;

import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.util.DiffCallback;

import java.util.LinkedList;

import androidx.annotation.NonNull;

/**
 * Much like a StaticAdapter, but only a limited amount of items is kept in memory at any given time.
 * When loading more items forward (appending data into the dataset), extra items at the beginning
 * of the list are removed; same goes for when loading items backwards (prepending data into the
 * dataset): extra items at the end of the dataset are removed. This ensures that there are never more
 * than a given amount of items in a dataset.
 *
 * @param <ViewType> The type of view contained in the owning RecyclerViewAbstract.
 * @param <DataType> The type of data contained in the dataset.
 */
public class PagedAdapter<
        ViewType extends View,
        DataType extends DiffCallback<DataType>
        > extends StaticAdapter<ViewType, DataType>
        implements StreamAbstract.OnReadComplete<DataType, StreamAbstract.ReadResult<DataType>>{

    public PagedAdapter(final int maxItemsCount,
                        final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView){
        super(recyclerView, maxItemsCount);
    }

    public PagedAdapter(final @NonNull RecyclerViewAbstract<ViewType, DataType> recyclerView){
        super(recyclerView);
    }

    @Override
    protected void loadMoreMaybe(final int index){
        if(getStream().isLoading()) return;

        if((index > previousReadIndex) // Reading forward
                && !getStream().isFinalizedForward() && index == (dataset.size() - remainingItemsCountTrigger))
            getStream().readForward(getItemsLoadCount(), dataset.getLast());
        else if((index < previousReadIndex) // Reading backward
                && !getStream().isFinalizedBackward() && index == remainingItemsCountTrigger)
            getStream().readBackward(getItemsLoadCount(), dataset.getFirst());
    }

    @Override
    public void onReadForwardComplete(final StreamAbstract.ReadResult<DataType> result){
        if(result.isSuccessful() && result.getObject().size() == 0){
            getStream().finalizeForward();
            return;
        }

        // It may be necessary to remove some items at the beginning of the dataset
        int remove_count = ((dataset.size() + result.getObject().size()) - getMaxItemsCount());
        if(remove_count > dataset.size()) remove_count = dataset.size();

        if(remove_count > 0) {
            // Remove extra items at the beginning of the dataset
            for (int i = 0; i < remove_count; i++)
                dataset.removeFirst();
            notifyItemRangeRemoved(0, remove_count);
        }

        // Add the new items at the end
        final int items_insert_position_start = dataset.size();
        dataset.addAll(result.getObject());
        notifyItemRangeInserted(items_insert_position_start, result.getObject().size());
        previousReadIndex = -1;
    }

    @Override
    public void onReadBackwardComplete(final StreamAbstract.ReadResult<DataType> result){
        if(result.isSuccessful() && result.getObject().size() == 0){
            getStream().finalizeBackward();
            return;
        }

        // It may be necessary to remove some items at the end of the dataset
        int remove_count = ((dataset.size() + result.getObject().size()) - getMaxItemsCount());
        if(remove_count > dataset.size()) remove_count = dataset.size();

        if(remove_count > 0){
            // Remove extra items at the end of the dataset
            for(int i = 0; i < remove_count; i++)
                dataset.removeLast();
            notifyItemRangeRemoved((dataset.size() - 1) - remove_count, remove_count);
        }

        // Add the new items at the beginning
        final LinkedList<DataType> items_to_add = result.getObject();
        for(int i = (items_to_add.size() - 1); i >= 0; i--)
            dataset.addFirst(items_to_add.get(i));
        notifyItemRangeInserted(0, items_to_add.size());
    }

}
