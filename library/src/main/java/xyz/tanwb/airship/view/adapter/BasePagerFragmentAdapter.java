package xyz.tanwb.airship.view.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BasePagerFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public BasePagerFragmentAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        this.mFragments = fragments;
    }

    @Override
    public int getCount() {
        return mFragments == null ? 0 : mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments == null ? null : mFragments.get(position);
    }

    public void setDatas(List<Fragment> datas) {
        mFragments = datas;
        notifyDataSetChanged();
    }

    public void addDatas(List<Fragment> datas) {
        if (mFragments == null) {
            mFragments = datas;
        } else {
            mFragments.addAll(datas);
        }
        notifyDataSetChanged();
    }

    public void addData(int position, Fragment model) {
        if (mFragments == null) {
            mFragments = new ArrayList<>();
        }
        mFragments.add(position, model);
        notifyDataSetChanged();
    }

    public void clearDatas() {
        if (mFragments != null) {
            mFragments.clear();
        }
        mFragments = null;
    }

    public void setItem(Fragment oldModel, Fragment newModel) {
        setItem(mFragments.indexOf(oldModel), newModel);
    }

    public void setItem(int location, Fragment newModel) {
        mFragments.set(location, newModel);
        notifyDataSetChanged();
    }

    public void removeItem(Fragment model) {
        removeItem(mFragments.indexOf(model));
    }

    public void removeItem(int position) {
        mFragments.remove(position);
        notifyDataSetChanged();
    }

}
