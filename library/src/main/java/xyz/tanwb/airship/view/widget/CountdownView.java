package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.R;

public class CountdownView extends View implements Runnable {

    private float mRadius = (int) getResources().getDimension(R.dimen.dp_4);
    private float mPadding = (int) getResources().getDimension(R.dimen.dp_4);
    private float mTextSize = (int) getResources().getDimension(R.dimen.sp_12);
    private int mTextColor = Color.WHITE;
    private int mBackGround = 0xFFE60012;
    private float mSpaceWidth = (int) getResources().getDimension(R.dimen.dp_10);
    private int mSpaceTextColor = 0xFF4A4A4A;

    private Paint paint;

    private int[] times;
    private boolean run;

    private RectF bgRectF;
    private int textBaseline;

    public CountdownView(Context context) {
        super(context);
        initView(context, null);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CountdownView);
            mRadius = array.getDimension(R.styleable.CountdownView_cvRadius, mRadius);
            mPadding = array.getDimension(R.styleable.CountdownView_cvPadding, mPadding);
            mTextSize = array.getDimension(R.styleable.CountdownView_cvTextSize, mTextSize);
            mTextColor = array.getColor(R.styleable.CountdownView_cvTextColor, mTextColor);
            mBackGround = array.getColor(R.styleable.CountdownView_cvBackground, mBackGround);
            mSpaceWidth = array.getDimension(R.styleable.CountdownView_cvSpaceWidth, mSpaceWidth);
            mSpaceTextColor = array.getColor(R.styleable.CountdownView_cvSpaceTextColor, mSpaceTextColor);
            array.recycle();
        }

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//使位图抗锯齿的标志
        paint.setStyle(Paint.Style.FILL);//设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE    Style.FILL: 实心   STROKE:空心   FILL_OR_STROKE:同时实心与空心
        paint.setStrokeCap(Paint.Cap.ROUND);//当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式 Cap.ROUND,或方形样式Cap.SQUARE
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mTextSize);

        Rect textRect = new Rect();
        paint.getTextBounds("00", 0, 2, textRect);

        // Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        // textBaseline = (textRect.height() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;

        bgRectF = new RectF(0, 0, textRect.width() + mPadding * 2, textRect.width() + mPadding * 2);

        textBaseline = (int) ((bgRectF.height() - textRect.height()) / 2 + textRect.height());
    }

    public int[] getTimes() {
        return times;
    }

    public void setTimes(int[] times) {
        this.times = times;
    }

    public void startCountdown() {
        if (!run && times != null) {
            postDelayed(this, 1000);
        }
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            int widthSize = (int) (bgRectF.width() * 3 + mSpaceWidth * 2);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            int heightSize = (int) bgRectF.height();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        RectF backgroundRect = new RectF(0, 0, 0, bgRectF.height());
        for (int i = 0; i < 3; i++) {
            int offset = (int) (bgRectF.width() + mSpaceWidth) * i;
            backgroundRect.left = offset;
            backgroundRect.right = bgRectF.width() + offset;
            paint.setColor(mBackGround);
            canvas.drawRoundRect(backgroundRect, mRadius, mRadius, paint);
            paint.setColor(mTextColor);

            int time = 0;
            if (times != null) {
                time = times[i];
                if (time > 99) time = 99;
            }
            String text = (time > 9 ? BaseConstants.NULL : "0") + time;
            canvas.drawText(text, backgroundRect.centerX(), textBaseline, paint);
        }

        paint.setColor(mSpaceTextColor);
        for (int i = 0; i < 2; i++) {
            int offset = (int) bgRectF.width() + (int) (bgRectF.width() + mSpaceWidth) * i;
            canvas.drawText(BaseConstants.COLON, mSpaceWidth / 2 + offset, textBaseline, paint);
        }
    }

    @Override
    public void run() {
        //标示已经启动
        run = true;
        computeTime();
        postInvalidate();
        if (run) {
            postDelayed(this, 1000);
        }
    }

    private void computeTime() {
        times[2]--;
        if (times[2] < 0) {
            times[1]--;
            times[2] = 59;
            if (times[1] < 0) {
                times[1] = 59;
                times[0]--;
                if (times[0] < 0) { // 倒计时结束
                    times[0] = 0;
                    times[1] = 0;
                    times[2] = 0;
                    run = false;
                }
            }
        }
    }
}
