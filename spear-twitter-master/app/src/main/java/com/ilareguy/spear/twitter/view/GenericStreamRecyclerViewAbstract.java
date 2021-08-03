package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;

import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.stream.StreamAbstract;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.util.DiffCallback;
import com.ilareguy.spear.util.OnErrorListener;
import com.ilareguy.spear.util.RestorableState;
import com.ilareguy.spear.view.MaterialBanner;
import com.ilareguy.spear.view.MaterialHeader;
import com.ilareguy.spear.view.PagedAdapter;
import com.ilareguy.spear.view.RecyclerViewAbstract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

// TODO: 2018-08-18 Move this into the common package
public abstract class GenericStreamRecyclerViewAbstract<
        ViewType extends View,
        RecyclerViewType extends RecyclerViewAbstract<ViewType, DataType>,
        DataType extends DiffCallback<DataType>> extends CoordinatorLayout
        implements OnErrorListener, RestorableState{

    private @Nullable CharSequence subtitle = null;
    private @Nullable RecyclerViewType recyclerView = null;

    private MaterialHeader subtitleHeaderView;
    private MaterialBanner errorBanner;

    public GenericStreamRecyclerViewAbstract(Context context){ super(context); init(context); }
    public GenericStreamRecyclerViewAbstract(Context context, AttributeSet attrs){ super(context, attrs); init(context); }
    public GenericStreamRecyclerViewAbstract(Context context, AttributeSet attrs, int d){ super(context, attrs, d); init(context); }

    /**
     * Will be called when it is time to inflate/show the provided stub.
     */
    protected abstract @NonNull RecyclerViewType inflateRecyclerView(ViewStub stub);

    private void init(final Context context){
        LayoutInflater.from(context).inflate(R.layout.generic_stream, this, true);

        subtitleHeaderView = findViewById(R.id.subtitle_label);
        //final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = inflateRecyclerView(findViewById(R.id.timeline_recycler_view_stub));
        errorBanner = findViewById(R.id.banner);

        recyclerView.setOnErrorListener(GenericStreamRecyclerViewAbstract.this);
        //recyclerView.setLoadingListener(loadingListener);
        //recyclerView.setPage(getPageInstance());
        //swipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        doSetSubtitle();
    }

    public @Nullable RecyclerViewType getRecyclerView(){ return recyclerView; }

    public void setStream(final @NonNull StreamAbstract<DataType, StreamAbstract.ReadResult<DataType>> stream,
                          boolean startLoading){
        recyclerView.setAdapter(new PagedAdapter<>(40, getRecyclerView()));
        recyclerView.setStream(stream, startLoading);
    }

    public PagedAdapter<ViewType, DataType> getAdapter(){
        return (PagedAdapter<ViewType, DataType>) recyclerView.getAdapter();
    }

    public void setSubtitle(@Nullable CharSequence text){
        subtitle = text;
        if(subtitleHeaderView != null)
            doSetSubtitle();
    }

    private void doSetSubtitle(){
        if(subtitle == null || subtitle == ""){
            subtitleHeaderView.setVisibility(View.GONE);
        }else{
            subtitleHeaderView.setVisibility(View.VISIBLE);
            subtitleHeaderView.setText(subtitle);
        }
    }

    @Override
    public Bundle saveState(){
        return recyclerView.saveState();
    }

    @Override
    public void restoreState(Bundle bundle){
        recyclerView.restoreState(bundle);
    }

    @Override
    public void onError(SpearError error){
        errorBanner.setOnEventListener(new MaterialBanner.OnEventListener(){
            @Override
            public void onPrimaryActionClick(){ errorBanner.hide();}
            @Override
            public void onSecondaryActionClick(){ errorBanner.hide();}
        });

        errorBanner.setIconResourceId(R.drawable.ic_regular_exclamation_circle);
        errorBanner.setHighlightColor(getContext().getResources().getColor(R.color.colorAccent, null));
        errorBanner.setText(error.getMessage());
        errorBanner.setPrimaryActionText("Dismiss");
        errorBanner.setSecondaryActionText(null);
        errorBanner.show();
    }

    public static final class CustomSwipeRefreshLayout extends SwipeRefreshLayout
            implements CoordinatorLayout.AttachedBehavior{

        public static final class Behavior extends CoordinatorLayout.Behavior<CustomSwipeRefreshLayout>{

            private float bannerY = 0;
            private float subtitleY = 0;
            private View bannerView;
            private View subtitleView;

            @Override
            public boolean layoutDependsOn(CoordinatorLayout parent, CustomSwipeRefreshLayout child, View dependency) {
                if(dependency instanceof MaterialBanner){
                    bannerView = dependency;
                    return true;
                }

                if(dependency.getId() == R.id.subtitle_label){
                    subtitleView = dependency;
                    return true;
                }

                return false;
            }

            @Override
            public boolean onDependentViewChanged(CoordinatorLayout parent, CustomSwipeRefreshLayout child, View dependency) {
                if(dependency == bannerView)
                    caclBannerTranslation();
                else if(dependency == subtitleView)
                    caclSubtitleTranslation();

                child.setTranslationY(Math.max(bannerY, subtitleY));
                return true;
            }

            private void caclBannerTranslation(){
                bannerY = Math.max(0, bannerView.getTranslationY() + bannerView.getHeight());
            }

            private void caclSubtitleTranslation(){
                subtitleY = Math.max(0, subtitleView.getTranslationY() + subtitleView.getHeight());
            }
        }

        public CustomSwipeRefreshLayout(Context context){ super(context); }
        public CustomSwipeRefreshLayout(Context context, AttributeSet attrs){ super(context, attrs); }
        @Override
        public @NonNull CoordinatorLayout.Behavior getBehavior(){ return new Behavior(); }
    }
}
