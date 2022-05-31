package com.ysp.mqcq02_applist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
    private static final String TAG = "PagerLayoutManager";

    private int rowCount = 4;
    private int columnCount = 3;
    private int childWidth;
    private int childHeight;
    private int currentPage = 0;
    private int pageCount;
    private int mSumDx = 0;
    private SparseArray<View> currentDisplayViews = new SparseArray<>();
    private List<Integer> lastDisplayIndex = new ArrayList<>();
    private ValueAnimator mAnimator;
    private OnPageChangeListener listener;
    private RecyclerView.Recycler mRecycler;
    RecyclerView.State mState;

    public PagerLayoutManager setPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public PagerLayoutManager(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
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
        mRecycler = recycler;
        mState = state;
        recycler.setViewCacheSize((rowCount + 1) * columnCount);
        childWidth = getWidth() / columnCount;
        childHeight = getHeight() / rowCount;
        pageCount = getItemCount() / getEachPageItemCount() + (getItemCount() % getEachPageItemCount() == 0 ? 0 : 1);
        reLayoutViews(recycler, state, 0);
    }


    private int reLayoutViews(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        List<Integer> visibilityIndex = getVisibilityIndex(dx < 0);
        if (visibilityIndex.isEmpty() || state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return 0;
        }
        if (lastDisplayIndex != null && !lastDisplayIndex.isEmpty()) {
            List<Integer> willRemove = removeListTwo4ListOne(lastDisplayIndex, visibilityIndex);
            List<Integer> willAdd = removeListTwo4ListOne(visibilityIndex, lastDisplayIndex);
            if (!willRemove.isEmpty()) {
                for (Integer i : willRemove) {
                    Log.d(TAG, "recyclerAndFillView: willRemove item  " + i);
                    View view = currentDisplayViews.get(i);
                    if (view != null) {
                        removeAndRecycleView(view, recycler);
                        currentDisplayViews.remove(i);
                        lastDisplayIndex.remove(i);
                    }
                }
            }
            if (!willAdd.isEmpty()) {
                for (Integer i : willAdd) {
                    Log.d(TAG, "recyclerAndFillView: willAdd item  " + i);
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
                        currentDisplayViews.put(i, viewForPosition);
                        lastDisplayIndex.add(i);
                    }
                }
            }
        } else {
            clearCache();
            for (Integer i : visibilityIndex) {
                Log.d(TAG, "recyclerAndFillView: visibilityIndex" + i);
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
                    currentDisplayViews.put(i, viewForPosition);
                }
            }
            lastDisplayIndex = visibilityIndex;
        }
        Log.d(TAG, "reLayoutViews: ChildCount= " + getChildCount());
        return 0;
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        Log.d(TAG, "onScrollStateChanged: SCROLL_STATE== " + state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            smoothScrollToPage(currentPage);
        } else if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            stopFixingAnimation();
        }
    }

    public void smoothScrollToPage(int page) {
        currentPage = page;
        if (page > -1 && page < pageCount) {
            stopFixingAnimation();
            Log.d(TAG, "smoothScrollToPage: page=" + page + "     mSumDx=" + mSumDx);
            mAnimator = ValueAnimator.ofFloat(0, page * getWidth() - mSumDx).setDuration(250);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private float mLastScrollOffset;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (float) animation.getAnimatedValue();
                    float offset = currentValue - mLastScrollOffset;
                    mSumDx += offset;
                    offsetChildrenHorizontal((int) -offset);
                    reLayoutViews(mRecycler, mState, (int) offset);
                    mLastScrollOffset = currentValue;
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {

                boolean isCanceled;

                @Override
                public void onAnimationCancel(Animator animation) {
                    isCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                }
            });
            mAnimator.start();
            if (listener != null) {
                listener.onPageChange(currentPage);
            }
        }
    }


    private void stopFixingAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    private int findSelectPositionPage(int position) {
        return position / getEachPageItemCount() + (position % getEachPageItemCount() == 0 ? 0 : 1);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        mState = state;
        smoothScrollToPage(findSelectPositionPage(position));
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter
            newAdapter) {
        clearCache();
        removeAllViews();
        requestLayout();
    }

    private void clearCache() {
        if (currentDisplayViews != null) {
            currentDisplayViews.clear();
        }
        if (lastDisplayIndex != null) {
            lastDisplayIndex.clear();
        }
    }

    private List<Integer> removeListTwo4ListOne(List<Integer> list1, List<Integer> list2) {
        ArrayList<Integer> integers = new ArrayList<>();
        for (Integer i : list1) {
            if (list2.contains(i)) {
            } else {
                integers.add(i);
            }
        }
        return integers;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() <= 0) {
            return dx;
        }
        mState = state;
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
        if (dx > 0 && pageOffset > (getWidth() * 2 / 5) && pagePos + 1 != currentPage) {
            currentPage = pagePos + 1;
        }
        if (dx < 0 && pageOffset < (getWidth() * 3 / 5) && pagePos != currentPage) {
            currentPage = pagePos;
        }
        reLayoutViews(recycler, state, dx);
        mSumDx += travel;
        offsetChildrenHorizontal(-travel);
        return travel;
    }

    private Rect getItemRang(int adapterPos) {
        int pageItemCount = getEachPageItemCount();
        int pageIndex = adapterPos / pageItemCount;
        int index = adapterPos % pageItemCount;
        int columnIndex = index / columnCount;
        int rowIndex = index % columnCount;
        int left = (pageIndex * getWidth()) + rowIndex * childWidth;
        int top = columnIndex * childHeight;
        return new Rect(left, top, left + childWidth, top + childHeight);
    }

    private List<Integer> getVisibilityIndex(boolean reversal) {
        ArrayList<Integer> integers = new ArrayList<>();
        int firstShouldVisiPos = getFirstShouldVisiPos();
        int pageRemainder = firstShouldVisiPos % getEachPageItemCount();
        if (pageRemainder == 0) {
            for (int i = 0; i < columnCount; i++) {
                int index = firstShouldVisiPos + i;
                for (int j = 0; j < rowCount; j++) {
                    integers.add(index);
                    index += columnCount;
                }
            }
            int nextPageStart = getFirstShouldVisiPos() + getEachPageItemCount();
            for (int i = 0; i < rowCount; i++) {
                integers.add(nextPageStart);
                nextPageStart += columnCount;
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


    private int getFirstShouldVisiPos() {
        int i = mSumDx / childWidth;
        int pageOffset = mSumDx / getWidth();
        int firstShouldVisiPos = (pageOffset * getEachPageItemCount()) + (i % columnCount);
        return firstShouldVisiPos;
    }

    private int getEachPageItemCount() {
        return rowCount * columnCount;
    }


    public interface OnPageChangeListener {
        void onPageChange(int page);
    }

}
