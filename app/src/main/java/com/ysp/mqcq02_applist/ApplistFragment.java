package com.ysp.mqcq02_applist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class ApplistFragment extends Fragment implements AppFinder.AppFindListener {

    private RecyclerView app_list;
    private ApplistAdapter applistAdapter;

    public ApplistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applistAdapter = new ApplistAdapter();
        AppFinder appFinder = new AppFinder(this, getContext());
        appFinder.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container, false);
        app_list = view.findViewById(R.id.app_list);
        app_list.setLayoutManager(new GridLayoutManager(getContext(), 3));
        app_list.setAdapter(applistAdapter);
        return view;
    }

    @Override
    public void onAppFind(ApplicationInformation applicationInformation) {

    }

    @Override
    public void onAppFindFinish(List<ApplicationInformation> applicationInformations) {
        applistAdapter.setInstalledApplications(applicationInformations);
    }
}