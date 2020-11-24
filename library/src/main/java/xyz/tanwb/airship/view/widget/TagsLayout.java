package xyz.tanwb.airship.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import xyz.tanwb.airship.R;

public class TagsLayout extends LinearLayout {

    private float tagSize = getResources().getDimension(R.dimen.sp_14);
    private int tagColor = getResources().getColor(R.color.colorLight);
    private int tagBackground = R.drawable.common_button_background;
    private int tagVerticalPadding = (int) getResources().getDimension(R.dimen.dp_6);
    private int tagHorizontalPadding = (int) getResources().getDimension(R.dimen.dp_12);
    private int verticalSpacing = (int) getResources().getDimension(R.dimen.dp_6);
    private int horizontalSpacing = (int) getResources().getDimension(R.dimen.dp_6);

    private Context mContext;
    private List<String> tips;// 标签数据
    private int layoutWidth;
    private OnItemClickListener onItemClickListener;

    // 是否换行
    private boolean isNewLine;
    // 一行加载item的宽度
    private int length;
    private LinearLayout layout;
    private LayoutParams layoutLp;

    public TagsLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public TagsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public TagsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public void initView(Context mContext, AttributeSet attrs) {
        this.mContext = mContext;
        if (attrs != null) {
            TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TagsLayout);
            tagSize = a.getDimension(R.styleable.TagsLayout_tagSize, tagSize);
            tagColor = a.getColor(R.styleable.TagsLayout_tagColor, tagColor);
            tagBackground = a.getResourceId(R.styleable.TagsLayout_tagBackground, tagBackground);
            tagVerticalPadding = (int) a.getDimension(R.styleable.TagsLayout_tagVerticalPadding, tagVerticalPadding);
            tagHorizontalPadding = (int) a.getDimension(R.styleable.TagsLayout_tagHorizontalPadding, tagHorizontalPadding);
            verticalSpacing = (int) a.getDimension(R.styleable.TagsLayout_verticalSpacing, verticalSpacing);
            horizontalSpacing = (int) a.getDimension(R.styleable.TagsLayout_horizontalSpacing, horizontalSpacing);
            a.recycle();
        }

        setOrientation(VERTICAL);//设置方向
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutWidth = getMeasuredWidth();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (tips != null) {
                    show();
                }
            }
        });
    }

    public TagsLayout setData(String[] tips) {
        return setData(Arrays.asList(tips));
    }

    public TagsLayout setData(List<String> tips) {
        this.tips = tips;
        return this;
    }

    public void show() {
        if (layoutWidth > 0) {
            removeAllViews();
            isNewLine = true;
            for (int i = 0; i < tips.size(); i++) {
                showViews(i);
            }
            addView(layout, layoutLp);
        }
    }

    private void showViews(int postion) {
        if (isNewLine) {
            layout = new LinearLayout(mContext);
            layout.setOrientation(HORIZONTAL);
            layoutLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (postion > 0) {
                layoutLp.topMargin = verticalSpacing;
            }
        }

        TextView tipView = getTipText();
        tipView.setTag(postion);
        tipView.setText(tips.get(postion));
        tipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onItemClickListener) {
                    onItemClickListener.onItemClick(v, (int) v.getTag());
                }
            }
        });

        //设置item的参数
        LayoutParams itemLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLp.leftMargin = isNewLine ? 0 : horizontalSpacing;
        //得到当前行的长度
        length += itemLp.leftMargin + getViewWidth(tipView);
        if (length > layoutWidth) {
            length = 0;
            addView(layout, layoutLp);
            isNewLine = true;
            showViews(postion);
        } else {
            isNewLine = false;
            layout.addView(tipView, itemLp);
        }
    }

    public TextView getTipText() {
        TextView tiptext = new TextView(mContext);
        tiptext.setTextSize(TypedValue.COMPLEX_UNIT_PX, tagSize);
        tiptext.setTextColor(tagColor);
        tiptext.setBackgroundResource(tagBackground);
        tiptext.setPadding(tagHorizontalPadding, tagVerticalPadding, tagHorizontalPadding, tagVerticalPadding);
        return tiptext;
    }

    private int getViewWidth(View view) {
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}

