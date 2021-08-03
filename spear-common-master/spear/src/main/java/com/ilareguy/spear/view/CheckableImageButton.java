package com.ilareguy.spear.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ilareguy.spear.R;
import com.ilareguy.spear.util.Utils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

public class CheckableImageButton extends FrameLayout{
    public interface OnCheckedStateChangeListener{
        /**
         * @return Your implementation must return true if you want to proceed with the [un]checking,
         *         or false to consume the event and cancel it.
         */
        boolean onCheckedStateChange(CheckableImageButton view, boolean checked);
    }

    public CheckableImageButton(Context c){ super(c); init(c, null); }
    public CheckableImageButton(Context c, AttributeSet a){ super(c, a); init(c, a); }
    public CheckableImageButton(Context c, AttributeSet a, int d){ super(c, a, d); init(c, a); }

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR
            = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR
            = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR
            = new OvershootInterpolator(4);

    private boolean checked = false, checkable = true, shouldAnimate = true;
    private @ColorInt int regularColor = 0, checkedColor = 0;
    private @Nullable OnCheckedStateChangeListener onCheckedStateChangeListener = null;
    private @Nullable View.OnClickListener onClickListener = null;

    private ImageView iconView;
    private DotsView dotsView;
    private CircleView circleView;

    private void init(final Context context, final @Nullable AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.checkable_image_button, this, true);

        iconView = findViewById(R.id.image);
        dotsView = findViewById(R.id.dots);
        circleView = findViewById(R.id.circle);
        final FrameLayout iconFrame = findViewById(R.id.icon_frame);

        iconFrame.setClickable(true);
        iconFrame.setFocusable(true);
        iconFrame.setOnClickListener(this::onImageClick);

        // Set attributes
        if(attrs != null) initAttributes(attrs);
    }

    private void initAttributes(final @Nullable AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CheckableImageButton,
                0, 0);

        try {
            iconView.setImageResource(a.getResourceId(R.styleable.CheckableImageButton_src, 0));
            regularColor = a.getColor(R.styleable.CheckableImageButton_regularColor, 0);
            checkedColor = a.getColor(R.styleable.CheckableImageButton_checkedColor, 0);
            setChecked(a.getBoolean(R.styleable.CheckableImageButton_checked, false));
            setCheckable(a.getBoolean(R.styleable.CheckableImageButton_checkable, true));
            setAnimationsEnabled(a.getBoolean(R.styleable.CheckableImageButton_animate, true));
        } finally {
            a.recycle();
        }
    }

    public void setOnCheckedStateChangeListener(@Nullable OnCheckedStateChangeListener l){
        this.onCheckedStateChangeListener = l;
    }

    public void setChecked(boolean c){
        if(onCheckedStateChangeListener != null
                && !onCheckedStateChangeListener.onCheckedStateChange(this, !checked)){
            // Event consumed; don't change the state of the button
            return;
        }

        doSetChecked(c);
        if(c && shouldAnimate){
            _animate();
        }
    }

    public void setAnimationsEnabled(boolean a){
        this.shouldAnimate = a;
    }

    /**
     * Like setChecked(), but no callback will be invoked.
     */
    public void setCheckedSilent(boolean c){
        doSetChecked(c);
    }

    private void doSetChecked(boolean c){
        checked = c;
        iconView.setColorFilter(isChecked() ? checkedColor : regularColor);
    }

    private void onImageClick(View var1){
        setChecked(!checked);

        if(onClickListener != null)
            onClickListener.onClick(this);
    }

    private void _animate(){
        iconView.animate().cancel();
        iconView.setScaleX(0);
        iconView.setScaleY(0);
        circleView.setInnerCircleRadiusProgress(0);
        circleView.setOuterCircleRadiusProgress(0);
        dotsView.setCurrentProgress(0);

        final AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        outerCircleAnimator.setDuration(250);
        outerCircleAnimator.setInterpolator(DECELERATE_INTERPOLATOR);

        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        innerCircleAnimator.setDuration(200);
        innerCircleAnimator.setStartDelay(200);
        innerCircleAnimator.setInterpolator(DECELERATE_INTERPOLATOR);

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(iconView, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(350);
        starScaleYAnimator.setStartDelay(250);
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(iconView, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(350);
        starScaleXAnimator.setStartDelay(250);
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(dotsView, DotsView.DOTS_PROGRESS, 0, 1f);
        dotsAnimator.setDuration(900);
        dotsAnimator.setStartDelay(50);
        dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                starScaleXAnimator,
                dotsAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                circleView.setInnerCircleRadiusProgress(0);
                circleView.setOuterCircleRadiusProgress(0);
                dotsView.setCurrentProgress(0);
                iconView.setScaleX(1);
                iconView.setScaleY(1);
            }
        });

        animatorSet.start();
    }

    @Override
    public final void setOnClickListener(View.OnClickListener onClickListener){ this.onClickListener = onClickListener; }
    public final boolean isChecked(){ return checkable && checked; }
    public final boolean isCheckable(){ return checkable; }
    public final void setCheckable(boolean c){
        this.checkable = c;
        if(checked)
            doSetChecked(false);
    }

    public static final class DotsView extends View{
        private static final int DOTS_COUNT = 7;
        private static final int OUTER_DOTS_POSITION_ANGLE = 360 / DOTS_COUNT;

        private static final int COLOR_1 = 0xFFFFC107;
        private static final int COLOR_2 = 0xFFFF9800;
        private static final int COLOR_3 = 0xFFFF5722;
        private static final int COLOR_4 = 0xFFF44336;

        private final Paint[] circlePaints = new Paint[4];

        private int centerX;
        private int centerY;

        private float maxOuterDotsRadius;
        private float maxInnerDotsRadius;
        private float maxDotSize;

        private float currentProgress = 0;

        private float currentRadius1 = 0;
        private float currentDotSize1 = 0;

        private float currentDotSize2 = 0;
        private float currentRadius2 = 0;

        private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        public DotsView(Context context) {
            super(context);
            init();
        }

        public DotsView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DotsView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public DotsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init();
        }

        private void init() {
            for (int i = 0; i < circlePaints.length; i++) {
                circlePaints[i] = new Paint();
                circlePaints[i].setStyle(Paint.Style.FILL);
                //setLayerType(LAYER_TYPE_HARDWARE, circlePaints[i]);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            centerX = w / 2;
            centerY = h / 2;
            maxDotSize = 12;
            maxOuterDotsRadius = w / 2 - maxDotSize * 2;
            maxInnerDotsRadius = 0.8f * maxOuterDotsRadius;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            drawOuterDotsFrame(canvas);
            drawInnerDotsFrame(canvas);
        }

        private void drawOuterDotsFrame(Canvas canvas) {
            for (int i = 0; i < DOTS_COUNT; i++) {
                int cX = (int) (centerX + currentRadius1 * Math.cos(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180));
                int cY = (int) (centerY + currentRadius1 * Math.sin(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180));
                canvas.drawCircle(cX, cY, currentDotSize1, circlePaints[i % circlePaints.length]);
            }
        }

        private void drawInnerDotsFrame(Canvas canvas) {
            for (int i = 0; i < DOTS_COUNT; i++) {
                int cX = (int) (centerX + currentRadius2 * Math.cos((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
                int cY = (int) (centerY + currentRadius2 * Math.sin((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
                canvas.drawCircle(cX, cY, currentDotSize2, circlePaints[(i + 1) % circlePaints.length]);
            }
        }

        public void setCurrentProgress(float currentProgress) {
            this.currentProgress = currentProgress;

            updateInnerDotsPosition();
            updateOuterDotsPosition();
            updateDotsPaints();
            updateDotsAlpha();

            postInvalidate();
        }

        public float getCurrentProgress() {
            return currentProgress;
        }

        private void updateInnerDotsPosition() {
            if (currentProgress < 0.3f) {
                this.currentRadius2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0, 0.3f, 0.f, maxInnerDotsRadius);
            } else {
                this.currentRadius2 = maxInnerDotsRadius;
            }

            if (currentProgress < 0.2) {
                this.currentDotSize2 = maxDotSize;
            } else if (currentProgress < 0.5) {
                this.currentDotSize2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.2f, 0.5f, maxDotSize, 0.3 * maxDotSize);
            } else {
                this.currentDotSize2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.5f, 1f, maxDotSize * 0.3f, 0);
            }

        }

        private void updateOuterDotsPosition() {
            if (currentProgress < 0.3f) {
                this.currentRadius1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.0f, 0.3f, 0, maxOuterDotsRadius * 0.8f);
            } else {
                this.currentRadius1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.3f, 1f, 0.8f * maxOuterDotsRadius, maxOuterDotsRadius);
            }

            if (currentProgress < 0.7) {
                this.currentDotSize1 = maxDotSize;
            } else {
                this.currentDotSize1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.7f, 1f, maxDotSize, 0);
            }
        }

        private void updateDotsPaints() {
            if (currentProgress < 0.5f) {
                float progress = (float) Utils.mapValueFromRangeToRange(currentProgress, 0f, 0.5f, 0, 1f);
                circlePaints[0].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_1, COLOR_2));
                circlePaints[1].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_2, COLOR_3));
                circlePaints[2].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_3, COLOR_4));
                circlePaints[3].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_4, COLOR_1));
            } else {
                float progress = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.5f, 1f, 0, 1f);
                circlePaints[0].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_2, COLOR_3));
                circlePaints[1].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_3, COLOR_4));
                circlePaints[2].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_4, COLOR_1));
                circlePaints[3].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_1, COLOR_2));
            }
        }

        private void updateDotsAlpha() {
            float progress = (float) Utils.clamp(currentProgress, 0.6f, 1f);
            int alpha = (int) Utils.mapValueFromRangeToRange(progress, 0.6f, 1f, 255, 0);
            circlePaints[0].setAlpha(alpha);
            circlePaints[1].setAlpha(alpha);
            circlePaints[2].setAlpha(alpha);
            circlePaints[3].setAlpha(alpha);
        }

        public static final Property<DotsView, Float> DOTS_PROGRESS = new Property<DotsView, Float>(Float.class, "dotsProgress") {
            @Override
            public Float get(DotsView object) {
                return object.getCurrentProgress();
            }

            @Override
            public void set(DotsView object, Float value) {
                object.setCurrentProgress(value);
            }
        };
    }

    public static final class CircleView extends View{
        private static final int START_COLOR = 0xFFFF5722;
        private static final int END_COLOR = 0xFFFFC107;

        private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        private Paint circlePaint = new Paint();
        private Paint maskPaint = new Paint();

        private Bitmap tempBitmap;
        private Canvas tempCanvas;

        private float outerCircleRadiusProgress = 0f;
        private float innerCircleRadiusProgress = 0f;

        private int maxCircleSize;

        public CircleView(Context context) {
            super(context);
            init();
        }

        public CircleView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init();
        }

        private void init() {
            circlePaint.setStyle(Paint.Style.FILL);
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            maxCircleSize = w / 2;
            tempBitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
            tempCanvas = new Canvas(tempBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            tempCanvas.drawColor(0xffffff, PorterDuff.Mode.CLEAR);
            tempCanvas.drawCircle(getWidth() / 2, getHeight() / 2, outerCircleRadiusProgress * maxCircleSize, circlePaint);
            tempCanvas.drawCircle(getWidth() / 2, getHeight() / 2, innerCircleRadiusProgress * maxCircleSize, maskPaint);
            canvas.drawBitmap(tempBitmap, 0, 0, null);
        }

        void setInnerCircleRadiusProgress(float innerCircleRadiusProgress) {
            this.innerCircleRadiusProgress = innerCircleRadiusProgress;
            postInvalidate();
        }

        float getInnerCircleRadiusProgress() {
            return innerCircleRadiusProgress;
        }

        void setOuterCircleRadiusProgress(float outerCircleRadiusProgress) {
            this.outerCircleRadiusProgress = outerCircleRadiusProgress;
            updateCircleColor();
            postInvalidate();
        }

        private void updateCircleColor() {
            float colorProgress = (float) Utils.clamp(outerCircleRadiusProgress, 0.5, 1);
            colorProgress = (float) Utils.mapValueFromRangeToRange(colorProgress, 0.5f, 1f, 0f, 1f);
            this.circlePaint.setColor((Integer) argbEvaluator.evaluate(colorProgress, START_COLOR, END_COLOR));
        }

        float getOuterCircleRadiusProgress() {
            return outerCircleRadiusProgress;
        }

        private static final Property<CircleView, Float> INNER_CIRCLE_RADIUS_PROGRESS =
                new Property<CircleView, Float>(Float.class, "innerCircleRadiusProgress") {
                    @Override
                    public Float get(CircleView object) {
                        return object.getInnerCircleRadiusProgress();
                    }

                    @Override
                    public void set(CircleView object, Float value) {
                        object.setInnerCircleRadiusProgress(value);
                    }
                };

        private static final Property<CircleView, Float> OUTER_CIRCLE_RADIUS_PROGRESS =
                new Property<CircleView, Float>(Float.class, "outerCircleRadiusProgress") {
                    @Override
                    public Float get(CircleView object) {
                        return object.getOuterCircleRadiusProgress();
                    }

                    @Override
                    public void set(CircleView object, Float value) {
                        object.setOuterCircleRadiusProgress(value);
                    }
                };
    }
}
