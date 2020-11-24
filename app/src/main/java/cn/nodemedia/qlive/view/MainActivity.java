package cn.nodemedia.qlive.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.legacy.app.FragmentPagerAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import java.util.ArrayList;
import java.util.List;

import cn.nodemedia.qlive.BottomNavigationViewHelper;
import cn.nodemedia.qlive.R;
import cn.nodemedia.qlive.contract.MainContract;
import cn.nodemedia.qlive.view.fragment.AboutFragment;
import cn.nodemedia.qlive.view.fragment.PlayFragment;
import cn.nodemedia.qlive.view.fragment.PushFragment;
import xyz.tanwb.airship.utils.ToastUtils;
import xyz.tanwb.airship.view.widget.NoScrollViewPager;



public class MainActivity extends ActionbarActivity<MainContract.Presenter> implements MainContract.View {

    private NoScrollViewPager mainContent;
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_push:
                    changeFooterState(0);
                    return true;
                case R.id.navigation_play:
                    changeFooterState(1);
                    return true;
//                case R.id.navigation_conv:
//                    changeFooterState(2);
//                    return true;
                case R.id.navigation_about:
                    changeFooterState(3);
                    return true;
            }
            return false;
        }
    };

    private int currentItem;
    private int selectIndex = -1;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        currentItem = intent.getIntExtra("p0", 0);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this,
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(getContext(),"onDenied permission:"+permission,Toast.LENGTH_LONG);
                    }
                });
        hasBack(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assignViews();
        if (savedInstanceState == null) {
            currentItem = getIntent().getIntExtra("p0", 0);
        } else {
            currentItem = savedInstanceState.getInt("p0", 0);
        }
        initData();
    }

    /**
     * 实例化视图控件
     */
    private void assignViews() {
        mainContent = getView(R.id.main_content);
        navigation = getView(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
    }

    /**
     * 初始化数据
     */
    public void initData() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new PushFragment());
        fragmentList.add(new PlayFragment());
//        fragmentList.add(new ConvFragment());
        fragmentList.add(new AboutFragment());
        BasePagerFragmentAdapter fragmentAdapter = new BasePagerFragmentAdapter(getFragmentManager(), fragmentList);
        mainContent.setAdapter(fragmentAdapter);
        mainContent.setOffscreenPageLimit(fragmentList.size());
        mainContent.setCurrentItem(currentItem);
        changeFooterState(currentItem);
    }

    /**
     * 改变选项卡状态
     */
    private void changeFooterState(int position) {
        if (position == selectIndex) {
            return;
        }

        switch (position) {
            case 0:
                setTitle(R.string.title_push);
                break;
            case 1:
                setTitle(R.string.title_play);
                break;
            case 2:
                setTitle(R.string.title_conv);
                break;
            case 3:
                setTitle(R.string.title_about);
                break;
        }

        mainContent.setCurrentItem(position, false);
        selectIndex = position;
    }

    @Override
    public void initPresenter() {
        mPresenter.initPresenter(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("p0", selectIndex);
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
    public void exit() {
        long newClickTime = System.currentTimeMillis();
        if (newClickTime - oldClickTime < 1000) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        } else {
            oldClickTime = newClickTime;
            ToastUtils.show(mActivity, R.string.app_exit);
        }
    }

    @Override
    public boolean hasLightMode() {
        return true;
    }

    @Override
    public boolean hasSwipeFinish() {
        return false;
    }

    public class BasePagerFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments;

        public BasePagerFragmentAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            this.mFragments = fragments;
        }

        public int getCount() {
            return this.mFragments == null ? 0 : this.mFragments.size();
        }

        public Fragment getItem(int position) {
            return this.mFragments == null ? null : (Fragment) this.mFragments.get(position);
        }

        public void setDatas(List<Fragment> datas) {
            this.mFragments = datas;
            this.notifyDataSetChanged();
        }

        public void addDatas(List<Fragment> datas) {
            if (this.mFragments == null) {
                this.mFragments = datas;
            } else {
                this.mFragments.addAll(datas);
            }

            this.notifyDataSetChanged();
        }

        public void addData(int position, Fragment model) {
            if (this.mFragments == null) {
                this.mFragments = new ArrayList();
            }

            this.mFragments.add(position, model);
            this.notifyDataSetChanged();
        }

        public void clearDatas() {
            if (this.mFragments != null) {
                this.mFragments.clear();
            }

            this.mFragments = null;
        }

        public void setItem(Fragment oldModel, Fragment newModel) {
            this.setItem(this.mFragments.indexOf(oldModel), newModel);
        }

        public void setItem(int location, Fragment newModel) {
            this.mFragments.set(location, newModel);
            this.notifyDataSetChanged();
        }

        public void removeItem(Fragment model) {
            this.removeItem(this.mFragments.indexOf(model));
        }

        public void removeItem(int position) {
            this.mFragments.remove(position);
            this.notifyDataSetChanged();
        }
    }


}
