package xyz.tanwb.airship.view.adapter;

import android.animation.Animator;
import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.tanwb.airship.view.adapter.animation.AlphaInAnimation;
import xyz.tanwb.airship.view.adapter.animation.BaseAnimation;
import xyz.tanwb.airship.view.adapter.animation.ScaleInAnimation;
import xyz.tanwb.airship.view.adapter.animation.SlideInBottomAnimation;
import xyz.tanwb.airship.view.adapter.animation.SlideInLeftAnimation;
import xyz.tanwb.airship.view.adapter.animation.SlideInRightAnimation;
import xyz.tanwb.airship.view.adapter.listener.OnItemChildClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemChildLongClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemDragListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemLongClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemSwipeListener;
import xyz.tanwb.airship.view.adapter.listener.OnLoadMoreListener;

public abstract class BaseRecyclerAdapter<M> extends RecyclerView.Adapter<BaseRecyclerHolder> {

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALEIN = 0x00000002;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_LEFT = 0x00000004;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_RIGHT = 0x00000005;

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    protected static final int HEADER_VIEW = 0x00000111;
    protected static final int LOADING_VIEW = 0x00000222;
    protected static final int FOOTER_VIEW = 0x00000333;
    protected static final int EMPTY_VIEW = 0x00000444;

    protected Context mContext;
    protected int mItemLayoutId;
    protected List<M> mDatas;

    protected OnItemChildClickListener mOnItemChildClickListener;
    protected OnItemChildLongClickListener mOnItemChildLongClickListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnLoadMoreListener mOnLoadMoreListener;

    private BaseAnimation mSelectAnimation;
    private boolean mOpenAnimationEnable;// 是否开启item加载动画
    private boolean mFirstOnlyEnable = true;// 是否仅首次加载启动动画
    private int mAnimationPosition = -1;// 动画指针
    private int mDuration = 300;// 动画持续时间
    private Interpolator mInterpolator = new LinearInterpolator();// 用来修饰动画效果,定义动画的变化率.

    private View mEmptyView;
    private boolean mEmptyEnable;
    private boolean mHeadOrFoorAndEmptyEnable;

    private View mHeaderView;

    private View mFooterView;

    private View mLoadingView;
    private boolean mNextLoadEnable;
    private boolean mLoadingMoreEnable;
    private int pageSize = -1;

    private int mToggleViewId;
    private ItemTouchHelper mItemTouchHelper;
    private boolean itemDragEnabled;
    private boolean itemSwipeEnabled;
    private OnItemDragListener mOnItemDragListener;
    private OnItemSwipeListener mOnItemSwipeListener;
    private boolean mDragOnLongPress = true;

    private View.OnTouchListener mOnToggleViewTouchListener;
    private View.OnLongClickListener mOnToggleViewLongClickListener;


    public BaseRecyclerAdapter(Context context, int itemLayoutId) {
        this(context, itemLayoutId, null);
    }

    public BaseRecyclerAdapter(Context context, int itemLayoutId, List<M> datas) {
        mContext = context;
        mItemLayoutId = itemLayoutId;
        mDatas = datas;
    }

    @Override
    public int getItemCount() {
        int count = getDataCount() + getHeaderViewsCount() + getFooterViewsCount();
        if (getDataCount() == 0) {
            if (count == 0 || mHeadOrFoorAndEmptyEnable) {
                count += getEmptyViewCount();
            }
        } else {
            count += getLoadMoreViewCount();
        }
        return count;
    }

    public int getDataCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public int getHeaderViewsCount() {
        return mHeaderView == null ? 0 : 1;
    }

    public int getFooterViewsCount() {
        return mFooterView == null ? 0 : 1;
    }

    public int getEmptyViewCount() {
        return (mEmptyView != null && mEmptyEnable) ? 1 : 0;
    }

    public int getLoadMoreViewCount() {
        return (mLoadingView != null && mNextLoadEnable) ? 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        // 如果position小于头部视图数量
        if (position < getHeaderViewsCount()) {
            return HEADER_VIEW;
        }

        if (getDataCount() == 0) {
            if (getEmptyViewCount() == 1) {
                if (mHeadOrFoorAndEmptyEnable) {
                    if (position == getHeaderViewsCount()) {
                        return EMPTY_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                } else {
                    if (position == 0) {
                        return EMPTY_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                }
            } else {
                return FOOTER_VIEW;
            }
        } else {
            if (position == getDataCount() + getHeaderViewsCount()) {
                if (mNextLoadEnable) {
                    return LOADING_VIEW;
                } else {
                    return FOOTER_VIEW;
                }
            } else {
                return getDefItemViewType(position - getHeaderViewsCount());
            }
        }
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public BaseRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case HEADER_VIEW:
                itemView = mHeaderView;
                break;
            case LOADING_VIEW:
                itemView = mLoadingView;
                break;
            case FOOTER_VIEW:
                itemView = mFooterView;
                break;
            case EMPTY_VIEW:
                itemView = mEmptyView;
                break;
            default:
                itemView = onCreateDefView(parent, viewType);
                break;
        }

        BaseRecyclerHolder viewHolder = new BaseRecyclerHolder(itemView, mOnItemClickListener, mOnItemLongClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildClickListener(mOnItemChildClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildLongClickListener(mOnItemChildLongClickListener);
        setItemChildListener(viewHolder.getViewHolderHelper(), viewType);
        return viewHolder;
    }

    protected View onCreateDefView(ViewGroup parent, int viewType) {
        return getItemView(mItemLayoutId, parent);
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(layoutResId, parent, false);
    }

    protected void setItemChildListener(ViewHolderHelper viewHolderHelper, int viewType) {
    }

    @Override
    public void onViewAttachedToWindow(BaseRecyclerHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder);
        }
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = (GridLayoutManager) manager;
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(BaseRecyclerHolder viewHolder, int position) {
        int viewType = viewHolder.getItemViewType();

        switch (viewType) {
            case LOADING_VIEW:
                if (!mLoadingMoreEnable) {
                    mLoadingMoreEnable = true;
                    mOnLoadMoreListener.onLoadMoreClick();
                }
                break;
            case HEADER_VIEW:
                setHeaderData(viewHolder.getViewHolderHelper(), position);
                break;
            case EMPTY_VIEW:
                setEmptyData(viewHolder.getViewHolderHelper(), position);
                break;
            case FOOTER_VIEW:
                setFooterData(viewHolder.getViewHolderHelper(), position);
                break;
            default:
                setItemData(viewHolder.getViewHolderHelper(), position, getItem(position - getHeaderViewsCount()));
                break;
        }
        setItemAnim(viewHolder);

//        if (mItemTouchHelper != null && itemDragEnabled && viewType != LOADING_VIEW && viewType != HEADER_VIEW
//                && viewType != EMPTY_VIEW && viewType != FOOTER_VIEW) {
//            if (mToggleViewId != NO_TOGGLE_VIEW) {
//                View toggleView = ((BaseViewHolder) holder).getView(mToggleViewId);
//                if (toggleView != null) {
//                    toggleView.setTag(holder);
//                    if (mDragOnLongPress) {
//                        toggleView.setOnLongClickListener(mOnToggleViewLongClickListener);
//                    } else {
//                        toggleView.setOnTouchListener(mOnToggleViewTouchListener);
//                    }
//                }
//            } else {
//                holder.itemView.setTag(holder);
//                holder.itemView.setOnLongClickListener(mOnToggleViewLongClickListener);
//            }
//        }

    }

    protected abstract void setItemData(ViewHolderHelper viewHolderHelper, int position, M model);

    private void setItemAnim(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mAnimationPosition) {
                if (mSelectAnimation != null) {
                    mSelectAnimation = new AlphaInAnimation();
                }
                for (Animator anim : mSelectAnimation.getAnimators(holder.itemView)) {
                    anim.setDuration(mDuration).start();
                    anim.setInterpolator(mInterpolator);
                }
                mAnimationPosition = holder.getLayoutPosition();
            }
        }
    }

    protected void setHeaderData(ViewHolderHelper viewHolderHelper, int position) {
    }

    protected void setFooterData(ViewHolderHelper viewHolderHelper, int position) {
    }

    protected void setEmptyData(ViewHolderHelper viewHolderHelper, int position) {
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemChildLongClickListener(OnItemChildLongClickListener onItemChildLongClickListener) {
        mOnItemChildLongClickListener = onItemChildLongClickListener;
    }

    public void openLoadAnimation() {
        openLoadAnimation(ALPHAIN);
    }

    public void openLoadAnimation(@AnimationType int animationType) {
        BaseAnimation animation = null;
        switch (animationType) {
            case ALPHAIN:
                animation = new AlphaInAnimation();
                break;
            case SCALEIN:
                animation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                animation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                animation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                animation = new SlideInRightAnimation();
                break;
        }
        openLoadAnimation(animation);
    }

    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mSelectAnimation = animation;
    }

    public void setAnimFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    public void setAnimDuration(int duratio) {
        this.mDuration = duratio;
    }

    public void setAnimInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public void setHeaderView(@LayoutRes int layoutId) {
        setHeaderView(getItemView(layoutId, null));
    }

    public void setHeaderView(View header) {
        this.mHeaderView = header;
        this.notifyDataSetChanged();
    }

    public void setFooterView(@LayoutRes int layoutId) {
        setFooterView(getItemView(layoutId, null));
    }

    public void setFooterView(View footer) {
        mNextLoadEnable = false;
        this.mFooterView = footer;
        this.notifyDataSetChanged();
    }

    public void setEmptyView(@LayoutRes int layoutId) {
        setEmptyView(getItemView(layoutId, null));
    }

    public void setEmptyView(@LayoutRes int layoutId, boolean isHeadOrFoorAndEmpty) {
        setEmptyView(isHeadOrFoorAndEmpty, getItemView(layoutId, null));
    }

    public void setEmptyView(View emptyView) {
        setEmptyView(false, emptyView);
    }

    public void setEmptyView(boolean isHeadOrFoorAndEmpty, View emptyView) {
        mHeadOrFoorAndEmptyEnable = isHeadOrFoorAndEmpty;
        mEmptyView = emptyView;
        mEmptyEnable = true;
    }

    public void setEmptyEnable(boolean isEmptyEnable) {
        this.mEmptyEnable = isEmptyEnable;
    }

    public void openLoadMore(@LayoutRes int layoutId, int pageSize) {
        setLoadingView(layoutId);
        setPageSize(pageSize);
        setLoadMoreEnable(true);
    }

    public void setLoadingView(@LayoutRes int layoutId) {
        setLoadingView(LayoutInflater.from(mContext).inflate(layoutId, null));
    }

    public void setLoadingView(View loadingView) {
        this.mLoadingView = loadingView;
        mLoadingView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setLoadMoreEnable(boolean enable) {
        mNextLoadEnable = enable;
    }

    public void notifyDataChangedAfterLoadMore(List<M> data) {
        if (pageSize > 0 && data.size() >= pageSize) {
            mNextLoadEnable = true;
        } else {
            mNextLoadEnable = false;
        }
        mLoadingMoreEnable = false;
        addDatas(data);
    }

    public List<M> getDatas() {
        return mDatas;
    }

    public void setDatas(List<M> datas) {
        mDatas = datas;
        mAnimationPosition = -1;
        notifyDataSetChanged();
    }

    public void addDatas(List<M> datas) {
        if (mDatas == null) {
            setDatas(datas);
        } else {
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    public void addData(int position, M model) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
        mDatas.add(position, model);
        notifyDataSetChanged();
    }

    public void clearDatas() {
        if (mDatas != null) {
            mDatas.clear();
        }
        mDatas = null;
    }

    public M getItem(int position) {
        return mDatas.get(position);
    }

    public void setItem(M oldModel, M newModel) {
        setItem(mDatas.indexOf(oldModel), newModel);
    }

    public void setItem(int position, M newModel) {
        mDatas.set(position, newModel);
        notifyItemInserted(position);
    }

    public void removeItem(M model) {
        removeItem(mDatas.indexOf(model));
    }

    public void removeItem(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position + getHeaderViewsCount());
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Set the toggle view's id which will trigger drag event.
     * If the toggle view id is not set, drag event will be triggered when the item is long pressed.
     *
     * @param toggleViewId the toggle view's id
     */
    public void setToggleViewId(int toggleViewId) {
        mToggleViewId = toggleViewId;
    }

    /**
     * Set the drag event should be trigger on long press.
     * Work when the toggleViewId has been set.
     *
     * @param longPress by default is true.
     */
    public void setToggleDragOnLongPress(boolean longPress) {
        mDragOnLongPress = longPress;
        if (mDragOnLongPress) {
            mOnToggleViewTouchListener = null;
            mOnToggleViewLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemTouchHelper != null && itemDragEnabled) {
                        mItemTouchHelper.startDrag((RecyclerView.ViewHolder) v.getTag());
                    }
                    return true;
                }
            };
        } else {
            mOnToggleViewTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN && !mDragOnLongPress) {
                        if (mItemTouchHelper != null && itemDragEnabled) {
                            mItemTouchHelper.startDrag((RecyclerView.ViewHolder) v.getTag());
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            mOnToggleViewLongClickListener = null;
        }
    }

    /**
     * Enable drag items.
     * Use itemView as the toggleView when long pressed.
     *
     * @param itemTouchHelper {@link ItemTouchHelper}
     */
    public void enableDragItem(@NonNull ItemTouchHelper itemTouchHelper) {
        enableDragItem(itemTouchHelper, 0, true);
    }

    /**
     * Enable drag items. Use the specified view as toggle.
     *
     * @param itemTouchHelper {@link ItemTouchHelper}
     * @param toggleViewId    The toggle view's id.
     * @param dragOnLongPress If true the drag event will be trigger on long press, otherwise on touch down.
     */
    public void enableDragItem(@NonNull ItemTouchHelper itemTouchHelper, int toggleViewId, boolean dragOnLongPress) {
        itemDragEnabled = true;
        mItemTouchHelper = itemTouchHelper;
        setToggleViewId(toggleViewId);
        setToggleDragOnLongPress(dragOnLongPress);
    }

    /**
     * Disable drag items.
     */
    public void disableDragItem() {
        itemDragEnabled = false;
        mItemTouchHelper = null;
    }

    public boolean isItemDraggable() {
        return itemDragEnabled;
    }

    /**
     * <p>Enable swipe items.</p>
     * You should attach {@link ItemTouchHelper} which construct with  to the Recycler when you enable this.
     */
    public void enableSwipeItem() {
        itemSwipeEnabled = true;
    }

    public void disableSwipeItem() {
        itemSwipeEnabled = false;
    }

    public boolean isItemSwipeEnable() {
        return itemSwipeEnabled;
    }

    /**
     * @param onItemDragListener Register a callback to be invoked when drag event happen.
     */
    public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
        mOnItemDragListener = onItemDragListener;
    }

    public int getViewHolderPosition(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition() - getHeaderViewsCount();
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        mOnItemSwipeListener = listener;
    }

    public void onItemDragStart(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragStart(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemDragMoving(RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        int from = getViewHolderPosition(source);
        int to = getViewHolderPosition(target);

        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(mDatas, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(mDatas, i, i - 1);
            }
        }
        notifyItemMoved(source.getAdapterPosition(), target.getAdapterPosition());

        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragMoving(source, from, target, to);
        }
    }

    public void onItemDragEnd(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragEnd(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.onItemSwipeStart(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemSwipeClear(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.clearView(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemSwiped(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.onItemSwiped(viewHolder, getViewHolderPosition(viewHolder));
        }
        int pos = getViewHolderPosition(viewHolder);
        mDatas.remove(pos);
        notifyItemRemoved(viewHolder.getAdapterPosition());
    }

}