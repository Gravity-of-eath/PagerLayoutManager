package com.ysp.mqcq02_applist;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        if (getItemCount() <= 0) {
            return;
        }
        recycler.setViewCacheSize((rowCount + 1) * columnCount);
        childWidth = getWidth() / columnCount;
        childHeight = getHeight() / rowCount;
        pageCount = getItemCount() / getEachPageItemCount() + (getItemCount() % getEachPageItemCount() == 0 ? 0 : 1);
        Log.d(TAG, "onLayoutChildren:pageCount= " + pageCount + "   getItemCount()=" + getItemCount());
        Log.d(TAG, "onLayoutChildren:childWidth= " + childWidth + "   childHeight=" + childHeight);
        detachAndScrapAttachedViews(recycler);
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
        if (dx > 0 && pageOffset > (getWidth() * 1 / 5) && pagePos + 1 != currentPage) {
            currentPage = pagePos + 1;
        }
        if (dx < 0 && pageOffset < (getWidth() * 4 / 5) && pagePos != currentPage) {
            currentPage = pagePos;
        }
        recyclerAndFillView(recycler, state, travel);
        mSumDx += travel;
        offsetChildrenHorizontal(-travel);
        return travel;
//
    }

    private void recycleChildren(RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        Log.d(TAG, "recycleChildren: scrapList=" + scrapList.size());
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeView(holder.itemView);
            recycler.recycleView(holder.itemView);
        }
    }

    private int recyclerAndFillView(RecyclerView.Recycler recycler, RecyclerView.State state, int travel) {

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            removeAndRecycleView(childAt, recycler);
        }
        List<Integer> visibilityIndex = getVisibilityIndex(travel < 0);
        if (visibilityIndex.isEmpty() || state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return 0;
        }
        Log.e(TAG, "recyclerAndFillView: getVisibilityIndex size=" + visibilityIndex.size());
        for (Integer i : visibilityIndex) {
            Log.e(TAG, "recyclerAndFillView: visibilityIndex" + i);
            if (i >= 0 && i < getItemCount()) {
                Rect itemRang = getItemRang(i);
                View viewForPosition = recycler.getViewForPosition(i);
                ViewGroup.LayoutParams layoutParams = viewForPosition.getLayoutParams();
                layoutParams.width = childWidth;
                layoutParams.height = childHeight;
                viewForPosition.setLayoutParams(layoutParams);
                addView(viewForPosition);
                measureChild(viewForPosition, 0, 0);
                layoutDecorated(viewForPosition, itemRang.left - mSumDx, itemRang.top, itemRang.right - mSumDx, itemRang.bottom);
            }
        }
//        recycleChildren(recycler);
        return travel;
    }

    private List<Integer> getVisibilityIndex(boolean reversal) {
        ArrayList<Integer> integers = new ArrayList<>();
        int firstShouldVisiPos = getFirstShouldVisiPos();
        int pageRemainder = firstShouldVisiPos % getEachPageItemCount();
        if (pageRemainder == 0) {
            for (int i = 0; i < columnCount; i++) {
                int index = firstShouldVisiPos + i;
                Log.d(TAG, "getVisibilityIndex: index=" + index);
                for (int j = 0; j < rowCount; j++) {
                    integers.add(index);
                    index += columnCount;
                }
            }
            int nextPageStart = getFirstShouldVisiPos() + getEachPageItemCount();
            for (int i = 0; i < rowCount; i++) {
                integers.add(nextPageStart);
                nextPageStart++;
            }
        } else {
            int xx = columnCount - pageRemainder;
            for (int i = 0; i < xx; i++) {
                int index = firstShouldVisiPos + i;
                for (int j = 0; j < rowCount; j++) {
                    integers.add(index);
                    index += columnCount;
                }
            }
            int yy = (firstShouldVisiPos / getEachPageItemCount() + 1) * getEachPageItemCount();
            for (int i = 0; i <= pageRemainder; i++) {
                int index = yy + i;
                for (int j = 0; j < rowCount; j++) {
                    integers.add(index);
                    index += columnCount;
                }
            }
        }
        if (reversal) {
            Collections.reverse(integers);
        }
        return integers;
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

    private int getFirstShouldVisiPos() {
        int i = mSumDx / childWidth;
        int pageOffset = mSumDx / getWidth();
        int firstShouldVisiPos = (pageOffset * getEachPageItemCount()) + (i % columnCount);
        Log.d(TAG, "recyclerAndFillView firstShouldVisiPos: " + firstShouldVisiPos + "    pageOffset=" + pageOffset);
        return firstShouldVisiPos;
    }

    private int getLastShouldVisiPos() {
        int i = mSumDx + getWidth() / childWidth;
        int pageOffset = (mSumDx + getWidth()) / getWidth();
        int lastShouldVisiPos = (pageOffset * getEachPageItemCount()) + (i % columnCount) + ((rowCount - 1) * columnCount);
        if (lastShouldVisiPos >= getItemCount()) {
            lastShouldVisiPos = getItemCount() - 1;
        }
        Log.d(TAG, "recyclerAndFillView lastShouldVisiPos: " + lastShouldVisiPos + "    pageOffset=" + pageOffset);
        return lastShouldVisiPos;
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_DRAGGING");
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                smoothScrollToPage(currentPage);
                Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_IDLE");
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
//            int i = mSumDx - page * getWidth();
//            offsetChildrenHorizontal(i);
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
