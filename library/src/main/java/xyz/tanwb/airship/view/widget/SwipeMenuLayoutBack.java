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

import xyz.tanwb.airship.R;

public class SwipeMenuLayoutBack extends FrameLayout {

    public static final int DEFAULT_SCROLLER_DURATION = 200;
    public static SwipeMenuLayoutBack oldSwipeMenuLayout;
    public static boolean mOldTouchedPosition;

    private View mContextView;//主内容视图
    private View mLeftMenuView;//左侧菜单视图
    private View mRightMenuView;//右侧菜单视图
    private View mOpenMenuView;//打开的菜单视图

    private int menuLocation = 2;// Menu位置 0:左右都有 1:只存在于左边 2:只存在于右边
    private float menuAutoOpenPercent = 0.6F;// 触发自动打开菜单的比例阀值
    private int menuScrollerDuration = DEFAULT_SCROLLER_DURATION;// 滑动动画持续时间

    private ScrollerCompat mScroller;//滚动辅助类
    private int mScaledTouchSlop;// 触发滑动的距离
    private int mScaledMinimumFlingVelocity;// 允许执行一个fling手势动作的最小速度值
    private int mScaledMaximumFlingVelocity;// 允许执行一个fling手势动作的最大速度值

    private VelocityTracker mVelocityTracker;// 滑动速率监听类（用于在手指离开屏幕时判断接下来应该执行的动作）

    private float mDownX;// 按下的X轴坐标
    private float mDownY;// 按下的Y轴坐标
    private float mLastX;// 上次记录的X轴坐标
    private float mLastY;// 上次记录的Y轴坐标

    private boolean isSwipeEnable = true;// 是否开启滑动Menu
    private boolean isDragging;// 是否达到触发滑动的距离
    private boolean isShouldResetSwiper;// 是否重置

    public SwipeMenuLayoutBack(Context context) {
        super(context);
        initView(null);
    }

    public SwipeMenuLayoutBack(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public SwipeMenuLayoutBack(Context context, AttributeSet attrs, int defStyleAttr) {
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mContextView != null) {
            int contentViewWidth = mContextView.getMeasuredWidthAndState();
            int contentViewHeight = mContextView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) mContextView.getLayoutParams();
            int start = getPaddingLeft();
            int top = getPaddingTop() + lp.topMargin;
            mContextView.layout(start, top, start + contentViewWidth, top + contentViewHeight);
        }

        if (mLeftMenuView != null) {
            int menuViewWidth = mLeftMenuView.getMeasuredWidthAndState();
            // nt menuViewHeight = contentViewHeight == 0 ? leftMenu.getMeasuredHeightAndState() : contentViewHeight;
            int menuViewHeight = mLeftMenuView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) mLeftMenuView.getLayoutParams();
            int top = getPaddingTop() + lp.topMargin;
            mLeftMenuView.layout(-menuViewWidth, top, 0, top + menuViewHeight);
        }

        if (mRightMenuView != null) {
            int menuViewWidth = mRightMenuView.getMeasuredWidthAndState();
            // int menuViewHeight = contentViewHeight == 0 ? rightMenu.getMeasuredHeightAndState() : contentViewHeight;
            int menuViewHeight = mRightMenuView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) mRightMenuView.getLayoutParams();
            int top = getPaddingTop() + lp.topMargin;

            int parentViewWidth = getMeasuredWidthAndState();
            mRightMenuView.layout(parentViewWidth, top, parentViewWidth + menuViewWidth, top + menuViewHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Log.e("onInterceptTouchEvent action:" + ev.getAction());
        // return FALSE 表示事件向子View传递;return TRUE 调用onTouchEvent方法
        boolean isIntercepted = super.onInterceptTouchEvent(ev);
        if (mOldTouchedPosition) {
            // Log.e("onInterceptTouchEvent action:OldTouched " + ev.getAction());
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
               // Log.e("onInterceptTouchEvent action:ACTION_DOWN");
                mDownX = ev.getX();
                mLastX = ev.getX();
                mDownY = ev.getY();
                mLastY = ev.getY();
                isIntercepted = false;
                if (oldSwipeMenuLayout != null && oldSwipeMenuLayout != this) {
                    try {
                        if (oldSwipeMenuLayout.isMenuOpen()) {
                            oldSwipeMenuLayout.smoothCloseMenu();
                            mOldTouchedPosition = true;
                        } else {
                            mOldTouchedPosition = false;
                        }
                    } catch (Exception e) {
                        mOldTouchedPosition = false;
                    }
                } else {
                    mOldTouchedPosition = false;
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                // Log.e("onInterceptTouchEvent action:ACTION_MOVE");
                float disX = ev.getX() - mDownX;
                float disY = ev.getY() - mDownY;
                // Log.e("disXTotal:" + disX + " disYTotal:" + disY + " mScaledTouchSlop:" + mScaledTouchSlop);
                // 判断触摸移动距离是否达到最小滑动距离,触发onTouchEvent方法
                // isIntercepted = Math.abs(disX) > mScaledTouchSlop / 2 && Math.abs(disX) * 2 > Math.abs(disY) && isSwipeEnable();
                isIntercepted = Math.abs(disX) > mScaledTouchSlop && Math.abs(disX) > Math.abs(disY) && isSwipeEnable();
                if (isIntercepted) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else if (Math.abs(disX) < mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                // Log.e("onInterceptTouchEvent action:ACTION_UP");
                // Log.e("ev.getX():" + ev.getX() + " mContextView.getWidth():" + mContextView.getWidth() + " mRightMenuView.getWidth():" + mRightMenuView.getWidth());
                if (isMenuOpen()) {
                    smoothCloseMenu();
                    isIntercepted = isClickOnContentView(ev.getX());
                } else {
                    isIntercepted = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // Log.e("onInterceptTouchEvent action:ACTION_CANCEL");
                isIntercepted = false;
                // 获取scroller是否已完成滚动
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();// 停止动画
                }
                break;
        }
        return isIntercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Log.e("onTouchEvent action:" + ev.getAction());
        if (mOldTouchedPosition) {
            // Log.e("onTouchEvent action:OldTouched " + ev.getAction());
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                mOldTouchedPosition = false;
            }
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        float disXTotal = mDownX - ev.getX();
        float disYTotal = mDownY - ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Log.e("onTouchEvent action:ACTION_DOWN");
                mLastX = (int) ev.getX();
                mLastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Log.e("onTouchEvent action:ACTION_MOVE");
                if (!isSwipeEnable()) break;
                float disX = mLastX - ev.getX();// 获取位移X轴长度 往左为正 往右为负
                float disY = mLastY - ev.getY();// 获取位移Y轴长度 往上为正 往下为负
                // Log.e("disX:" + disX + " disY:" + disY);
                // if (!isDragging && Math.abs(disX) > mScaledTouchSlop / 2 && Math.abs(disX) * 2 > Math.abs(disY)) {
                // if (!isDragging && Math.abs(disX) > mScaledTouchSlop && Math.abs(disX) > Math.abs(disY)) {
                // isDragging = true;
                // }

                if (!isDragging) {
                    if (Math.abs(disX) > mScaledTouchSlop && Math.abs(disY) < mScaledTouchSlop) {
                        isDragging = true;
                    } else if (Math.abs(disY) > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop) {
                        return false;
                    }
                }

                if (isDragging) {
                    if (mOpenMenuView == null || isShouldResetSwiper) {
                        if (disX < 0) {
                            if (mLeftMenuView != null) {
                                mOpenMenuView = mLeftMenuView;
                            } else {
                                mOpenMenuView = mRightMenuView;
                            }
                        } else {
                            if (mRightMenuView != null) {
                                mOpenMenuView = mRightMenuView;
                            } else {
                                mOpenMenuView = mLeftMenuView;
                            }
                        }
                    }
                    scrollBy((int) disX, 0);
                    mLastX = ev.getX();
                    mLastY = ev.getY();
                    isShouldResetSwiper = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                // Log.e("onTouchEvent action:ACTION_UP");
                // Log.e("disXTotal:" + disXTotal + " disYTotal:" + disYTotal);
                isDragging = false;
                mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
                int velocityX = (int) mVelocityTracker.getXVelocity();// 获取滑动的瞬时速度
                // Log.e("velocityX:" + velocityX + " mScaledMinimumFlingVelocity:" + mScaledMinimumFlingVelocity);
                int velocity = Math.abs(velocityX);
                if (velocity > mScaledMinimumFlingVelocity) {
                    if (mOpenMenuView != null) {
                        int duration = getSwipeDuration(ev, velocity);
                        if (mOpenMenuView == mLeftMenuView) {
                            if (velocityX > 0) {
                                smoothOpenMenu(duration);
                            } else {
                                smoothCloseMenu(duration);
                            }
                        } else {
                            if (velocityX < 0) {
                                smoothOpenMenu(duration);
                            } else {
                                smoothCloseMenu(duration);
                            }
                        }
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                } else {
                    judgeOpenClose(disXTotal, disYTotal);
                }
                mVelocityTracker.clear();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                if (Math.abs(mDownX - ev.getX()) > mScaledTouchSlop || Math.abs(mDownY - ev.getY()) > mScaledTouchSlop || isMenuOpen()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // Log.e("onTouchEvent action:ACTION_CANCEL");
                isDragging = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                } else {
                    judgeOpenClose(disXTotal, disYTotal);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setSwipeEnable(boolean swipeEnable) {
        isSwipeEnable = swipeEnable;
    }

    public boolean isSwipeEnable() {
        return isSwipeEnable;
    }

    public boolean isMenuOpen() {
        return isLeftMenuOpen() || isRightMenuOpen();
    }

    public boolean isLeftMenuOpen() {
        if (mLeftMenuView != null) {
            int i = -mLeftMenuView.getWidth();
            return getScrollX() <= i && i != 0;
        }
        return false;
    }

    public boolean isRightMenuOpen() {
        if (mRightMenuView != null) {
            int i = mRightMenuView.getWidth();
            return getScrollX() >= i && i != 0;
        }
        return false;
    }

    /**
     * 判断是否点击的内容视图
     */
    public boolean isClickOnContentView(float x) {
        if (mOpenMenuView != null) {
            if (mOpenMenuView == mLeftMenuView) {
                return x > mLeftMenuView.getWidth();
            } else {
                // 可替换getWidth()为mContextView.getWidth()
                return x < (getWidth() - mOpenMenuView.getWidth());
            }
        }
        return false;
    }

    public boolean isMenuOpenNotEqual() {
        return isLeftMenuOpenNotEqual() || isRightMenuOpenNotEqual();
    }

    public boolean isLeftMenuOpenNotEqual() {
        return mLeftMenuView != null && getScrollX() < -mLeftMenuView.getWidth();
    }

    public boolean isRightMenuOpenNotEqual() {
        return mRightMenuView != null && getScrollX() > mRightMenuView.getWidth();
    }

    /**
     * 平滑的打开左侧滑动菜单 默认滑动时间
     */
    public void smoothOpenLeftMenu() {
        smoothOpenLeftMenu(menuScrollerDuration);
    }

    /**
     * 平滑的打开左侧滑动菜单
     */
    public void smoothOpenLeftMenu(int duration) {
        if (mLeftMenuView != null) {
            mOpenMenuView = mLeftMenuView;
            smoothOpenMenu(duration);
        }
    }

    /**
     * 平滑的打开右侧滑动菜单 默认滑动时间
     */
    public void smoothOpenRightMenu() {
        smoothOpenRightMenu(menuScrollerDuration);
    }

    /**
     * 平滑的打开右侧滑动菜单
     */
    public void smoothOpenRightMenu(int duration) {
        if (mRightMenuView != null) {
            mOpenMenuView = mRightMenuView;
            smoothOpenMenu(duration);
        }
    }

    /**
     * 平滑的打开滑动菜单 默认滑动时间
     */
    public void smoothOpenMenu() {
        smoothOpenMenu(menuScrollerDuration);
    }

    /**
     * 平滑的打开滑动菜单
     */
    public void smoothOpenMenu(int duration) {
        if (mOpenMenuView != null) {
            // Log.e("smoothOpenMenu >> isLeftMenuView:" + (mOpenMenuView == mLeftMenuView) + " scrollX:" + getScrollX() + " scrollY:" + getScaleY() + " duration:" + duration);
            if (mOpenMenuView == mLeftMenuView) {
                mScroller.startScroll(Math.abs(getScrollX()), 0, mLeftMenuView.getWidth() - Math.abs(getScrollX()), 0, duration);
            } else {
                mScroller.startScroll(Math.abs(getScrollX()), 0, mRightMenuView.getWidth() - Math.abs(getScrollX()), 0, duration);
            }
            invalidate();
        }
    }

    /**
     * 平滑的关闭左侧滑动菜单 默认滑动时间
     */
    public void smoothCloseLeftMenu() {
        if (mLeftMenuView != null) {
            mOpenMenuView = mLeftMenuView;
            smoothCloseMenu();
        }
    }

    /**
     * 平滑的关闭右侧滑动菜单 默认滑动时间
     */
    public void smoothCloseRightMenu() {
        if (mRightMenuView != null) {
            mOpenMenuView = mRightMenuView;
            smoothCloseMenu();
        }
    }

    /**
     * 平滑的关闭滑动菜单 默认滑动时间
     */
    public void smoothCloseMenu() {
        // Log.e("oldSwipeMenuLayout被清空啦！");
        oldSwipeMenuLayout = null;
        mOldTouchedPosition = false;
        smoothCloseMenu(menuScrollerDuration);
    }

    /**
     * 平滑的关闭滑动菜单
     */
    public void smoothCloseMenu(int duration) {
        if (mOpenMenuView != null) {
            // Log.e("smoothCloseMenu >> isLeftMenuView:" + (mOpenMenuView == mLeftMenuView) + " scrollX:" + getScrollX() + " scrollY:" + getScaleY() + " duration:" + duration);
            if (mOpenMenuView == mLeftMenuView) {
                mScroller.startScroll(-Math.abs(getScrollX()), 0, Math.abs(getScrollX()), 0, duration);
            } else {
                mScroller.startScroll(-Math.abs(getScrollX()), 0, Math.abs(getScrollX()), 0, duration);
            }
            invalidate();
        }
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
        final int width = mOpenMenuView == null ? 0 : mOpenMenuView.getWidth();
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

    private void judgeOpenClose(float dx, float dy) {
        // Log.e("judgeOpenClose: isLeftMenuView:" + (mOpenMenuView == mLeftMenuView) + " dx:" + dx + " dy:" + dy);
        if (mOpenMenuView != null) {
            if (Math.abs(getScrollX()) >= (mOpenMenuView.getWidth() * menuAutoOpenPercent)) { // auto open
                if (Math.abs(dx) > mScaledTouchSlop || Math.abs(dy) > mScaledTouchSlop) { // swipe up
                    if (isMenuOpenNotEqual()) smoothCloseMenu();
                    else smoothOpenMenu();
                } else { // normal up
                    if (isMenuOpen()) smoothCloseMenu();
                    else smoothOpenMenu();
                }
            } else { // auto close
                smoothCloseMenu();
            }
        }
    }

    @Override
    public void computeScroll() {
        // Log.e("mScroller.getCurrX():" + mScroller.getCurrX() + " mScroller.getCurrY():" + mScroller.getCurrY());
        if (mScroller.computeScrollOffset() && mOpenMenuView != null) {
            if (mOpenMenuView == mLeftMenuView) {
                scrollTo(-Math.abs(mScroller.getCurrX()), 0);
            } else {
                scrollTo(Math.abs(mScroller.getCurrX()), 0);
            }
            invalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (mOpenMenuView == null) {
            super.scrollTo(x, y);
        } else {
            isShouldResetSwiper = x == 0;
            if (mOpenMenuView == mLeftMenuView) {
                if (x > 0) {
                    x = 0;
                }
                if (x < -mLeftMenuView.getWidth()) {
                    x = -mLeftMenuView.getWidth();
                }
            } else {
                if (x < 0) {
                    x = 0;
                }
                if (x > mRightMenuView.getWidth()) {
                    x = mRightMenuView.getWidth();
                }
            }
            if (x != getScrollX()) {
                super.scrollTo(x, y);
                if (Math.abs(x) == mOpenMenuView.getWidth()) {
                    // Log.e("oldSwipeMenuLayout被赋值啦！");
                    oldSwipeMenuLayout = this;
                }
            }
        }
    }
}
