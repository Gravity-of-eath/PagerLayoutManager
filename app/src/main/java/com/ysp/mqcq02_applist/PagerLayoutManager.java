package com.ysp.mqcq02_applist;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class PagerLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "PageLayoutManager";
    private int rowCount = 4;
    private int columnCount = 3;
    private Context context;
    private int childWidth;
    private int childHeight;
    private int currentPage = 0;
    private int pageCount;
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position

    public PagerLayoutManager(int rowCount, int columnCount, Context context) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        if (rowCount < 1 || columnCount < 1) {
            throw new IllegalArgumentException("rowCount or columnCount < 1");
        }
        this.context = context;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private SparseArray<Rect> mItemRects = new SparseArray<>();


    private int getEachPageItemCount() {
        return rowCount * columnCount;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (getItemCount() <= 0) {
            return;
        }
        childWidth = getWidth() / columnCount;
        childHeight = getHeight() / rowCount;
        pageCount = getItemCount() / getEachPageItemCount() + (getItemCount() % getEachPageItemCount() == 0 ? 0 : 1);
        Log.d(TAG, "onLayoutChildren:pageCount= " + pageCount + "   getItemCount()=" + getItemCount());
        Log.d(TAG, "onLayoutChildren:childWidth= " + childWidth + "   childHeight=" + childHeight);
        recyclerAndFillView(recycler, state, 0);
    }

    private int mSumDx = 0;

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() <= 0) {
            return dx;
        }
        int travel = dx;
        //如果滑动到最左边
        if (mSumDx + dx < 0) {
            travel = -mSumDx;
        } else if (mSumDx + dx > pageCount * getWidth() - getWidth()) {
            //如果滑动到最右边
            travel = pageCount * getWidth() - getWidth() - mSumDx;
        }
        int pageOffset = mSumDx % getWidth();
        int pagePos = mSumDx / getWidth();
        if (dx > 0 && pageOffset > (getWidth() * 3 / 5) && pagePos + 1 != currentPage) {
            currentPage = pagePos + 1;//right
//            addRightPage(recycler);
        }
        if (dx < 0 && pageOffset < (getWidth() * 2 / 5) && pagePos != currentPage) {
            currentPage = pagePos;
//            addLeftPage(recycler);
        }
        recyclerAndFillView(recycler, state, travel);
        mSumDx += travel;
        offsetChildrenHorizontal(-travel);
        return travel;
//
    }

    private Rect getVisibleArea(int travel) {
        Rect result = new Rect(travel, 0, getWidth() + travel, getHeight());
        return result;
    }

    private synchronized void addLeftPage(RecyclerView.Recycler recycler) {
        int willAddPageIndex = currentPage - 1;
        Log.d(TAG, "addFirstPage: willAddPageIndex=" + willAddPageIndex);
        if (willAddPageIndex < 0) {
            return;
        }
        int firstAddIndex = willAddPageIndex * (columnCount * rowCount);
        Log.d(TAG, "addFirstPage: firstAddIndex=" + firstAddIndex + "  getChildCount=" + getChildCount());
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                View viewForPosition = recycler.getViewForPosition(firstAddIndex);
                ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                layoutParams.width = childWidth;
                layoutParams.height = childHeight;
                viewForPosition.setLayoutParams(layoutParams);
                addView(viewForPosition);
                measureChild(viewForPosition, 0, 0);
                int left = j * childWidth + (willAddPageIndex * getWidth()) - mSumDx;
                int top = i * childHeight;
                layoutDecorated(viewForPosition, left, top, left + childWidth, top + childHeight);
                firstAddIndex++;
            }
        }
    }

    private synchronized void addRightPage(RecyclerView.Recycler recycler) {
        int willAddPageIndex = currentPage + 1;
        Log.d(TAG, "addLastPage: willAddPageIndex=" + willAddPageIndex);
        if (willAddPageIndex >= pageCount) {
            return;
        }
        int firstAddIndex = willAddPageIndex * (columnCount * rowCount);
        Log.d(TAG, "addLastPage: firstAddIndex=" + firstAddIndex + "  getChildCount=" + getChildCount());
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                View viewForPosition = recycler.getViewForPosition(firstAddIndex);
                ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                layoutParams.width = childWidth;
                layoutParams.height = childHeight;
                viewForPosition.setLayoutParams(layoutParams);
                addView(viewForPosition);
                measureChild(viewForPosition, 0, 0);
                int left = j * childWidth + (willAddPageIndex * getWidth()) - mSumDx;
                int top = i * childHeight;
                layoutDecorated(viewForPosition, left, top, left + childWidth, top + childHeight);
                firstAddIndex++;
                if (firstAddIndex >= getItemCount()) {
                    break;
                }
            }
            if (firstAddIndex >= getItemCount()) {
                break;
            }
        }
    }


    private int recyclerAndFillView(RecyclerView.Recycler recycler, RecyclerView.State state, int travel) {
        //回收越界子View
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (travel >= 0) {//需要回收当前屏幕，左越界的View
                if (getDecoratedRight(child) - travel < 0) {
                    removeAndRecycleView(child, recycler);
                    Log.d(TAG, "scrollHorizontallyBy:travel > 0 removeAndRecycleView i=" + i);
                }
            } else if (travel < 0) {//回收当前屏幕，右越界的View
                if (getDecoratedLeft(child) - travel > getWidth()) {
                    removeAndRecycleView(child, recycler);
                    Log.d(TAG, "scrollHorizontallyBy:travel < 0 removeAndRecycleView i=" + i);
                }
            }
        }
        int startIndex = 0;
        int endIndex = getItemCount();
        int step = 1;
        if (travel == 0) {
            startIndex = getFirstVisiPos();
            endIndex = startIndex + getEachPageItemCount();
            for (int i = startIndex; i <= endIndex; i += step) {
                if (i >= 0 && i < getItemCount()) {
                    Rect itemRang = getItemRang(i);
//                    Rect visibleArea = getVisibleArea(mSumDx);
//                    boolean intersects = Rect.intersects(itemRang, visibleArea);
                    View viewForPosition = recycler.getViewForPosition(i);
//                    if (intersects) {
                    ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                    layoutParams.width = childWidth;
                    layoutParams.height = childHeight;
                    viewForPosition.setLayoutParams(layoutParams);
                    addView(viewForPosition);
                    measureChild(viewForPosition, 0, 0);
                    layoutDecorated(viewForPosition, itemRang.left - mSumDx, itemRang.top, itemRang.right - mSumDx, itemRang.bottom);
//                    }
                }
            }
        } else if (travel > 0) {
//            if (getChildCount() > 0) {
//                View childAt = getChildAt(getChildCount() - 1);
            startIndex = getLastVisiPos();
            if (startIndex % getEachPageItemCount() != 0) {
                endIndex = startIndex;
                startIndex = startIndex - ((rowCount - 1) * columnCount);
            } else {
                endIndex = startIndex + ((rowCount - 1) * columnCount) + 1;
            }
            step = columnCount;
//            } else {
//                startIndex = 0;
//                endIndex = getEachPageItemCount();
//            }
            Log.d(TAG, "recyclerAndFillView: " + "travel=" + travel + "  startIndex= " + startIndex + "        endIndex=" + endIndex);
            for (int i = startIndex; i <= endIndex; i += step) {
                if (i >= 0 && i < getItemCount()) {
                    Log.d(TAG, "recyclerAndFillView travel > 0: index" + i);
                    Rect itemRang = getItemRang(i);
//                    Rect visibleArea = getVisibleArea(mSumDx);
//                    boolean intersects = Rect.intersects(itemRang, visibleArea);
                    View viewForPosition = recycler.getViewForPosition(i);
//                    if (intersects) {
                    ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                    layoutParams.width = childWidth;
                    layoutParams.height = childHeight;
                    viewForPosition.setLayoutParams(layoutParams);
                    addView(viewForPosition);
                    measureChild(viewForPosition, 0, 0);
                    layoutDecorated(viewForPosition, itemRang.left - mSumDx, itemRang.top, itemRang.right - mSumDx, itemRang.bottom);
//                    }
                }
            }
        } else {
            if (getChildCount() > 0) {
                int firstVisiPos = getFirstVisiPos();
                Log.d(TAG, "recyclerAndFillView:endIndex() ==  " + endIndex);
                if (firstVisiPos % getEachPageItemCount() == 0) {
                    startIndex = firstVisiPos - 1;
                    endIndex = startIndex - ((rowCount - 1) * columnCount);
                    Log.d(TAG, "recyclerAndFillView:getEachPageItemCount() == 0 ");
                } else {
                    endIndex = firstVisiPos - 1;
                    startIndex = endIndex + ((rowCount - 1) * columnCount);
                    Log.d(TAG, "recyclerAndFillView: getEachPageItemCount() ！=------------ 0");
                }
                if (startIndex < 0) {
                    startIndex = 0;
                }
                step = columnCount;
            } else {
            }
            Log.d(TAG, "recyclerAndFillView: " + "travel=" + travel + " startIndex= " + startIndex + "        endIndex=" + endIndex);
            for (int i = startIndex; i >= endIndex; i -= step) {
                if (i >= 0) {
                    Rect itemRang = getItemRang(i);
//                    Rect visibleArea = getVisibleArea(mSumDx);
//                    boolean intersects = Rect.intersects(itemRang, visibleArea);
                    View viewForPosition = recycler.getViewForPosition(i);
//                    Log.d(TAG, "recyclerAndFillView: " + "index= " + i + "  itemRang= " + itemRang + "        intersects=" + intersects);
//                    if (intersects) {
                    ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                    layoutParams.width = childWidth;
                    layoutParams.height = childHeight;
                    viewForPosition.setLayoutParams(layoutParams);
                    addView(viewForPosition);
                    measureChild(viewForPosition, 0, 0);
                    layoutDecorated(viewForPosition, itemRang.left - mSumDx, itemRang.top, itemRang.right - mSumDx, itemRang.bottom);
//                    }
                }
            }
        }
        return travel;
    }

    private Rect getItemRang(int adapterPos) {
        if (adapterPos == 21 || adapterPos == 22) {
            Log.d(TAG, "getItemRang: ");
        }
        int pageItemCount = getEachPageItemCount();
        int pageIndex = adapterPos / pageItemCount;
        int index = adapterPos % pageItemCount;
        int columnIndex = index / columnCount;
        int rowIndex = index % columnCount;
        int left = (pageIndex * getWidth()) + rowIndex * childWidth;
        int top = columnIndex * childHeight;
        return new Rect(left, top, left + childWidth, top + childHeight);
    }

    private int getFirstVisiPos() {
        int i = mSumDx / childWidth;
        int pageOffset = mSumDx / getWidth();
        mFirstVisiPos = (pageOffset * getEachPageItemCount()) + (i % columnCount);
        Log.d(TAG, "recyclerAndFillView mFirstVisiPos: " + mFirstVisiPos + "    pageOffset=" + pageOffset);
        return mFirstVisiPos;
    }

    private int getLastVisiPos() {
        int i = mSumDx + getWidth() / childWidth;
        int pageOffset = (mSumDx + getWidth()) / getWidth();
        mLastVisiPos = (pageOffset * getEachPageItemCount()) + (i % columnCount) + ((rowCount - 1) * columnCount);
        if (mLastVisiPos >= getItemCount()) {
            mLastVisiPos = getItemCount() - 1;
        }
        Log.d(TAG, "recyclerAndFillView getLastVisiPos: " + mLastVisiPos + "    pageOffset=" + pageOffset);
        return mLastVisiPos;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_DRAGGING");
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_IDLE");
                smoothScrollToPage(currentPage);
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_SETTLING");
                break;
        }
    }

    private int findSelectPositionPage(int position) {
        return position / getEachPageItemCount() + (position % getEachPageItemCount() == 0 ? 0 : 1);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        smoothScrollToPage(findSelectPositionPage(position));
    }

    public void smoothScrollToPage(int page) {
        if (page > -1 && page < pageCount) {
            mSumDx = page * getWidth();
            requestLayout();
            Log.d(TAG, "smoothScrollToPage: page=" + page + "     mSumDx=" + mSumDx);
        }
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter
            newAdapter) {
        //Completely scrap the existing layout
        removeAllViews();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

}
