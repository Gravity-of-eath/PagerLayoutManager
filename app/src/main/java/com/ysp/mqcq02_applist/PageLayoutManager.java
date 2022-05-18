package com.ysp.mqcq02_applist;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

public class PageLayoutManager extends RecyclerView.LayoutManager {

    private int spanCount = 3;
    private Context context;

    public PageLayoutManager(int spanCount, Context context) {
        this.spanCount = spanCount;
        this.context = context;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {

        return null;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

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
