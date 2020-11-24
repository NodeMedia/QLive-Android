package xyz.tanwb.airship.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.tanwb.airship.view.adapter.entity.SectionEntity;

public abstract class BaseRecyclerSectionAdapter<T extends SectionEntity> extends BaseRecyclerAdapter<T> {

    protected static final int SECTION_HEADER_VIEW = 0x00000555;
    protected int mSectionHeadResId;

    public BaseRecyclerSectionAdapter(Context context, int sectionHeadResId, int layoutResId, List<T> data) {
        super(context, layoutResId, data);
        this.mSectionHeadResId = sectionHeadResId;
    }

    @Override
    protected int getDefItemViewType(int position) {
        return ((SectionEntity) mDatas.get(position)).isHeader ? SECTION_HEADER_VIEW : 0;
    }

    @Override
    protected View onCreateDefView(ViewGroup parent, int viewType) {
        if (viewType == SECTION_HEADER_VIEW) return getItemView(mSectionHeadResId, parent);
        return super.onCreateDefView(parent, viewType);
    }

    @Override
    protected void setItemData(ViewHolderHelper viewHolderHelper, int position, T model) {
        switch (viewHolderHelper.mRecyclerViewHolder.getItemViewType()) {
            case SECTION_HEADER_VIEW:
                setFullSpan(viewHolderHelper.mRecyclerViewHolder);
                setHeadData(viewHolderHelper, position, model);
                break;
            default:
                setContentData(viewHolderHelper, position, model);
                break;
        }
    }

    protected abstract void setHeadData(ViewHolderHelper viewHolderHelper, int position, T model);

    protected abstract void setContentData(ViewHolderHelper viewHolderHelper, int position, T model);

}
