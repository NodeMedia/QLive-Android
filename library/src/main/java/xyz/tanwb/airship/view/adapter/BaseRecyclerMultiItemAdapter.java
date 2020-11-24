package xyz.tanwb.airship.view.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.tanwb.airship.view.adapter.entity.MultiItemEntity;

public abstract class BaseRecyclerMultiItemAdapter<T extends MultiItemEntity> extends BaseRecyclerAdapter<T> {

    private SparseArray<Integer> layouts;

    public BaseRecyclerMultiItemAdapter(Context context, List<T> data) {
        super(context, 0, data);
    }

    @Override
    protected int getDefItemViewType(int position) {
        return ((MultiItemEntity) mDatas.get(position)).getItemType();
    }

    @Override
    protected View onCreateDefView(ViewGroup parent, int viewType) {
        return getItemView(getLayoutId(viewType), parent);
    }

    private int getLayoutId(int viewType) {
        return layouts.get(viewType);
    }

    protected void addItemType(int type, int layoutResId) {
        if (layouts == null) {
            layouts = new SparseArray<>();
        }
        layouts.put(type, layoutResId);
    }

    @Override
    protected void setItemData(ViewHolderHelper viewHolderHelper, int position, T model) {
        setMultiData(viewHolderHelper, position, model);
    }

    protected abstract void setMultiData(ViewHolderHelper viewHolderHelper, int position, T model);

}


