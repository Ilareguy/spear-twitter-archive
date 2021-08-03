package com.ilareguy.spear.twitter.page;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data_stream.HomeFeedStream;
import com.ilareguy.spear.twitter.view.GenericStreamRecyclerViewAbstract;
import com.ilareguy.spear.twitter.view.TweetRecyclerView;
import com.ilareguy.spear.twitter.view.TweetViewBig;
import com.ilareguy.spear.view.BackdropLayout;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeFeed extends PageAbstract {
    public HomeFeed(Context context){
        super(context);
        setTitle("Home Feed");
    }

    @Override
    protected  @NonNull PageAbstract.Variant<?> onCreateVariant(ActivityAbstract.VariantType type,
                                                                ActivityAbstract.Orientation orientation){
        return new MobileVariant(this);
    }

    private final class MobileVariant extends PageAbstract.Variant<BackdropLayout> implements Variant.AsyncLifecycle{
        MobileVariant(PageAbstract page){
            super(page);
            // Create stream, which will be required in async call load()
            stream = new HomeFeedStream(HomeFeed.this, TwitterApplication.getTwitterInstance().getCurrentLogonUser());
        }

        private final HomeFeedStream stream;

        private HomeFeedStreamRecyclerView streamRecyclerView;

        private @Nullable List<Tweet> restoredTweets = null;
        private @Nullable Bundle recyclerViewState = null;

        @Override
        protected @NonNull View inflate(LayoutInflater inflater){
            // Create layout
            final BackdropLayout root_view = new BackdropLayout(getContext());

            // Inflate
            inflater.inflate(R.layout.page_home_feed, root_view, true);
            root_view.onFinishInflate();

            return root_view;
        }

        @Override
        protected void initializeLayout(){
            streamRecyclerView = getRootView().findViewById(R.id.stream_recycler_view);
            streamRecyclerView.setStream(stream, (restoredTweets == null));

            if(restoredTweets != null)
                streamRecyclerView.getAdapter().setDataset(restoredTweets);

            if(recyclerViewState != null){
                streamRecyclerView.restoreState(recyclerViewState);
                recyclerViewState = null;
            }
        }

        @Override
        public void load(@Nullable Bundle savedState){
            if(savedState != null){
                // Restore the previous dataset
                restoredTweets = stream.loadTweets(savedState.getLong("DATASET_FIRST_TWEET_ID"),
                        savedState.getInt("DATASET_SIZE"));
            }
        }

        @Override public void save(){}

        @Override
        public Bundle saveState(){
            return streamRecyclerView.saveState();
        }

        @Override
        public void restoreState(Bundle bundle){
            recyclerViewState = bundle;
        }
    }

    public static final class HomeFeedStreamRecyclerView
            extends GenericStreamRecyclerViewAbstract<TweetViewBig, TweetRecyclerView, Tweet>{

        public HomeFeedStreamRecyclerView(Context context){ super(context); }
        public HomeFeedStreamRecyclerView(Context context, AttributeSet attrs){ super(context, attrs); }
        public HomeFeedStreamRecyclerView(Context context, AttributeSet attrs, int d){ super(context, attrs, d); }

        @Override
        protected @NonNull TweetRecyclerView inflateRecyclerView(ViewStub stub){
            stub.setLayoutResource(R.layout.tweet_recycler_view);
            TweetRecyclerView view = (TweetRecyclerView) stub.inflate();
            //view.setOnRecyclerEventListener(this);
            return view;
        }

        @Override
        public Bundle saveState(){
            // Save the current dataset's state
            final Bundle bundle = super.saveState();
            final LinkedList<Tweet> dataset = getAdapter().getRawDataset();
            if(dataset.size() == 0) return bundle;
            bundle.putLong("DATASET_FIRST_TWEET_ID", dataset.getFirst().getId());
            bundle.putInt("DATASET_SIZE", dataset.size());
            return bundle;
        }
    }
}
