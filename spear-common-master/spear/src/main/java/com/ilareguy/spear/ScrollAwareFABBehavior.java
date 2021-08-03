package com.ilareguy.spear;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior{

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild, @NonNull View target,
                                       @ViewCompat.ScrollAxis int axes, @ViewCompat.NestedScrollType int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        axes, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton button,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        super.onNestedScroll(coordinatorLayout, button, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, type);

        if (dyConsumed > 0 && button.getVisibility() == View.VISIBLE) {
            /*
             * Because just calling hide() wouldn't work... Android's absolutely glorious design decisions
             * strike again. The default implementation of hide() will set the FAB's visibility to
             * View.GONE, in which case the owning CoordinatorLayout won't recognize the FAB as part
             * of the layout anymore thus preventing onNestedScroll() from ever being called again and
             * showing the button back.
             */
            button.hide(visibilityChangedListener);
        } else if (dyConsumed < 0 && button.getVisibility() != View.VISIBLE) {
            button.show();
        }
    }

    private FloatingActionButton.OnVisibilityChangedListener visibilityChangedListener =
            new FloatingActionButton.OnVisibilityChangedListener() {
        @Override
        public void onShown(FloatingActionButton fab) {
            super.onShown(fab);
        }

        @Override
        public void onHidden(FloatingActionButton fab) {
            super.onHidden(fab);
            fab.setVisibility(View.INVISIBLE);
        }
    };

}
