package xyz.tanwb.airship.view.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.View;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.R;

public class BaseRecyclerDivider extends RecyclerView.ItemDecoration {

    private Paint paint;

    private boolean includeStartEdge;
    private boolean includeEndEdge;
    private int horizontalDivider;
    private int vertivalDivider;
    private int edgeDivider;
    private int color;

    public BaseRecyclerDivider() {
        this(false, 1);
    }

    public BaseRecyclerDivider(boolean includeEdge, int divider) {
        this(includeEdge, divider, ContextCompat.getColor(App.app(), R.color.colorLineDivider));
    }

    public BaseRecyclerDivider(boolean includeEdge, int divider, int color) {
        setTopOffsets(includeEdge);
        setDivider(divider);
        setColor(color);
    }

    public void setTopOffsets(boolean includeEdge) {
        this.includeStartEdge = includeEdge;
        this.includeEndEdge = includeEdge;
    }

    public void setIncludeStartEdge(boolean includeStartEdge) {
        this.includeStartEdge = includeStartEdge;
    }

    public void setIncludeEndEdge(boolean includeEndEdge) {
        this.includeEndEdge = includeEndEdge;
    }

    public void setDivider(int divider) {
        this.horizontalDivider = divider;
        this.vertivalDivider = divider;
        this.edgeDivider = divider;
    }

    public void setHorizontalDivider(int horizontalDivider) {
        this.horizontalDivider = horizontalDivider;
    }

    public void setVertivalDivider(int vertivalDivider) {
        this.vertivalDivider = vertivalDivider;
    }

    public void setEdgeDivider(int edgeDivider) {
        this.edgeDivider = edgeDivider;
    }

    public void setColor(int myColor) {
        this.color = myColor;
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);// 设置画笔的抗锯齿效果
            paint.setStyle(Paint.Style.FILL);//设置填满
        }
        paint.setColor(color);// 设置颜色
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (color != 0) {
            int itemCount = parent.getChildCount();
            float left;
            float right;
            float top;
            float bottom;
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
                    left = parent.getPaddingLeft();
                    right = parent.getWidth() - parent.getPaddingRight();
                    for (int i = 0; i < itemCount; i++) {
                        View child = parent.getChildAt(i);
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                        top = child.getBottom() + params.bottomMargin;

                        if (i == 0 && includeStartEdge) {
                            c.drawRect(left, child.getTop() - edgeDivider, right, child.getTop(), paint);
                        }

                        if (i == itemCount - 1) {
                            if (includeEndEdge) {
                                bottom = top + edgeDivider;
                            } else {
                                break;
                            }
                        } else {
                            bottom = top + vertivalDivider;
                        }
                        c.drawRect(left, top, right, bottom, paint);
                    }
                } else {
                    top = parent.getPaddingTop();
                    bottom = parent.getHeight() - parent.getPaddingBottom();
                    for (int i = 0; i < itemCount; i++) {
                        View child = parent.getChildAt(i);
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                        left = child.getRight() + params.rightMargin;

                        if (i == 0 && includeStartEdge) {
                            c.drawRect(child.getLeft() - edgeDivider, top, child.getLeft(), bottom, paint);
                        }

                        if (i == itemCount - 1) {
                            if (includeEndEdge) {
                                right = left + edgeDivider;
                            } else {
                                break;
                            }
                        } else {
                            right = left + horizontalDivider;
                        }
                        c.drawRect(left, top, right, bottom, paint);
                    }
                }
            }
            // else if (layoutManager instanceof GridLayoutManager) {
            // if (((GridLayoutManager) layoutManager).getOrientation() == GridLayoutManager.VERTICAL) {
            // } else {
            // }
            // } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            // }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        int columnTotal;
        int lineTotal;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            columnTotal = ((GridLayoutManager) layoutManager).getSpanCount();//总列数
            lineTotal = (itemCount / columnTotal) + (itemCount % columnTotal > 0 ? 1 : 0);//总行数
            getGridItemOffsets(outRect, ((GridLayoutManager) layoutManager).getOrientation(), position, columnTotal, lineTotal);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            columnTotal = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();//总列数
            lineTotal = (itemCount / columnTotal) + (itemCount % columnTotal > 0 ? 1 : 0);//总行数
            getGridItemOffsets(outRect, ((StaggeredGridLayoutManager) layoutManager).getOrientation(), position, columnTotal, lineTotal);
        } else if (layoutManager instanceof LinearLayoutManager) {
            getLinearItemOffsets(outRect, ((LinearLayoutManager) layoutManager).getOrientation(), position, itemCount);
        }
    }

    private void getGridItemOffsets(Rect outRect, int orientation, int position, int columnTotal, int lineTotal) {
        int line = position / columnTotal;// item所在的行
        int column = position % columnTotal; // item所在的列
        int divider;
        int offsets;
        if (orientation == GridLayoutManager.VERTICAL) {
            if (line == 0 && includeStartEdge) {
                outRect.top = edgeDivider;
            } else {
                outRect.top = 0;
            }
            if (line == lineTotal - 1) {
                if (includeEndEdge) {
                    outRect.bottom = edgeDivider;
                } else {
                    outRect.bottom = 0;
                }
            } else {
                outRect.bottom = vertivalDivider;
            }

            if (includeStartEdge) {
                divider = ((columnTotal + 1) * horizontalDivider) / columnTotal;
                offsets = divider / (columnTotal + 1);
                outRect.left = (columnTotal - column) * offsets;
                outRect.right = divider - outRect.left;
            } else {
                divider = ((columnTotal - 1) * horizontalDivider) / columnTotal;
                offsets = divider / (columnTotal - 1);
                outRect.left = column * offsets;
                outRect.right = divider - outRect.left;
            }
        } else {
            if (line == 0 && includeStartEdge) {
                outRect.left = edgeDivider;
            } else {
                outRect.left = 0;
            }
            if (line == lineTotal - 1) {
                if (includeEndEdge) {
                    outRect.right = edgeDivider;
                } else {
                    outRect.right = 0;
                }
            } else {
                outRect.right = vertivalDivider;
            }

            if (includeStartEdge) {
                divider = ((columnTotal + 1) * horizontalDivider) / columnTotal;
                offsets = divider / (columnTotal + 1);
                outRect.top = (columnTotal - column) * offsets;
                outRect.bottom = divider - outRect.top;
            } else {
                divider = ((columnTotal - 1) * horizontalDivider) / columnTotal;
                offsets = divider / (columnTotal - 1);
                outRect.top = column * offsets;
                outRect.bottom = divider - outRect.top;
            }
        }
    }

    private void getLinearItemOffsets(Rect outRect, int orientation, int position, int itemCount) {
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        if (orientation == LinearLayoutManager.VERTICAL) {
            if (position == 0 && includeStartEdge) {
                top = edgeDivider;
            }
            if (position == itemCount - 1) {
                if (includeEndEdge) {
                    bottom = edgeDivider;
                } else {
                    bottom = 0;
                }
            } else {
                bottom = vertivalDivider;
            }
        } else {
            if (position == 0 && includeStartEdge) {
                left = edgeDivider;
            }
            if (position == itemCount - 1) {
                if (includeEndEdge) {
                    right = edgeDivider;
                } else {
                    right = 0;
                }
            } else {
                right = horizontalDivider;
            }
        }
        outRect.set(left, top, right, bottom);
    }

}