package xyz.tanwb.airship.view.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import xyz.tanwb.airship.view.adapter.listener.OnItemClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemLongClickListener;

public class BaseRecyclerHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    protected Context mContext;
    protected ViewHolderHelper mViewHolderHelper;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;

    public BaseRecyclerHolder(View itemView) {
        this(itemView, null, null);
    }

    public BaseRecyclerHolder(View itemView, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        super(itemView);
        mContext = itemView.getContext();
        mViewHolderHelper = new ViewHolderHelper(this.itemView);
        mViewHolderHelper.setRecyclerViewHolder(this);
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public ViewHolderHelper getViewHolderHelper() {
        return mViewHolderHelper;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == this.itemView.getId() && null != mOnItemClickListener) {
            mOnItemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == this.itemView.getId() && null != mOnItemLongClickListener) {
            return mOnItemLongClickListener.onItemLongClick(v, getLayoutPosition());
        }
        return false;
    }
}