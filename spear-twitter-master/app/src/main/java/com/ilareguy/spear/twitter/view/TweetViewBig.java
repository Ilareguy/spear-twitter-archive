package com.ilareguy.spear.twitter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.ilareguy.spear.EventOrigin;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.R;
import com.ilareguy.spear.twitter.TwitterUtils;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.User;
import com.ilareguy.spear.util.Helper;
import com.ilareguy.spear.util.LastTouchListenerHelper;
import com.ilareguy.spear.util.StringHelper;
import com.ilareguy.spear.util.ViewHelper;
import com.ilareguy.spear.view.CheckableImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A View containing the entirety of a tweet, including the author's thumbnail,
 * the tweet's contents, the toolbar, as well as the action button on the side.
 */
public class TweetViewBig extends TweetViewAbstract {

    private long userId = 0;
    private View retweetedByIcon;
    private ViewGroup quotedTweetFrame;
    private FrameLayout tweetCardFrame;
    private TextView timestamp;
    private TextView retweetedByText;
    private TextView authorDisplayName;
    private TextView authorUsername;
    private TextView likesCount;
    private TextView retweetsCount;
    private TwitterTextView textView;
    private SimpleDraweeView authorThumbnail;
    private ImageView badgeVerified;
    private ImageView badgeTranslator;
    private CheckableImageButton likeButton;
    private CheckableImageButton retweetButton;
    private CheckableImageButton replyButton;
    private CheckableImageButton quoteButton;
    private CheckableImageButton shareButton;

    private @Nullable TweetMediasCard mediasCard = null;

    public TweetViewBig(Context context) {
        this(context, null);
    }

    public TweetViewBig(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TweetViewBig(Context context, AttributeSet attrs, int d) {
        super(context, attrs, d);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.tweet_view_big, this, true);
        final int padding = (int) Helper.dpToPx(8, getResources());
        setPadding(0, padding, 0, padding);
    }

    /**
     * If you create a new TweetViewSmall manually (i.e. new()), you need to call this
     * once for the view to work as expected.
     */
    @Override
    public void onFinishInflate(){
        textView = findViewById(R.id.contents);
        timestamp = findViewById(R.id.timestamp_text);
        quotedTweetFrame = findViewById(R.id.quoted_tweet_frame);
        retweetedByIcon = findViewById(R.id.retweeted_by_icon);
        retweetedByText = findViewById(R.id.retweeted_by_text);
        tweetCardFrame = findViewById(R.id.card_frame);
        authorDisplayName = findViewById(R.id.author_display_name);
        authorUsername = findViewById(R.id.author_username);
        authorThumbnail = findViewById(R.id.author_thumbnail);
        badgeVerified = findViewById(R.id.badge_verified);
        badgeTranslator = findViewById(R.id.badge_translator);
        likesCount = findViewById(R.id.likes_count);
        retweetsCount = findViewById(R.id.retweets_count);
        likeButton = findViewById(R.id.like_button);
        retweetButton = findViewById(R.id.retweet_button);
        replyButton = findViewById(R.id.reply_button);
        quoteButton = findViewById(R.id.quote_button);
        shareButton = findViewById(R.id.share_button);

        // Hook listeners
        authorDisplayName.setOnClickListener(this::onUserOverviewClick);
        authorUsername.setOnClickListener(this::onUserOverviewClick);
        authorThumbnail.setOnClickListener(this::onUserOverviewClick);

        LastTouchListenerHelper.listen(this);
        this.setOnClickListener((View view) ->
                MainActivity.getInstance().viewTweet(getTweet().getId(),
                        new EventOrigin(LastTouchListenerHelper.getLastTouchXY()))
        );

        likeButton.setOnCheckedStateChangeListener(
                (final CheckableImageButton button, boolean checked) -> {
                    return false;
                }
        );

        retweetButton.setOnCheckedStateChangeListener(
                (final CheckableImageButton button, boolean checked) -> {
                    return false;
                }
        );

        replyButton.setOnCheckedStateChangeListener(
                (final CheckableImageButton button, boolean checked) -> {
                    return false;
                }
        );

        quoteButton.setOnCheckedStateChangeListener(
                (final CheckableImageButton button, boolean checked) -> {
                    return false;
                }
        );

        shareButton.setOnCheckedStateChangeListener(
                (final CheckableImageButton button, boolean checked) -> {
                    return false;
                }
        );

        ViewHelper.applyCardShapeTo(quotedTweetFrame);
        super.onFinishInflate();
    }

    private void onUserOverviewClick(View v){
        MainActivity.getInstance().viewUser(userId, null);
    }

    @Override
    public void setTweet(Tweet tweet) {
        if(tweet.isRetweet()){
            super.setTweet(tweet.getRetweetedTweet());
            setIsRetweet(tweet,true);
            doSetTweet();
            return;
        }

        super.setTweet(tweet);
        setIsRetweet(null,false);
        doSetTweet();
    }

    private void doSetTweet(){
        final Tweet tweet = getTweet();

        likesCount.setText(StringHelper.numberWithSuffix(tweet.getFavoriteCount()));
        retweetsCount.setText(StringHelper.numberWithSuffix(tweet.getRetweetCount()));
        textView.setTextWithEntities(tweet.getText(), tweet.getEntities());
        timestamp.setText(TwitterUtils.dateToShortString(TwitterUtils.twitterTimestampToDate(tweet.getCreatedAt())));
        likeButton.setCheckedSilent(tweet.isLiked());
        retweetButton.setCheckedSilent(tweet.isRetweeted());

        setUserOverview(tweet.getAuthor());
        processCard();
        processQuotedTweet();
    }

    private void setUserOverview(@NonNull User user){
        this.userId = user.getUid();

        authorUsername.setText("@" + user.getUsername());
        authorDisplayName.setText(user.getDisplay_name());
        badgeVerified.setVisibility(user.getVerified() ? VISIBLE : GONE);
        badgeTranslator.setVisibility(user.getTranslator() ? VISIBLE : GONE);
        authorThumbnail.setImageURI(user.getThumbnailUrl());
    }

    private void processCard(){
        Tweet.Entities tweet_entities = getTweet().getEntities();
        tweetCardFrame.removeAllViews();

        // If there is at least one media, show that
        if(tweet_entities.getMedias().size() > 0){
            // TODO: 2018-08-30 Stop calling new() every time this is needed. Keep one in memory and lazy-new() it when required
            mediasCard = new TweetMediasCard(getContext(), tweet_entities.getMedias());
            tweetCardFrame.setVisibility(View.VISIBLE);
            tweetCardFrame.addView(mediasCard);
        }else if(tweet_entities.getUrls().size() > 0){
            tweetCardFrame.setVisibility(View.VISIBLE);
            // @TODO: Show URL
        }else{
            // Remove card
            tweetCardFrame.setVisibility(View.GONE);
        }
    }

    private void processQuotedTweet(){
        final Tweet quoted_tweet = getTweet().getQuotedTweet();
        if (quoted_tweet != null) {
            quotedTweetFrame.removeAllViews();
            TweetViewSmall new_tweet_view = new TweetViewSmall(getContext(), null, R.style.SelectableItemForeground);
            new_tweet_view.onFinishInflate();
            new_tweet_view.setTweet(quoted_tweet, true);
            new_tweet_view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            quotedTweetFrame.addView(new_tweet_view);

            LastTouchListenerHelper.listen(new_tweet_view);
            new_tweet_view.setOnClickListener((View v) ->
                MainActivity.getInstance().viewTweet(quoted_tweet.getId(),
                        new EventOrigin(LastTouchListenerHelper.getLastTouchXY()))
            );

            quotedTweetFrame.setVisibility(VISIBLE);
        } else {
            quotedTweetFrame.setVisibility(GONE);
            quotedTweetFrame.removeAllViews();
        }
    }

    private void setIsRetweet(@Nullable Tweet parent_tweet, boolean is_retweet){
        retweetedByIcon.setVisibility(is_retweet ? VISIBLE : GONE);
        retweetedByText.setVisibility(is_retweet ? VISIBLE : GONE);
        if(is_retweet && parent_tweet != null){
            retweetedByText.setText(parent_tweet.getAuthor().getDisplay_name());
        }
    }
}
