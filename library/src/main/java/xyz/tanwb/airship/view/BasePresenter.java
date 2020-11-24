package xyz.tanwb.airship.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import java.util.List;

import xyz.tanwb.airship.rxjava.RxBusManage;
import xyz.tanwb.airship.utils.ToastUtils;

public abstract class BasePresenter<T extends BaseView> {

    protected Context mContext;
    protected Activity mActivity;
    protected RxBusManage mRxBusManage;
    protected T mView;

    /**
     * 初始化
     */
    public void initPresenter(T v) {
        this.mView = v;
        mContext = mView.getContext();
        mActivity = mView.getActivity();
        mRxBusManage = new RxBusManage();
        this.onStart();
    }

    public abstract void onStart();

    /**
     * 开始执行耗时操作，显示加载动画
     */
    public void onAtte() {
        if (mView != null) {
            mView.showProgress();
        }
    }

    /**
     * 操作成功，隐藏加载动画
     */
    public void onSucc() {
        if (mView != null) {
            mView.hideProgress();
        }
    }

    /**
     * 操作失败，隐藏加载动画
     */
    public void onFail(String strMsg) {
        if (mView != null) {
            mView.hideProgress();
            if (!TextUtils.isEmpty(strMsg)) {
                ToastUtils.show(mContext, strMsg);
            }
        }
    }

    /**
     * 批量设置控件是否可见
     */
    public void setVisibility(int visible, View... views) {
        for (View view : views) {
            view.setVisibility(visible);
        }
    }

    /**
     * 判断String是否不为Null
     */
    public boolean isNotEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    /**
     * 判断List是否不为Null
     */
    public <T> boolean isNotNull(List<T> objects) {
        return objects != null && objects.size() > 0;
    }

    /**
     * 执行完毕，销毁相关对象
     */
    public void onDestroy() {
        mView = null;
        if (mRxBusManage != null) {
            mRxBusManage.clear();
            mRxBusManage = null;
        }
        mActivity = null;
        mContext = null;
    }

}
