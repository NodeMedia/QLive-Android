package xyz.tanwb.airship.imgsel;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import xyz.tanwb.airship.R;
import xyz.tanwb.airship.utils.StatusBarUtils;
import xyz.tanwb.airship.view.ToolBarActivity;

public class ImgSelActivity extends ToolBarActivity<ImgselContract.Presenter> implements ImgselContract.View {

    public static ImgSelConfig config;

    private Button imgselFolder;

    @Override
    public int getLayoutId() {
        return R.layout.layout_imgsel;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        imgselFolder = getView(R.id.imgsel_folder);
        imgselFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClick(getView(R.id.imgsel_bottom_layout));
            }
        });

        toolbarTitle.setText(config.title);

        if (config.statusBarColor > -1) {
            StatusBarUtils.setColorNoTranslucent(mActivity, config.statusBarColor);
        }
        if (config.backResId > -1) {
            toolbar.setNavigationIcon(config.backResId);
        }
    }

    @Override
    public void initPresenter() {
        mPresenter.initPresenter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (config != null && config.maxNum > 1) {
            menu.add(0, 1, 0, String.format(getString(R.string.imgsel_confirm), mPresenter.getSelImageSize(), config.maxNum)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPresenter.resultExit();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean hasLightMode() {
        return config != null && config.statusBarTextToBlack;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return getView(R.id.imgsel_recycler);
    }

    @Override
    public ImgSelConfig getImgSelConfig() {
        return config;
    }

    @Override
    public void setBtnAlbumSelected(String content) {
        imgselFolder.setText(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.handleResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
