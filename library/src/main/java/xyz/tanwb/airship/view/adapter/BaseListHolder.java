package xyz.tanwb.airship.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class BaseListHolder {

    protected View mConvertView;
    protected ViewHolderHelper mViewHolderHelper;

    private BaseListHolder(Context context, ViewGroup parent, int layoutId) {
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        mConvertView.setTag(this);
        mViewHolderHelper = new ViewHolderHelper(mConvertView);
    }

    public static BaseListHolder dequeueReusableAdapterViewHolder(Context context, View convertView, ViewGroup parent, int layoutId) {
        if (convertView == null) {
            return new BaseListHolder(context, parent, layoutId);
        }
        return (BaseListHolder) convertView.getTag();
    }

    public ViewHolderHelper getViewHolderHelper() {
        return mViewHolderHelper;
    }

    public View getConvertView() {
        return mConvertView;
    }

}