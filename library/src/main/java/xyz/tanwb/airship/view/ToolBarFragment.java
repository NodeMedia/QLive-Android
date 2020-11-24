package xyz.tanwb.airship.view;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.R;

public abstract class ToolBarFragment<T extends BasePresenter> extends BaseFragment<T> {

    protected Toolbar toolbar;
    protected TextView toolbarTitle;
    protected FrameLayout content;

    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.layout_toolbar, null);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbarTitle = (TextView) rootView.findViewById(R.id.toolbar_title);
        content = (FrameLayout) rootView.findViewById(R.id.content);
        View contentView = inflater.inflate(getLayoutId(), null);
        content.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return rootView;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        toolbar.setTitle(BaseConstants.NULL);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.exit();
            }
        });
    }
}
