package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import androidx.core.widget.ScrollerCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.R;

/**
 * WheelView滚轮
 */
public class WheelView extends View {

    //需要显示的行数
    private int showCount = 3;
    //当前默认选择的位置,默认第一项
    private int selectItem;
    //默认项字体大小
    private float normalFont = getResources().getDimension(R.dimen.sp_14);
    //选中项字体大小
    private float selectedFont = getResources().getDimension(R.dimen.sp_18);
    //默认字体颜色
    private int normalColor = 0xff000000;
    //选中项字体颜色
    private int selectedColor = 0xffff0000;
    //行垂直间距
    private float verticalPadding = getResources().getDimension(R.dimen.dp_12);
    //行水平间距，默认无间距
    private float horizontalPadding;

    //选中项分割线高度，默认无分割线
    private float lineHeight;
    //选中项分割线颜色
    private int lineColor = 0x10FF0000;

    //上下边半透明遮挡板高度，默认无遮挡板
    private float maskHeight;
    //上下边半透明遮挡板颜色
    private int maskColor = 0x10000000;

    //控件宽度
    private float width;
    //控件高度
    private float height;
    //Item 高度
    private float itemHeight;
    //字体画笔
    private TextPaint textPaint;
    //分割线画笔
    private Paint linePaint;
    //文字区域
    private Rect textRect;
    //文本列表
    private List<String> itemTexts;
    //附属内容
    private String subtext = BaseConstants.NULL;
    //每一项Item和选中项
    private List<WheelItem> wheelItems;

    //上次触摸的X坐标
    private float mLastX;
    //上次触摸的Y坐标
    private float mLastY;
    //手指抬起后还需要滑动的距离
    private int needScrollY;
    //监听器
    private OnWheelViewSelectListener onWheelViewSelectListener;
    //滚动辅助类
    private ScrollerCompat mScroller;
    // 触发滑动的距离
    private int mScaledTouchSlop;

    // 单极预加载Item数量
    private int preloadingItem = 1;
    //是否循环滚动
    private boolean isLoop = true;
    //居中Item的位置
    private int centerPosition;
    // 过滤多点触碰
    private boolean isPointerDown;
    //是否可以滚动
    private boolean isSlide;

    public WheelView(Context context) {
        super(context);
        init(null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attribute = getContext().obtainStyledAttributes(attrs, R.styleable.WheelView);
            showCount = attribute.getInt(R.styleable.WheelView_showCount, showCount);
            normalFont = attribute.getDimension(R.styleable.WheelView_normalTextSize, normalFont);
            selectedFont = attribute.getDimension(R.styleable.WheelView_selectedTextSize, selectedFont);
            normalColor = attribute.getColor(R.styleable.WheelView_normalTextColor, normalColor);
            selectedColor = attribute.getColor(R.styleable.WheelView_selectedTextColor, selectedColor);
            verticalPadding = attribute.getDimension(R.styleable.WheelView_verticalPadding, verticalPadding);
            horizontalPadding = attribute.getDimension(R.styleable.WheelView_horizontalPadding, horizontalPadding);
            attribute.recycle();
        }

        if (showCount % 2 == 0) {
            throw new IllegalArgumentException("WheelView showCount must be odd");
        } else {
            centerPosition = showCount / 2;
        }

        if (textPaint == null) {
            textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(selectedFont);
            textPaint.setColor(selectedColor);
        }

        if (textRect == null) {
            textRect = new Rect();
            textPaint.getTextBounds(this.getClass().getName(), 0, this.getClass().getName().length(), textRect);
        }

        itemHeight = verticalPadding * 2 + textRect.height();

        mScroller = ScrollerCompat.create(getContext());
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = showCount * itemHeight;
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Log.e("onTouchEvent action:" + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSlide = false;
                isPointerDown = false;
                mLastX = event.getX();
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isPointerDown = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 1) {
                    isPointerDown = false;
                    mLastX = event.getX();
                    mLastY = event.getY();
                } else {
                    isPointerDown = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (isPointerDown) break;

                if (!isSlide) {
                    float disX = event.getX() - mLastX;
                    float disY = event.getY() - mLastY;
                    isSlide = Math.abs(disY) > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop;
                }
                if (isSlide) {
                    float scrollY = (event.getY() - mLastY) * 0.7F;
                    for (WheelItem item : wheelItems) {
                        item.adjust(scrollY);
                        if (item.isCenter()) {
                            selectItem = item.id;
                            needScrollY = (int) item.moveToSelected();
                        }
                    }
                    mLastY = event.getY();
                    // Log.e("SelectItem:" + selectItem + " needScrollY" + needScrollY);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isSlide) {
                    isSlide = false;
                    if (needScrollY != 0) {
                        mScroller.startScroll(0, needScrollY, 0, -needScrollY);
                        invalidate();
                    } else {
                        if (onWheelViewSelectListener != null) {
                            onWheelViewSelectListener.onWheelViewSelect(getSelectItem(), getSelectText());
                        }
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // Log.e("computeScroll isFinished:" + mScroller.isFinished() + " mScroller.computeScrollOffset():" + mScroller.computeScrollOffset());
        if (mScroller.computeScrollOffset()) {
            // Log.e("mScroller.getFinalY:" + mScroller.getFinalY() + " mScroller.getCurrY:" + mScroller.getCurrY() + " needScrollY:" + needScrollY);
            if (mScroller.getFinalY() != mScroller.getCurrY()) {
                int scrolly = needScrollY - mScroller.getCurrY();
                if (scrolly != 0) {
                    for (int i = 0; i < wheelItems.size(); i++) {
                        WheelItem item = wheelItems.get(i);
                        item.adjust(scrolly);
                        if (item.isCenter()) {
                            selectItem = item.id;
                        }
                    }
                }
                needScrollY = mScroller.getCurrY();
                invalidate();
            } else {
                //设置选择项
                if (onWheelViewSelectListener != null && !isSlide) {
                    onWheelViewSelectListener.onWheelViewSelect(getSelectItem(), getSelectText());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("onDraw");
        drawList(canvas);
        drawLine(canvas);
        drawMask(canvas);
    }

    private synchronized void drawList(Canvas canvas) {
        if (isLoop) {
            int firstPosition = 0;
            int lastPosition = wheelItems.size() - 1;
            WheelItem firstWheelItem = wheelItems.get(firstPosition);
            WheelItem lastWheelItem = wheelItems.get(lastPosition);
            //如果向下或向下滑动超出半个Item的高度，则调整容器
            if (firstWheelItem.y < -(itemHeight * (preloadingItem + 0.5F))) {
                //向上滑动
                WheelItem newItem = new WheelItem();
                newItem.id = getId(lastWheelItem.id + 1);
                newItem.y = lastWheelItem.y + itemHeight;
                newItem.itemText = itemTexts.get(newItem.id);
                wheelItems.add(newItem);
                wheelItems.remove(firstPosition);
            } else if (lastWheelItem.y > (height + itemHeight * (preloadingItem - 0.5F))) {
                //向下滑动
                WheelItem newItem = new WheelItem();
                newItem.id = getId(firstWheelItem.id - 1);
                newItem.y = firstWheelItem.y - itemHeight;
                newItem.itemText = itemTexts.get(newItem.id);
                wheelItems.add(firstPosition, newItem);
                wheelItems.remove(wheelItems.size() - 1);
            }
        }

        for (int i = 0; i < wheelItems.size(); i++) {
            WheelItem wheelInfo = wheelItems.get(i);
            // Log.e("wheelInfo.id:" + wheelInfo.id);
            wheelInfo.drawSelf(canvas);
        }
    }

    private synchronized void drawLine(Canvas canvas) {
        if (lineHeight > 0) {
            if (linePaint == null) {
                linePaint = new Paint();
                linePaint.setColor(lineColor);
                linePaint.setAntiAlias(true);
                linePaint.setStrokeWidth(lineHeight);
            }
            canvas.drawLine(0, (height - itemHeight) / 2 - lineHeight / 2, width, (height - itemHeight) / 2 + lineHeight / 2, linePaint);
            canvas.drawLine(0, (height + itemHeight) / 2 - lineHeight / 2, width, (height + itemHeight) / 2 + lineHeight / 2, linePaint);
        }
    }

    private synchronized void drawMask(Canvas canvas) {
        if (maskHeight > 0) {
            //绘制顶部遮盖板
            LinearGradient lg = new LinearGradient(0, 0, 0, maskHeight, maskColor, 0x10FFFFFF, Shader.TileMode.MIRROR);
            Paint paint1 = new Paint();
            paint1.setShader(lg);
            canvas.drawRect(0, 0, width, maskHeight, paint1);
            //绘制底部遮盖板
            LinearGradient lg2 = new LinearGradient(0, height - maskHeight, 0, height, 0x10FFFFFF, maskColor, Shader.TileMode.MIRROR);
            Paint paint2 = new Paint();
            paint2.setShader(lg2);
            canvas.drawRect(0, height - maskHeight, width, height, paint2);
        }
    }

    /**
     * 最后调用的方法，判断是否有必要函数没有被调用
     */
    public void build() {
        if (itemTexts == null) {
            throw new IllegalStateException("this method must invoke after the method [lists]");
        }

        if (wheelItems == null) {
            wheelItems = new ArrayList<>();
        }

        if (wheelItems.size() > 0) {
            wheelItems.clear();
        }

        if (selectItem < 0 || selectItem >= itemTexts.size()) {
            selectItem = 0;
        }

        if (isLoop) {
            for (int i = 0; i < showCount + preloadingItem * 2; i++) {
                WheelItem wheelItem = new WheelItem();
                wheelItem.id = getId(i + selectItem - centerPosition - preloadingItem);
                wheelItem.y = (i - preloadingItem) * itemHeight;
                wheelItem.itemText = itemTexts.get(wheelItem.id);
                // Log.e("Loop build id:" + wheelItem.id);
                wheelItems.add(wheelItem);
            }
        } else {
            for (int i = -centerPosition; i < itemTexts.size() + centerPosition; i++) {
                WheelItem wheelItem = new WheelItem();
                wheelItem.id = i;
                wheelItem.y = itemHeight * (i + centerPosition - selectItem);
                wheelItem.maxY = itemHeight * (i + centerPosition);
                wheelItem.minY = wheelItem.maxY - itemHeight * (itemTexts.size() - 1);
                if (i < 0 || i >= itemTexts.size()) {
                    wheelItem.itemText = null;
                } else {
                    wheelItem.itemText = itemTexts.get(wheelItem.id);
                }
                // Log.e("Common build id:" + wheelItem.id);
                wheelItems.add(wheelItem);
            }
        }
        invalidate();
    }

    private int getId(int id) {
        if (id < 0) {
            id += itemTexts.size();
            return getId(id);
        } else if (id >= itemTexts.size()) {
            id -= itemTexts.size();
            return getId(id);
        }
        return id;
    }

    public List<String> getData() {
        return itemTexts;
    }

    public WheelView setData(List<String> data) {
        this.itemTexts = data;
        this.selectItem = 0;
        return this;
    }

    public String getSubtext() {
        return subtext;
    }

    public WheelView setSubtext(String subtext) {
        this.subtext = subtext;
        return this;
    }

    public int getShowCount() {
        return showCount;
    }

    public WheelView setShowCount(int showCount) {
        this.showCount = showCount;
        return this;
    }

    public int getSelectItem() {
        return selectItem;
    }

    public WheelView setSelectItem(int selectItem) {
        if (selectItem >= 0 && selectItem < itemTexts.size()) {
            this.selectItem = selectItem;
        }
        return this;
    }

    public String getSelectText() {
        return itemTexts.get(selectItem);
    }

    public WheelView setSelectText(String selectText) {
        setSelectItem(itemTexts.indexOf(selectText));
        return this;
    }

    public float getNormalFont() {
        return normalFont;
    }

    public WheelView setNormalFont(float normalFont) {
        this.normalFont = normalFont;
        return this;
    }

    public float getSelectedFont() {
        return selectedFont;
    }

    public WheelView setSelectedFont(float selectedFont) {
        this.selectedFont = selectedFont;
        return this;
    }

    public int getNormalColor() {
        return normalColor;
    }

    public WheelView setNormalColor(int normalColor) {
        this.normalColor = normalColor;
        return this;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public WheelView setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        return this;
    }

    public float getVerticalPadding() {
        return verticalPadding;
    }

    public WheelView setVerticalPadding(float verticalPadding) {
        this.verticalPadding = verticalPadding;
        return this;
    }

    public float getHorizontalPadding() {
        return horizontalPadding;
    }

    public WheelView setHorizontalPadding(float horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        return this;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public WheelView setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public int getLineColor() {
        return lineColor;
    }

    public WheelView setLineColor(int lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public float getMaskHeight() {
        return maskHeight;
    }

    public WheelView setMaskHeight(float maskHeight) {
        this.maskHeight = maskHeight;
        return this;
    }

    public int getMaskColor() {
        return maskColor;
    }

    public WheelView setMaskColor(int maskColor) {
        this.maskColor = maskColor;
        return this;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public int getPreloadingItem() {
        return preloadingItem;
    }

    public void setPreloadingItem(int preloadingItem) {
        this.preloadingItem = preloadingItem;
    }

    public WheelView setOnWheelViewSelectListener(OnWheelViewSelectListener onWheelViewSelectListener) {
        this.onWheelViewSelectListener = onWheelViewSelectListener;
        return this;
    }

    /**
     * 单条内容
     */
    private class WheelItem {

        //Item Id
        public int id;
        //文本内容
        public String itemText;

        //y坐标
        public float y;

        public float minY;
        public float maxY;

        /**
         * 设置新的坐标
         */
        public void adjust(float dy) {
            y += dy;
            if (!isLoop) {
                if (y > maxY) {
                    y = maxY;
                } else if (y < minY) {
                    y = minY;
                }
            }
        }

        /**
         * 绘制自身
         */
        public void drawSelf(Canvas canvas) {

            // 判断是否可视
            if (TextUtils.isEmpty(itemText) || !isInView()) return;

            if (textPaint == null) {
                textPaint = new TextPaint();
                textPaint.setAntiAlias(true);
            }

            if (textRect == null) textRect = new Rect();

            // 判断是否被选择
            if (isSelected()) {
                // 获取距离标准位置的距离
                float moveToSelect = Math.abs(moveToSelected());
                float radio = 1.0f - moveToSelect / itemHeight;
                textPaint.setColor(getColor(normalColor, selectedColor, radio));
                textPaint.setTextSize(getFont(normalFont, selectedFont, radio));
            } else {
                textPaint.setColor(normalColor);
                textPaint.setTextSize(normalFont);
            }

            String drawText;
            if (selectItem == id && !isSlide) {
                drawText = itemText + subtext;
            } else {
                drawText = itemText;
            }

            // 获取包围整个字符串的最小的一个Rect区域
            drawText = TextUtils.ellipsize(drawText, textPaint, width - horizontalPadding * 2, TextUtils.TruncateAt.END).toString();

            textPaint.getTextBounds(drawText, 0, drawText.length(), textRect);

            // 绘制内容
            canvas.drawText(drawText, (width - textRect.width()) / 2, y + itemHeight / 2 + textRect.height() / 2, textPaint);
        }

        private int getColor(int normalColor, int selectedColor, float radio) {
            int redNormal = Color.red(normalColor);
            int greenNormal = Color.green(normalColor);
            int blueNormal = Color.blue(normalColor);
            int redSelected = Color.red(selectedColor);
            int greenSelected = Color.green(selectedColor);
            int blueSelected = Color.blue(selectedColor);

            int red = (int) (redNormal + ((redSelected - redNormal) * radio + 0.5F));
            int greed = (int) (greenNormal + ((greenSelected - greenNormal) * radio + 0.5F));
            int blue = (int) (blueNormal + ((blueSelected - blueNormal) * radio + 0.5F));
            return Color.argb(255, red, greed, blue);
        }

        private float getFont(float normalFont, float selectedFont, float radio) {
            return normalFont + ((selectedFont - normalFont) * radio);
        }

        /**
         * 是否在可视界面内
         */
        private boolean isInView() {
            return y > -itemHeight && y < height + itemHeight;
        }

        /**
         * 判断是否在选择区域内
         */
        private boolean isSelected() {
            return y >= itemHeight * (centerPosition - 1) && y <= itemHeight * (centerPosition + 1);
        }

        /**
         * 获取移动到标准位置需要的距离
         */
        private float moveToSelected() {
            return itemHeight * centerPosition - y;
        }

        /**
         * 判断是否在居中区域内
         */
        public boolean isCenter() {
            return y >= itemHeight * (centerPosition - 0.5F) && y < itemHeight * (centerPosition + 0.5F);
        }

    }

    public interface OnWheelViewSelectListener {
        void onWheelViewSelect(int position, String data);
    }
}