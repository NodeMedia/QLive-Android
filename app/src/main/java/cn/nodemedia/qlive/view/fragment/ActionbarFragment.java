package cn.nodemedia.qlive.view.fragment;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.nodemedia.qlive.R;
import xyz.tanwb.airship.view.BaseFragment;
import xyz.tanwb.airship.view.BasePresenter;

public abstract class ActionbarFragment<T extends BasePresenter> extends BaseFragment<T> {

    protected FrameLayout content;

    private RelativeLayout actionbar;
    private ImageView actionbarBack;
    private ImageView actionbarTips;
    private TextView actionbarTitle;
    private LinearLayout actionbarMenu;
    private View actionbarBottomLine;

    private LinearLayout functionbar;
    private TextView functionbarButton;
    private RelativeLayout loading;
    private ImageView loadingImage;

    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.layout_actionbar, null);
        if (getLayoutId() > 0) {
            FrameLayout content = (FrameLayout) rootView.findViewById(R.id.content);
            View contentView = inflater.inflate(getLayoutId(), null);
            content.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return rootView;
    }

    @Override
    public void initView(View view, Bundle bundle) {
        actionbar = getView(R.id.actionbar);
        actionbarBack = getView(R.id.actionbar_back);
        actionbarTips = getView(R.id.actionbar_tips);
        actionbarTitle = getView(R.id.actionbar_title);
        actionbarMenu = getView(R.id.actionbar_menu);
        actionbarBottomLine = getView(R.id.actionbar_bottom_line);
        content = getView(R.id.content);
        functionbar = getView(R.id.functionbar);
        functionbarButton = getView(R.id.functionbar_button);
        loading = getView(R.id.loading);
        loadingImage = getView(R.id.loading_image);

        actionbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyNetwork();//判断网络连接状态
    }

    /**
     * 批量设置控件的显示状态
     *
     * @param visible 显示状态
     * @param views   控件集
     */
    public void setVisibility(int visible, View... views) {
        for (View view : views) view.setVisibility(visible);
    }

    /**
     * 设置是否显示加载进度
     *
     * @param visible 显示状态
     */
    public void hasProgress(int visible) {
        hasProgress(loading, visible);
    }

    /**
     * 设置是否显示加载进度
     *
     * @param visible 显示状态
     */
    public void hasProgress(RelativeLayout loading, int visible) {
        if (loading != null) {
            if (loading.getVisibility() == visible) {
                return;
            }
            AnimationDrawable animationDrawable = (AnimationDrawable) loadingImage.getDrawable();
            if (visible == View.VISIBLE) {
                animationDrawable.start();
            } else {
                animationDrawable.stop();
            }
            loading.setVisibility(visible);
        }
    }

    /**
     * 判断是否有网络
     */
    public void verifyNetwork() {
//        if (!NetUtils.isConnected()) {
//            if(mActivity instanceof MainActivity) isRemindshow(View.VISIBLE);
//            else if(mActivity instanceof StartActivity) isRemindshow(View.GONE);
//            else
        //if (mActivity instanceof StartActivity) isRemindshow(View.GONE);
        //else
        // disconnectPage.setVisibility(View.VISIBLE);         //ToastUtils.show(mActivity, "网络连接异常，请检查网络设置");
//        } else {
//            isRemindshow(View.GONE);
        //disconnectPage.setVisibility(View.GONE);
//        }
    }

    /**
     * 是否显示网络提示
     */
    public void isRemindshow(int visible) {
//        if(visible == View.VISIBLE) networkRemind.setVisibility(View.VISIBLE);
//        else networkRemind.setVisibility(View.GONE);
    }

    /**
     * 设置是否显示ActionBar
     *
     * @param visible 显示参数
     */
    public void hasActionBar(int visible) {
        actionbar.setVisibility(visible);
    }

    /**
     * 设置title背景图片
     *
     * @param resId 图片ID
     */
    public void setBackgroundByRes(int resId) {
        actionbar.setBackgroundResource(resId);
    }

    /**
     * 设置title背景颜色
     *
     * @param color 颜色值
     */
    public void setBackgroundByColor(String color) {
        actionbar.setBackgroundColor(Color.parseColor(color));
    }

    /**
     * 设置是否显示返回键
     *
     * @param visible 显示参数
     */
    public void hasBack(int visible) {
        actionbarBack.setVisibility(visible == View.GONE ? View.INVISIBLE : visible);
    }

    /**
     * 设置返回键图片
     *
     * @param resId 图片ID
     */
    public void setBackRes(int resId) {
        actionbarBack.setImageResource(resId);
    }

//    /**
//     * 设置返回键图片
//     *
//     * @param imagePath 网络图片对象信息
//     */
//    public void setBackRes(ImagePath imagePath) {
//        GlideUtils.getCircleImageToUrl(mActivity, imagePath, actionbarBack, R.drawable.actionbar_head);
//        // Glide.with(mActivity).load("http://img3.imgtn.bdimg.com/it/u=2631545466,886965387&fm=21&gp=0.jpg").transform(new TransformToCircle(mContext)).into(actionbarBack);
//    }

    /**
     * 返回键被点击
     */
    public void onBackClick() {
        exit();
    }

    /**
     * 设置是否显示提示标志
     *
     * @param visible 显示参数
     */
    public void hasActionBarTips(int visible) {
        actionbarTips.setVisibility(visible);
    }

    /**
     * 设置是否显示标题
     *
     * @param visible 显示参数
     */
    public void hasTitle(int visible) {
        actionbarTitle.setVisibility(visible);
    }

    /**
     * 设置标题
     *
     * @param resId 标题StringID
     */
    public void setTitle(int resId) {
        setTitle(getResources().getString(resId));
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        actionbarTitle.setText(title);
    }

    /**
     * 设置标题颜色
     *
     * @param color 颜色值
     */
    public void setTitleColor(String color) {
        actionbarTitle.setTextColor(Color.parseColor(color));
    }

    /**
     * 设置标题颜色
     *
     * @param colorRes 颜色值
     */
    public void setTitleColor(int colorRes) {
        actionbarTitle.setTextColor(colorRes);
    }

    /**
     * 设置是否显示菜单
     *
     * @param visible 显示参数
     */
    public void hasMenu(int visible) {
        actionbarMenu.setVisibility(visible);
    }

    /**
     * 添加Title功能
     *
     * @param view 菜单项视图
     */
    public void addMenuItme(View view, LinearLayout.LayoutParams layoutParams) {
        hasMenu(View.VISIBLE);
        actionbarMenu.addView(view, layoutParams);
    }

    /**
     * 添加Title功能
     *
     * @param resId 图片ID
     */
    public ImageView addMenuImageItme(int resId, View.OnClickListener mOnClickListener) {
        ImageView mMenuItme = new ImageView(mActivity);
        mMenuItme.setScaleType(ImageView.ScaleType.CENTER);
        mMenuItme.setImageResource(resId);
        mMenuItme.setOnClickListener(mOnClickListener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dp_48), LinearLayout.LayoutParams.MATCH_PARENT);
        // params.leftMargin = (int) getResources().getDimension(R.dimen.dp_4);
        addMenuItme(mMenuItme, params);
        return mMenuItme;
    }

    /**
     * 添加Title功能
     *
     * @param textId 文字ID
     */
    public TextView addMenuTextItme(int textId, View.OnClickListener mOnClickListener) {
        return addMenuTextItme(textId, R.color.colorTheme, mOnClickListener);
    }

    /**
     * 添加Title功能
     *
     * @param textId       文字ID
     * @param textColorRes 文字颜色资源id
     */
    public TextView addMenuTextItme(int textId, int textColorRes, View.OnClickListener mOnClickListener) {
        TextView mTextView = new TextView(mActivity);
        mTextView.setTextAppearance(mActivity, R.style.ToolBarStyle);
        // mTextView.setMaxWidth((int) getResources().getDimension(R.dimen.dp_144));
        int padding = (int) getResources().getDimension(R.dimen.dp_6);
        mTextView.setPadding(padding, 0, padding, 0);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        mTextView.setTextColor(ContextCompat.getColor(mActivity, textColorRes));
        mTextView.setText(textId);
        mTextView.setOnClickListener(mOnClickListener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        addMenuItme(mTextView, params);
        return mTextView;
    }

    /**
     * 获取底部功能区是否显示
     */
    public boolean isFunctionbar() {
        return functionbar.getVisibility() == View.VISIBLE;
    }

    /**
     * 设置是否显示底部功能区
     *
     * @param visible 显示参数
     */
    public void hasFunctionbar(int visible) {
        functionbar.setVisibility(visible);
    }

    /**
     * 设置功能按钮名称
     *
     * @param resId 标题StringID
     */
    public void setFunctionbarButton(int resId) {
        setFunctionbarButton(getResources().getString(resId));
    }

    /**
     * 设置功能按钮名称
     *
     * @param title 标题
     */
    public void setFunctionbarButton(String title) {
        hasFunctionbar(View.VISIBLE);
        functionbarButton.setText(title);
    }

    /**
     * 设置功能按钮是否能被点击
     *
     * @param isClickable 是否能被点击
     */
    public void setFunctionbarButtonClickable(boolean isClickable) {
        functionbarButton.setBackgroundResource(isClickable ? R.drawable.common_button_background : R.drawable.common_round_gray);
        functionbarButton.setEnabled(isClickable);
    }

    /**
     * 设置底部线的可见性
     *
     * @param visibility 可见性
     */
    public void setBottomLineVisibility(int visibility) {
        actionbarBottomLine.setVisibility(visibility);
    }

    @Override
    public void showProgress() {
        hasProgress(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        hasProgress(View.GONE);
    }

}
