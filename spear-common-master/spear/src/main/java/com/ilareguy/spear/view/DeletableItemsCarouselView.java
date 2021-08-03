package com.ilareguy.spear.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ilareguy.spear.R;
import com.ilareguy.spear.util.Helper;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class DeletableItemsCarouselView<D , V extends View> extends RecyclerView{
    public DeletableItemsCarouselView(Context c){ super(c); init(c, null); }
    public DeletableItemsCarouselView(Context c, AttributeSet a){ super(c, a); init(c, a); }
    public DeletableItemsCarouselView(Context c, AttributeSet a, int d){ super(c, a, d); init(c, a); }

    public interface OnEventListener<D , V extends View>{
        void onItemClick(final @NonNull DeletableItemsCarouselView<D, V> carousel,
                         final @NonNull V view, final @NonNull D data);
        void onItemRemoved(final @NonNull DeletableItemsCarouselView<D, V> carousel,
                           final @NonNull V removedView, final @NonNull D removedData);
    }

    private _Adapter<D, V> adapter;
    private @Nullable OnEventListener<D, V> onEventListener = null;

    private void init(@NonNull Context c, @Nullable AttributeSet attrs){
        adapter = new _Adapter<>(this);
        final LinearLayoutManager layout_manager = new LinearLayoutManager(getContext());
        layout_manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setAdapter(adapter);
        setLayoutManager(layout_manager);
        new PagerSnapHelper().attachToRecyclerView(this);

        /*if(attrs != null)
            processAttributes(attrs);*/
    }

    /*private void processAttributes(@NonNull AttributeSet attrs){
        //
    }*/

    public void clear(){
        adapter.dataset.clear();
        adapter.notifyDataSetChanged();
    }

    public void addItem(final D item){
        adapter.dataset.add(item);
        adapter.notifyDataSetChanged();
    }

    public boolean removeItem(final D item){
        final boolean r = adapter.dataset.remove(item);
        if(r) adapter.notifyDataSetChanged();
        return r;
    }

    public final int getItemPosition(final D item){
        int i = 0;
        for(D d : adapter.dataset){
            if(d.equals(item))
                return i;
            i++;
        }
        return -1;
    }

    public final int getItemCount(){ return adapter.getItemCount(); }

    public void setOnEventListener(final @Nullable OnEventListener<D, V> onEventListener){
        this.onEventListener = onEventListener;
    }

    protected abstract @NonNull V createView(final @NonNull Context context);
    protected abstract void bindView(final @NonNull V view, final @NonNull D data);

    private static final class _ViewHolder<D , V extends View> extends RecyclerView.ViewHolder{
        _ViewHolder(final _ViewContainer<D, V> container){ super(container); this.container = container; }
        _ViewContainer<D, V> container;
    }

    private static final class _Adapter<D , V extends View> extends RecyclerView.Adapter<_ViewHolder<D, V>>{
        final DeletableItemsCarouselView<D, V> recyclerView;
        final ArrayList<D> dataset = new ArrayList<>();

        _Adapter(final DeletableItemsCarouselView<D, V> recyclerView){
            super();
            this.recyclerView = recyclerView;
        }

        @Override
        public @NonNull _ViewHolder<D, V> onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            final _ViewContainer<D, V> new_container = new _ViewContainer<>(parent.getContext(), recyclerView);
            new_container.onFinishInflate();
            new_container.setView(recyclerView.createView(parent.getContext()));
            return new _ViewHolder<>(new_container);
        }

        @Override
        public void onBindViewHolder(@NonNull _ViewHolder<D, V> holder, int position){
            holder.container.data = dataset.get(position);
            recyclerView.bindView(holder.container.view, holder.container.data);
        }

        @Override
        public int getItemCount(){ return dataset.size(); }
    }

    private static final class _ViewContainer<D , V extends View> extends ConstraintLayout{
        _ViewContainer(Context c, final DeletableItemsCarouselView<D, V> recyclerView){
            super(c);
            this.recyclerView = recyclerView;
            init(c);
        }

        final DeletableItemsCarouselView<D, V> recyclerView;
        ImageView deleteButton;
        V view;
        D data;

        private void init(@NonNull Context c){
            LayoutInflater.from(c).inflate(R.layout.deletable_items_carousel_view_item, this, true);
            setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        @Override
        public void onFinishInflate(){
            super.onFinishInflate();
            deleteButton = findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(onDeleteButtonClickListener);

            deleteButton.setOutlineProvider(deleteButtonOutlineProvider);
            deleteButton.setClipToOutline(true);
        }

        public void setView(final V view){
            this.view = view;
            view.setOnClickListener(onItemClickListener);
            final FrameLayout container = findViewById(R.id.view_container);
            container.addView(view);
        }

        private final View.OnClickListener onDeleteButtonClickListener = new OnClickListener(){
            @Override
            public void onClick(View view){
                if(recyclerView.removeItem(_ViewContainer.this.data) && recyclerView.onEventListener != null)
                    recyclerView.onEventListener.onItemRemoved(recyclerView, _ViewContainer.this.view,
                            _ViewContainer.this.data);
            }
        };

        private final View.OnClickListener onItemClickListener = new OnClickListener(){
            @Override
            public void onClick(View view){
                if(recyclerView.onEventListener != null)
                    recyclerView.onEventListener.onItemClick(recyclerView, _ViewContainer.this.view,
                            _ViewContainer.this.data);
            }
        };

        private static final ViewOutlineProvider deleteButtonOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        Helper.dpToPx(15, view.getResources()));
            }
        };
    }
}