package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import xyz.tanwb.airship.R;

public class PullToRefreshView extends RelativeLayout {

    private static final int INIT = 0;// 初始状态
    private static final int RELEASE_TO_REFRESH = 1; // 释放刷新
    private static final int REFRESHING = 2; // 正在刷新
    private static final int RELEASE_TO_LOAD = 3;// 释放加载
    private static final int LOADING = 4;// 正在加载

    private Context mContext;
    private Scroller mScroller;
    private int mScaledTouchSlop;// 触发滑动的距离
    private RotateAnimation mRotateUpAnim;//箭头旋转动画(向上)
    private RotateAnimation mRotateDownAnim;//箭头旋转动画(向下)

    // 下拉视图
    private View refreshView;
    // 下拉的箭头
    private ImageView refreshArrowView;
    // 正在刷新的图标
    private ProgressBar refreshingView;
    // 刷新结果图标
    private ImageView refreshStateView;
    // 刷新状态文字
    private TextView refreshTextView;

    // 实现了Pullable接口的内容View
    private View contentView;

    // 上拉视图
    private View loadmoreView;
    // 上拉的箭头
    private ImageView loadmoreArrowView;
    // 正在加载的图标
    private ProgressBar loadingView;
    // 加载结果图标
    private ImageView loadStateView;
    // 加载状态文字
    private TextView loadTextView;

    // 刷新回调接口
    private OnRefreshListener onRefreshListener;
    // 加载更多回调接口
    private OnLoadMoreListener onLoadMoreListener;

    private boolean canRefresh = true;
    private boolean canLoadMore = true;

    // 是否第一次执行布局
    private boolean isLayout = true;
    // 触发刷新的Y轴移动距离
    private float refreshYDist;

    private boolean trigger;

    //记录上次的触摸点X坐标
    private float lastX;
    //记录上次的触摸点Y坐标
    private float lastY;
    //下拉或上拉的距离（大于0为下拉,小于0为上拉）
    private float pullYDist;
    // 过滤多点触碰
    private boolean isPointerDown;
    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    private float radio = 2;
    // 当前状态
    private int mState;

    public PullToRefreshView(Context context) {
        super(context);
        initView(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(180);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(180);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isLayout) {
            isLayout = false;

            if (getChildCount() > 1) {
                throw new IllegalArgumentException("Only allowed to have a sub view");
            }

            contentView = getChildAt(0);

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            refreshView = LayoutInflater.from(mContext).inflate(R.layout.layout_loading, null);
            refreshArrowView = (ImageView) refreshView.findViewById(R.id.loading_arrow);
            refreshingView = (ProgressBar) refreshView.findViewById(R.id.loading_progressbar);
            refreshStateView = (ImageView) refreshView.findViewById(R.id.loading_state);
            refreshTextView = (TextView) refreshView.findViewById(R.id.loading_text);
            refreshView.setLayoutParams(lp);

            addView(refreshView);

            if (contentView instanceof AdapterView) {
                canLoadMore = true;
                loadmoreView = LayoutInflater.from(mContext).inflate(R.layout.layout_loading, null);
                loadmoreArrowView = (ImageView) loadmoreView.findViewById(R.id.loading_arrow);
                loadingView = (ProgressBar) loadmoreView.findViewById(R.id.loading_progressbar);
                loadStateView = (ImageView) loadmoreView.findViewById(R.id.loading_state);
                loadTextView = (TextView) loadmoreView.findViewById(R.id.loading_text);
                loadmoreView.setLayoutParams(lp);
                addView(loadmoreView);
            } else {
                canLoadMore = false;
            }

            changeState(INIT);
        }

        if (refreshYDist <= 10) {
            refreshYDist = refreshView.getMeasuredHeight() + 10;
        }

        contentView.layout(0, (int) pullYDist, contentView.getMeasuredWidth(), (int) pullYDist + contentView.getMeasuredHeight());
        // 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分
        if (refreshView != null) {
            refreshView.layout(0, (int) pullYDist - refreshView.getMeasuredHeight(), refreshView.getMeasuredWidth(), (int) pullYDist);
        }
        if (loadmoreView != null) {
            loadmoreView.layout(0, (int) pullYDist + getMeasuredHeight(), loadmoreView.getMeasuredWidth(), (int) pullYDist + getMeasuredHeight() + loadmoreView.getMeasuredHeight());
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            pullYDist = mScroller.getCurrY();
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
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPointerDown = false;
                trigger = false;
                lastX = ev.getX();
                lastY = ev.getY();
                changeState(INIT);
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
                if (!trigger) {
                    float disX = ev.getX() - lastX;
                    float disY = ev.getY() - lastY;
                    // Log.e("disX:" + disX + " disY:" + disY + " mScaledTouchSlop:" + mScaledTouchSlop + " canPullDown():" + canPullDown() + " canPullUp():" + canPullUp());
                    if (disY > 0 && disY > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop && canPullDown()) {
                        trigger = true;
                    } else if (disY < 0 && Math.abs(disY) > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop && canPullUp()) {
                        trigger = true;
                    }
                }
                if (!isPointerDown && trigger) {
                    // 计算Y轴偏移量,对实际滑动距离做缩小，造成用力拉的感觉
                    pullYDist = pullYDist + (ev.getY() - lastY) / radio;
                    // 记录当前触摸Y坐标
                    lastY = ev.getY();
                    // 根据下拉距离改变比例
                    radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * Math.abs(pullYDist)));

                    if ((mState == INIT || mState == RELEASE_TO_REFRESH) && pullYDist > 0 && canPullDown() && canRefresh) {
                        if (pullYDist > getMeasuredHeight()) {
                            pullYDist = getMeasuredHeight();
                        }

                        if (pullYDist < refreshYDist && mState == RELEASE_TO_REFRESH) {
                            changeState(INIT);
                        } else if (pullYDist > refreshYDist && mState == INIT) {
                            changeState(RELEASE_TO_REFRESH);
                        }
                    } else if ((mState == INIT || mState == RELEASE_TO_LOAD) && pullYDist < 0 && canPullUp() && canLoadMore) {
                        if (pullYDist < -getMeasuredHeight()) {
                            pullYDist = -getMeasuredHeight();
                        }

                        if (-pullYDist < refreshYDist && mState == RELEASE_TO_LOAD) {
                            changeState(INIT);
                        } else if (-pullYDist > refreshYDist && mState == INIT) {
                            changeState(RELEASE_TO_LOAD);
                        }
                    } else {
                        pullYDist = 0;
                    }

                    requestLayout();
                    if (Math.abs(pullYDist) > mScaledTouchSlop) {
                        // 防止下拉过程中误触发长按事件和点击事件
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (refreshView != null && refreshView.getBottom() > 0) {
                    pullYDist = refreshView.getBottom();
                } else if (loadmoreView != null && loadmoreView.getTop() < getMeasuredHeight()) {
                    pullYDist = loadmoreView.getTop() - getMeasuredHeight();
                } else {
                    pullYDist = 0;
                }

                if (pullYDist != 0) {
                    changeDist();
                }
                break;
        }
        // 事件分发交给父类
        return super.dispatchTouchEvent(ev);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        // Log.e("onInterceptTouchEvent action:" + ev.getAction());
//        boolean isIntercepted = false;
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                isIntercepted = mState != INIT;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                isIntercepted = trigger;
//                break;
//        }
//        // if (isIntercepted) {
//        // return isIntercepted;
//        // }
//        return isIntercepted;
//    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.e("onInterceptTouchEvent action:" + ev.getAction());
//        boolean isIntercepted = false;
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                lastX = ev.getX();
//                lastY = ev.getY();
//                isIntercepted = mState != INIT;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float disX = ev.getX() - lastX;
//                float disY = ev.getY() - lastY;
//                if (disY > 0 && disY > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop && canPullDown()) {
//                    isIntercepted = true;
//                } else if (disY < 0 && Math.abs(disY) > mScaledTouchSlop && Math.abs(disX) < mScaledTouchSlop && canPullUp()) {
//                    isIntercepted = true;
//                }
//                // isIntercepted = disY > mScaledTouchSlop && disX < mScaledTouchSlop && (canPullDown() || canPullUp());
//                break;
//            case MotionEvent.ACTION_UP:
//                isIntercepted = true;
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                isIntercepted = true;
//                break;
//        }
//        // if (isIntercepted) {
//        // return isIntercepted;
//        // }
//        return isIntercepted;
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        Log.e("onTouchEvent action:" + ev.getAction());
//        switch (ev.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                isPointerDown = false;
//                lastY = ev.getY();
//                lastX = ev.getX();
//                changeState(INIT);
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                isPointerDown = true;
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                if (ev.getPointerCount() == 1) {
//                    isPointerDown = false;
//                    lastY = ev.getY();
//                } else {
//                    isPointerDown = true;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (!isPointerDown) {
//                    // 计算Y轴偏移量,对实际滑动距离做缩小，造成用力拉的感觉
//                    pullYDist = pullYDist + (ev.getY() - lastY) / radio;
//                    // 记录当前触摸Y坐标
//                    lastY = ev.getY();
//                    // 根据下拉距离改变比例
//                    radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * Math.abs(pullYDist)));
//
//                    if ((mState == INIT || mState == RELEASE_TO_REFRESH) && pullYDist > 0 && canPullDown()) {
//                        if (pullYDist > getMeasuredHeight()) {
//                            pullYDist = getMeasuredHeight();
//                        }
//
//                        if (pullYDist < refreshYDist && mState == RELEASE_TO_REFRESH) {
//                            changeState(INIT);
//                        } else if (pullYDist > refreshYDist && mState == INIT) {
//                            changeState(RELEASE_TO_REFRESH);
//                        }
//                    } else if ((mState == INIT || mState == RELEASE_TO_LOAD) && pullYDist < 0 && canPullUp()) {
//                        if (pullYDist < -getMeasuredHeight()) {
//                            pullYDist = -getMeasuredHeight();
//                        }
//
//                        if (-pullYDist < refreshYDist && mState == RELEASE_TO_LOAD) {
//                            changeState(INIT);
//                        } else if (-pullYDist > refreshYDist && mState == INIT) {
//                            changeState(RELEASE_TO_LOAD);
//                        }
//                    } else {
//                        pullYDist = 0;
//                    }
//
//                    requestLayout();
//                    if (Math.abs(pullYDist) > mScaledTouchSlop) {
//                        // 防止下拉过程中误触发长按事件和点击事件
//                        ev.setAction(MotionEvent.ACTION_CANCEL);
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                // Log.e("pullYDist Old:" + pullYDist);
//                if (refreshView != null && refreshView.getBottom() > 0) {
//                    pullYDist = refreshView.getBottom();
//                } else if (loadmoreView != null && loadmoreView.getTop() < getMeasuredHeight()) {
//                    pullYDist = loadmoreView.getTop() - getMeasuredHeight();
//                } else {
//                    pullYDist = 0;
//                }
//                // Log.e("pullYDist New:" + pullYDist);
//                if (pullYDist != 0) {
//                    changeDist();
//                }
//                break;
//        }
//        // return super.onTouchEvent(ev);
//        return true;
//    }

    private void changeDist() {
        switch (mState) {
            case RELEASE_TO_REFRESH:
                changeState(REFRESHING);
                if (onRefreshListener != null) {
                    onRefreshListener.onRefresh(this);
                }
                mScroller.startScroll(0, (int) pullYDist, 0, (int) (refreshYDist - pullYDist));
                break;
            case REFRESHING:
                mScroller.startScroll(0, (int) pullYDist, 0, (int) (refreshYDist - pullYDist));
                break;
            case RELEASE_TO_LOAD:
                changeState(LOADING);
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore(this);
                }
                mScroller.startScroll(0, (int) pullYDist, 0, (int) -(pullYDist + refreshYDist));
                break;
            case LOADING:
                mScroller.startScroll(0, (int) pullYDist, 0, (int) -(pullYDist + refreshYDist));
                break;
            default:
                mScroller.startScroll(0, (int) pullYDist, 0, (int) -pullYDist);
                break;
        }
        invalidate();// 触发 computeScroll
    }

    private void changeState(int toState) {
        switch (toState) {
            case INIT:
                // 下拉布局初始状态
                if (refreshView != null) {
                    refreshTextView.setText(R.string.refresh_header_hint_normal);
                    refreshArrowView.setVisibility(View.VISIBLE);
                    refreshStateView.setVisibility(View.GONE);
                    refreshingView.setVisibility(View.GONE);
                }

                // 上拉布局初始状态
                if (loadmoreView != null) {
                    loadTextView.setText(R.string.refresh_footer_hint_normal);
                    loadmoreArrowView.setVisibility(View.VISIBLE);
                    loadStateView.setVisibility(View.GONE);
                    loadingView.setVisibility(View.GONE);
                }

                switch (mState) {
                    case RELEASE_TO_REFRESH:
                        refreshArrowView.startAnimation(mRotateDownAnim);
                        break;
                    case RELEASE_TO_LOAD:
                        loadmoreArrowView.startAnimation(mRotateDownAnim);
                        break;
                }
                break;
            case RELEASE_TO_REFRESH:
                // 释放刷新状态
                refreshTextView.setText(R.string.refresh_header_hint_ready);
                refreshArrowView.startAnimation(mRotateUpAnim);
                break;
            case REFRESHING:
                // 正在刷新状态
                refreshingView.setVisibility(View.VISIBLE);
                refreshArrowView.setVisibility(View.GONE);
                refreshArrowView.clearAnimation();
                refreshTextView.setText(R.string.refreshing);
                break;
            case RELEASE_TO_LOAD:
                // 释放加载状态
                loadTextView.setText(R.string.refresh_footer_hint_ready);
                loadmoreArrowView.startAnimation(mRotateUpAnim);
                break;
            case LOADING:
                // 正在加载状态
                loadingView.setVisibility(View.VISIBLE);
                loadmoreArrowView.setVisibility(View.GONE);
                loadmoreArrowView.clearAnimation();
                loadTextView.setText(R.string.loading);
                break;
        }
        mState = toState;
    }

    public boolean canPullDown() {
        if (canRefresh) {
            if (contentView instanceof AdapterView) {
                if (((AdapterView) contentView).getCount() == 0) {
                    return true;
                } else {
                    int firstVisiblePosition = ((AdapterView) contentView).getFirstVisiblePosition();
                    return firstVisiblePosition == 0 && getChildAt(firstVisiblePosition).getTop() >= 0;
                }
            } else if (contentView instanceof RecyclerView) {
                return !contentView.canScrollVertically(-1);
            } else if (contentView instanceof ScrollView) {
                return contentView.getScrollY() == 0;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean canPullUp() {
        if (canLoadMore) {
            if (contentView instanceof AdapterView) {
                int chlidCount = ((AdapterView) contentView).getCount();
                if (chlidCount == 0) {
                    return false;
                } else {
                    int lastVisiblePosition = ((AdapterView) contentView).getLastVisiblePosition();
                    return lastVisiblePosition == chlidCount - 1 && getChildAt(lastVisiblePosition).getBottom() <= contentView.getMeasuredHeight();
                }
            } else if (contentView instanceof RecyclerView) {
                // computeVerticalScrollExtent()是当前屏幕显示的区域高度
                // computeVerticalScrollOffset() 是当前屏幕之前滑过的距离
                // computeVerticalScrollRange()是整个View控件的高度
                return !contentView.canScrollVertically(1);
            } else if (contentView instanceof ScrollView) {
                /**
                 * 监听ScrollView滑动到顶端和底部
                 *
                 * 注意事项:
                 * 1 mScrollView.getChildAt(0).getMeasuredHeight()表示:
                 *   ScrollView所占的高度.即ScrollView内容的高度.常常有一
                 *   部分内容要滑动后才可见,这部分的高度也包含在了
                 *   mScrollView.getChildAt(0).getMeasuredHeight()中
                 *
                 * 2 view.getScrollY表示:
                 *   ScrollView顶端已经滑出去的高度
                 *
                 * 3 view.getHeight()表示:
                 *   ScrollView的可见高度
                 *
                 */
                int scrollY = contentView.getScrollY();
                int height = contentView.getHeight();
                int scrollViewMeasuredHeight = ((ScrollView) contentView).getChildAt(0).getMeasuredHeight();

                return (scrollY + height) == scrollViewMeasuredHeight;
            } else {
                return false;
            }
        }
        return false;
    }

    public void setRefreshEnable(boolean canRefresh) {
        this.canRefresh = canRefresh;
    }

    public void setLoadMoreEnable(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public void refreshFinish(boolean backState) {
        if (mState == REFRESHING) {
            refreshingView.setVisibility(View.GONE);
            if (backState) {
                refreshStateView.setVisibility(View.VISIBLE);
                refreshStateView.setImageResource(R.drawable.refresh_succeed);
                refreshTextView.setText(R.string.refresh_succeed);
            } else {
                refreshStateView.setVisibility(View.VISIBLE);
                refreshStateView.setImageResource(R.drawable.refresh_failed);
                refreshTextView.setText(R.string.refresh_fail);
            }
            changeState(INIT);
            changeDist();
        }
    }

    public void loadmoreFinish(boolean backState) {
        if (mState == LOADING) {
            loadingView.setVisibility(View.GONE);
            if (backState) {
                loadStateView.setVisibility(View.VISIBLE);
                loadStateView.setImageResource(R.drawable.refresh_succeed);
                loadTextView.setText(R.string.load_succeed);
            } else {
                loadStateView.setVisibility(View.VISIBLE);
                loadStateView.setImageResource(R.drawable.refresh_failed);
                loadTextView.setText(R.string.load_fail);
            }
            changeState(INIT);
            changeDist();
        }
    }

    public void autoRefresh() {
        AutoRefreshAndLoadTask task = new AutoRefreshAndLoadTask();
        task.execute(20);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        onLoadMoreListener = listener;
    }

    private class AutoRefreshAndLoadTask extends AsyncTask<Integer, Float, String> {

        @Override
        protected String doInBackground(Integer... params) {
            while (pullYDist < 4 / 3 * refreshYDist) {
                pullYDist += 10;
                publishProgress(pullYDist);
                try {
                    Thread.sleep(params[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            if (pullYDist > refreshYDist) {
                changeState(RELEASE_TO_REFRESH);
            }
            requestLayout();
        }

        @Override
        protected void onPostExecute(String result) {
            changeState(REFRESHING);
            // 刷新操作
            if (onRefreshListener != null) {
                onRefreshListener.onRefresh(PullToRefreshView.this);
            }
        }
    }

    public interface OnRefreshListener {
        void onRefresh(PullToRefreshView pullToRefreshLayout);
    }

    public interface OnLoadMoreListener {
        void onLoadMore(PullToRefreshView pullToRefreshLayout);
    }

}
