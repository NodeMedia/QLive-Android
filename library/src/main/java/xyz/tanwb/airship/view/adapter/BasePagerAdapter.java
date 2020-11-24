package xyz.tanwb.airship.view.adapter;

import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BasePagerAdapter extends PagerAdapter {

    private List<View> mViews;

    public BasePagerAdapter(List<View> viewList) {
        this.mViews = viewList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews.get(position));
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getCount() {
        return mViews == null ? 0 : mViews.size();
    }

    public void setDatas(List<View> datas) {
        mViews = datas;
        notifyDataSetChanged();
    }

    public void addDatas(List<View> datas) {
        if (mViews == null) {
            mViews = datas;
        } else {
            mViews.addAll(datas);
        }
        notifyDataSetChanged();
    }

    public void addData(int position, View model) {
        if (mViews == null) {
            mViews = new ArrayList<>();
        }
        mViews.add(position, model);
        notifyDataSetChanged();
    }

    public void clearDatas() {
        if (mViews != null) {
            mViews.clear();
        }
        mViews = null;
    }

    public void setItem(View oldModel, View newModel) {
        setItem(mViews.indexOf(oldModel), newModel);
    }

    public void setItem(int location, View newModel) {
        mViews.set(location, newModel);
        notifyDataSetChanged();
    }

    public View getItem(int position) {
        return mViews == null ? null : mViews.get(position);
    }

    public void removeItem(View model) {
        removeItem(mViews.indexOf(model));
    }

    public void removeItem(int position) {
        mViews.remove(position);
        notifyDataSetChanged();
    }

}
