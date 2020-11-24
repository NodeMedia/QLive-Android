package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xyz.tanwb.airship.R;

public class SwipeMenuLayout extends FrameLayout {

    public static final int DEFAULT_SCROLLER_DURATION = 200;// 默认动画持续时间
    public static ConcurrentHashMap<SwipeMenuLayout, Long> swipeMenus = new ConcurrentHashMap<>();

    private View mContextView;//主内容视图
    private View mLeftMenuView;//左侧菜单视图
    private View mRightMenuView;//右侧菜单视图

    private int menuLocation = 2;// Menu位置 0:左右都有 1:只存在于左边 2:只存在于右边
    private float menuAutoOpenPercent = 0.6F;// 触发自动打开菜单的比例阀值
    private int menuScrollerDuration = DEFAULT_SCROLLER_DURATION;// 滑动动画持续时间

    private VelocityTracker mVelocityTracker;// 滑动速率监听类（用于在手指离开屏幕时判断接下来应该执行的动作）
    private ScrollerCompat mScroller;//滚动辅助类
    private int mScaledTouchSlop;// 触发滑动的距离
    private int mScaledMinimumFlingVelocity;// 允许执行一个fling手势动作的最小速度值
    private int mScaledMaximumFlingVelocity;// 允许执行一个fling手势动作的最大速度值

    // 是否第一次执行布局
    // private boolean isLayout = true;

    // 是否开启滑动Menu
    private boolean isSwipeEnable = true;

    //记录上次的触摸点X坐标
    private float lastX;
    //记录上次的触摸点Y坐标
    private float lastY;
    //拉动的距离（大于0为向左拉或向下拉,小于0为向右拉或向上拉）
    private float pullDist;
    // 过滤多点触碰
    private boolean isPointerDown;
    // 是否可触发滑动
    private boolean isTrigger;
    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    // private float radio;

    public SwipeMenuLayout(Context context) {
        super(context);
        initView(null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        int interpolatorId = 0;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeMenuLayout);
            menuLocation = a.getInteger(R.styleable.SwipeMenuLayout_location, menuLocation);
            menuAutoOpenPercent = a.getFloat(R.styleable.SwipeMenuLayout_autoOpenPercent, menuAutoOpenPercent);
            menuScrollerDuration = a.getInteger(R.styleable.SwipeMenuLayout_scrollerDuration, menuScrollerDuration);
            interpolatorId = a.getResourceId(R.styleable.SwipeMenuLayout_scrollerInterpolator, interpolatorId);
            a.recycle();
        }
        Interpolator mInterpolator;
        if (interpolatorId > 0) {
            mInterpolator = AnimationUtils.loadInterpolator(getContext(), interpolatorId);
        } else {
            mInterpolator = new LinearInterpolator();
        }
        mScroller = ScrollerCompat.create(getContext(), mInterpolator);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mScaledTouchSlop = configuration.getScaledTouchSlop();
        mScaledMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mScaledMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        switch (getChildCount()) {
            case 0:
                throwException();
                break;
            case 1:
                throwException();
                break;
            case 2:
                switch (menuLocation) {
                    case 1:
                        mLeftMenuView = getChildAt(1);
                        break;
                    case 2:
                        mRightMenuView = getChildAt(1);
                        break;
                    default:
                        throwException();
                        break;
                }
                break;
            case 3:
                if (menuLocation == 0) {
                    mLeftMenuView = getChildAt(1);
                    mRightMenuView = getChildAt(2);
                } else {
                    throwException();
                }
                break;
            default:
                throwException();
                break;
        }
        mContextView = getChildAt(0);
    }

    private void throwException() {
        throw new IllegalArgumentException("SwipeMenuLayout init Exception");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int buttom) {
        super.onLayout(changed, left, top, right, buttom);

        // Log.e("onLayout left:" + left + " top:" + top + " right:" + right + " buttom" + buttom);

        if (mLeftMenuView != null) {
            left = -mLeftMenuView.getMeasuredWidthAndState() + (int) pullDist;
            top = 0;
            right = left + mLeftMenuView.getMeasuredWidthAndState();
            buttom = top + mLeftMenuView.getMeasuredHeightAndState();
            mLeftMenuView.layout(left, top, right, buttom);
        }

        if (mRightMenuView != null) {
            left = getWidth() + (int) pullDist;
            top = 0;
            right = left + mRightMenuView.getMeasuredWidthAndState();
            buttom = top + mRightMenuView.getMeasuredHeightAndState();
            mRightMenuView.layout(left, top, right, buttom);
        }

        if (mContextView != null) {
            left = (int) pullDist;
            top = 0;
            right = left + mContextView.getMeasuredWidthAndState();
            buttom = top + mContextView.getMeasuredHeightAndState();
            mContextView.layout(left, top, right, buttom);
        }

        if (pullDist == 0) {
            if (swipeMenus.containsKey(this)) {
                swipeMenus.remove(this);
            }
        } else {
            if (!swipeMenus.containsKey(this)) {
                swipeMenus.put(this, System.currentTimeMillis());
            }
        }

//        if (isLayout) {
//            bringChildToFront(mContextView);
//            updateViewLayout(mContextView, mContextView.getLayoutParams());
//            postInvalidate();
//            isLayout = false;
//        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            pullDist = mScroller.getCurrX();
            requestLayout();
        }
        super.computeScroll();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Log.e("dispatchTouchEvent action:" + ev.getAction());
        /**
         * （非 Javadoc）由父控件决定是否分发事件，防止事件冲突
         *
         * @see android.view.ViewGroup#dispatchTouchEvent(MotionEvent)
         * dispatchTouchEvent是处理触摸事件分发,事件(多数情况)是从Activity的dispatchTouchEvent开始的。执行super.dispatchTouchEvent(ev)，事件向下分发。
         * onInterceptTouchEvent是ViewGroup提供的方法，默认返回false，返回true表示拦截。
         * onTouchEvent是View中提供的方法，ViewGroup也有这个方法，view中不提供onInterceptTouchEvent。view中默认返回true，表示消费了这个事件。
         */
        // ACTION_DOWN: 第一个点按下时触发
        // ACTION_UP: 屏幕上唯一的一个点抬起时触发
        // ACTION_MOVE: 当屏幕上的点移动时触发。
        // ACTION_POINTER_DOWN: 当屏幕上已经有一点按住，再按下其他点便会触发该事件。
        // ACTION_POINTER_UP: 当屏幕上有多个点被按住，松开其中的点时触发（非最后一个点，最后一个点触发ACTION_UP）。
        // ACTION_CANCEL：当前的手势被释放时会触发，当前手势指的是从ACTION_DOWN开始以及后面一系列的ACTION_MOVE、ACTION_POINTER_DOWN、ACTION_POINTER_UP操作。

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPointerDown = false;
                isTrigger = false;
                lastX = ev.getX();
                lastY = ev.getY();

                getParent().requestDisallowInterceptTouchEvent(false);
                // Log.e("mScaledTouchSlop:" + mScaledTouchSlop + " isSwipeEnable():" + isSwipeEnable());

                // 判断是否有其他Menu被打开
                for (Map.Entry<SwipeMenuLayout, Long> entry : swipeMenus.entrySet()) {
                    SwipeMenuLayout sm = entry.getKey();
                    if (sm != this) {
                        if (!sm.isCloseMenu()) {
                            sm.closeMenu();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isPointerDown = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (ev.getPointerCount() == 1) {
                    isPointerDown = false;
                    lastX = ev.getX();
                    lastY = ev.getY();
                } else {
                    isPointerDown = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isSwipeEnable() || isPointerDown) break;

                if (!isTrigger) {
                    float disX = ev.getX() - lastX;
                    float disY = ev.getY() - lastY;
                    // Log.e("disX:" + disX + " disY:" + disY + " pullDist:" + pullDist);

                    switch (menuLocation) {
                        case 1:
                            if (pullDist == 0) {
                                isTrigger = disX > 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else if (pullDist == mLeftMenuView.getMeasuredWidthAndState()) {
                                isTrigger = disX < 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else {
                                isTrigger = Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            }
                            break;
                        case 2:
                            if (pullDist == 0) {
                                isTrigger = disX < 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else if (pullDist == -mRightMenuView.getMeasuredWidthAndState()) {
                                isTrigger = disX > 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else {
                                isTrigger = Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            }
                            break;
                        default:
                            if (pullDist == mLeftMenuView.getMeasuredWidthAndState()) {
                                isTrigger = disX < 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else if (pullDist == -mRightMenuView.getMeasuredWidthAndState()) {
                                isTrigger = disX > 0 && Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            } else {
                                isTrigger = Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop;
                            }
                            break;
                    }
                }
                if (isTrigger) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    // 根据下拉距离改变比例
                    // radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * Math.abs(pullDist)));
                    // Log.e("radio:" + radio + " " + ((ev.getX() - lastX) / radio));
                    // 计算Y轴偏移量,对实际滑动距离做缩小，造成用力拉的感觉
                    // pullDist = pullDist + (ev.getX() - lastX);
                    pullDist = pullDist + (ev.getX() - lastX);
                    // 记录当前触摸X、Y坐标
                    lastX = ev.getX();
                    lastY = ev.getY();

                    // Log.e("pullDist:" + pullDist);

                    switch (menuLocation) {
                        case 1:
                            if (pullDist < 0) {
                                pullDist = 0;
                            }
                            if (pullDist > mLeftMenuView.getMeasuredWidthAndState()) {
                                pullDist = mLeftMenuView.getMeasuredWidthAndState();
                            }
                            break;
                        case 2:
                            if (pullDist > 0) {
                                pullDist = 0;
                            }
                            if (pullDist < -mRightMenuView.getMeasuredWidthAndState()) {
                                pullDist = -mRightMenuView.getMeasuredWidthAndState();
                            }
                            break;
                        default:
                            if (pullDist > mLeftMenuView.getMeasuredWidthAndState()) {
                                pullDist = mLeftMenuView.getMeasuredWidthAndState();
                            } else if (pullDist < -mRightMenuView.getMeasuredWidthAndState()) {
                                pullDist = -mRightMenuView.getMeasuredWidthAndState();
                            }
                            break;
                    }
                    requestLayout();
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                // Log.e("onTouchEvent action:ACTION_UP");
                // Log.e("disXTotal:" + disXTotal + " disYTotal:" + disYTotal);

                if (pullDist != 0 && !isOpenMenu()) {
                    mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
                    int velocityX = (int) mVelocityTracker.getXVelocity();// 获取滑动的瞬时速度
                    // Log.e("velocityX:" + velocityX + " mScaledMinimumFlingVelocity:" + mScaledMinimumFlingVelocity);
                    int velocity = Math.abs(velocityX);
                    if (velocity > mScaledMinimumFlingVelocity) {
                        int duration = getSwipeDuration(ev, velocity);
                        if (pullDist > 0) {
                            if (velocityX > 0) {
                                mScroller.startScroll((int) pullDist, 0, (int) (mLeftMenuView.getMeasuredWidthAndState() - pullDist), 0, duration);
                            } else {
                                mScroller.startScroll((int) pullDist, 0, (int) -pullDist, 0, duration);
                            }
                        } else if (pullDist < 0) {
                            if (velocityX < 0) {
                                mScroller.startScroll((int) pullDist, 0, (int) -(mRightMenuView.getMeasuredWidthAndState() + pullDist), 0, duration);
                            } else {
                                mScroller.startScroll((int) pullDist, 0, (int) -pullDist, 0, duration);
                            }
                        }
                        ViewCompat.postInvalidateOnAnimation(this);
                    } else {
                        changeDist();
                    }
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                if (isTrigger) {
                    // 防止滑动过程中误触发长按事件和点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                } else {
                    if (isClickOnContentView(lastX) && !isCloseMenu()) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    closeMenu();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.clear();
                mVelocityTracker.recycle();
                mVelocityTracker = null;

                pullDist = mContextView.getLeft();
                if (pullDist != 0) {
                    changeDist();
                }
                break;
        }
        // 事件分发交给父类
        return super.dispatchTouchEvent(ev);
    }

    private void changeDist() {
        float openDist;
        if (pullDist > 0 && mLeftMenuView != null) {
            openDist = mLeftMenuView.getMeasuredWidthAndState() * menuAutoOpenPercent;
            if (pullDist < openDist) {
                // 关闭
                mScroller.startScroll((int) pullDist, 0, (int) -pullDist, 0, menuScrollerDuration);
            } else {
                // 打开
                mScroller.startScroll((int) pullDist, 0, (int) (mLeftMenuView.getMeasuredWidthAndState() - pullDist), 0, menuScrollerDuration);
            }
        } else if (pullDist < 0 && mRightMenuView != null) {
            openDist = mRightMenuView.getMeasuredWidthAndState() * menuAutoOpenPercent;
            if (Math.abs(pullDist) < openDist) {
                mScroller.startScroll((int) pullDist, 0, (int) -pullDist, 0, menuScrollerDuration);
            } else {
                mScroller.startScroll((int) pullDist, 0, (int) -(mRightMenuView.getMeasuredWidthAndState() + pullDist), 0, menuScrollerDuration);
            }
        }
        invalidate();// 触发 computeScroll
    }

    public boolean isOpenMenu() {
        if (mContextView.getLeft() > 0) {
            return mContextView.getLeft() == mLeftMenuView.getWidth();
        } else if (mContextView.getLeft() < 0) {
            return mContextView.getLeft() == -mRightMenuView.getWidth();
        }
        return false;
    }

    public boolean isCloseMenu() {
        return mContextView.getLeft() == 0;
    }

    /**
     * 判断是否点击的内容视图
     */
    public boolean isClickOnContentView(float x) {
        return x > mContextView.getLeft() && x < mContextView.getRight();
    }

    public void closeMenu() {
        mScroller.startScroll(mContextView.getLeft(), 0, -mContextView.getLeft(), 0, menuScrollerDuration);
        invalidate();// 触发 computeScroll
    }

    public void setSwipeEnable(boolean swipeEnable) {
        isSwipeEnable = swipeEnable;
    }

    public boolean isSwipeEnable() {
        return isSwipeEnable;
    }

    /**
     * 计算完成时间
     *
     * @param ev       up event.
     * @param velocity 滑动速度
     * @return 完成时间.
     */
    private int getSwipeDuration(MotionEvent ev, int velocity) {
        // Log.e("getSwipeDuration getX:" + ev.getX() + " getScaleX:" + getScaleX() + " velocity:" + velocity);
        int sx = getScrollX();
        int dx = (int) (ev.getX() - sx);
        final int width = mContextView.getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);//滑动距离比
        final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);//得到滑动的距离
        int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dx) / width;
            duration = (int) ((pageDelta + 1) * 100);
        }
        duration = Math.min(duration, menuScrollerDuration);
        return duration;
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // 中心值约为0
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }
}
