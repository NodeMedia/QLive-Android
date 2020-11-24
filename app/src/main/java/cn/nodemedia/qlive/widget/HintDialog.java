package cn.nodemedia.qlive.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.InputFilter;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import cn.nodemedia.qlive.R;
import xyz.tanwb.airship.utils.ScreenUtils;
import xyz.tanwb.airship.view.adapter.BaseRecyclerAdapter;
import xyz.tanwb.airship.view.adapter.BaseRecyclerDivider;
import xyz.tanwb.airship.view.adapter.ViewHolderHelper;

/**
 * 提示Dialog
 */
public class HintDialog extends Dialog implements DialogInterface {

    public static final int LEFT = -2;
    public static final int RIGHT = -1;

    public HintDialog(Context context) {
        this(context, R.style.hint_dialog);
    }

    public HintDialog(Context context, int theme) {
        super(context, theme);
    }

    public interface OnClickListener {

        /**
         * 点击监听
         */
        void onClick(Builder builder, View view, int position, Object param);
    }

    public interface OnItemClickListener {

        /**
         * 长按监听
         */
        void onItemClick(Builder builder, int position, Object param);
    }

    public static class Builder implements View.OnClickListener {

        private View dialogContentListLine;
        private TextView dialogContentListTitle;
        private LinearLayout dialogContentListLayout;
        private TextView dialogContentEditTitle;
        private LinearLayout dialogContentEditLayout;
        private LinearLayout dialogLayout;
        private View view;
        private LinearLayout dialogTitleLayout;
        private TextView dialogTitleText;
        private FrameLayout dialogContent;
        private TextView dialogContentText;
        private EditText dialogContentEdit;
        private RecyclerView dialogContentList;
        private LinearLayout dialogButton;
        private TextView dialogButtonLeft;
        private TextView dialogButtonRight;
        private ImageView titleImage;

        private Context mContext;
        private HintDialog dialog;

        private SparseArray<View> mViews;

        private OnClickListener onClickListener;
        private OnItemClickListener onItemClickListener;
        private DialogAdapter dialogAdapter;

        private int itemGraty = Gravity.CENTER;
        private int showType;
        private int selectId = -1;
        private Object param;
        private String key = "";

        public Builder(Context context) {
            this(context, Gravity.CENTER);
        }

        public Builder(Context context, int gravity) {
            this.mContext = context;
            mViews = new SparseArray<>();

            dialog = new HintDialog(mContext);

            view = dialog.getLayoutInflater().inflate(R.layout.dialog_hint, null);
            dialogTitleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
            dialogTitleText = (TextView) view.findViewById(R.id.dialog_title);
            titleImage = (ImageView) view.findViewById(R.id.title_image);
            dialogContent = (FrameLayout) view.findViewById(R.id.dialog_content);
            dialogContentText = (TextView) view.findViewById(R.id.dialog_content_text);
            dialogContentEdit = (EditText) view.findViewById(R.id.dialog_content_edit);
            dialogContentList = (RecyclerView) view.findViewById(R.id.dialog_content_recycler);
            dialogButton = (LinearLayout) view.findViewById(R.id.dialog_button);
            dialogButtonLeft = (TextView) view.findViewById(R.id.dialog_button_left);
            dialogButtonRight = (TextView) view.findViewById(R.id.dialog_button_right);
            dialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);
            dialogContentEditLayout = (LinearLayout) view.findViewById(R.id.dialog_content_edit_layout);
            dialogContentEditTitle = (TextView) view.findViewById(R.id.dialog_content_edit_title);
            dialogContentListLayout = (LinearLayout) view.findViewById(R.id.dialog_content_recycler_layout);
            dialogContentListTitle = (TextView) view.findViewById(R.id.dialog_content_recycler_title);
            dialogContentListLine = view.findViewById(R.id.dialog_content_recycler_line);

            dialogButtonLeft.setOnClickListener(this);
            dialogButtonRight.setOnClickListener(this);

            dialog.setContentView(view);

            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = gravity;
            if (gravity == Gravity.BOTTOM) {
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setWindowAnimations(R.style.dialog_match_anim);
                dialogLayout.setBackgroundResource(R.color.colorLight);
            } else {
                layoutParams.width = (int) (ScreenUtils.getScreenWidth() * 0.8f);
                dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
            }
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        }

        public Builder(Context context, int gravity, View view) {
            this.mContext = context;
            mViews = new SparseArray<>();

            this.view = view;
            dialog = new HintDialog(mContext);
            dialog.setContentView(view);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = gravity;
            if (gravity == Gravity.BOTTOM) {
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setWindowAnimations(R.style.dialog_match_anim);
            } else {
                layoutParams.width = (int) (ScreenUtils.getScreenWidth() * 0.8f);
                dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
            }
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        /**
         * 显示Dialog
         */
        public Dialog show() {
            if (dialog != null && mContext instanceof Activity && !((Activity) mContext).isFinishing() && !dialog.isShowing()) {
                dialog.show();
            }
            return dialog;
        }

        public Dialog getDialog() {
            return dialog;
        }

        public Object getParam() {
            return param;
        }

        public void setParam(Object param) {
            this.param = param;
        }

        /**
         * 设置是否点击Dialog外层取消
         */
        public Builder setCancelable(boolean cancelable) {
            dialog.setCancelable(cancelable);
            return this;
        }

        /**
         * 设置是否点击Dialog外层取消监听
         */
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            dialog.setOnCancelListener(onCancelListener);
            return this;
        }

        /**
         * 设置标题
         */
        public Builder setTitle(CharSequence title) {
            dialogTitleLayout.setVisibility(View.VISIBLE);
            dialogTitleText.setText(title);
            return this;
        }

        /**
         * 设置标题
         */
        public Builder setTitle(@StringRes int resId) {
            dialogTitleLayout.setVisibility(View.VISIBLE);
            dialogTitleText.setText(resId);
            return this;
        }

        /**
         * 设置标题的位置（居中还是靠左，默认居中）
         */
        public Builder setTitleGravity(int gravity) {
            dialogTitleLayout.setGravity(gravity);
            dialogTitleLayout.setPadding(ScreenUtils.dp2px(20), ScreenUtils.dp2px(20), ScreenUtils.dp2px(20), 0);
            return this;
        }

        /**
         * 设置视图显示状态
         */
        public Builder setContentState(int showType) {
            this.showType = showType;
            switch (showType) {
                case 1:
                    dialogContentText.setVisibility(View.VISIBLE);
                    dialogContentEditLayout.setVisibility(View.GONE);
                    dialogContentListLayout.setVisibility(View.GONE);
                    break;
                case 2:
                    dialogContentText.setVisibility(View.GONE);
                    dialogContentEditLayout.setVisibility(View.VISIBLE);
                    dialogContentListLayout.setVisibility(View.GONE);
                    break;
                case 3:
                    dialogContentText.setVisibility(View.GONE);
                    dialogContentEditLayout.setVisibility(View.GONE);
                    dialogContentListLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    dialogContentText.setVisibility(View.GONE);
                    dialogContentEditLayout.setVisibility(View.GONE);
                    dialogContentListLayout.setVisibility(View.GONE);
                    break;
            }
            return this;
        }

        /**
         * 设置文字内容
         */
        public Builder setMessage(CharSequence content) {
            dialogContentText.setText(content);
            dialogContentText.setVisibility(View.VISIBLE);
            return setContentState(1);
        }

        /**
         * 设置文字内容
         */
        public Builder setMessage(@StringRes int resId) {
            dialogContentText.setText(resId);
            dialogContentText.setVisibility(View.VISIBLE);
            return setContentState(1);
        }

        /**
         * 设置Edit 数据
         */
        public Builder setEditData(CharSequence content, CharSequence hint) {
            dialogContentEdit.setText(content);
            dialogContentEdit.setHint(hint);
            return setContentState(2);
        }

        /**
         * 设置Edit 数据
         */
        public Builder setEditData(CharSequence content, @StringRes int hintId) {
            dialogContentEdit.setText(content);
            dialogContentEdit.setHint(hintId);
            return setContentState(2);
        }

        /**
         * 设置Edit 文本内容
         */
        public Builder setEditText(CharSequence content) {
            dialogContentEdit.setText(content);
            return setContentState(2);
        }

        /**
         * 设置Edit 文本内容
         */
        public Builder setEditText(@StringRes int resId) {
            dialogContentEdit.setText(resId);
            return setContentState(2);
        }

        /**
         * 设置编辑框最大输入长度
         */
        public Builder setEditMaxLength(int size) {
            //即限定最大输入字符数为size
            dialogContentEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(size)});
            return setContentState(2);
        }

        /**
         * 设置编辑框输入类型
         */
        public Builder setEditInputType(int type) {
            dialogContentEdit.setInputType(type);
            return setContentState(2);
        }

        /**
         * 设置Edit 标题内容
         */
        public Builder setEditTitle(CharSequence content) {
            dialogContentEditTitle.setText(content);
            return setContentState(2);
        }

        /**
         * 设置Edit 标题内容
         */
        public Builder setEditTitle(@StringRes int resId) {
            dialogContentEditTitle.setText(resId);
            return setContentState(2);
        }

        /**
         * 设置Edit 标题内容是否显示
         */
        public Builder setEditTitleVisible(int visible) {
            dialogContentEditTitle.setVisibility(visible);
            return setContentState(2);
        }

        /**
         * 设置最大长度
         */
        public Builder setEditMaxText(int maxEms) {
//            dialogContentEdit.setMaxEms(maxEms);
            dialogContentEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxEms)});
            return setContentState(2);
        }

        /**
         * 设置是否单行
         */
        public Builder setEditMaxText(boolean isSingleLine) {
            dialogContentEdit.setSingleLine(isSingleLine);
            return setContentState(2);
        }

        /**
         * 设置输入类型
         */
        public Builder setEditType(int type) {
            dialogContentEdit.setInputType(type);
            return setContentState(2);
        }

        /**
         * 设置全选
         */
        public Builder setSelectAll() {
            dialogContentEdit.selectAll();
            return setContentState(2);
        }

        /**
         * 设置Edit Hint内容
         */
        public Builder setEditHint(CharSequence hint) {
            dialogContentEdit.setHint(hint);
            return setContentState(2);
        }

        /**
         * 设置Edit Hint内容
         */
        public Builder setEditHint(@StringRes int resId) {
            dialogContentEdit.setHint(resId);
            return setContentState(2);
        }

        /**
         * 设置List数据
         */
        public Builder setItems(String[] items) {
            return setItems(Arrays.asList(items));
        }

        /**
         * 设置List数据
         */
        public Builder setItems(List<String> items) {
            if (items != null && items.size() > 0) {
                dialogAdapter = new DialogAdapter(mContext);
                dialogAdapter.setOnItemClickListener(new xyz.tanwb.airship.view.adapter.listener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int i) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(Builder.this, i, dialogAdapter.getItem(i));
                            dialog.dismiss();
                        } else {
                            selectId = i;
                            dialogAdapter.notifyDataSetChanged();
                        }
                    }
                });
                dialogAdapter.setDatas(items);

                dialogContentList.setLayoutManager(new LinearLayoutManager(mContext));
                BaseRecyclerDivider divider = new BaseRecyclerDivider();
                divider.setColor(ContextCompat.getColor(mContext, R.color.colorLineDivider));
                divider.setDivider((int) mContext.getResources().getDimension(R.dimen.dp_05));
                dialogContentList.addItemDecoration(divider);
                dialogContentList.setAdapter(dialogAdapter);
            }
            return setContentState(3);
        }

        /**
         * 设置Item对齐方式
         */
        public Builder setItemGravity(int gravity) {
            itemGraty = gravity;
            return this;
        }

        /**
         * 设置选择的Item
         */
        public Builder setSelectItem(int selectId) {
            this.selectId = selectId;
            if (dialogAdapter != null) {
                dialogAdapter.notifyDataSetChanged();
            }
            return this;
        }

        /**
         * 设置Item点击事件
         */
        public Builder setOnItemClickListener(OnItemClickListener onItemClickListener) {
            dialogTitleLayout.setVisibility(View.GONE);
            dialogContentText.setVisibility(View.GONE);
            dialogContentEdit.setVisibility(View.GONE);
            setButtonVisibility(View.GONE);
            this.selectId = -1;
            if (dialogAdapter != null) {
                dialogAdapter.notifyDataSetChanged();
            }
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        /**
         * 设置Button是否显示
         */
        public Builder setButtonVisibility(int visibility) {
            dialogButton.setVisibility(visibility);
            return this;
        }

        /**
         * 设置右侧Button文字
         */
        public Builder setRightButton(CharSequence text) {
            dialogButtonRight.setText(text);
            return this;
        }

        /**
         * 设置右侧Button文字
         */
        public Builder setRightButton(@StringRes int resId) {
            dialogButtonRight.setText(resId);
            return this;
        }

        /**
         * 设置左侧Button文字
         */
        public Builder setLeftButton(CharSequence text) {
            dialogButtonLeft.setVisibility(View.VISIBLE);
            dialogButtonLeft.setText(text);
            return this;
        }

        /**
         * 设置左侧Button文字
         */
        public Builder setLeftButton(@StringRes int resId) {
            dialogButtonLeft.setVisibility(View.VISIBLE);
            dialogButtonLeft.setText(resId);
            return this;
        }

        /**
         * 设置点击时间
         */
        public Builder setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        /**
         * 设置内容视图
         */
        public Builder setContentView(@LayoutRes int layoutResId) {
            setContentView(dialog.getLayoutInflater().inflate(layoutResId, null));
            return this;
        }

        /**
         * 设置内容视图
         */
        public Builder setContentView(View view) {
            if (dialogContent.getChildCount() > 0) dialogContent.removeAllViews();
            dialogContent.addView(view);
            return setContentState(0);
        }

        /**
         * 添加View的点击事件
         */
        public Builder addViewOnClickListener(int viewId) {
            View view = getView(viewId);
            if (view != null) view.setOnClickListener(this);
            return this;
        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                int i = v.getId();
                if (i == R.id.dialog_button_right) {
                    switch (showType) {
                        case 1:
                            onClickListener.onClick(this, v, RIGHT, param);
                            break;
                        case 2:
                            onClickListener.onClick(this, v, RIGHT, dialogContentEdit.getText().toString());
                            break;
                        case 3:
                            if (dialogAdapter != null) {
                                onClickListener.onClick(this, v, selectId, dialogAdapter.getItem(selectId));
                            }
                            break;
                        default:
                            onClickListener.onClick(this, v, RIGHT, param);
                            break;
                    }
                } else if (i == R.id.dialog_button_left) {
                    onClickListener.onClick(this, v, LEFT, param);
                } else {
                    onClickListener.onClick(this, v, 0, param);
                }
            }
            dialog.dismiss();
        }

        /**
         * 通过控件的Id获取对应的控件，如果没有则加入mViews，则从item根控件中查找并保存到mViews中
         *
         * @param viewId 控件ID
         */
        public <T extends View> T getView(int viewId) {
            View v = mViews.get(viewId);
            if (v == null) {
                v = view.findViewById(viewId);
                mViews.put(viewId, v);
            }
            return (T) v;
        }

        /**
         * 设置Tag
         */
        public Builder setTag(int viewId, Object tag) {
            View view = getView(viewId);
            if (view != null) view.setTag(tag);
            return this;
        }

        /**
         * 设置Tag
         */
        public Builder setTag(int viewId, int key, Object tag) {
            View view = getView(viewId);
            if (view != null) view.setTag(key, tag);
            return this;
        }

        /**
         * 设置对某视图控件的显示隐藏
         */
        public Builder setVisibility(int viewId, int visibility) {
            View view = getView(viewId);
            if (view != null) view.setVisibility(visibility);
            return this;
        }

        /**
         * 设置输入框光标位置
         */
        public Builder setEditSelection(int viewId) {
            EditText view = getView(viewId);
            if (view != null) view.setSelection(view.length());
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId 控件ID
         * @param text   文本内容
         */
        public Builder setEditHint(int viewId, String text) {
            EditText view = getView(viewId);
            if (view != null) view.setHint(text);
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId      控件ID
         * @param stringResId 字符串资源id
         */
        public Builder setEditHint(int viewId, @StringRes int stringResId) {
            EditText view = getView(viewId);
            if (view != null) view.setHint(stringResId);
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId 控件ID
         * @param text   文本内容
         */
        public Builder setEditText(int viewId, String text) {
            EditText view = getView(viewId);
            if (view != null) view.setText(text);
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId      控件ID
         * @param stringResId 字符串资源id
         */
        public Builder setEditText(int viewId, @StringRes int stringResId) {
            EditText view = getView(viewId);
            if (view != null) view.setText(stringResId);
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId 控件ID
         * @param text   文本内容
         */
        public Builder setText(int viewId, String text) {
            TextView view = getView(viewId);
            if (view != null) view.setText(text);
            return this;
        }

        /**
         * 设置对应id的控件的文本内容
         *
         * @param viewId      控件ID
         * @param stringResId 字符串资源id
         */
        public Builder setText(int viewId, @StringRes int stringResId) {
            TextView view = getView(viewId);
            if (view != null) view.setText(stringResId);
            return this;
        }

        /**
         * 设置对应id的控件的html文本内容
         *
         * @param viewId 控件ID
         * @param source html文本
         */
        public Builder setHtml(int viewId, String source) {
            TextView view = getView(viewId);
            if (view != null) view.setText(Html.fromHtml(source));
            return this;
        }

        /**
         * 设置文字颜色
         *
         * @param viewId    控件ID
         * @param textColor 颜色值
         */

        public Builder setTextColor(int viewId, int textColor) {
            TextView view = getView(viewId);
            if (view != null) view.setTextColor(textColor);
            return this;
        }

        /**
         * 设置文字颜色
         *
         * @param viewId         控件ID
         * @param textColorResId 颜色资源id
         */
        public Builder setTextColorRes(int viewId, @ColorRes int textColorResId) {
            return setTextColor(viewId, ContextCompat.getColor(mContext, textColorResId));
        }

        /**
         * 设置文字大小
         *
         * @param viewId   控件ID
         * @param textSize 文字大小 sp值
         * @return
         */
        public Builder setTextSize(int viewId, int textSize) {
            TextView view = getView(viewId);
            if (view != null) view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            return this;
        }

        /**
         * 设置ImageView的前景图片
         *
         * @param viewId     控件ID
         * @param imageResId 图像资源id
         */
        public Builder setImageResource(int viewId, int imageResId) {
            ImageView view = getView(viewId);
            if (view != null) view.setImageResource(imageResId);
            view.setVisibility(View.VISIBLE);
            return this;
        }

        /**
         * 设置ImageView的前景图片
         *
         * @param viewId 控件ID
         * @param bitmap Bitmap
         */
        public Builder setImageBitmap(int viewId, Bitmap bitmap) {
            ImageView view = getView(viewId);
            if (view != null) view.setImageBitmap(bitmap);
            view.setVisibility(View.VISIBLE);
            return this;
        }

        /**
         * 设置ImageView的前景图片
         *
         * @param viewId   控件ID
         * @param drawable Drawable
         */
        public Builder setImageDrawable(int viewId, Drawable drawable) {
            ImageView view = getView(viewId);
            if (view != null) view.setImageDrawable(drawable);
            view.setVisibility(View.VISIBLE);
            return this;
        }

        /**
         * 设置控件背景
         *
         * @param viewId 控件ID
         * @param resId  背景资源id
         */
        public Builder setBackgroundRes(int viewId, int resId) {
            View view = getView(viewId);
            if (view != null) view.setBackgroundResource(resId);
            return this;
        }

        /**
         * 设置控件背景
         *
         * @param viewId 控件ID
         * @param color  颜色值
         */
        public Builder setBackgroundColor(int viewId, int color) {
            View view = getView(viewId);
            if (view != null) view.setBackgroundColor(color);
            return this;
        }

        /**
         * 设置控件背景
         *
         * @param viewId     控件ID
         * @param colorResId 颜色值资源id
         */
        public Builder setBackgroundColorRes(int viewId, @ColorRes int colorResId) {
            return setBackgroundColor(viewId, ContextCompat.getColor(mContext, colorResId));
        }

        class DialogAdapter extends BaseRecyclerAdapter<String> {

            DialogAdapter(Context context) {
                super(context, R.layout.item_dialog);
            }

            @Override
            protected void setItemData(ViewHolderHelper viewHolderHelper, int position, String model) {
                TextView dialogContent = viewHolderHelper.getView(R.id.dialog_content);
                dialogContent.setGravity(itemGraty);
                if (selectId == position) {
                    dialogContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorTheme));
                } else {
                    dialogContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorDark));
                }
                dialogContent.setText(model);
            }
        }
    }
}
