package com.ilareguy.spear.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ilareguy.spear.util.Helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/**
 * A quick implementation of the BackdropLayer component as described in the Material Guidelines:
 * https://material.io/design/components/backdrop.html
 */
public class BackdropLayout extends RelativeLayout {
    public BackdropLayout(Context c){ super(c); init(c, null); }
    public BackdropLayout(Context c, AttributeSet a){ super(c, a); init(c, a); }
    public BackdropLayout(Context c, AttributeSet a, int d){ super(c, a, d); init(c, a); }

    public enum LayerType{
        BACK_LAYER,
        FRONT_LAYER
    }

    /**
     * The amount of milliseconds needed for an expand animation to finish. You can use this to
     * synchronize your custom animations with the BackdropLayout's expand animation.
     */
    public static final int EXPAND_ANIMATION_TIME = 250;
    public static final int BACK_LAYER_ELEVATION = 5;
    public static final int FRONT_LAYER_ELEVATION = 10;

    private static ViewOutlineProvider frontLayerWrapperOutlineProvider = null;
    private static Interpolator expandAnimationInterpolator = null;

    private final View[] layers = new View[LayerType.values().length];

    private @Nullable Animator expandAnimator = null;

    private int backLayerHeight;
    private int previousFrontLayerY;
    private FrameLayout frontLayerWrapper;

    private void init(Context context, @Nullable AttributeSet attrs){
        initFrontLayerWrapper(context);
    }

    @Override
    public void onFinishInflate(){
        super.onFinishInflate();

        // There should only be two views in this layout: the back and front layers.
        if(getChildCount() != 2)
            throw new RuntimeException("A BackdropLayout should have two children: "
                    + "a back layer and a front layer.");

        // Find the two children
        final View back_layer = getChildAt(0);
        final View front_layer = getChildAt(1);
        layers[LayerType.BACK_LAYER.ordinal()] = back_layer;
        layers[LayerType.FRONT_LAYER.ordinal()] = front_layer;

        // Add the front layer into the wrapper
        ((ViewGroup) front_layer.getParent()).removeView(front_layer);
        frontLayerWrapper.addView(front_layer);

        // Add the front layer wrapper to the layout
        addView(frontLayerWrapper);

        // Set properties
        setElevation(BACK_LAYER_ELEVATION - 1);
        back_layer.setElevation(BACK_LAYER_ELEVATION);
        frontLayerWrapper.setElevation(FRONT_LAYER_ELEVATION);
        frontLayerWrapper.setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /**
     * Sets the given view as the current back layer. This will expand the front layer just enough
     * for the back layer to be fully visible.
     * @param view The view to display in the back layer. It must be inflated and ready to be
     *             displayed. It must not have a parent.
     */
    public void setBackLayer(@NonNull View view){
        // Save the previous back layer's height for animation purposes
        final View current_back_layer = getBackLayer();
        previousFrontLayerY = (current_back_layer == null)
                ? 0
                : current_back_layer.getHeight();

        // Remove the previous back layer
        if(current_back_layer != null) removeViewAt(0);

        // Add the new view
        layers[LayerType.BACK_LAYER.ordinal()] = view;
        addView(view, 0);

        // Set the new back layer's properties
        view.setElevation(BACK_LAYER_ELEVATION);

        // Expand appropriately
        expand();
    }

    /**
     * Sets the given view as the current front layer.
     * @param view The view to display in the front layer. It must be inflated and ready to be
     *             displayed. It must not have a parent.
     */
    public void setFrontLayer(@NonNull View view){
        final View front_layer = getFrontLayer();

        // Remove the previous front layer
        if(front_layer != null) removeViewAt(0);

        // Add the new front layer
        addView(view, 1);

        // Set the new layer's properties
        view.setElevation(FRONT_LAYER_ELEVATION);
    }

    /**
     * Sets the focus on either the back or the front layer. If the focus is set the on back layer,
     * then the front layer will have a scrim applied to it and it won't receive touch events
     * until it comes back into focus.
     * @param layer The layer to focus.
     */
    public void setLayerFocus(LayerType layer){
        // @TODO
    }

    /**
     * Tells the BackdropLayout that the back layer has changed (or visually updated), which will
     * cause the front layer to expand accordingly for the front layer to be fully visible again.
     */
    public void notifyBackLayerChanged(){
        expand();
    }

    public final View getBackLayer(){ return getLayer(LayerType.BACK_LAYER); }
    public final View getFrontLayer(){ return getLayer(LayerType.FRONT_LAYER); }
    public final View getLayer(LayerType l){ return layers[l.ordinal()]; }





    private void expand(){
        final View back_layer = getBackLayer();

        if(back_layer == null){
            // There is currently no back layer; expand now
            backLayerHeight = 0;
            doExpand();
            return;
        }

        // Expand when we figure out the height of the back layer
        back_layer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                back_layer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                backLayerHeight = back_layer.getHeight();
                doExpand();
            }
        });
    }

    private void doExpand(){
        // When this method is invoked, backLayerHeight should contain a valid value.
        // Stop previous animation if it's still running
        if(expandAnimator != null) expandAnimator.cancel();

        // Build and run the new expand animation
        previousFrontLayerY = (int) frontLayerWrapper.getY();
        buildExpandAnimator();
        expandAnimator.start();
    }

    private void buildExpandAnimator(){
        expandAnimator = ObjectAnimator.ofFloat(frontLayerWrapper, "y",
                previousFrontLayerY, backLayerHeight);
        expandAnimator.setDuration(EXPAND_ANIMATION_TIME);
        expandAnimator.setInterpolator(getExpandAnimationInterpolator());
    }

    private void initFrontLayerWrapper(Context context){
        frontLayerWrapper = new FrameLayout(context);
        frontLayerWrapper.setLayoutParams(buildNewFrontLayerWrapperLayoutParams());
        frontLayerWrapper.setBackgroundColor(context.getResources().getColor(android.R.color.white, null));
        applyBackdropLayerShapeTo(frontLayerWrapper);
    }

    private static ViewOutlineProvider initFrontLayerOutlineProvider(Resources res){
        frontLayerWrapperOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 1, view.getWidth(), view.getHeight() + 35,
                        Helper.dpToPx(16, res));
            }
        };

        return frontLayerWrapperOutlineProvider;
    }

    private void replaceCurrentFrontLayer(View newFrontLayer){
        //final View current_front_layer = layers[LayerType.FRONT_LAYER.ordinal()]; // Can be null
        layers[LayerType.FRONT_LAYER.ordinal()] = newFrontLayer;
        frontLayerWrapper.removeAllViews();
        frontLayerWrapper.addView(newFrontLayer);
    }

    private static FrameLayout.LayoutParams buildNewFrontLayerWrapperLayoutParams(){
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }

    private static ViewOutlineProvider getBackdropFrontLayerOutlineProvider(Resources res){
        return (frontLayerWrapperOutlineProvider == null)
                ? initFrontLayerOutlineProvider(res)
                : frontLayerWrapperOutlineProvider;
    }

    private static Interpolator getExpandAnimationInterpolator(){
        if(expandAnimationInterpolator == null)
            expandAnimationInterpolator = new FastOutSlowInInterpolator();
        return expandAnimationInterpolator;
    }

    public static void applyBackdropLayerShapeTo(View victim){
        victim.setOutlineProvider(getBackdropFrontLayerOutlineProvider(victim.getResources()));
        victim.setClipToOutline(true);
    }

    @Override
    public Parcelable onSaveInstanceState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("backLayerHeight", backLayerHeight);
        bundle.putInt("previousFrontLayerY", previousFrontLayerY);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state){
        if(state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            backLayerHeight = bundle.getInt("backLayerHeight");
            previousFrontLayerY = bundle.getInt("previousFrontLayerY");
            state = bundle.getParcelable("superState");

            frontLayerWrapper.setY(backLayerHeight);
        }

        super.onRestoreInstanceState(state);
    }
}
