package xyz.tanwb.airship.view.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.bumptech.glide.Glide;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import xyz.tanwb.airship.R;
import xyz.tanwb.airship.utils.ScreenUtils;
import xyz.tanwb.airship.view.adapter.BasePagerAdapter;

public class SlideView extends RelativeLayout implements Runnable, OnPageChangeListener, View.OnClickListener {

    /**
     * 引导小点图片
     */
    private int dotsImageResource;

    /**
     * 引导小点位置
     */
    private int dotsPosition;

    /**
     * 引导小点间距
     */
    private float dotsSpacing = getResources().getDimension(R.dimen.dp_10);

    /**
     * 引导小点的背景颜色或背景图的透明度，取值为0-1,0代表透明
     */
    private float dotsAlpha = 0.8F;

    /**
     * ViewPager是否自动切换
     */
    private boolean isAutoShift = true;

    /**
     * ViewPager自动切换的时间间隔，单位为s，默认为5s
     */
    private int intervalTime = 5;

    /**
     * ViewPager宽高比,0表示均参照父类
     */
    private float aspectRatio = 0.618f;

    /**
     * 自动轮播的切换时间（控制速度）,默认为800ms
     */
    private int transformDuration = 800;

    private boolean isLane;
    private float laneMarginLeft = getResources().getDimension(R.dimen.dp_20);
    private float laneMarginRight = getResources().getDimension(R.dimen.dp_20);
    private float lanePageMargin = getResources().getDimension(R.dimen.dp_6);

    private ViewPager viewPager;
    private LinearLayout dotsLayout;

    private List<View> contentsList;
    private List<ImageView> dotsList;

    private BasePagerAdapter pagerAdapter;

    private boolean isStartPlay;
    private boolean isAutoPlay;
    private int countdown;

    private OnItemChildClickListener onItemChildClickListener;

    public SlideView(Context context) {
        this(context, null);
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
            dotsImageResource = a.getResourceId(R.styleable.SlideView_svDotsImageResource, dotsImageResource);
            dotsPosition = a.getInteger(R.styleable.SlideView_svDotsPosition, dotsPosition);
            dotsSpacing = a.getDimension(R.styleable.SlideView_svDotsSpacing, dotsSpacing);
            dotsAlpha = a.getFloat(R.styleable.SlideView_svDotsAlpha, dotsAlpha);
            isAutoShift = a.getBoolean(R.styleable.SlideView_svIsAutoShift, isAutoShift);
            intervalTime = a.getInteger(R.styleable.SlideView_svIntervalTime, intervalTime);
            aspectRatio = a.getFloat(R.styleable.SlideView_svAspectRatio, aspectRatio);
            transformDuration = a.getInt(R.styleable.SlideView_transfromDuration, transformDuration);

            isLane = a.getBoolean(R.styleable.SlideView_isLane, false);
            // laneMarginLeft = a.getFloat(R.styleable.SlideView_laneMarginLeft, 0);
            // laneMarginRight = a.getFloat(R.styleable.SlideView_laneMarginRight, 0);
            // lanePageMargin = a.getFloat(R.styleable.SlideView_lanePageMargin, 0);

            laneMarginLeft = 60;
            laneMarginRight = 60;
            lanePageMargin = 20;

            a.recycle();
        }

        setMeasuredDimension(ScreenUtils.getScreenWidth(), (int) (ScreenUtils.getScreenWidth() * aspectRatio));

        viewPager = new ViewPager(getContext());
        viewPager.setId(R.id.slidepager);
        viewPager.addOnPageChangeListener(this);

        contentsList = new ArrayList<>();

        initViewPagerScroll();

        LayoutParams viewPagerLP;
        if (isLane) {
            setClipChildren(false);
            viewPager.setClipChildren(false);
            viewPager.setPageMargin((int) lanePageMargin);
            viewPager.setOffscreenPageLimit(3);

            viewPagerLP = new LayoutParams(ScreenUtils.getScreenWidth(), (int) ((ScreenUtils.getScreenWidth() - laneMarginLeft - laneMarginRight) * aspectRatio));
            viewPagerLP.setMargins((int) laneMarginLeft, 0, (int) laneMarginRight, 0);
        } else {
            viewPagerLP = new LayoutParams(ScreenUtils.getScreenWidth(), (int) (ScreenUtils.getScreenWidth() * aspectRatio));
        }

        addView(viewPager, viewPagerLP);

        if (dotsImageResource != 0) {
            dotsList = new ArrayList<>();
            dotsLayout = new LinearLayout(getContext());
            LayoutParams dotsLayoutLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            switch (dotsPosition) {
                case 0:
                    dotsLayoutLP.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.slidepager);
                    dotsLayoutLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                case 1:
                    dotsLayoutLP.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.slidepager);
                    dotsLayoutLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    break;
                case 2:
                    dotsLayoutLP.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.slidepager);
                    dotsLayoutLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    break;
            }
            addView(dotsLayout, dotsLayoutLP);
        }
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getImageNumber() {
        return dotsList.size();
    }

    public void setImageViews(List<Integer> resIds) {
        contentsList.clear();

        ImageView imageView1 = getImageView();
        imageView1.setImageResource(resIds.get(resIds.size() - 1));
        contentsList.add(imageView1);

        for (int i = 0; i < resIds.size(); i++) {
            ImageView imageView = getImageView();
            imageView.setImageResource(resIds.get(i));
            contentsList.add(imageView);
        }

        ImageView imageView2 = getImageView();
        imageView2.setImageResource(resIds.get(0));
        contentsList.add(imageView2);

        initSlideView();
    }

    public void setImageViewsToUrl(List<String> urls) {
        contentsList.clear();

        ImageView imageView1 = getImageView();
        Glide.with(getContext()).load(urls.get(urls.size() - 1)).thumbnail(0.1F).into(imageView1);
        contentsList.add(imageView1);

        for (int i = 0; i < urls.size(); i++) {
            ImageView imageView = getImageView();
            Glide.with(getContext()).load(urls.get(i)).thumbnail(0.1F).into(imageView);
            contentsList.add(imageView);
        }

        ImageView imageView2 = getImageView();
        Glide.with(getContext()).load(urls.get(0)).thumbnail(0.1F).into(imageView2);
        contentsList.add(imageView2);

        initSlideView();
    }

    private ImageView getImageView() {
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LayoutParams(ScreenUtils.getScreenWidth(), (int) (ScreenUtils.getScreenWidth() * aspectRatio)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setOnClickListener(SlideView.this);
        return imageView;
    }

    private void initSlideView() {
        pagerAdapter = new BasePagerAdapter(contentsList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1, false);
        countdown = intervalTime;
        if (dotsImageResource != 0) {
            dotsList.clear();
            dotsLayout.removeAllViews();
            for (int i = 0; i < contentsList.size() - 2; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setAlpha(dotsAlpha);
                imageView.setImageResource(dotsImageResource);
                int padding = (int) dotsSpacing / 2;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(padding, (int) dotsSpacing, padding, (int) dotsSpacing);
                dotsLayout.addView(imageView, layoutParams);
                dotsList.add(imageView);
            }
            setSelectedDot(0);
        }

        if (isAutoShift && !isStartPlay) {
            isStartPlay = true;
            isAutoPlay = true;
            postDelayed(this, 1000);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Log.e("position:" + position + " positionOffset:" + positionOffset + " positionOffsetPixels:" + positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Log.e("onPageScrollStateChanged state:" + state);
        switch (state) {
            case 1:// 手势滑动，空闲中
                isAutoPlay = false;
                break;
            case 2:// 界面切换中
                isAutoPlay = false;
                break;
            case 0:// 滑动结束，即切换完毕或者加载完毕
                isAutoPlay = true;
                countdown = intervalTime;
                int position = viewPager.getCurrentItem();
                if (viewPager.getCurrentItem() < 1) { //首位之前，跳转到末尾（N）
                    viewPager.setCurrentItem(contentsList.size() - 2, false);
                } else if (position > contentsList.size() - 2) { //末位之后，跳转到首位（1）
                    viewPager.setCurrentItem(1, false);
                }
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {
        // Log.e("onPageSelected position:" + position);
        if (dotsImageResource != 0) {
            if (position < 1) {
                position = dotsList.size();
            } else if (position > dotsList.size()) {
                position = 1;
            }
            setSelectedDot(position - 1);
        }
    }

    private void setSelectedDot(int position) {
        for (int i = 0; i < dotsList.size(); i++) {
            dotsList.get(i).setSelected(i == position);
        }
    }

    @Override
    public void run() {
        if (isAutoPlay) {
            countdown--;
        }
        if (countdown < 1) {
            // Log.e("切换至" + (viewPager.getCurrentItem() + 1));
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
        postDelayed(this, 1000);
    }

    @Override
    public void onClick(View view) {
        if (onItemChildClickListener != null) {
            onItemChildClickListener.onItemChildClick(this, viewPager.getCurrentItem() % dotsList.size());
        }
    }

    /**
     * 设置ViewPager的滑动速度
     */
    private void initViewPagerScroll() {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            ViewPagerScroller scroller = new ViewPagerScroller(viewPager.getContext());
            scroller.setScrollDuration(transformDuration);
            mScroller.set(viewPager, scroller);

            if (isLane) {
                viewPager.setPageTransformer(true, new AlphaPageTransformer());
            } else {
                viewPager.setPageTransformer(true, new DepthPageTransformer());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public interface OnItemChildClickListener {
        void onItemChildClick(View v, int position);
    }

    /**
     * 重写Scroller控制ViewPager的切换速度
     */
    public class ViewPagerScroller extends Scroller {

        private int mScrollDuration = 800;// 滑动速度,值越大滑动越慢，滑动太快会使效果不明显
        private boolean zero;

        ViewPagerScroller(Context context) {
            super(context);
        }

        public ViewPagerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, zero ? 0 : mScrollDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, zero ? 0 : mScrollDuration);
        }

        public int getScrollDuration() {
            return mScrollDuration;
        }

        public void setScrollDuration(int scrollDuration) {
            this.mScrollDuration = scrollDuration;
        }

        public boolean isZero() {
            return zero;
        }

        public void setZero(boolean zero) {
            this.zero = zero;
        }
    }

    /**
     * ViewPager 切换动画
     */
    public class DepthPageTransformer implements ViewPager.PageTransformer {

        @SuppressLint("NewApi")
        @Override
        public void transformPage(View view, float position) {
            position %= dotsList.size();
            try {
                int pageWidth = view.getWidth();
                if (position < -1) {
                    // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);
                } else if (position <= 0) {
                    // [-1,0]
                    // Use the default slide transition when
                    // moving to the left page
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else if (position <= 1) {
                    // (0,1]
                    // Fade the page out.
                    view.setAlpha(1 - position);
                    // Counteract the default slide transition
                    view.setTranslationX(pageWidth * -position);
                    // Scale the page down (between MIN_SCALE and 1)
                    float minScale = 0.75f;
                    float scaleFactor = minScale + (1 - minScale) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else {
                    // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AlphaPageTransformer implements ViewPager.PageTransformer {

        private static final float DEFAULT_CENTER = 0.5f;
        private static final float DEFAULT_MIN_ALPHA = 0.5f;

        protected ViewPager.PageTransformer mPageTransformer = NonPageTransformer.INSTANCE;

        private float mMinAlpha = DEFAULT_MIN_ALPHA;

        public AlphaPageTransformer() {
        }

        public AlphaPageTransformer(float minAlpha) {
            this(minAlpha, NonPageTransformer.INSTANCE);
        }

        public AlphaPageTransformer(ViewPager.PageTransformer pageTransformer) {
            this(DEFAULT_MIN_ALPHA, pageTransformer);
        }

        public AlphaPageTransformer(float minAlpha, ViewPager.PageTransformer pageTransformer) {
            mMinAlpha = minAlpha;
            mPageTransformer = pageTransformer;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void pageTransform(View view, float position) {
            view.setScaleX(0.999f);//hack

            if (position < -1) { // [-Infinity,-1)
                view.setAlpha(mMinAlpha);
            } else if (position <= 1) { // [-1,1]

                if (position < 0) //[0，-1]
                {           //[1,min]
                    float factor = mMinAlpha + (1 - mMinAlpha) * (1 + position);
                    view.setAlpha(factor);
                } else //[1，0]
                {
                    //[min,1]
                    float factor = mMinAlpha + (1 - mMinAlpha) * (1 - position);
                    view.setAlpha(factor);
                }
            } else { // (1,+Infinity]
                view.setAlpha(mMinAlpha);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void transformPage(View view, float position) {
            if (mPageTransformer != null) {
                mPageTransformer.transformPage(view, position);
            }

            pageTransform(view, position);
        }

    }

    public static class NonPageTransformer implements ViewPager.PageTransformer {

        public static final ViewPager.PageTransformer INSTANCE = new NonPageTransformer();

        @Override
        public void transformPage(View page, float position) {
            page.setScaleX(0.999f);//hack
        }
    }

}
