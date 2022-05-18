package com.ysp.mqcq02_applist;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class ApplistFragment extends Fragment implements AppFinder.AppFindListener, AppClickListener {

    private RecyclerView app_list;
    private ApplistAdapter applistAdapter;
    private PagerSnapHelper helper;

    public ApplistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applistAdapter = new ApplistAdapter(this);
        AppFinder appFinder = new AppFinder(this, getContext());
        appFinder.execute();
        helper = new PagerSnapHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container, false);
        app_list = view.findViewById(R.id.app_list);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, RecyclerView.HORIZONTAL);
        staggeredGridLayoutManager.setGapStrategy(0);
        app_list.setLayoutManager(staggeredGridLayoutManager);
        app_list.setAdapter(applistAdapter);
        helper.attachToRecyclerView(app_list);
        return view;
    }

    @Override
    public void onAppFind(ApplicationInformation applicationInformation) {

    }

    @Override
    public void onAppFindFinish(List<ApplicationInformation> applicationInformations) {
        applistAdapter.setInstalledApplications(applicationInformations);
        applistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAppClick(ApplicationInformation info) {
        startActivity(info.launchIntent);
    }
}