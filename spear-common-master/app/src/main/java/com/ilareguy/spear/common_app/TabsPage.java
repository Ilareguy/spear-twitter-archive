package com.ilareguy.spear.common_app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.util.DiffCallback;
import com.ilareguy.spear.view.BackdropLayout;
import com.ilareguy.spear.view.PageTabLayout;
import com.ilareguy.spear.view.PagedAdapter;
import com.ilareguy.spear.view.RecyclerViewAbstract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class TabsPage extends PageAbstract {

    private static final class MobileVariant extends PageAbstract.Variant<ConstraintLayout>{

        private MobileVariant(final PageAbstract pageInstance){
            super(pageInstance);
        }

        private PageAbstract[] pages = new PageAbstract[3];
        private int selectedPageIndex = 0;

        @Override
        public @Nullable Animator buildEnterAnimator(){
            Animator fade = ObjectAnimator.ofFloat(getRootView(), "alpha",
                    0, 1);
            fade.setDuration(350);
            fade.setInterpolator(new FastOutSlowInInterpolator());
            return fade;
        }

        @Override
        public @Nullable Animator buildExitAnimator(){
            Animator fade = ObjectAnimator.ofFloat(getRootView(), "alpha",
                    1, 0);
            fade.setDuration(350);
            fade.setInterpolator(new FastOutSlowInInterpolator());
            return fade;
        }

        @Override
        protected @NonNull View inflate(final LayoutInflater inflater){
            // Create root view
            final ConstraintLayout root_view = new ConstraintLayout(inflater.getContext());

            // Inflate its contents
            inflater.inflate(R.layout.tabs_page, root_view, true);

            // Create tabs
            pages[0] = new TestPage(getContext(), "Page 1");
            pages[1] = new PagedAdapterTestPage(getContext());
            pages[2] = new TestPage(getContext(), "Page 3");

            // Set tabs
            final PageTabLayout tab_layout = root_view.findViewById(R.id.tab_layout);
            tab_layout.setViewPager(root_view.findViewById(R.id.pager));

            return root_view;
        }

        @Override
        protected void initializeLayout(){
            final PageTabLayout page_tab_layout = (getRootView().findViewById(R.id.tab_layout));

            // Set the pages AFTER restoreState was called
            page_tab_layout.setPages(pages, selectedPageIndex);
        }

        @Override
        public @NonNull Bundle saveState(){
            final Bundle state = super.saveState();
            state.putBundle("1", pages[0].saveState());
            state.putBundle("2", pages[1].saveState());
            state.putBundle("3", pages[2].saveState());
            state.putInt("SELECTED_TAB", ((TabLayout) getRootView().findViewById(R.id.tab_layout)).getSelectedTabPosition());
            return state;
        }

        @Override
        public void restoreState(final @NonNull Bundle savedState){
            super.restoreState(savedState);
            pages[0].restoreState(savedState.getBundle("1"));
            pages[1].restoreState(savedState.getBundle("2"));
            pages[2].restoreState(savedState.getBundle("3"));
            selectedPageIndex = savedState.getInt("SELECTED_TAB");
        }
    }

    // Required to be public for PageBundler to work properly!
    public TabsPage(final Context context){ super(context); }

    @Override
    protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                            ActivityAbstract.Orientation orientation){
        return new MobileVariant(this);
    }

    private static final class PagedAdapterTestPage extends PageAbstract{

        PagedAdapterTestPage(final Context context){
            super(context);
            setTitle("PagedAdapter");
        }

        @Override
        protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                                ActivityAbstract.Orientation orientation){
            return new PagedAdapterVariant(this);
        }

        private static final class PagedAdapterVariant
                extends PageAbstract.Variant<RecyclerViewAbstract<TextView, ItemData>>
                implements Variant.AsyncLifecycle{
            PagedAdapterVariant(PageAbstract pageInstance){ super(pageInstance); }

            @Override
            protected RecyclerViewAbstract<TextView, ItemData> inflate(LayoutInflater inflater){
                // Create recycler view
                final RecyclerViewAbstract<TextView, ItemData> root_view = new RecyclerViewAbstract<TextView, ItemData>(getContext()) {
                    @Override
                    public TextView buildView(Context context) {
                        final TextView new_view = new TextView(context);
                        new_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                150));
                        return new_view;
                    }

                    @Override
                    public void onBindViewHolder(TextView view, ItemData data) {
                        view.setText(data.val);
                    }
                };
                root_view.setBackgroundColor(root_view.getResources().getColor(R.color.colorPrimaryDark, null));
                return root_view;
            }

            @Override
            public void restoreState(Bundle bundle){
                //
            }

            @Override
            protected void initializeLayout(){
                getRootView().setAdapter(new PagedAdapter<>(80, getRootView()));
                getRootView().setStream(new TestStream(getPageInstance()), true);
            }

            @Override
            public Bundle saveState(){ return super.saveState(); }

            @Override
            public void load(@Nullable Bundle savedState){
                //
            }

            @Override
            public void save(){}
        }

        private static final class TestStream extends StreamAbstract<ItemData, StreamAbstract.ReadResult<ItemData>>{
            final static int ACTUAL_DATASET_SIZE = 300;

            ArrayList<ItemData> actualDataset = new ArrayList<>(ACTUAL_DATASET_SIZE);
            int currentIndexStart = 0;

            TestStream(final PageAbstract page){
                super(page);
                for(int i = 0; i < ACTUAL_DATASET_SIZE; i++){
                    actualDataset.add(new ItemData("Item at index " + String.valueOf(i)));
                }
            }

            @Override
            protected StreamAbstract.ReadResult<ItemData> buildResultObject(final @NonNull LinkedList<ItemData> o){
                return new StreamAbstract.ReadResult<>(o);
            }

            @Override
            protected StreamAbstract.ReadResult<ItemData> buildResultObject(final @NonNull LinkedList<ItemData> o,
                                                                            final @NonNull SpearError e){
                return new StreamAbstract.ReadResult<>(o, e);
            }

            @Override
            protected @NonNull StreamAbstract.ReadResult<ItemData> doReadForward(int maxCount, @Nullable ItemData thresholdObject){
                final LinkedList<ItemData> result = new LinkedList<>();
                if(maxCount > getRemainingItemsCount()) maxCount = getRemainingItemsCount();

                for(int i = 0; i < maxCount; i++) {
                    result.addLast(actualDataset.get(currentIndexStart + i));
                }

                currentIndexStart += maxCount;
                if(currentIndexStart == (actualDataset.size() - 1))
                    finalizeForward();
                finalizeBackward(false);

                return buildResultObject(result);
            }

            @Override
            protected @NonNull StreamAbstract.ReadResult<ItemData> doReadBackward(int maxCount, @NonNull ItemData thresholdObject){
                final LinkedList<ItemData> result = new LinkedList<>();
                final int index_start = actualDataset.indexOf(thresholdObject) - 1;
                final int read_count = ((index_start - maxCount) < 0)
                        ? maxCount - (-(index_start - maxCount))
                        : maxCount;

                int actual_index = 0;
                for(int i = 0; i < read_count; i++){
                    actual_index = index_start - i;
                    result.addFirst(actualDataset.get(actual_index));
                }

                if(actual_index == 0)
                    finalizeBackward();
                finalizeForward(false);

                currentIndexStart -= read_count;
                return buildResultObject(result);
            }

            int getRemainingItemsCount(){ return ACTUAL_DATASET_SIZE - currentIndexStart; }
        }

        private static final class ItemData implements DiffCallback<ItemData>{
            final String val;

            ItemData(String val){ this.val = val; }
            @Override public boolean isSame(@NonNull ItemData newObject){ return newObject == this; }
            @Override public boolean isContentsSame(@NonNull ItemData newObject){ return newObject == this; }
        }
    }

    private static final class TestPage extends PageAbstract{

        TestPage(final Context context, final String title){
            super(context);
            setTitle(title);
        }

        @Override
        protected @NonNull PageAbstract.Variant onCreateVariant(ActivityAbstract.VariantType type,
                                                                ActivityAbstract.Orientation orientation){
            return new TestVariant(this);
        }

        private static final class TestVariant extends PageAbstract.Variant<BackdropLayout>{

            TestVariant(TestPage page){
                super(page);
            }

            @Override
            protected @NonNull View inflate(final LayoutInflater inflater){
                // Create root view
                final BackdropLayout root_view = new BackdropLayout(inflater.getContext());

                // Inflate its contents
                inflater.inflate(R.layout.test_tab, root_view, true);
                root_view.onFinishInflate();
                root_view.setBackgroundColor(root_view.getResources().getColor(R.color.colorPrimaryDark, null));

                return root_view;
            }

            @Override
            protected void initializeLayout(){
                final Random random = new Random();
                final BackdropLayout backdrop_layout = getRootView();
                final ViewGroup back_layer = backdrop_layout.findViewById(R.id.back_layer);

                backdrop_layout.findViewById(R.id.front_layer).setOnClickListener((View v) ->
                {
                    back_layer.setLayoutParams(new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            random.nextInt(1500)
                    ));
                    backdrop_layout.notifyBackLayerChanged();
                });
            }

            @Override
            public @NonNull Bundle saveState(){
                final Bundle bundle = super.saveState();
                bundle.putParcelable("LAYOUT", getRootView().onSaveInstanceState());
                return bundle;
            }

            @Override
            public void restoreState(final @NonNull Bundle savedState){
                super.restoreState(savedState);
                getRootView().onRestoreInstanceState(savedState.getParcelable("LAYOUT"));
            }

        }
    }

}
