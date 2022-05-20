package com.ysp.mqcq02_applist;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PageLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "PageLayoutManager";
    private int rowCount = 4;
    private int columnCount = 3;
    private Context context;
    private int childWidth;
    private int childHeight;
    private int currentPage = 0;
    private int pageCount;

    public PageLayoutManager(int rowCount, int columnCount, Context context) {
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

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (getItemCount() <= 0) {
            return;
        }
        childWidth = getWidth() / columnCount;
        childHeight = getHeight() / rowCount;
        pageCount = getItemCount() / (columnCount * rowCount) + 1;
        Log.d(TAG, "onLayoutChildren:pageCount= " + pageCount + "   getItemCount()=" + getItemCount());
        Log.d(TAG, "onLayoutChildren:childWidth= " + childWidth + "   childHeight=" + childHeight);
        int index = 0;
        int pageIndex = 0;
        while (pageIndex < pageCount) {
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    int left = j * childWidth + (pageIndex * getWidth());
                    int top = i * childHeight;
                    mItemRects.put(index, new Rect(left, top, left + childWidth, top + childHeight));
                    if (index < rowCount * columnCount * 2) {
                        View viewForPosition = recycler.getViewForPosition(index);
                        ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                        layoutParams.width = childWidth;
                        layoutParams.height = childHeight;
                        viewForPosition.setLayoutParams(layoutParams);
                        addView(viewForPosition);
                        Log.d(TAG, "onLayoutChildren:index= " + index);
                        measureChild(viewForPosition, 0, 0);
                        layoutDecorated(viewForPosition, left, top, left + childWidth, top + childHeight);
                    }
                    index++;
                    if (index >= getItemCount()) {
                        break;
                    }
                }
                if (index >= getItemCount()) {
                    break;
                }
            }
            pageIndex++;
            if (index >= getItemCount()) {
                break;
            }
        }

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
        //回收越界子View
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (travel > 0) {//需要回收当前屏幕，左越界的View
                if (getDecoratedRight(child) - travel < -getWidth()) {
                    removeAndRecycleView(child, recycler);
                    Log.d(TAG, "scrollHorizontallyBy:travel > 0 removeAndRecycleView i=" + i);
                }
            } else if (travel < 0) {//回收当前屏幕，右越界的View
                if (getDecoratedLeft(child) - travel > getWidth() * 2) {
                    removeAndRecycleView(child, recycler);
                    Log.d(TAG, "scrollHorizontallyBy:travel < 0 removeAndRecycleView i=" + i);
                }
            }
        }
        int pageOffset = mSumDx % getWidth();
        int pagePos = mSumDx / getWidth();
        if (dx > 0 && pageOffset > (getWidth() * 3 / 5) && pagePos + 1 != currentPage) {
            currentPage = pagePos + 1;//right
            addRightPage(recycler);
        }
        if (dx < 0 && pageOffset < (getWidth() * 2 / 5) && pagePos != currentPage) {
            currentPage = pagePos;
            addLeftPage(recycler);
        }
//        Rect visibleRect = getVisibleArea(travel);
//        //布局子View阶段
//        if (travel >= 0) {
//            View lastView = getChildAt(getChildCount() - 1);
//            int minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
//            //顺序addChildView
//            for (int i = 0; i <= getItemCount() - 1; i++) {
//                Rect rect = mItemRects.get(i);
//                if (Rect.intersects(visibleRect, rect)) {
//                    View child = recycler.getViewForPosition(i);
//                    addView(child);
//                    measureChildWithMargins(child, 0, 0);
//                    layoutDecorated(child, rect.left - mSumDx, rect.top, rect.right - mSumDx, rect.bottom);
//                } else {
//                    break;
//                }
//            }
//        } else {
//            View firstView = getChildAt(0);
//            int maxPos = getPosition(firstView) - 1;
//            for (int i = maxPos; i >= 0; i--) {
//                Rect rect = mItemRects.get(i);
//                if (Rect.intersects(visibleRect, rect)) {
//                    View child = recycler.getViewForPosition(i);
//                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
//                    measureChildWithMargins(child, 0, 0);
//                    layoutDecoratedWithMargins(child, rect.left - mSumDx, rect.top, rect.right - mSumDx, rect.bottom);
//                } else {
//                    break;
//                }
//            }
//        }
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


    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
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
