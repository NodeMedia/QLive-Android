package xyz.tanwb.airship.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment<T extends BasePresenter> extends RxFragment {

    protected BaseActivity mActivity;

    protected T mPresenter;

    protected Unbinder unbinder;
    protected boolean isOnClick = true;
    protected List<Integer> noLinitClicks;
    protected long clickSleepTime = 300;
    protected long oldClickTime;

    protected SparseArray<View> views;

    // 标识fragment视图已经初始化完毕
    protected boolean isViewPrepared;
    // 标识已经触发过懒加载数据
    protected boolean hasFetchData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mPresenter = getT(this, 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getRootView(inflater, container);
        if (view == null) {
            view = inflater.inflate(getLayoutId(), container, false);
        }
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initView(view, savedInstanceState);
        isViewPrepared = true;
        if (getUserVisibleHint() && !hasFetchData && isViewPrepared) {
            hasFetchData = true;
            this.initPresenter();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !hasFetchData && isViewPrepared) {
            hasFetchData = true;
            this.initPresenter();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mActivity == null) {
            mActivity = (BaseActivity) getActivity();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        hasFetchData = false;
        isViewPrepared = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        mPresenter = null;
        mActivity = null;
    }

    /**
     * 获取根View
     */
    public View getRootView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    public abstract int getLayoutId();

    public abstract void initView(View view, Bundle savedInstanceState);

    /**
     * 使用懒加载的方式获取数据，仅在满足fragment可见和视图已经准备好的时候调用一次
     */
    public abstract void initPresenter();

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
     * 显示进度动画
     */
    public void showProgress() {
        mActivity.showProgress();
    }

    /**
     * 隐藏进度动画
     */
    public void hideProgress() {
        mActivity.hideProgress();
    }

    /**
     * 跳转Activity
     */
    public void advance(Class<?> cls, Object... params) {
        mActivity.advance(cls, params);
    }

    /**
     * 跳转Activity
     */
    public void advance(String clsName, Object... params) {
        mActivity.advance(clsName, params);
    }

    /**
     * 跳转Activity
     */
    public void advance(Intent intent) {
        mActivity.advance(intent);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(Class<?> cls, int requestCode, Object... params) {
        mActivity.advanceForResult(cls, requestCode, params);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(String clsName, int requestCode, Object... params) {
        mActivity.advanceForResult(clsName, requestCode, params);
    }

    /**
     * 跳转Activity并接收返回数据
     */
    public void advanceForResult(Intent intent, int requestCode) {
        mActivity.advanceForResult(intent, requestCode);
    }

    /**
     * 退出当前页面
     */
    public void exit() {
        mActivity.exit();
    }

    /**
     * 退出当前页面
     */
    public void exit(boolean isAnim) {
        mActivity.exit(isAnim);
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
            view = getActivity().findViewById(viewId);
            views.put(viewId, view);
        }
        return (V) view;
    }
}
