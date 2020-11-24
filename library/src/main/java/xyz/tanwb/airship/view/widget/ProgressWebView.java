package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ProgressBar;

import xyz.tanwb.airship.rxjava.RxBus;

public class ProgressWebView extends WebView {

    private ProgressBar progressbar;

    public ProgressWebView(Context context) {
        super(context);
        initView(context);
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProgressWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 15, 0, -2));
        addView(progressbar);
        setWebChromeClient(new WebChromeClient());
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
                // EventBus.getDefault().post(new EventBusInfo(ProgressWebView.class.getName()));
                RxBus.getInstance().post(ProgressWebView.class.getName());
            } else {
                if (progressbar.getVisibility() == GONE) {
                    progressbar.setVisibility(VISIBLE);
                }
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

}




