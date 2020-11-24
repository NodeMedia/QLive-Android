package xyz.tanwb.airship.view;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.R;

public abstract class ToolBarActivity<T extends BasePresenter> extends BaseActivity<T> {

    protected Toolbar toolbar;
    protected TextView toolbarTitle;
    protected FrameLayout content;

    @Override
    public void setContentView(int layoutResID) {
        View rootView = getLayoutInflater().inflate(R.layout.layout_toolbar, null);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbarTitle = (TextView) rootView.findViewById(R.id.toolbar_title);
        content = (FrameLayout) rootView.findViewById(R.id.content);
        View contentView = getLayoutInflater().inflate(layoutResID, null);
        content.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(rootView);
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        toolbar.setTitle(BaseConstants.NULL);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
    }
}
