package cn.nodemedia.qlive.view;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.qlive.R;
import xyz.tanwb.airship.utils.StatusBarUtils;
import xyz.tanwb.airship.view.BaseActivity;
import xyz.tanwb.airship.view.BasePresenter;

public abstract class ActionbarActivity<T extends BasePresenter> extends BaseActivity<T> {

    @BindView(R.id.actionbar_back)
    ImageView actionbarBack;
    @BindView(R.id.actionbar_tips)
    ImageView actionbarTips;
    @BindView(R.id.actionbar_title)
    TextView actionbarTitle;
    @BindView(R.id.actionbar_menu)
    LinearLayout actionbarMenu;
    @BindView(R.id.actionbar_bottom_line)
    View actionbarBottomLine;
    @BindView(R.id.actionbar)
    RelativeLayout actionbar;
    @BindView(R.id.content)
    FrameLayout content;
    @BindView(R.id.functionbar_button)
    TextView functionbarButton;
    @BindView(R.id.functionbar)
    LinearLayout functionbar;
    @BindView(R.id.loading_image)
    ImageView loadingImage;
    @BindView(R.id.loading)
    RelativeLayout loading;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setContentView(int layoutResID) {
//        Window window = getWindow();
//        // 设置透明状态栏,这样才能让 ContentView 向上
//        // window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//
//        // 需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        // 设置状态栏颜色
//        window.setStatusBarColor(Color.WHITE);

        StatusBarUtils.setColorNoTranslucent(this, getResources().getColor(R.color.colorLight));

        View rootView = getLayoutInflater().inflate(R.layout.layout_actionbar, null);
        FrameLayout content = (FrameLayout) rootView.findViewById(R.id.content);
        View contentView = getLayoutInflater().inflate(layoutResID, null);
        content.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(rootView);
    }

    @Override
    public void initView(Bundle bundle) {
    }

    @OnClick({R.id.actionbar_back, R.id.loading})
    public void onBarClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_back:
                onBackClick();
                break;
            case R.id.loading:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyNetwork();//判断网络连接状态
    }

    @Override
    public boolean hasLightMode() {
        return true;
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
    public void hasProgress(RelativeLayout loading, int visible) {
        if (loading != null) {
            if (loading.getVisibility() == visible) {
                return;
            }
            ImageView loadingImage = (ImageView) loading.findViewById(R.id.loading_image);
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
     * 设置是否显示加载进度
     *
     * @param visible 显示状态
     */
    public void hasProgress(int visible) {
        hasProgress(loading, visible);
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

    /**
     * 设置返回键图片
     *
     * @param imagePath 网络图片对象信息
     */
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
        ImageView mMenuItme = new ImageView(this);
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
        return addMenuTextItme(textId, R.color.colorLight, mOnClickListener);
    }

    /**
     * 添加Title功能
     *
     * @param textId       文字ID
     * @param textColorRes 文字颜色资源id
     */
    public TextView addMenuTextItme(int textId, int textColorRes, View.OnClickListener mOnClickListener) {
        TextView mTextView = new TextView(this);
        mTextView.setTextAppearance(mContext, R.style.ToolBarStyle);
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
    public boolean hasSwipeFinish() {
        return false;
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
