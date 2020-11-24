package xyz.tanwb.airship.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import xyz.tanwb.airship.BaseApplication;
import xyz.tanwb.airship.R;
import xyz.tanwb.airship.utils.StatusBarUtils;
import xyz.tanwb.airship.view.widget.SwipeBackLayout;

public abstract class BaseActivity<T extends BasePresenter> extends RxAppCompatActivity {

    protected Context mContext;
    protected Activity mActivity;
    protected BaseApplication mApplication;

    protected T mPresenter;

    protected Unbinder unbinder;
    protected boolean isOnClick = true;
    protected List<Integer> noLinitClicks;
    protected long clickSleepTime = 300L;
    protected long oldClickTime;

    protected SparseArray<View> views;

    private ImageView ivShadow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(getLayoutId());
        if (hasLightMode()) {
            StatusBarUtils.setStatusBarMode(this, true);
        }
        unbinder = ButterKnife.bind(this);
        mContext = this;
        mActivity = this;
        mApplication = (BaseApplication) getApplication();
        mApplication.addActivity(this);
        mPresenter = getT(this, 0);
        this.initView(savedInstanceState);
        this.initPresenter();
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(LayoutInflater.from(this).inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        if (hasWindowBackground()) {
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground));
        }
        if (hasSwipeFinish()) {
            SwipeBackLayout swipeBackLayout = new SwipeBackLayout(this);
            swipeBackLayout.setOnSwipeBackListener(new SwipeBackLayout.SwipeBackListener() {
                @Override
                public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
                    ivShadow.setAlpha(1 - fractionScreen);
                }

                @Override
                public void onFinish() {
                    exit();
                }
            });
            swipeBackLayout.addView(view);
            ivShadow = new ImageView(this);
            ivShadow.setBackgroundColor(Color.parseColor("#7F000000"));
            RelativeLayout container = new RelativeLayout(this);
            container.addView(ivShadow, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            container.addView(swipeBackLayout);
            super.setContentView(container);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mContext == null) {
            mContext = this;
        }
        if (mActivity == null) {
            mActivity = this;
        }
        if (mApplication == null) {
            mApplication = (BaseApplication) getApplication();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        mPresenter = null;
        mActivity = null;
        mContext = null;
        mApplication.removeActivity(this);
        mApplication = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);

    public abstract void initPresenter();

    /**
     * 是否使用高亮模式
     */
    public boolean hasLightMode() {
        return false;
    }

    /**
     * 是否设置窗口背景
     */
    public boolean hasWindowBackground() {
        return true;
    }

    /**
     * 是否使用滑动返回
     */
    public boolean hasSwipeFinish() {
        return true;
    }

    /**
     * 是否可以继续执行点击时间
     */
    public boolean isCanClick(View view) {
        if (isOnClick) {
            int newClickView = view.getId();
            if (noLinitClicks != null && noLinitClicks.size() > 0) {
                for (int viewId : noLinitClicks) {
                    if (newClickView == viewId) {
                        return true;
                    }
                }
            }
            long newClickTime = System.currentTimeMillis();
            if ((newClickTime - oldClickTime) < clickSleepTime) {
                return false;
            } else {
                oldClickTime = newClickTime;
            }
            return true;
        }
        return false;
    }

    /**
     * 获取上下文对象
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 获取Activity实例
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * 显示进度动画
     */
    public void showProgress() {
    }

    /**
     * 隐藏进度动画
     */
    public void hideProgress() {
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advance(Class<?> cls, Object... params) {
        advance(getAdvanceIntent(cls, params));
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advance(String clsName, Object... params) {
        advance(getAdvanceIntent(clsName, params));
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advance(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.view_in_from_right, R.anim.view_out_to_left);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(Class<?> cls, int requestCode, Object... params) {
        advanceForResult(getAdvanceIntent(cls, params), requestCode);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(String clsName, int requestCode, Object... params) {
        advanceForResult(getAdvanceIntent(clsName, params), requestCode);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.view_in_from_right, R.anim.view_out_to_left);
    }

    private Intent getAdvanceIntent(Class<?> cls, Object... params) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        return putParams(intent, params);
    }

    private Intent getAdvanceIntent(String clsName, Object... params) {
        Intent intent = new Intent();
        intent.setClassName(this, clsName);
        return putParams(intent, params);
    }

    private Intent putParams(Intent intent, Object... params) {
        if (intent != null && params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                intent.putExtra("p" + i, (java.io.Serializable) params[i]);
            }
        }
        return intent;
    }

    /**
     * 退出当前页面
     */
    public void exit() {
        exit(true);
    }

    /**
     * 退出当前页面
     */
    public void exit(boolean isAnim) {
        finish();
        if (isAnim) {
            overridePendingTransition(R.anim.view_in_from_left, R.anim.view_out_to_right);
        }
    }

    /**
     * 获取引用实体
     */
    public <T> T getT(Object o, int i) {
        try {
            return ((Class<T>) ((ParameterizedType) (o.getClass().getGenericSuperclass())).getActualTypeArguments()[i]).newInstance();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 获取控件对象
     */
    public <V extends View> V getView(int viewId) {
        if (views == null) {
            views = new SparseArray<>();
        }
        View view = views.get(viewId);
        if (view == null) {
            view = findViewById(viewId);
            views.put(viewId, view);
        }
        return (V) view;
    }
}
