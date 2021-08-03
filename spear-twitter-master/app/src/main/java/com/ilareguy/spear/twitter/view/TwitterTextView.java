package com.ilareguy.spear.twitter.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ilareguy.spear.EventOrigin;
import com.ilareguy.spear.twitter.MainActivity;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.data.Tweet;
import com.ilareguy.spear.twitter.data.TweetEntityAbstract;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Represents the contents of a tweet only (text). Links, usernames, and hashtags are
 * highlighted and clickable.
 */
public class TwitterTextView extends AppCompatTextView {

    private SpannableStringBuilder text;
    private float[] lastTouchXY = new float[2];

    public TwitterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        text = new SpannableStringBuilder("");

        setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTouchXY[0] = event.getRawX();
                lastTouchXY[1] = event.getRawY();
            }

            v.performClick();
            return false;
        });
    }

    public void setText(String text){
        setTextWithEntities(text, null);
    }

    public void setTextWithEntities(String text, @Nullable final Tweet.Entities provided_entities) {
        this.text.clear();
        this.text.clearSpans();

        if(!text.equals("")){
            setVisibility(View.VISIBLE);
            this.text.append(text);
            Tweet.Entities entities = (provided_entities == null ?
                    TweetEntityAbstract.getEntities(text) :
                    provided_entities);

            setExtractedEntities(entities.getExtractedEntities());
            setUrlEntities(entities.getUrls());
        }else
            setVisibility(View.GONE);

        super.setText(this.text);
    }

    private void setExtractedEntities(List<Tweet.Entities.ExtractedEntity> entities){
        for(final Tweet.Entities.ExtractedEntity entity : entities){
            setSpan(new ClickableSpan(){
                @Override
                public void onClick(View view){
                    switch(entity.getType()){
                        case MENTION:
                            MainActivity.getInstance().viewUser(entity.value, new EventOrigin(lastTouchXY));
                            return;
                        case CASHTAG:
                        case HASHTAG:
                            MainActivity.getInstance().viewHashtag(entity.value, new EventOrigin(lastTouchXY));
                    }
                }
            },
            entity);
        }
    }

    private void setUrlEntities(List<Tweet.Entities.UrlEntity> entities){
        for(final Tweet.Entities.UrlEntity entity : entities){
            setSpan(new ClickableSpan(){
                @Override
                public void onClick(View view){
                    MainActivity.getInstance().viewURL(entity.expandedUrl, new EventOrigin(lastTouchXY));
                }
            },
            entity);
        }
    }

    private void setSpan(ClickableSpan clickableSpan, TweetEntityAbstract entity){
        if(entity.indexStart < 0 || entity.indexEnd > text.length()){
            // Error!
            TwitterApplication._e("Invalid span!");
            return;
        }

        text.setSpan(clickableSpan, entity.indexStart, entity.indexEnd, 0);
        text.setSpan(new ForegroundColorSpan(Color.argb(255, 29, 161, 242)),
                entity.indexStart, entity.indexEnd, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Check if a ClickableSpan was touched
        Layout layout = getLayout();

        if (layout != null && text != null) {
            int line = layout.getLineForVertical((int) event.getY());
            int offset = layout.getOffsetForHorizontal(line, event.getX());

            ClickableSpan[] links = text.getSpans(offset, offset, ClickableSpan.class);

            if (links.length > 0) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    links[0].onClick(this);
                } else {
                    return super.onTouchEvent(event);
                }
            }
        }

        return super.onTouchEvent(event);
    }

}
