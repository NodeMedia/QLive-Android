package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

import xyz.tanwb.airship.R;

/**
 * IOS switch
 */
public class ToggleButton extends View {

    /**
     * 开启颜色
     */
    private int onColor = Color.parseColor("#4ebb7f");
    /**
     * 关闭颜色
     */
    private int offBorderColor = Color.parseColor("#dadbda");
    /**
     * 灰色带颜色
     */
    private int offColor = Color.parseColor("#ffffff");
    /**
     * 手柄颜色
     */
    private int spotColor = offColor;
    /**
     * 边框颜色
     */
    private int borderColor = offBorderColor;
    /**
     * 边框大小
     */
    private int borderWidth = 2;
    /**
     * 默认使用动画
     */
    private boolean defaultAnimate = true;
    /**
     * 是否默认处于打开状态
     */
    private boolean isDefaultOn;

    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 圆弧半径
     */
    private float radius;
    /**
     * 开关状态
     */
    private boolean toggleOn;
    /**
     * 垂直中心
     */
    private float centerY;
    /**
     * 按钮的开始和结束位置
     */
    private float startX;
    private float endX;
    /**
     * 手柄X位置的最小和最大值
     */
    private float spotMinX;
    private float spotMaxX;
    /**
     * 手柄大小
     */
    private int spotSize;
    /**
     * 手柄X位置
     */
    private float spotX;
    /**
     * 关闭时内部灰色带高度
     */
    private float offLineWidth;

    private SpringSystem springSystem;
    private Spring spring;

    private OnToggleChanged listener;

    SimpleSpringListener springListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            final double value = spring.getCurrentValue();
            calculateEffect(value);
        }
    };

    private ToggleButton(Context context) {
        super(context);
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(attrs);
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        spring.removeListener(springListener);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        spring.addListener(springListener);
    }

    public void setup(AttributeSet attrs) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setStrokeCap(Cap.ROUND);

        springSystem = SpringSystem.create();
        spring = springSystem.createSpring();
        spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(50, 7));

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggle(defaultAnimate);
            }
        });

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleButton);
        offBorderColor = typedArray.getColor(R.styleable.ToggleButton_tbOffBorderColor, offBorderColor);
        onColor = typedArray.getColor(R.styleable.ToggleButton_tbOnColor, onColor);
        spotColor = typedArray.getColor(R.styleable.ToggleButton_tbSpotColor, spotColor);
        offColor = typedArray.getColor(R.styleable.ToggleButton_tbOffColor, offColor);
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.ToggleButton_tbBorderWidth, borderWidth);
        defaultAnimate = typedArray.getBoolean(R.styleable.ToggleButton_tbAnimate, defaultAnimate);
        isDefaultOn = typedArray.getBoolean(R.styleable.ToggleButton_tbAsDefaultOn, isDefaultOn);
        typedArray.recycle();

        borderColor = offBorderColor;

        if (isDefaultOn) {
            toggleOn();
        }
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
        toggleOn = !toggleOn;
        takeEffect(animate);

        if (listener != null) {
            listener.onToggle(this, toggleOn);
        }
    }

    public void toggleOn() {
        setToggleOn();
        if (listener != null) {
            listener.onToggle(this, toggleOn);
        }
    }

    public void toggleOff() {
        setToggleOff();
        if (listener != null) {
            listener.onToggle(this, toggleOn);
        }
    }

    /**
     * 设置显示成打开样式，不会触发toggle事件
     */
    public void setToggleOn() {
        setToggleOn(true);
    }

    /**
     * @param animate asd
     */
    public void setToggleOn(boolean animate) {
        toggleOn = true;
        takeEffect(animate);
    }

    /**
     * 设置显示成关闭样式，不会触发toggle事件
     */
    public void setToggleOff() {
        setToggleOff(true);
    }

    public void setToggleOff(boolean animate) {
        toggleOn = false;
        takeEffect(animate);
    }

    private void takeEffect(boolean animate) {
        if (animate) {
            spring.setEndValue(toggleOn ? 1 : 0);
        } else {
            //这里没有调用spring，所以spring里的当前值没有变更，这里要设置一下，同步两边的当前值
            spring.setCurrentValue(toggleOn ? 1 : 0);
            calculateEffect(toggleOn ? 1 : 0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Resources r = Resources.getSystem();
        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED || heightSize == MeasureSpec.AT_MOST) {
            heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int width = getWidth();
        final int height = getHeight();

        radius = Math.min(width, height) * 0.5f;
        centerY = radius;
        startX = radius;
        endX = width - radius;
        spotMinX = startX + borderWidth;
        spotMaxX = endX - borderWidth;
        spotSize = height - 4 * borderWidth;
        spotX = toggleOn ? spotMaxX : spotMinX;
        offLineWidth = 0;
    }

    @Override
    public void draw(Canvas canvas) {

        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        paint.setColor(borderColor);
        canvas.drawRoundRect(rect, radius, radius, paint);

        if (offLineWidth > 0) {
            final float cy = offLineWidth * 0.5f;
            rect.set(spotX - cy, centerY - cy, endX + cy, centerY + cy);
            paint.setColor(offColor);
            canvas.drawRoundRect(rect, cy, cy, paint);
        }

        rect.set(spotX - 1 - radius, centerY - radius, spotX + 1.1f + radius, centerY + radius);
        paint.setColor(borderColor);
        canvas.drawRoundRect(rect, radius, radius, paint);

        final float spotR = spotSize * 0.5f;
        rect.set(spotX - spotR, centerY - spotR, spotX + spotR, centerY + spotR);
        paint.setColor(spotColor);
        canvas.drawRoundRect(rect, spotR, spotR, paint);
    }

    private void calculateEffect(final double value) {
        spotX = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, spotMinX, spotMaxX);

        offLineWidth = (float) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, 10, spotSize);

        int sr = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, Color.red(onColor), Color.red(offBorderColor));
        int sg = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, Color.green(onColor), Color.green(offBorderColor));
        int sb = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, Color.blue(onColor), Color.blue(offBorderColor));

        borderColor = Color.rgb(clamp(sr, 0, 255), clamp(sg, 0, 255), clamp(sb, 0, 255));

        postInvalidate();
    }

    private int clamp(int value, int low, int high) {
        return Math.min(Math.max(value, low), high);
    }

    public void setOnToggleChanged(OnToggleChanged onToggleChanged) {
        listener = onToggleChanged;
    }

    public boolean isAnimate() {
        return defaultAnimate;
    }

    public void setAnimate(boolean animate) {
        this.defaultAnimate = animate;
    }

    public interface OnToggleChanged {
        void onToggle(View view, boolean on);
    }

}
