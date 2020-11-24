package cn.nodemedia.qlive.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import cn.nodemedia.NodeCameraView;
import cn.nodemedia.qlive.R;
import cn.nodemedia.qlive.contract.PushContract;
import xyz.tanwb.airship.view.BaseActivity;

public class PushActivity extends BaseActivity<PushContract.Presenter> implements PushContract.View, View.OnClickListener {

    private NodeCameraView pushSurface;
    private ImageView pushBack,pushSwitch,pushFlash;
    private TextView pushButton;

    @Override
    public int getLayoutId() {
        return R.layout.activity_push;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
//        StatusBarUtils.setColorToTransparent(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int videoOrientation = Integer.parseInt(sp.getString("video_orientation", "0"));
        if(videoOrientation == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if(videoOrientation == 2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        assignViews();
    }


    /**
     * 实例化视图控件
     */
    private void assignViews() {
        pushSurface = getView(R.id.push_surface);
        pushBack = getView(R.id.push_back);
        pushBack.setOnClickListener(this);
        pushSwitch = getView(R.id.push_switch);
        pushSwitch.setOnClickListener(this);
        pushFlash = getView(R.id.push_flash);
        pushFlash.setOnClickListener(this);
        pushButton = getView(R.id.push_button);
        pushButton.setOnClickListener(this);
    }

    @Override
    public void initPresenter() {
        mPresenter.initPresenter(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.push_back:
                exit();
                break;
            case R.id.push_switch:
                mPresenter.switchCamera();
                FlipAnimatorXViewShow(pushSwitch,pushSwitch,300);
                break;
            case R.id.push_flash:
                mPresenter.switchFlash();
                break;
            case R.id.push_button:
                mPresenter.pushChange();
                pushButton.setText(R.string.push_wait);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void FlipAnimatorXViewShow(final View oldView, final View newView, final long time) {

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(oldView, "rotationY", 0, 90);
        final ObjectAnimator animator2 = ObjectAnimator.ofFloat(newView, "rotationY", -90, 0);
        animator2.setInterpolator(new OvershootInterpolator(2.0f));

        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                oldView.setVisibility(View.GONE);
                animator2.setDuration(time).start();
                newView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator1.setDuration(time).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public NodeCameraView getNodeCameraView() {
        return pushSurface;
    }

    @Override
    public void buttonAvailable(boolean isStarting) {
//        pushButton.setClickable(true);
        pushButton.setBackgroundColor(isStarting ? 0x3FF9493B : 0x3F000000);
        pushButton.setText(isStarting ? R.string.push_stop : R.string.push_start);
    }

    @Override
    public void buttonUnavailability() {
//        pushButton.setClickable(false);
//        pushButton.setTextColor(mContext.getResources().getColor(R.color.colorGray));

    }

    @Override
    public void flashChange(boolean onOrOff) {
        if(onOrOff) {
            pushFlash.setImageResource(R.drawable.ic_flash_on);
        } else {
            pushFlash.setImageResource(R.drawable.ic_flash_off);
        }
    }
}
